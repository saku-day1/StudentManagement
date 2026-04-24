package raisetech.StudentManagement.exception;

/**
 * 申込状況が論理削除済みの場合にスローされる例外です
 */
public class ApplicationStatusAlreadyDeletedException extends RuntimeException {
    public ApplicationStatusAlreadyDeletedException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " の申込状況は論理削除済みです。");
    }
}
