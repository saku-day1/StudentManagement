package raisetech.StudentManagement.controller.converter;

import org.junit.jupiter.api.Test;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class StudentConverterTest {
    @Test
    void 受講生IDを元に受講生と受講生コース情報の紐づけが行われること()  {
        Student student = new Student();
        student.setId("1");
        StudentCourse course = new StudentCourse();
        course.setStudentId("1");
        StudentConverter converter = new StudentConverter();
        List<Student> studentList = new ArrayList<>();
        studentList.add(student);

        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(course);
        List<StudentDetail> converterStudentDetails =
                converter.convertStudentDetails(studentList, studentCourseList);
        assertEquals(1,converterStudentDetails.size());

        StudentDetail actual =converterStudentDetails.get(0);

        assertEquals("1",actual.getStudent().getId());
        assertEquals(1,actual.getStudentCourseList().size());
        assertEquals("1",actual.getStudentCourseList().get(0).getStudentId());
    }

}