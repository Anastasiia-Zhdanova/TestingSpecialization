package com.company.gym.workload.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "processed_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedTransaction {
    @Id
    private String transactionId;
    private LocalDateTime processedAt;
}
