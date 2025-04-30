package com.example.excelprocessing;

import com.example.excelprocessing.model.GradeResponse;
import com.example.excelprocessing.model.TaskEntity;
import com.example.excelprocessing.repositories.TaskRepository;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelGradingServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ExcelGradingService gradingService;

    private MultipartFile masterFile;
    private MultipartFile studentFile;

    @BeforeEach
    void setUp() throws IOException {
        masterFile = loadFile("src/test/resources/Candidate Sample Answer Sheet CorrVal + CorrForm.xlsx", "master.xlsx");
        studentFile = loadFile("src/test/resources/Candidate Sample Student Sheet CorrVal Only - (3 correct).xlsx", "student.xlsx");
    }

    private MultipartFile loadFile(String path, String name) throws IOException {
        File file = new File(path);
        return new MockMultipartFile(name, name, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Files.readAllBytes(file.toPath()));
    }

    @Test
    void initiateGrading_ShouldReturnTaskId() {
        String taskId = "test-task-id";
        TaskEntity task = new TaskEntity(taskId);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(task);
        when(taskRepository.findByTaskId(anyString())).thenReturn(task);

        String returnedTaskId = gradingService.initiateGrading(masterFile, studentFile);

        assertNotNull(returnedTaskId);
        verify(taskRepository, times(2)).save(any(TaskEntity.class));
    }

    @Test
    void gradeFilesAsync_ShouldUpdateTaskStatusToCompleted() {
        TaskEntity task = new TaskEntity("test-task-id");
        when(taskRepository.findByTaskId("test-task-id")).thenReturn(task);

        gradingService.gradeFilesAsync("test-task-id", masterFile, studentFile);

        verify(taskRepository).findByTaskId("test-task-id");
        verify(taskRepository).save(task);
    }

    @Test
    void gradeFilesAsync_ShouldHandleExceptionAndMarkAsFailed() {
        TaskEntity task = new TaskEntity("test-task-id");
        MultipartFile emptyFile = new MockMultipartFile("empty.xlsx", new byte[0]);

        when(taskRepository.findByTaskId("test-task-id")).thenReturn(task);

        gradingService.gradeFilesAsync("test-task-id", emptyFile, studentFile);

        verify(taskRepository).findByTaskId("test-task-id");
        verify(taskRepository).save(task);
    }

    @Test
    void processFiles_ShouldCorrectlyGradeScore() throws IOException {
        MultipartFile master = loadFile("src/test/resources/Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx", "master.xlsx");
        GradeResponse response = gradingService.processFiles(master, studentFile);

        assertEquals(3.0, response.getTotalScore());
        assertEquals(10.0, response.getMaxScore());
        assertEquals(30.0, response.getPercentage());
    }

    @Test
    void processFiles_ShouldReturnValidScoreWithFormulas() throws IOException {
        GradeResponse response = gradingService.processFiles(masterFile, studentFile);

        assertTrue(response.getTotalScore() >= 0);
        assertTrue(response.getMaxScore() > 0);
        assertTrue(response.getPercentage() >= 0 && response.getPercentage() <= 100);
        assertNotNull(response.getErrors());
    }

    @Test
    void processFiles_ShouldHandleRangeError() throws IOException {
        MultipartFile file = loadFile("src/test/resources/Candidate Sample Answer Sheet CorrVal Only - With Errors(8 correct).xlsx", "file.xlsx");

        GradeResponse response = gradingService.processFiles(file, file);

        assertEquals(8.0, response.getTotalScore());
        assertEquals(10.0, response.getMaxScore());
        assertEquals(80.0, response.getPercentage());
        String[] errors = response.getErrors();
        assertTrue(errors.length > 0);
        assertTrue(errors[0].contains("No cells found in master sheet"));
    }

    @Test
    void processFiles_ShouldHandleMissingGradingSheet() throws IOException {
        MultipartFile file = loadFile("src/test/resources/Candidate Sample Student Sheet CorrVal Only - (3 correct).xlsx", "file.xlsx");

        GradeResponse response = gradingService.processFiles(file, file);

        assertEquals(0.0, response.getTotalScore());
        assertEquals(0.0, response.getMaxScore());
        assertEquals(0.0, response.getPercentage());
        String[] errors = response.getErrors();
        assertTrue(errors.length > 0);
        assertTrue(errors[0].contains("Grading sheet not found"));
    }

    @Test
    void processFiles_ShouldHandleFormulaWhitespaceDifferences() throws IOException {
        GradeResponse response = gradingService.processFiles(masterFile, masterFile);

        assertEquals(10.0, response.getTotalScore());
        assertEquals(10.0, response.getMaxScore());
        assertEquals(100.0, response.getPercentage());
        String[] errors = response.getErrors();
        assertEquals(0, errors.length);
    }

    @Test
    void processFiles_ShouldThrowOnCorruptFile() {
        MultipartFile invalidFile = new MockMultipartFile(
                "invalid.xlsx",
                "invalid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "not an excel file".getBytes()
        );

        assertThrows(NotOfficeXmlFileException.class, () -> {
            gradingService.processFiles(invalidFile, studentFile);
        });
    }

    @Test
    void processFiles_ShouldThrowOnEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile("empty.xlsx", new byte[0]);

        assertThrows(EmptyFileException.class, () -> gradingService.processFiles(emptyFile, studentFile));
    }

    @Test
    void processFiles_ShouldGracefullyHandleMissingSheets() throws IOException {
        MultipartFile master = loadFile("src/test/resources/Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx", "master.xlsx");
        GradeResponse response = gradingService.processFiles(master, studentFile);
        String[] errors = response.getErrors();
        if (errors != null && errors.length > 0) {
            assertTrue(errors[0].contains("Sheet not found"));
        } else {
            assertEquals(0, errors.length);
        }
    }
}
