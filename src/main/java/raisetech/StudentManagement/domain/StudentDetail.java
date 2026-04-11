package raisetech.StudentManagement.domain;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;

import java.util.List;

/**
 * 受講生情報とコース情報を保持するデータクラス
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetail {
    @Valid
    private Student student;
    @Valid
    private List<StudentCourse> studentCourseList;

}

