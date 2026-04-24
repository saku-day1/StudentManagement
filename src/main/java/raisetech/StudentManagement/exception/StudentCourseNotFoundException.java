package raisetech.StudentManagement.exception;

/**
 * 受講生コースIDが存在しない場合にスローされる例外です
 */
public class StudentCourseNotFoundException extends RuntimeException {
    /**
     * 例外メッセージを指定して例外を生成します。
     *
     * @param studentCourseId 受講生コースID
     */
    public StudentCourseNotFoundException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " が見つかりません。");
    }
}
