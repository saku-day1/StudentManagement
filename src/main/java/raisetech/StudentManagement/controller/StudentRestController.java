package raisetech.StudentManagement.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raisetech.StudentManagement.controller.converter.StudentConverter;
import raisetech.StudentManagement.data.Student;
import raisetech.StudentManagement.data.StudentsCourses;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.dto.UpdateStudentRequest;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;

@RestController
//一覧取得
@RequestMapping("/api/students")
public class StudentRestController {
    private final StudentService service;
    private final StudentConverter converter;
    public StudentRestController(StudentService service, StudentConverter converter) {
        this.service = service;
        this.converter = converter;
    }
    @GetMapping
    public List<StudentDetail> getStudentsList() {
        List<Student> students = service.searchStudentList();
        List<StudentsCourses> courses = service.searchStudentCourseList();
        return converter.convertStudentDetails(students, courses);
    }
    // 更新処理
    @PostMapping("/update")
    public ResponseEntity<String> updateStudent(
            @Valid @RequestBody UpdateStudentRequest request) {
        service.updateStudent(request);
        return ResponseEntity.ok("更新処理が成功しました。");
    }
}