package com.company.gym.workload.service;

import com.company.gym.workload.dto.TrainerWorkloadRequest;
import com.company.gym.workload.model.MonthSummary;
import com.company.gym.workload.model.TrainerWorkload;
import com.company.gym.workload.model.YearSummary;
import com.company.gym.workload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;

    public void updateWorkload(TrainerWorkloadRequest request) {
        log.debug("OPERATION: Fetching or creating workload profile for trainer: {}", request.getTrainerUsername());

        TrainerWorkload workload = repository.findByUsername(request.getTrainerUsername())
                .orElseGet(() -> {
                    log.debug("OPERATION: Profile not found. Creating new workload profile for: {}", request.getTrainerUsername());
                    return new TrainerWorkload(
                            request.getTrainerUsername(),
                            request.getTrainerFirstName(),
                            request.getTrainerLastName(),
                            request.getIsActive(),
                            new ArrayList<>()
                    );
                });

        LocalDate date = request.getTrainingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = date.getYear();
        int month = date.getMonthValue();

        log.debug("OPERATION: Updating workload for Year: {}, Month: {}", year, month);

        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary(year, new ArrayList<>());
                    workload.getYears().add(newYear);
                    return newYear;
                });

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonthValue() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = new MonthSummary(month, 0L);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        long newDuration = monthSummary.getTotalDuration();
        if (request.getActionType() == TrainerWorkloadRequest.ActionType.ADD) {
            newDuration += request.getTrainingDuration();
            log.debug("OPERATION: Added {} minutes. New total: {}", request.getTrainingDuration(), newDuration);
        } else {
            newDuration -= request.getTrainingDuration();
            if (newDuration < 0) newDuration = 0;
            log.debug("OPERATION: Removed {} minutes. New total: {}", request.getTrainingDuration(), newDuration);
        }
        monthSummary.setTotalDuration(newDuration);

        if (newDuration == 0) {
            log.debug("OPERATION: Total duration for month is 0. Removing month entry.");
            yearSummary.getMonths().remove(monthSummary);
        }
        if (yearSummary.getMonths().isEmpty()) {
            log.debug("OPERATION: No months left for year {}. Removing year entry.", year);
            workload.getYears().remove(yearSummary);
        }

        log.debug("OPERATION: Saving updated profile to MongoDB...");
        repository.save(workload);
        log.info("Workload updated successfully.");
    }

    //here I use first_last_name_idx
    public List<TrainerWorkload> searchByName(String firstName, String lastName) {
        log.info("Searching workloads for: {} {}", firstName, lastName);
        return repository.findByFirstNameAndLastName(firstName, lastName);
    }
}