package com.company.gym.workload.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_workload")
@CompoundIndex(name="first_last_name_idx", def="{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {
    @Id
    private String username;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private List<YearSummary> years;
}