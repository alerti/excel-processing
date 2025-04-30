package com.example.excelprocessing.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskEntityTest {

    @Test
    void constructorAndGetters_ShouldWorkCorrectly() {
        Long id = 1L;
        String taskId = "test-task-id";
        String status = "COMPLETED";
        GradeResponse result = new GradeResponse();
        String errorMessage = "Test error";

        TaskEntity task = new TaskEntity(id, taskId, status, result, errorMessage);

        assertEquals(id, task.getId());
        assertEquals(taskId, task.getTaskId());
        assertEquals(status, task.getStatus());
        assertEquals(result, task.getResult());
        assertEquals(errorMessage, task.getErrorMessage());
    }

    @Test
    void defaultConstructor_ShouldSetDefaultValues() {
        TaskEntity task = new TaskEntity();

        assertNull(task.getId());
        assertNull(task.getTaskId());
        assertNull(task.getStatus());
        assertNull(task.getResult());
        assertNull(task.getErrorMessage());
    }

    @Test
    void taskIdConstructor_ShouldSetPendingStatus() {
        String taskId = "test-task-id";

        TaskEntity task = new TaskEntity(taskId);

        assertEquals(taskId, task.getTaskId());
        assertEquals("PENDING", task.getStatus());
        assertNull(task.getResult());
        assertNull(task.getErrorMessage());
    }

    @Test
    void setters_ShouldWorkCorrectly() {
        TaskEntity task = new TaskEntity();
        Long id = 1L;
        String taskId = "test-task-id";
        String status = "COMPLETED";
        GradeResponse result = new GradeResponse();
        String errorMessage = "Test error";

        task.setId(id);
        task.setTaskId(taskId);
        task.setStatus(status);
        task.setResult(result);
        task.setErrorMessage(errorMessage);

        assertEquals(id, task.getId());
        assertEquals(taskId, task.getTaskId());
        assertEquals(status, task.getStatus());
        assertEquals(result, task.getResult());
        assertEquals(errorMessage, task.getErrorMessage());
    }
}
