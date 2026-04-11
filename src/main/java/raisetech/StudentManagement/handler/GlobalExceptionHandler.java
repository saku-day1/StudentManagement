package raisetech.StudentManagement.handler;

import org.springframework.context.MessageSourceResolvable;
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
 * 受講生管理システムで発生する各種例外をハンドリングし、
 * エラーメッセージをレスポンスとして返却するクラスです。
 */
@RestControllerAdvice

public class GlobalExceptionHandler {
    /**
     * 受講生が見つからない場合の例外を処理し、
     * 404 Not Found のエラーレスポンスを返却します。
     *
     * @param e 受講生が見つからなかった場合の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleStudentException(StudentNotFoundException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);

    }

    /**
     * 重複したメールアドレスが指定された場合の例外を処理し、
     * 400 Bad Request のエラーレスポンスを返却します。
     *
     * @param e メールアドレス重複時の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateEmail(DuplicateEmailException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * リクエストボディのバリデーションエラーを処理し、
     * 400 Bad Request のエラーレスポンスを返却します。
     *
     * @param e バリデーションエラー
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    String field = error.getField();
                    field = field.replaceAll("studentCourseList\\[\\d+]\\.", "");
                    return field + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * リクエストボディの読み取りに失敗した場合の例外を処理し、
     * 400 Bad Request のエラーレスポンスを返却します。
     *
     * @param e リクエストボディの読み取り失敗時の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage("入力値の型が正しくありません");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * リクエストパラメータやパスパラメータのバリデーションエラーを処理し、
     * 400 Bad Request のエラーレスポンスを返却します。
     *
     * @param e メソッド引数のバリデーションエラー
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorMessage> handleHandlerMethodValidationException(
            HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}