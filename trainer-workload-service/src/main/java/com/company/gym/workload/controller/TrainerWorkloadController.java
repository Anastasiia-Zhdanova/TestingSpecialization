package com.company.gym.workload.controller;

import com.company.gym.workload.model.TrainerWorkload;
import com.company.gym.workload.service.TrainerWorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workloads")
public class TrainerWorkloadController {
    private final TrainerWorkloadService service;

    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    // /api/v1/workloads/search?firstName=Ben&lastName=Solo
    @GetMapping("/search")
    public ResponseEntity<List<TrainerWorkload>> search(
            @RequestParam String firstName,
            @RequestParam String lastName) {

        return ResponseEntity.ok(service.searchByName(firstName, lastName));
    }
}
