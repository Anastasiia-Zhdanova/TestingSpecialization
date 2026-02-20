package com.company.gym.workload.jms;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadListener Unit Tests")
public class TrainerWorkloadListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @InjectMocks
    private TrainerWorkloadListener workloadListener;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("processWorkload: Success - Should handle MDC and call service")
    void processWorkload_Success() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("trainer.test")
                .transactionId("test-txn-123")
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .trainingDuration(60)
                .build();

        workloadListener.processWorkload(request);

        verify(workloadService, times(1)).updateWorkload(request);
        assertNull(MDC.get("transactionId"), "MDC should be cleared after processing");
    }

    @Test
    @DisplayName("processWorkload: Failure - Should throw exception to trigger DLQ and clear MDC")
    void processWorkload_ErrorHandling() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("test.user");

        doThrow(new RuntimeException("DB Error")).when(workloadService).updateWorkload(any());

        assertThrows(RuntimeException.class, () -> workloadListener.processWorkload(request));

        verify(workloadService, times(1)).updateWorkload(request);
        assertNull(MDC.get("transactionId"), "MDC should be cleared even after exception");
    }
}