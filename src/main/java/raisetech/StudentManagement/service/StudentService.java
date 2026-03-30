package raisetech.StudentManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentsCourses;
import raisetech.StudentManagement.domein.StudentDetail;
import raisetech.StudentManagement.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class StudentService {
    private StudentRepository repository;

    @Autowired
    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<Student> searchStudentList() {
        return repository.search();
    }

    public StudentDetail searchStudent(String id) {
        Student student = repository.searchStudent(id);
        List<StudentsCourses> studentsCourses = repository.searchStudentsCourses(student.getId());

        StudentDetail studentDetail = new StudentDetail();
        studentDetail.setStudent(student);
        studentDetail.setStudentCourses(studentsCourses);

        if (studentsCourses != null && !studentsCourses.isEmpty()) {
            studentDetail.setStudentCourse(studentsCourses.get(0));
        } else {
            StudentsCourses studentCourse = new StudentsCourses();
            studentCourse.setStudentId(student.getId());
            studentDetail.setStudentCourse(studentCourse);
        }

        return studentDetail;
    }

    public List<StudentsCourses> searchStudentCourseList() {
        return repository.searchStudentsCourseList();
    }

    @Transactional
    public void registerStudent(StudentDetail studentDetail) {
        repository.registerStudent(studentDetail.getStudent());
        for (StudentsCourses studentsCourse : studentDetail.getStudentCourses()) {
            studentsCourse.setStudentId(studentDetail.getStudent().getId());
            studentsCourse.setCourseStartAt(LocalDateTime.now());
            studentsCourse.setCourseEndAt(LocalDateTime.now().plusYears(1));
            repository.registerStudentCourse(studentsCourse);
        }

    }

    public void updateStudent(StudentDetail studentDetail) {
        repository.updateStudent(studentDetail.getStudent());

        if (studentDetail.getStudentCourse() != null) {
            repository.updateStudentCourse(studentDetail.getStudentCourse());
        }


    }

}

