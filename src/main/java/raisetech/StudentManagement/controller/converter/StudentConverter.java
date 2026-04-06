package raisetech.StudentManagement.controller.converter;
import org.springframework.stereotype.Component;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentCourse;
import raisetech.StudentManagement.domain.StudentDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentConverter {

    public List<StudentDetail> convertStudentDetails(
            List<Student> students, List<StudentCourse> studentCourses) {

        List<StudentDetail> studentDetails = new ArrayList<>();

        students.forEach(student -> {
            StudentDetail studentDetail = new StudentDetail();
            studentDetail.setStudent(student);

            List<StudentCourse> converterStudentCourses = studentCourses.stream()
                    .filter(course -> student.getId().equals(course.getStudentId()))
                    .collect(Collectors.toList());

            studentDetail.setStudentCourseList(converterStudentCourses);

            studentDetails.add(studentDetail);
        });

        return studentDetails;
    }
}