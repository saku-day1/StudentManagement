package raisetech.StudentManagement.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import raisetech.StudentManagement.dto.ErrorMessage;
import raisetech.StudentManagement.exception.StudentNotFoundException;


@RestControllerAdvice

public class GlobalExceptionHandler {
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleStudentException(StudentNotFoundException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }
}
