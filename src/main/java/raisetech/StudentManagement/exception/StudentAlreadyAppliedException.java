package raisetech.StudentManagement.exception;

/**
 * すでに申込状況が存在する場合にスローされる例外です
 */
public class StudentAlreadyAppliedException extends RuntimeException {
    /**
     * 例外メッセージを指定して例外を生成します
     *
     * @param studentCourseId 受講生コースID
     */
    public StudentAlreadyAppliedException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " はすでに申し込まれています。");
    }
}
