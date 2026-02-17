package com.company.gym.service;

import com.company.gym.config.JmsConfig;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingDAO;
import com.company.gym.dto.request.TrainerWorkloadRequest;
import com.company.gym.entity.Trainee;
import com.company.gym.entity.Trainer;
import com.company.gym.entity.Training;
import com.company.gym.exception.ValidationException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class TrainingService {

    private final TrainingDAO trainingDAO;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final JmsTemplate jmsTemplate;
    private final Timer createTrainingTime;

    public TrainingService(TrainingDAO trainingDAO,
                           TraineeDAO traineeDAO,
                           TrainerDAO trainerDAO,
                           MeterRegistry meterRegistry,
                           JmsTemplate jmsTemplate) {
        this.trainingDAO = trainingDAO;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.jmsTemplate = jmsTemplate;
        this.createTrainingTime = Timer.builder("app.training.creation.time")
                .description("Time taken to create a training")
                .register(meterRegistry);
    }

    @Transactional
    public Training createTraining(String traineeUsername, String trainerUsername, String trainingName, Date date, Integer duration) {
        return createTrainingTime.record(() -> {
            log.info("Attempting to create training: '{}' for Trainee {} and Trainer {}", trainingName, traineeUsername, trainerUsername);

            Trainee trainee = traineeDAO.findByUsernameWithTrainers(traineeUsername);
            Trainer trainer = trainerDAO.findByUsername(trainerUsername);

            if (trainee == null || trainer == null) {
                log.error("Trainee or Trainer not found: {} / {}", traineeUsername, trainerUsername);
                throw new ValidationException("Trainee or Trainer not found.");
            }

            if (!trainee.getTrainers().contains(trainer)) {
                log.error("Trainer '{}' is not associated with Trainee '{}'.", trainerUsername, traineeUsername);
                throw new ValidationException("Trainer '" + trainerUsername + "' is not associated with Trainee '" + traineeUsername + "'.");
            }

            Training training = new Training();
            training.setTrainee(trainee);
            training.setTrainer(trainer);
            training.setTrainingName(trainingName);
            training.setTrainingDate(date);
            training.setTrainingDuration(duration);
            training.setTrainingType(trainer.getSpecialization());

            Training savedTraining = trainingDAO.save(training);
            TrainerWorkloadRequest workloadRequest = createWorkloadRequest(savedTraining, TrainerWorkloadRequest.ActionType.ADD);
            sendWorkloadUpdate(workloadRequest);

            log.info("Training '{}' created and message sent to queue.", trainingName);
            return savedTraining;
        });
    }

    @Transactional
    public void deleteTraining(Long trainingId) {
        log.info("Attempting to delete training with ID: {}", trainingId);
        Training training = trainingDAO.findById(trainingId);

        if (training != null) {
            TrainerWorkloadRequest workloadRequest = createWorkloadRequest(training, TrainerWorkloadRequest.ActionType.DELETE);

            trainingDAO.delete(training);
            sendWorkloadUpdate(workloadRequest);
            log.info("Training ID {} deleted and message sent to queue.", trainingId);
        } else {
            log.warn("Training with ID {} not found for deletion.", trainingId);
        }
    }

    private void sendWorkloadUpdate(TrainerWorkloadRequest request) {
        try {
            log.debug("Sending message to ActiveMQ queue: {} for trainer: {}", JmsConfig.TRAINER_WORKLOAD_QUEUE, request.getTrainerUsername());
            jmsTemplate.convertAndSend(JmsConfig.TRAINER_WORKLOAD_QUEUE, request);
        } catch (Exception e) {
            log.error("Failed to send message to ActiveMQ. Trainer: {}. Error: {}", request.getTrainerUsername(), e.getMessage());
        }
    }

    private TrainerWorkloadRequest createWorkloadRequest(Training training, TrainerWorkloadRequest.ActionType actionType) {
        Trainer trainer = training.getTrainer();
        return TrainerWorkloadRequest.builder()
                .trainerUsername(trainer.getUser().getUsername())
                .trainerFirstName(trainer.getUser().getFirstName())
                .trainerLastName(trainer.getUser().getLastName())
                .isActive(trainer.getUser().getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(actionType)
                .build();
    }
}