package com.example.excelprocessing.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the grading results of an Excel file comparison")
public class GradeResponse {
    @Schema(description = "Total points awarded", example = "10.0")
    private double totalScore;

    @Schema(description = "Maximum possible points", example = "10.0")
    private double maxScore;

    @Schema(description = "List of errors encountered during grading", example = "[]")
    private String[] errors;

    @Schema(description = "Timestamp when the grading was created", example = "2025-04-24T14:49:43.054835")
    private String createdAt;

    @Schema(description = "Timestamp when the grading was last updated", example = "2025-04-24T14:49:43.054836")
    private String updatedAt;

    @Schema(description = "Percentage score (totalScore/maxScore * 100)", example = "100.0")
    private double percentage;

    public GradeResponse(double totalScore, double maxScore, String[] errors, String createdAt, String updatedAt, double percentage) {
        this.totalScore = totalScore;
        this.maxScore = maxScore;
        this.errors = errors;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.percentage = percentage;
    }

    public GradeResponse() {
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public String[] getErrors() {
        return errors;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public double getPercentage() {
        return percentage;
    }
}
