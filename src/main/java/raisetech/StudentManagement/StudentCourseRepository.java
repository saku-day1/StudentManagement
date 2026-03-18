package raisetech.StudentManagement;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentCourseRepository {
    @Select("SELECT * FROM student_courses")
    List<StudentCourse> search();
}
