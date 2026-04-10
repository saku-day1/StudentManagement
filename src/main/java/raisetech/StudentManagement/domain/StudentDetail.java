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
 * 生徒情報とコース情報をもつオブジェクト
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

