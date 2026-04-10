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

    private String id;

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "フリガナは必須です")
    @Pattern(regexp = "^[ァ-ヶー]+$", message = "フリガナはカタカナで入力してください")

    private String furigana;

    private String nickname;


    @NotBlank(message = "メールは必須です")
    @Email(message = "メール形式が不正です")
    private String email;

    @Min(value = 0, message = "年齢は0以上で入力してください")
    @Max(value = 150, message = "年齢は150以下で入力してください")
    private Integer age;

    @NotBlank
    private String area;

    @Pattern(regexp = "^(男性|女性|その他)$",
            message = "性別は指定された値を入力してください")
    private String gender;

    private String remarks;

    private boolean deleted;
}
