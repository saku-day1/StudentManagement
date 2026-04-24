package raisetech.StudentManagement.exception;

/**
 * 申込状況が見つからない場合にスローされる例外です
 */
public class ApplicationStatusNotFoundException extends RuntimeException {
    /**
     * 例外メッセージを指定して例外を生成します
     *
     * @param studentCourseId 受講生コースID
     */
    public ApplicationStatusNotFoundException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " の申込状況が見つかりませんでした。");
    }
}
