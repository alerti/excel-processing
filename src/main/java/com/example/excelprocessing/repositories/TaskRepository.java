package com.example.excelprocessing.repositories;

import com.example.excelprocessing.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    TaskEntity findByTaskId(String taskId);
}