package com.example.excelprocessing.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeResponseTest {

    @Test
    void constructorAndGetters_ShouldWorkCorrectly() {
        double totalScore = 8.0;
        double maxScore = 10.0;
        String[] errors = new String[]{"Error 1", "Error 2"};
        String createdAt = "2023-01-01T00:00:00";
        String updatedAt = "2023-01-01T00:00:00";
        double percentage = 80.0;

        GradeResponse response = new GradeResponse(totalScore, maxScore, errors, createdAt, updatedAt, percentage);

        assertEquals(totalScore, response.getTotalScore());
        assertEquals(maxScore, response.getMaxScore());
        assertArrayEquals(errors, response.getErrors());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        assertEquals(percentage, response.getPercentage());
    }

    @Test
    void setters_ShouldWorkCorrectly() {
        GradeResponse response = new GradeResponse();
        double totalScore = 8.0;
        double maxScore = 10.0;
        String[] errors = new String[]{"Error 1", "Error 2"};
        String createdAt = "2023-01-01T00:00:00";
        String updatedAt = "2023-01-01T00:00:00";
        double percentage = 80.0;

        response.setTotalScore(totalScore);
        response.setMaxScore(maxScore);
        response.setErrors(errors);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        response.setPercentage(percentage);

        assertEquals(totalScore, response.getTotalScore());
        assertEquals(maxScore, response.getMaxScore());
        assertArrayEquals(errors, response.getErrors());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        assertEquals(percentage, response.getPercentage());
    }

    @Test
    void testGradeResponse() {
        GradeResponse response = new GradeResponse();
        response.setTotalScore(8.0);
        response.setMaxScore(10.0);
        response.setErrors(new String[]{"Error 1", "Error 2"});
        response.setPercentage(80.0);

        assertEquals(8.0, response.getTotalScore());
        assertEquals(10.0, response.getMaxScore());
        assertArrayEquals(new String[]{"Error 1", "Error 2"}, (String[]) response.getErrors());
        assertEquals(80.0, response.getPercentage());
    }

    @Test
    void testGradeResponseWithNoErrors() {
        GradeResponse response = new GradeResponse();
        response.setTotalScore(10.0);
        response.setMaxScore(10.0);
        response.setErrors(new String[0]);
        response.setPercentage(100.0);

        assertEquals(10.0, response.getTotalScore());
        assertEquals(10.0, response.getMaxScore());
        assertArrayEquals(new String[0], (String[]) response.getErrors());
        assertEquals(100.0, response.getPercentage());
    }
}
