package raisetech.StudentManagement.controller.converter;

import org.springframework.stereotype.Component;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 受講生一覧と受講生コース一覧をもとに、受講生ごとの詳細情報を生成するコンバーターです。
 */
@Component
public class StudentConverter {
    /**
     * 受講生一覧と受講生コース一覧を紐づけて、
     * 受講生詳細一覧に変換します。
     * @param students 受講生一覧
     * @param studentCourses 受講生コース一覧
     * @return 受講生詳細一覧
     */
    public List<StudentDetail> convertStudentDetails(
            List<Student> students, List<StudentCourse> studentCourses) {
        List<StudentDetail> studentDetails = new ArrayList<>();
        students.forEach(student -> {
            StudentDetail studentDetail = new StudentDetail();
            studentDetail.setStudent(student);

            // 現在の受講生IDに紐づく受講生コース情報を抽出する
            List<StudentCourse> filterStudentCourses = studentCourses.stream()
                    .filter(course -> student.getId().equals(course.getStudentId()))
                    .collect(Collectors.toList());

            studentDetail.setStudentCourseList(filterStudentCourses);
            studentDetails.add(studentDetail);
        });

        return studentDetails;
    }
}