package raisetech.StudentManagement.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import raisetech.StudentManagement.dto.ErrorMessage;
import raisetech.StudentManagement.exception.DuplicateEmailException;
import raisetech.StudentManagement.exception.StudentNotFoundException;

import java.util.stream.Collectors;

/**
 * 例外処理を行うためのクラスです
 */
@RestControllerAdvice

/**
 * 受講生情報の検索時に存在しない値を入力時に例外処理を発生させます。
 *
 */
public class GlobalExceptionHandler {
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleStudentException(StudentNotFoundException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);

    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateEmail(DuplicateEmailException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    String field = error.getField();
                    field = field.replaceAll("studentCourseList\\[\\d+\\]\\.", "");
                    return field + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage("入力値の型が正しくありません");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }


    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorMessage> handleHandlerMethodValidationException(
            HandlerMethodValidationException e) {

        String message = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}