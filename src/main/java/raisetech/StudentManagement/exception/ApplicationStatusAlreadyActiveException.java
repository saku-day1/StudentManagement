package raisetech.StudentManagement.exception;

/**
 * 申込状況がすでに有効状態（未削除）の場合にスローされる例外です。
 */
public class ApplicationStatusAlreadyActiveException extends RuntimeException {
    public ApplicationStatusAlreadyActiveException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " の申込状況はすでに有効状態です。");
    }
}
