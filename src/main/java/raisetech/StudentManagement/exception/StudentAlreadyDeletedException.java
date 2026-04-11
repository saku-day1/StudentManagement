package raisetech.StudentManagement.exception;

/**
 * 受講生情報がすでに論理削除されている場合にスローされる例外です。
 */
public class StudentAlreadyDeletedException extends RuntimeException {
    /**
     * 例外メッセージを指定して例外を作成します。
     * @param id 受講生ID
     */
    public StudentAlreadyDeletedException(String id) {
        super("受講生ID：" + id + " はすでに論理削除済みです。");
    }
}
