package raisetech.StudentManagement.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 受講生コース情報を扱うオブジェクト
 */
@JsonPropertyOrder({
        "id",
        "studentId",
        "courseName",
        "courseStartAt",
        "courseEndAt"
})

@Getter
@Setter

public class StudentCourse {

    @Pattern(regexp = "^\\d+$")
    private String id;

    @Pattern(regexp = "^\\d+$")
    private String studentId;

    @NotBlank(message = "コース名は必須です")
    @Pattern(regexp = "^(Javaコース|AWSコース|Webデザインコース)$",
            message = "コース名は指定された値から入力してください")
    private String courseName;

    private LocalDateTime courseStartAt;

    private LocalDateTime courseEndAt;
}

