package sg.edu.ntu.taskflow_api.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//import sg.edu.ntu.taskflow_api.exceptions.ErrorResponse;

// ==================================================================================================================
// Validation annotation references:
// https://www.baeldung.com/java-validation
// https://education.launchcode.org/java-web-development/chapters/spring-model-validation/validation-annotations.html
// ==================================================================================================================

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // Get all validation errors
        List<ObjectError> validationErrors = ex.getBindingResult().getAllErrors();

        // Build an error message string from all errors
        StringBuilder sb = new StringBuilder();
        for (ObjectError error : validationErrors) {
            sb.append(error.getDefaultMessage()).append(". ");
        }

        ErrorResponse errorResponse = new ErrorResponse(sb.toString(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("Something went wrong: " + ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}