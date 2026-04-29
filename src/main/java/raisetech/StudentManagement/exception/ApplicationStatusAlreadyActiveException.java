package raisetech.StudentManagement.exception;

/**
 * 申込状況が有効状態の場合にスローされる例外です
 */
public class ApplicationStatusAlreadyActiveException extends RuntimeException {
    public ApplicationStatusAlreadyActiveException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " の申込状況は有効状態です。");
    }
}
