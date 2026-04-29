package raisetech.StudentManagement.exception;

/**
 * 受講生IDが存在しない場合にスローされる例外です
 */
public class StudentNotFoundException extends RuntimeException {
    /**
     * 例外メッセージを指定して例外を生成します。
     *
     * @param id 受講生ID
     */
    public StudentNotFoundException(String id) {
        super("受講生ID：" + id + " が見つかりません。");
    }
}
