package raisetech.StudentManagement.data;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 受講生情報を扱うオブジェクト
 */
@Getter
@Setter
public class Student {

    @Pattern(regexp = "^\\d+$")
    private String id;

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "フリガナは必須です")
    @Pattern(regexp = "^[ァ-ヶー　]+$",
            message = "フリガナはカタカナで入力してください")
    private String furigana;

    private String nickname;

    @NotBlank(message = "メールアドレスは必須です")
    @Email
    private String email;

    @NotBlank
    private String area;

    private Integer age;

    private String gender;

    private String remarks;

    private boolean deleted;

}
