package com.company.gym.workload.repository;

import com.company.gym.workload.model.ProcessedTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedTransactionRepository extends MongoRepository<ProcessedTransaction, String> {

}
