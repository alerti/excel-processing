package com.example.excelprocessing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String taskId;

    @Column(nullable = false)
    private String status;

    @Embedded
    private GradeResponse result;

    @Column(length = 1000)
    private String errorMessage;

    public TaskEntity() {
    }

    public TaskEntity(String taskId) {
        this.taskId = taskId;
        this.status = "PENDING";
    }

    public TaskEntity(Long id, String taskId, String status, GradeResponse result, String errorMessage) {
        this.id = id;
        this.taskId = taskId;
        this.status = status;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GradeResponse getResult() {
        return result;
    }

    public void setResult(GradeResponse result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}