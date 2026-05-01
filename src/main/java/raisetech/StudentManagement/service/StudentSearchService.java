package raisetech.StudentManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;
import raisetech.StudentManagement.repository.StudentSearchRepository;

import java.util.List;

/**
 * 指定した検索条件に応じて受講生サマリーの検索を行うサービスです
 */
@Service
public class StudentSearchService {
    private final StudentSearchRepository repository;

    @Autowired
    public StudentSearchService(StudentSearchRepository repository) {
        this.repository = repository;
    }

    /**
     * 検索条件に基づいて受講生サマリー情報を取得します。
     *
     * <p>Controllerから受け取った検索条件をもとにRepositoryへ処理を委譲し、
     * 検索結果をそのまま返却します。</p>
     *
     * @param criteria 検索条件
     * @return 受講生サマリー情報のリスト（該当なしの場合は空リスト）
     */
    public List<StudentSearchSummary> searchStudentSummaries(StudentSearchCriteria criteria) {
        return repository.searchStudentSummaries(criteria);

    }
}
