package raisetech.StudentManagement.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;
import raisetech.StudentManagement.service.StudentSearchService;

import java.util.List;

@RestController

public class StudentSearchRestController {
    private final StudentSearchService service;

    public StudentSearchRestController(StudentSearchService service) {
        this.service = service;
    }

    @GetMapping("/api/students/search")
    public List<StudentSearchSummary> searchStudentSummaries(
            @Validated  @ModelAttribute StudentSearchCriteria condition) {
        return service.searchStudentSummaries(condition);
    }
}

