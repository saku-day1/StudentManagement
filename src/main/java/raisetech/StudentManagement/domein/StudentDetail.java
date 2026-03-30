package raisetech.StudentManagement.domein;

import lombok.Getter;
import lombok.Setter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentsCourses;

import java.util.List;

@Getter
@Setter
public class StudentDetail {
    private Student student;
    private List<StudentsCourses> studentCourses;
    private StudentsCourses studentCourse;
}
