package raisetech.StudentManagement.dto;

import jakarta.validation.constraints.Positive;
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
public class StudentSearchCriteria {

    private String name;

    private String furigana;

    private String courseName;

    @Positive
    private Integer studentId;

    @Positive
    private Integer studentCourseId;

    private String status;
}
