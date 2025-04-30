# Excel Processing Application

A Spring Boot application that processes Excel files for grading based on specific rules.

## Overview

This application provides an API endpoint for processing and comparing Excel files according to specific rules. It supports both value-based and formula-based grading criteria.

### Features

- Excel file processing and comparison
- Support for both value-based (CorrVal) and formula-based (CorrForm) grading
- Asynchronous processing with task tracking
- Comprehensive error handling
- Detailed grading results with percentage calculation
- Docker containerization for easy deployment
- Automated test reporting
- Environment configuration management

## Technical Stack

- **Backend Framework:** Spring Boot 3.4.5
- **Language:** Java 21
- **Build Tool:** Maven
- **Database:** PostgreSQL
- **Containerization:** Docker & Docker Compose
- **Testing Framework:** JUnit 5
- **API Documentation:** Springdoc OpenAPI (Swagger)
- **Logging:** SLF4J with Logback
- **Excel Processing:** Apache POI
- **Database Access:** Spring Data JPA
- **Task Management:** Spring @Async
- **Validation:** Spring Validation

## Core Components

### 1. ExcelGradingService
The main service class responsible for processing and comparing Excel files.

#### Key Responsibilities:
- Asynchronous file processing using `@Async`
- Excel file validation and comparison
- Score calculation and error tracking
- Task status management

#### Main Methods:
- `initiateGrading`: Creates a new grading task
- `gradeFilesAsync`: Asynchronously processes files
- `processFiles`: Core grading logic implementation
- `compareRange`: Compares cell ranges between files
- `compareCellValues`: Compares cell values
- `compareCellFormulas`: Compares cell formulas

### 2. ExcelController
REST controller handling file uploads and task status queries.

#### Key Responsibilities:
- File upload validation
- Task initiation
- Status query handling
- Error response management

#### Endpoints:
- `POST /api/v1/excel/grade`: Submit files for grading
- `GET /api/v1/excel/grade/{taskId}`: Get grading status

### 3. GradeResponse
Model class representing grading results.

#### Fields:
- `totalScore`: Points awarded
- `maxScore`: Maximum possible points
- `errors`: Array of error messages
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `percentage`: Score percentage

### 4. TaskEntity
JPA entity for tracking grading tasks.

#### Fields:
- `id`: Database primary key
- `taskId`: Unique task identifier
- `status`: Task status (PENDING/COMPLETED/FAILED)
- `result`: Grading results
- `errorMessage`: Error details if failed

## Project Structure

```
excel-processing/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/excelprocessing/
│   │   │       ├── controller/
│   │   │       │   └── ExcelController.java
│   │   │       ├── service/
│   │   │       │   └── ExcelGradingService.java
│   │   │       ├── model/
│   │   │       │   ├── GradeResponse.java
│   │   │       │   └── TaskEntity.java
│   │   │       ├── repositories/
│   │   │       │   └── TaskRepository.java
│   │   │       └── ExcelProcessingApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/
│       │   └── com/example/excelprocessing/
│       │       ├── controller/
│       │       │   └── ExcelControllerTest.java
│       │       ├── service/
│       │       │   └── ExcelGradingServiceTest.java
│       │       ├── model/
│       │       │   ├── GradeResponseTest.java
│       │       │   └── TaskEntityTest.java
│       │       └── ExcelProcessingApplicationTests.java
│       └── resources/
│           └── test-excel-files/
│               ├── Candidate Sample Answer Sheet CorrVal Only (10 correct).xlsx
│               ├── Candidate Sample Answer Sheet CorrVal Only - With Errors(8 correct).xlsx
│               ├── Candidate Sample Student Sheet CorrVal Only - (3 correct).xlsx
│               └── Candidate Sample Answer Sheet CorrVal + CorrForm.xlsx
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── generate-test-report.sh
```

## Setup and Installation

### Environment Setup

1. Create your `.env` file:
   ```bash
   cp .env.example .env
   ```

2. Update the `.env` file with your specific values:
   ```bash
   # Database Configuration
   DB_URL=jdbc:postgresql://db:5432/your_database_name
   DB_USERNAME=your_database_username
   DB_PASSWORD=your_database_password
   DB_DDL_AUTO=update
   DB_NAME=your_database_name
   DB_PORT=5432

   # Application Configuration
   APP_PORT=8080
   SPRING_PROFILES_ACTIVE=dev

   # Logging Configuration
   LOG_LEVEL=INFO
   ```

3. The `.env` file is already in `.gitignore`, so your sensitive information will not be committed.

### Local Development

#### Using Maven

1. Build the application:
   ```bash
   ./mvnw clean package
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

#### Using Docker

1. Clean up existing containers (optional):
   ```bash
   ./cleanup.sh
   ```

2. Build and start the containers:
   ```bash
   docker compose up --build
   ```

3. To stop the containers:
   ```bash
   docker compose down
   ```

4. To view logs:
   ```bash
   docker compose logs -f
   ```

## API Documentation

Once the application is running, you can access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### Available Endpoints

1. **POST /api/v1/excel/grade**
   - **Description:** Initiates Excel file grading
   - **Request Body:** Multipart form data with:
     - `master_file`: Master Excel file
     - `student_file`: Student Excel file
   - **Response:** Task ID for tracking
   - **Status Codes:**
     - 200: Success
     - 400: Bad Request (invalid files)
     - 500: Internal Server Error

2. **GET /api/v1/excel/grade/{taskId}**
   - **Description:** Retrieves grading results for a specific task
   - **Path Parameters:**
     - `taskId`: ID of the grading task
   - **Response:** GradeResponse object with:
     - `totalScore`: Total points scored
     - `maxScore`: Maximum possible score
     - `percentage`: Score percentage
     - `errors`: Array of error messages
   - **Status Codes:**
     - 200: Success
     - 404: Task not found
     - 500: Internal Server Error

## Test Documentation

### Test Categories

1. **Service Layer Tests** (`ExcelGradingServiceTest.java`)
   - File processing and validation
   - Grading logic implementation
   - Error handling
   - Asynchronous processing

2. **Controller Tests** (`ExcelControllerTest.java`)
   - API endpoint validation
   - Request/response handling
   - Error responses

3. **Model Tests**
   - `GradeResponseTest.java`: Response object validation
   - `TaskEntityTest.java`: Task management validation

4. **Integration Tests** (`ExcelProcessingApplicationTests.java`)
   - Application startup
   - Database connectivity
   - End-to-end workflow

### Test Cases

`1. Initial Test Case`
- **Files Used:**
  - Master: "Candidate Sample Answer Sheet CorrVal Only (10 correct)"
  - Student: "Candidate Sample Answer Sheet CorrVal Only (10 correct)"
- **Expected:**
  - Total Score: 10.0
  - Max Score: 10.0
  - Percentage: 100.0
  - No errors

API output:

  ```
  {
  "totalScore": 10,
  "maxScore": 10,
  "errors": [],
  "createdAt": "2025-04-30T13:45:48.582880097",
  "updatedAt": "2025-04-30T13:45:48.582880097",
  "percentage": 100
}
```


`2. Error Test Case`
- **Files Used:**
  - Master: "Candidate Sample Answer Sheet CorrVal Only - With Errors(8 correct)"
  - Student: "Candidate Sample Answer Sheet CorrVal Only - With Errors(8 correct)"
- **Expected:**
  - Total Score: 8.0
  - Max Score: 10.0
  - Percentage: 80.0
  - Contains error messages for invalid ranges

API output:

  ```
  {
  "totalScore": 8,
  "maxScore": 10,
  "errors": [
    "Error grading value: No cells found in master sheet: Test Sheet cell: A12:14",
    "Error grading value: No cells found in master sheet: Test Sheet 2 cell: A12:14"
  ],
  "createdAt": "2025-04-30T13:47:51.665600542",
  "updatedAt": "2025-04-30T13:47:51.665600542",
  "percentage": 80
}
```

`3. Partial Correctness Test Case`
- **Files Used:**
  - Master: "Candidate Sample Answer Sheet CorrVal Only (10 correct)"
  - Student: "Candidate Sample Student Sheet CorrVal Only - (3 correct)"
- **Expected:**
  - Total Score: 3.0
  - Max Score: 10.0
  - Percentage: 30.0
  - No errors

API output:
  ```
  {
  "totalScore": 3,
  "maxScore": 10,
  "errors": [],
  "createdAt": "2025-04-30T13:48:54.300898835",
  "updatedAt": "2025-04-30T13:48:54.300898835",
  "percentage": 30
}
```

`4. Bonus Challenge Test Case`
- **Files Used:**
  - Master: "Candidate Sample Answer Sheet CorrVal + CorrForm"
  - Student: "Candidate Sample Answer Sheet CorrVal + CorrForm"
- **Expected:**
  - Total Score: 10.0
  - Max Score: 10.0
  - Percentage: 100.0
  - No errors

API output:
```
{
  "totalScore": 10,
  "maxScore": 10,
  "errors": [],
  "createdAt": "2025-04-30T12:54:46.335772055",
  "updatedAt": "2025-04-30T12:54:46.335772055",
  "percentage": 100
}
```

### Test Coverage Report

To generate a test coverage report:

1. Run the test report generation script:
   ```bash
   ./generate-test-report.sh
   ```

2. View the report at `target/test-reports/summary.md`

The report includes:
- Test execution statistics
- Coverage metrics
- Failed tests (if any)

## Business Rules Implementation

### Master File Structure
- Contains 3 sheets:
  1. Two sheets with answer data
  2. One 'Grading' sheet with:
     - WorksheetName: Specifies sheet to grade
     - Range: Cell range for grading
     - Comparison: Type (CorrForm or CorrVal)

### Grading Criteria
1. **CorrVal:**
   - Only value must be correct
   - One point per correct cell/range

2. **CorrForm:**
   - Formula must be correct
   - One point per correct formula

## Error Handling

The application handles various error scenarios:
- Invalid Excel formats
- Empty files
- Missing sheets
- Invalid ranges
- Formula evaluation errors

### Custom Exceptions
- `EmptyFileException`: Thrown when an empty file is provided
- `NotOfficeXmlFileException`: Thrown when an invalid Excel file is provided

## Performance Considerations

- Asynchronous processing for large files
- Efficient memory management
- Proper resource cleanup
- Database optimization for task tracking
- Docker container resource limits
- Connection pooling for database operations


