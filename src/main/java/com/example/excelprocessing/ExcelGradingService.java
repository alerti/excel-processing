package com.example.excelprocessing;

import com.example.excelprocessing.model.GradeResponse;
import com.example.excelprocessing.model.TaskEntity;
import com.example.excelprocessing.repositories.TaskRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ExcelGradingService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelGradingService.class);

    private final TaskRepository taskRepository;

    public ExcelGradingService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public String initiateGrading(MultipartFile masterFile, MultipartFile studentFile) {
        String taskId = UUID.randomUUID().toString();
        TaskEntity task = new TaskEntity(taskId);
        taskRepository.save(task);
        logger.info("Created task with taskId: {}", taskId);
        gradeFilesAsync(taskId, masterFile, studentFile);
        return taskId;
    }

    @Async("taskExecutor")
    public void gradeFilesAsync(String taskId, MultipartFile masterFile, MultipartFile studentFile) {
        logger.debug("Starting async grading for taskId: {}", taskId);
        TaskEntity task = taskRepository.findByTaskId(taskId);
        try {
            GradeResponse response = processFiles(masterFile, studentFile);
            task.setStatus("COMPLETED");
            task.setResult(response);
            logger.info("Grading completed successfully for taskId: {}", taskId);
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage("Error processing files: " + e.getMessage());
            logger.error("Grading failed for taskId: {}. Error: {}", taskId, e.getMessage(), e);
        }
        taskRepository.save(task);
    }

    public GradeResponse processFiles(MultipartFile masterFile, MultipartFile studentFile) throws IOException {
        List<String> errors = new ArrayList<>();
        double totalScore = 0.0;
        double maxScore = 0.0;

        try (XSSFWorkbook masterWorkbook = new XSSFWorkbook(masterFile.getInputStream());
             XSSFWorkbook studentWorkbook = new XSSFWorkbook(studentFile.getInputStream())) {

            XSSFSheet gradingSheet = masterWorkbook.getSheet("Grading");
            if (gradingSheet == null) {
                errors.add("Grading sheet not found in master file");
                logger.warn("Grading sheet missing in master file");
                return createResponse(totalScore, maxScore, errors);
            }

            for (Row row : gradingSheet) {
                if (row.getRowNum() == 0) continue;

                String sheetName = getCellValue(row.getCell(0));
                String range = getCellValue(row.getCell(1));
                String comparisonType = getCellValue(row.getCell(2));

                Sheet masterSheet = masterWorkbook.getSheet(sheetName);
                Sheet studentSheet = studentWorkbook.getSheet(sheetName);

                if (masterSheet == null || studentSheet == null) {
                    errors.add("Sheet not found: " + sheetName);
                    logger.warn("Sheet {} not found in workbook", sheetName);
                    continue;
                }

                try {
                    maxScore++;

                    CellRangeAddress rangeAddress;
                    try {
                        rangeAddress = CellRangeAddress.valueOf(range);
                    } catch (IllegalArgumentException e) {
                        errors.add("Error grading value: No cells found in master sheet: " + sheetName + " cell: " + range);
                        logger.warn("Invalid range {} in sheet {}", range, sheetName);
                        continue;
                    }

                    if (compareRange(masterSheet, studentSheet, rangeAddress, comparisonType, errors, sheetName)) {
                        totalScore++;
                    }
                } catch (Exception e) {
                    errors.add("Error processing range in sheet " + sheetName + ": " + e.getMessage());
                    logger.error("Error processing range in sheet {}: {}", sheetName, e.getMessage(), e);
                }
            }
        }

        return createResponse(totalScore, maxScore, errors);
    }

    private boolean compareRange(Sheet masterSheet, Sheet studentSheet, CellRangeAddress range,
                                String comparisonType, List<String> errors, String sheetName) {
        for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
            Row masterRow = masterSheet.getRow(row);
            Row studentRow = studentSheet.getRow(row);
            if (masterRow == null || studentRow == null) {
                errors.add("Error grading value: No cells found in master sheet: " + sheetName + " cell: " + getCellAddress(row, range.getFirstColumn()));
                logger.warn("Row {} missing in sheet {}", row + 1, sheetName);
                return false;
            }

            for (int col = range.getFirstColumn(); col <= range.getLastColumn(); col++) {
                Cell masterCell = masterRow.getCell(col);
                Cell studentCell = studentRow.getCell(col);
                if (masterCell == null || studentCell == null) {
                    errors.add("Error grading value: No cells found in master sheet: " + sheetName + " cell: " + getCellAddress(row, col));
                    logger.warn("Cell {} missing in sheet {}", getCellAddress(row, col), sheetName);
                    return false;
                }

                if ("CorrVal".equalsIgnoreCase(comparisonType)) {
                    if (!compareCellValues(masterCell, studentCell)) {
                        return false;
                    }
                } else if ("CorrForm".equalsIgnoreCase(comparisonType)) {
                    if (!compareCellFormulas(masterCell, studentCell)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean compareCellValues(Cell masterCell, Cell studentCell) {
        try {
            if (masterCell.getCellType() != studentCell.getCellType()) {
                return false;
            }

            switch (masterCell.getCellType()) {
                case NUMERIC:
                    return Double.compare(masterCell.getNumericCellValue(), studentCell.getNumericCellValue()) == 0;
                case STRING:
                    return masterCell.getStringCellValue().trim().equals(studentCell.getStringCellValue().trim());
                case BOOLEAN:
                    return masterCell.getBooleanCellValue() == studentCell.getBooleanCellValue();
                case FORMULA:
                    return compareCellFormulas(masterCell, studentCell);
                default:
                    return masterCell.toString().trim().equals(studentCell.toString().trim());
            }
        } catch (Exception e) {
            logger.error("Error comparing cell values: {}", e.getMessage());
            return false;
        }
    }

    private boolean compareCellFormulas(Cell masterCell, Cell studentCell) {
        try {
            if (masterCell.getCellType() != CellType.FORMULA || studentCell.getCellType() != CellType.FORMULA) {
                return false;
            }

            String masterFormula = masterCell.getCellFormula().replaceAll("\\s+", "").toUpperCase();
            String studentFormula = studentCell.getCellFormula().replaceAll("\\s+", "").toUpperCase();

            return masterFormula.equals(studentFormula);
        } catch (Exception e) {
            logger.error("Error comparing formulas: {}", e.getMessage());
            return false;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.toString().trim();
    }

    private String getCellAddress(int row, int col) {
        return String.format("%s%d", (char) ('A' + col), row + 1);
    }

    private GradeResponse createResponse(double totalScore, double maxScore, List<String> errors) {
        double percentage = maxScore > 0 ? (totalScore / maxScore) * 100 : 0.0;
        String timestamp = LocalDateTime.now().toString();
        return new GradeResponse(totalScore, maxScore, errors.toArray(new String[0]), timestamp, timestamp, percentage);
    }
}
