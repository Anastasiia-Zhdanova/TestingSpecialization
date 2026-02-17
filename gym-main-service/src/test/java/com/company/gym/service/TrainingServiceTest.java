package com.company.gym.service;

import com.company.gym.config.JmsConfig;
import com.company.gym.dao.TraineeDAO;
import com.company.gym.dao.TrainerDAO;
import com.company.gym.dao.TrainingDAO;
import com.company.gym.dto.request.TrainerWorkloadRequest;
import com.company.gym.entity.*;
import com.company.gym.exception.ValidationException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingService Unit Tests (JMS Integration)")
public class TrainingServiceTest {

    @Mock private TrainingDAO trainingDAO;
    @Mock private TraineeDAO traineeDAO;
    @Mock private TrainerDAO trainerDAO;
    @Mock private JmsTemplate jmsTemplate; // Мокаем JmsTemplate вместо Feign
    @Mock private MeterRegistry meterRegistry;
    @Mock private Timer timer;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee mockTrainee;
    private Trainer mockTrainer;
    private User mockUser;

    @BeforeEach
    void setUp() {
        when(meterRegistry.timer(anyString())).thenReturn(timer);
        when(timer.record(any(java.util.function.Supplier.class))).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        mockUser = new User();
        mockUser.setUsername("trainer.pro");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setIsActive(true);

        mockTrainer = new Trainer();
        mockTrainer.setUser(mockUser);
        mockTrainer.setSpecialization(new TrainingType("Yoga"));

        mockTrainee = new Trainee();
        mockTrainee.setUser(new User());
        mockTrainee.setTrainers(Set.of(mockTrainer));
    }

    @Test
    @DisplayName("createTraining: Success - Should save training and send JMS message")
    void createTraining_Success() {
        // GIVEN
        String traineeUser = "trainee.user";
        String trainerUser = "trainer.pro";
        Date date = new Date();
        Integer duration = 60;

        when(traineeDAO.findByUsernameWithTrainers(traineeUser)).thenReturn(mockTrainee);
        when(trainerDAO.findByUsername(trainerUser)).thenReturn(mockTrainer);
        when(trainingDAO.save(any(Training.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Training result = trainingService.createTraining(traineeUser, trainerUser, "Morning Yoga", date, duration);

        // THEN
        assertNotNull(result);
        verify(trainingDAO, times(1)).save(any(Training.class));

        // Проверяем отправку сообщения в очередь (Требование №5)
        ArgumentCaptor<TrainerWorkloadRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(JmsConfig.TRAINER_WORKLOAD_QUEUE), captor.capture());

        TrainerWorkloadRequest sentRequest = captor.getValue();
        assertEquals(trainerUser, sentRequest.getTrainerUsername());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, sentRequest.getActionType());
        assertEquals(duration, sentRequest.getTrainingDuration());
    }

    @Test
    @DisplayName("createTraining: Failure - Should throw ValidationException when Trainer not linked")
    void createTraining_NotLinked_ThrowsException() {
        // GIVEN
        mockTrainee.setTrainers(Collections.emptySet()); // Тренер не связан с учеником
        when(traineeDAO.findByUsernameWithTrainers("user")).thenReturn(mockTrainee);
        when(trainerDAO.findByUsername("trainer")).thenReturn(mockTrainer);

        // WHEN & THEN
        assertThrows(ValidationException.class, () ->
                trainingService.createTraining("user", "trainer", "Test", new Date(), 30));

        verify(jmsTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("deleteTraining: Success - Should delete and send DELETE message to queue")
    void deleteTraining_Success() {
        // GIVEN
        Long trainingId = 1L;
        Training mockTraining = new Training();
        mockTraining.setTrainer(mockTrainer);
        mockTraining.setTrainingDate(new Date());
        mockTraining.setTrainingDuration(45);

        when(trainingDAO.findById(trainingId)).thenReturn(mockTraining);

        // WHEN
        trainingService.deleteTraining(trainingId);

        // THEN
        verify(trainingDAO, times(1)).delete(mockTraining);
        verify(jmsTemplate, times(1)).convertAndSend(eq(JmsConfig.TRAINER_WORKLOAD_QUEUE), any(TrainerWorkloadRequest.class));
    }

    @Test
    @DisplayName("sendWorkloadUpdate: Error Handling - Should not crash if JMS fails")
    void sendWorkloadUpdate_JmsException_Logged() {
        // GIVEN
        when(traineeDAO.findByUsernameWithTrainers(any())).thenReturn(mockTrainee);
        when(trainerDAO.findByUsername(any())).thenReturn(mockTrainer);
        doThrow(new RuntimeException("MQ Broker Down")).when(jmsTemplate).convertAndSend(anyString(), any(Object.class));

        // WHEN & THEN
        assertDoesNotThrow(() ->
                trainingService.createTraining("u", "t", "N", new Date(), 10));

        verify(trainingDAO, times(1)).save(any());
    }
}