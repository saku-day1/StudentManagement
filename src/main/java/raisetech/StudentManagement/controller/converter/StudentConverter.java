package raisetech.StudentManagement.controller.converter;

import org.springframework.stereotype.Component;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentsCourses;
import raisetech.StudentManagement.domein.StudentDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component

public class StudentConverter {
    public List<StudentDetail> convertStudentDetails
            (List<Student> students, List<StudentsCourses> studentsCourses) {
        List<StudentDetail> studentDetails = new ArrayList<>();
        students.forEach(student -> {
            StudentDetail studentDetail = new StudentDetail();
            studentDetail.setStudent(student);
            List<StudentsCourses> converterStudentCourses = studentsCourses.stream()
                    .filter(studentCourse -> student.getId().equals(studentCourse.getStudentId()))
                    .collect(Collectors.toList());
            studentDetail.setStudentCourses(converterStudentCourses);
            studentDetails.add(studentDetail);
        });
        return studentDetails;
    }
}
