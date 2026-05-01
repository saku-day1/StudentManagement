package raisetech.StudentManagement.dto;

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
    private String studentId;
    private String studentCourseId;
    private String status;
    private String email;
    private String area;
    private Integer age;
    private String gender;
    private Boolean deleted;
}
