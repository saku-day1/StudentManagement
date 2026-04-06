package raisetech.StudentManagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentRequest {

    @NotBlank(message = "IDは必須です")
    private String id;

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "フリガナは必須です")
    private String furigana;

    @Email(message = "メール形式が不正です")
    @NotBlank(message = "メールは必須です")
    private String email;

    private String nickname;
    private String area;
    private int age;
    private String gender;
    private String remarks;
    private boolean deleted;

    @NotBlank(message = "コースIDは必須です")
    private String courseId;

    @NotBlank(message = "コース名は必須です")
    private String courseName;
}