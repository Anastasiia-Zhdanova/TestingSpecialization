package com.company.gym.workload.jms;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadListener Unit Tests")
public class TrainerWorkloadListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @InjectMocks
    private TrainerWorkloadListener workloadListener;

    @Test
    @DisplayName("processWorkload: Success - Should call service when message received")
    void processWorkload_Success() {
        // GIVEN
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("trainer.test")
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .trainingDuration(60)
                .build();

        // WHEN
        workloadListener.processWorkload(request);

        // THEN
        verify(workloadService, times(1)).updateWorkload(request);
    }

    @Test
    @DisplayName("processWorkload: Failure - Should throw exception to trigger DLQ/Redelivery")
    void processWorkload_ErrorHandling() {
        // GIVEN
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        doThrow(new RuntimeException("DB Error")).when(workloadService).updateWorkload(any());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> workloadListener.processWorkload(request));
        verify(workloadService, times(1)).updateWorkload(request);
    }
}