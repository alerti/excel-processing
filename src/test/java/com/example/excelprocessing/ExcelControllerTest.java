package com.example.excelprocessing;

import com.example.excelprocessing.model.TaskEntity;
import com.example.excelprocessing.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelControllerTest {

    @Mock
    private ExcelGradingService gradingService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ExcelController excelController;

    private MultipartFile masterFile;
    private MultipartFile studentFile;

    @BeforeEach
    void setUp() throws IOException {

        File masterFile = new File("src/test/resources/Candidate Sample Answer Sheet CorrVal + CorrForm.xlsx");
        File studentFile = new File("src/test/resources/Candidate Sample Student Sheet CorrVal Only - (3 correct).xlsx");

        this.masterFile = new MockMultipartFile(
            "master.xlsx",
            "master.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            Files.readAllBytes(masterFile.toPath())
        );

        this.studentFile = new MockMultipartFile(
            "student.xlsx",
            "student.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            Files.readAllBytes(studentFile.toPath())
        );
    }

    @Test
    void submitGrading_ShouldReturnTaskId() {
        when(gradingService.initiateGrading(any(), any())).thenReturn("test-task-id");

        ResponseEntity<String> response = excelController.submitGrading(masterFile, studentFile);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("test-task-id", response.getBody());
        verify(gradingService, times(1)).initiateGrading(any(), any());
    }

    @Test
    void submitGrading_ShouldReturnBadRequestForEmptyFiles() {
        MultipartFile emptyFile = new MockMultipartFile("empty.xlsx", new byte[0]);

        ResponseEntity<String> response = excelController.submitGrading(emptyFile, studentFile);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Files cannot be empty", response.getBody());
        verify(gradingService, never()).initiateGrading(any(), any());
    }

    @Test
    void getGradingStatus_ShouldReturnTaskStatus() {
        String taskId = "test-task-id";
        TaskEntity task = new TaskEntity(taskId);
        task.setStatus("COMPLETED");
        when(taskRepository.findByTaskId(taskId)).thenReturn(task);

        ResponseEntity<?> response = excelController.getGradingStatus(taskId);

        assertEquals(200, response.getStatusCode().value());
        verify(taskRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void getGradingStatus_ShouldReturnNotFoundForInvalidTaskId() {
        String taskId = "invalid-task-id";
        when(taskRepository.findByTaskId(taskId)).thenReturn(null);

        ResponseEntity<?> response = excelController.getGradingStatus(taskId);

        assertEquals(404, response.getStatusCode().value());
        verify(taskRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void submitGrading_ShouldHandleLargeFiles() throws IOException {
        File masterFile = new File("src/test/resources/Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx");
        File studentFile = new File("src/test/resources/Candidate Sample Student Sheet CorrVal Only - (3 correct).xlsx");

        MultipartFile masterMultipart = new MockMultipartFile(
            "master.xlsx",
            "master.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            Files.readAllBytes(masterFile.toPath())
        );

        MultipartFile studentMultipart = new MockMultipartFile(
            "student.xlsx",
            "student.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            Files.readAllBytes(studentFile.toPath())
        );

        when(gradingService.initiateGrading(any(), any())).thenReturn("test-task-id");

        ResponseEntity<String> response = excelController.submitGrading(masterMultipart, studentMultipart);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("test-task-id", response.getBody());
        verify(gradingService, times(1)).initiateGrading(any(), any());
    }
}
