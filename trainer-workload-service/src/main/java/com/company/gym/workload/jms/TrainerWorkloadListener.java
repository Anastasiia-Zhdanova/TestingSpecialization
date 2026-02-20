package com.company.gym.workload.jms;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadListener {

    private final TrainerWorkloadService workloadService;
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @JmsListener(destination = "trainer-workload-queue", containerFactory = "jmsListenerContainerFactory")
    public void processWorkload(TrainerWorkloadRequest request) {
        String transactionId = request.getTransactionId() != null
                ? request.getTransactionId()
                : UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        log.info("TRANSACTION START: Received workload update request for trainer: {}", request.getTrainerUsername());

        try {
            workloadService.updateWorkload(request);
            log.info("TRANSACTION END: Successfully processed workload for trainer: {}", request.getTrainerUsername());
        } catch (Exception e) {
            log.error("TRANSACTION ERROR: Failed to process workload message for trainer: {}. Reason: {}",
                    request.getTrainerUsername(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}