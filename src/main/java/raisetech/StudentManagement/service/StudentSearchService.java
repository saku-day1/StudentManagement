package raisetech.StudentManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raisetech.StudentManagement.dto.StudentSearchCondition;
import raisetech.StudentManagement.dto.StudentSearchSummary;
import raisetech.StudentManagement.repository.StudentSearchRepository;

import java.util.List;

@Service
public class StudentSearchService {
    private final StudentSearchRepository repository;

    @Autowired
    public StudentSearchService(StudentSearchRepository repository) {
        this.repository = repository;
    }

    public List<StudentSearchSummary> searchStudentSummaries(StudentSearchCondition condition) {
        return repository.searchStudentSummaries(condition);

    }
}
