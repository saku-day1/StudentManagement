package raisetech.StudentManagement.controller.converter;

import org.junit.jupiter.api.Test;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class StudentConverterTest {
    @Test
    void 受講生IDを元に受講生と受講生コース情報の紐づけが行われること() {
        //準備
        //受講生情報とコース情報とコンバーターの準備
        List<StudentDetail> converterStudentDetails = getStudentDetails("1");
        assertEquals(1, converterStudentDetails.size());

        StudentDetail actual = converterStudentDetails.get(0);

        assertEquals("1", actual.getStudent().getId());
        assertEquals(1, actual.getStudentCourseList().size());
        assertEquals("1", actual.getStudentCourseList().get(0).getStudentId());
    }

    @Test
    void 受講生IDが違う場合に受講生と受講生コース情報は紐づかないこと() {
        List<StudentDetail> converterStudentDetails = getStudentDetails("2");

        assertEquals(1, converterStudentDetails.size());

        StudentDetail actual = converterStudentDetails.get(0);

        assertEquals("1", actual.getStudent().getId());
        assertTrue(actual.getStudentCourseList().isEmpty());
    }

    // 受講生IDが"1"の受講生情報と、引数で指定した受講生IDを持つコース情報を作成し、
    // コンバーターで受講生詳細リストに変換するメソッド
    private static List<StudentDetail> getStudentDetails(String courseStudentId) {
        Student student = new Student();
        student.setId("1");
        StudentCourse course = new StudentCourse();
        course.setStudentId(courseStudentId);
        StudentConverter converter = new StudentConverter();

        List<Student> studentList = new ArrayList<>();
        studentList.add(student);
        List<StudentCourse> studentCourseList = new ArrayList<>();
        studentCourseList.add(course);

        List<StudentDetail> converterStudentDetails =
                converter.convertStudentDetails(studentList, studentCourseList);
        return converterStudentDetails;
    }
}