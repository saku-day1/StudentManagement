package raisetech.StudentManagement.exception;

/**
 * 論理削除されていない受講生を復元しようとした場合
 */
public class StudentAlreadyActiveException extends RuntimeException {
    /**
     * 例外メッセージを指定して例外を生成します。
     * @param id 受講生ID
     */
    public StudentAlreadyActiveException(String id) {
        super("受講生ID：" + id + " はすでに有効状態のため復元できません。");
    }
}
