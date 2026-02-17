package com.company.gym.workload.service;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.model.TrainerWorkload;
import com.company.gym.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadService Business Logic Tests")
public class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private TrainerWorkloadService workloadService;

    private TrainerWorkloadRequest addRequest;

    @BeforeEach
    void setUp() {
        addRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("trainer.ben")
                .trainerFirstName("Ben")
                .trainerLastName("Solo")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(90)
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();
    }

    @Test
    @DisplayName("updateWorkload: ADD - Create new profile if not exists")
    void updateWorkload_AddNewProfile() {
        when(repository.findByUsername("trainer.ben")).thenReturn(Optional.empty());

        workloadService.updateWorkload(addRequest);

        verify(repository, times(1)).save(any(TrainerWorkload.class));
    }

    @Test
    @DisplayName("updateWorkload: DELETE - Reduce duration and cleanup")
    void updateWorkload_DeleteAndCleanup() {
        // GIVEN
        addRequest.setActionType(TrainerWorkloadRequest.ActionType.DELETE);
        TrainerWorkload existing = new TrainerWorkload("trainer.ben", "Ben", "Solo", true, new ArrayList<>());
        when(repository.findByUsername("trainer.ben")).thenReturn(Optional.of(existing));

        // WHEN
        workloadService.updateWorkload(addRequest);

        // THEN
        verify(repository, times(1)).save(existing);
    }
}