package raisetech.StudentManagement.repository;

import org.apache.ibatis.annotations.Mapper;
import raisetech.StudentManagement.dto.StudentSearchCondition;
import raisetech.StudentManagement.dto.StudentSearchSummary;

import java.util.List;

@Mapper
public interface StudentSearchRepository {
    List<StudentSearchSummary> searchStudentSummaries(StudentSearchCondition condition);
}
