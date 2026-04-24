package raisetech.StudentManagement.exception;

/**
 * 登録済みのメールアドレスが指定された場合にスローされる例外です。
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * 例外メッセージを指定して例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public DuplicateEmailException(String message) {
        super(message);
    }
}