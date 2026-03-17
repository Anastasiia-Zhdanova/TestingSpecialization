package com.company.gym.workload.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadRequestTest {

    private JacksonTester<TrainerWorkloadRequest> json;

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void testSerialize() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("test.user");
        request.setTrainingDuration(100);
        request.setActionType(TrainerWorkloadRequest.ActionType.ADD);
        request.setTrainingDate(new Date());

        var result = json.write(request);

        assertThat(result).extractingJsonPathStringValue("$.trainerUsername").isEqualTo("test.user");
        assertThat(result).extractingJsonPathStringValue("$.actionType").isEqualTo("ADD");
        assertThat(result).hasJsonPathValue("$.trainingDate");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"trainerUsername\":\"test.user\",\"trainingDuration\":100,\"actionType\":\"ADD\"}";

        TrainerWorkloadRequest result = json.parseObject(content);

        assertThat(result.getTrainerUsername()).isEqualTo("test.user");
        assertThat(result.getTrainingDuration()).isEqualTo(100);
        assertThat(result.getActionType()).isEqualTo(TrainerWorkloadRequest.ActionType.ADD);
    }
}