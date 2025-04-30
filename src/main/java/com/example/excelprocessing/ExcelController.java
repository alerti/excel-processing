package com.example.excelprocessing;

import com.example.excelprocessing.model.TaskEntity;
import com.example.excelprocessing.repositories.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/excel")
@Tag(name = "Excel Grading API", description = "API for grading Excel files")
public class ExcelController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final ExcelGradingService gradingService;
    private final TaskRepository taskRepository;

    public ExcelController(ExcelGradingService gradingService, TaskRepository taskRepository) {
        this.gradingService = gradingService;
        this.taskRepository = taskRepository;
    }

    @Operation(summary = "Submit Excel files for grading", description = "Uploads master and student Excel files for asynchronous grading and returns a task ID.")
    @PostMapping(value = "/grade", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitGrading(
            @RequestParam("master_file")  MultipartFile masterFile,
            @RequestParam("student_file")  MultipartFile studentFile) {
        logger.info("Received grading request for files: {} and {}", masterFile.getOriginalFilename(), studentFile.getOriginalFilename());
        
        if (masterFile.isEmpty() || studentFile.isEmpty()) {
            logger.warn("Empty files uploaded: masterFile={}, studentFile={}", masterFile.getOriginalFilename(), studentFile.getOriginalFilename());
            return ResponseEntity.badRequest().body("Files cannot be empty");
        }
        if (masterFile.getSize() > MAX_FILE_SIZE || studentFile.getSize() > MAX_FILE_SIZE) {
            logger.warn("File size exceeds limit: masterFile={} bytes, studentFile={} bytes");
            return ResponseEntity.badRequest().body("File size exceeds 10MB limit");
        }
        if (!isValidExcelFile(masterFile) || !isValidExcelFile(studentFile)) {
            logger.warn("Invalid file type: masterFile={}, studentFile={}", masterFile.getContentType(), studentFile.getContentType());
            return ResponseEntity.badRequest().body("Invalid file type. Only .xlsx files are supported");
        }

        String taskId = gradingService.initiateGrading(masterFile, studentFile);
        logger.info("Grading task initiated with taskId: {}", taskId);
        return ResponseEntity.ok(taskId);
    }

    @Operation(summary = "Get grading task status", description = "Retrieves the status and result of a grading task by task ID.")
    @GetMapping("/grade/{taskId}")
    public ResponseEntity<?> getGradingStatus(@PathVariable String taskId) {
        logger.debug("Checking status for taskId: {}", taskId);
        TaskEntity task = taskRepository.findByTaskId(taskId);
        if (task == null) {
            logger.warn("Task not found: {}", taskId);
            return ResponseEntity.notFound().build();
        }

        if ("PENDING".equals(task.getStatus())) {
            logger.debug("Task {} is still processing", taskId);
            return ResponseEntity.ok("Task is still processing");
        } else if ("COMPLETED".equals(task.getStatus())) {
            logger.info("Returning completed task result for taskId: {}", taskId);
            return ResponseEntity.ok(task.getResult());
        } else {
            logger.error("Task {} failed: {}", taskId, task.getErrorMessage());
            return ResponseEntity.status(500).body(task.getErrorMessage());
        }
    }

    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}