package raisetech.StudentManagement.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import raisetech.StudentManagement.dto.ApiResult;
import raisetech.StudentManagement.exception.DuplicateEmailException;
import raisetech.StudentManagement.exception.StudentAlreadyActiveException;
import raisetech.StudentManagement.exception.StudentAlreadyDeletedException;
import raisetech.StudentManagement.exception.StudentNotFoundException;

import java.util.stream.Collectors;

/**
 * 受講生管理システムで発生する各種例外をハンドリングし、
 * 統一形式のエラーレスポンスを返却するクラスです。
 *
 * <p>レスポンス形式は {@code ApiResponse<Void>} とし、
 * エラー時は {@code status} に {@code "error"}、
 * {@code message} にエラー内容、
 * {@code data} に {@code null} を設定します。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 指定した受講生が存在しない場合の例外を処理し、
     * 404 Not Found のレスポンスを返却します。
     *
     * @param e 受講生が見つからない場合の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleStudentNotFoundException(StudentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(e.getMessage()));
    }

    /**
     * 重複したメールアドレスが指定された場合の例外を処理し、
     * 409 Conflict のレスポンスを返却します。
     *
     * @param e メールアドレス重複時の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResult<Void>> handleDuplicateEmailException(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(e.getMessage()));
    }

    /**
     * リクエストボディに対するバリデーションエラーを処理し、
     * 400 Bad Request のレスポンスを返却します。
     *
     * <p>各フィールドエラーのメッセージを連結して返却します。
     * また、studentCourseList の添字付きフィールド名は見やすい形に整形します。</p>
     *
     * @param e リクエストボディのバリデーションエラー
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    String field = error.getField();
                    field = field.replaceAll("studentCourseList\\[\\d+]\\.", "");
                    return field + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(message));
    }

    /**
     * パスパラメータやリクエストパラメータに対するバリデーションエラーを処理し、
     * 400 Bad Request のレスポンスを返却します。
     *
     * @param e 制約違反が発生した場合の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolationException(
            ConstraintViolationException e) {

        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("入力値が不正です。");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(message));
    }

    /**
     * リクエストボディの読み取りに失敗した場合の例外を処理し、
     * 400 Bad Request のレスポンスを返却します。
     *
     * <p>主に、JSONの構文不正や型不一致などで発生します。</p>
     *
     * @param e リクエストボディの読み取り失敗時の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse("入力値の型が正しくありません"));
    }

    /**
     * メソッド引数に対するバリデーションエラーを処理し、
     * 400 Bad Request のレスポンスを返却します。
     *
     * <p>主に、{@code @PathVariable} や {@code @RequestParam} に付与した
     * バリデーションアノテーションの検証失敗時に発生します。</p>
     *
     * @param e メソッド引数のバリデーションエラー
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResult<Void>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e) {

        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(message));
    }

    /**
     * すでに論理削除済みの受講生を削除しようとした場合の例外を処理し、
     * 409 Conflict のレスポンスを返却します。
     *
     * @param e すでに論理削除済みの受講生に対する削除操作時の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(StudentAlreadyDeletedException.class)
    public ResponseEntity<ApiResult<Void>> handleStudentAlreadyDeletedException(
            StudentAlreadyDeletedException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(e.getMessage()));
    }

    /**
     * すでに有効状態の受講生を復元しようとした場合の例外を処理し、
     * 409 Conflict のレスポンスを返却します。
     *
     * @param e すでに有効状態の受講生に対する復元操作時の例外
     * @return エラーメッセージを含むレスポンス
     */
    @ExceptionHandler(StudentAlreadyActiveException.class)
    public ResponseEntity<ApiResult<Void>> handleStudentAlreadyActiveException(
            StudentAlreadyActiveException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(e.getMessage()));
    }

    /**
     * 統一形式のエラーレスポンスを生成します。
     *
     * @param message エラーメッセージ
     * @return status, message, data から成るエラーレスポンス
     */
    private ApiResult<Void> buildErrorResponse(String message) {
        return new ApiResult<>("error", message, null);
    }
}


