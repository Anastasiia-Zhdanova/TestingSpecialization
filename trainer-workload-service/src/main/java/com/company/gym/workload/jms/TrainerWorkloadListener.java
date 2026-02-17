package com.company.gym.workload.jms;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadListener {

    private final TrainerWorkloadService workloadService;

    @JmsListener(destination = "trainer-workload-queue", containerFactory = "jmsListenerContainerFactory")
    public void processWorkload(TrainerWorkloadRequest request) {
        log.info("Received message from queue for trainer: {}", request.getTrainerUsername());
        try {
            workloadService.updateWorkload(request);
        } catch (Exception e) {
            log.error("Error processing workload message: {}", e.getMessage());
            throw e;
        }
    }
}