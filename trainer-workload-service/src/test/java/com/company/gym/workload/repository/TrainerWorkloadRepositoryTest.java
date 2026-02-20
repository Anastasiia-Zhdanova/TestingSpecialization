package com.company.gym.workload.repository;

import com.company.gym.workload.model.TrainerWorkload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class TrainerWorkloadRepositoryTest {

    @Autowired
    private TrainerWorkloadRepository repository;

    @Test
    void shouldSaveAndFindWorkload() {
        // Given
        TrainerWorkload workload = new TrainerWorkload(
                "trainer.test", "Test", "Trainer", true, new ArrayList<>()
        );

        // When
        repository.save(workload);
        Optional<TrainerWorkload> found = repository.findByUsername("trainer.test");

        // Then
        assertTrue(found.isPresent());
        assertEquals("trainer.test", found.get().getUsername());
    }

    @Test
    @DisplayName("should find workloads by First Name and Last Name using Compound Index")
    void shouldFindByFirstNameAndLastName() {
        // GIVEN
        TrainerWorkload targetTrainer = new TrainerWorkload(
                "trainer.ben", "Ben", "Solo", true, new ArrayList<>()
        );
        TrainerWorkload otherTrainer = new TrainerWorkload(
                "trainer.luke", "Luke", "Skywalker", true, new ArrayList<>()
        );

        repository.save(targetTrainer);
        repository.save(otherTrainer);

        // WHEN
        List<TrainerWorkload> foundTrainers = repository.findByFirstNameAndLastName("Ben", "Solo");

        // THEN
        assertFalse(foundTrainers.isEmpty(), "Should find at least one trainer");
        assertEquals(1, foundTrainers.size(), "Should find exactly one trainer");
        assertEquals("trainer.ben", foundTrainers.get(0).getUsername(), "Username should match");
        assertEquals("Ben", foundTrainers.get(0).getFirstName());
        assertEquals("Solo", foundTrainers.get(0).getLastName());
    }
}