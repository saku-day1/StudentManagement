package raisetech.StudentManagement.exception;

/**
 * 申込状況が期待する状態ではない場合にスローされる例外です。
 */
public class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String studentCourseId) {
        super("受講生コースID：" + studentCourseId + " は現在の状態では処理できません。");
    }
}