package raisetech.StudentManagement.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 検索条件を受け取るDTOです
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentSearchCondition {

    private String name;

    private String furigana;

    private String courseName;

    @Pattern(regexp = "^\\d+$", message = "受講生IDは数字で入力してください")
    private String studentId;

    @Pattern(regexp = "^\\d+$", message = "受講生コースIDは数字で入力してください")
    private String studentCourseId;

    private String status;
}
