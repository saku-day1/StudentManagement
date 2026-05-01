package raisetech.StudentManagement.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagement.dto.StudentSearchCriteria;
import raisetech.StudentManagement.dto.StudentSearchSummary;
import raisetech.StudentManagement.service.StudentSearchService;

import java.util.List;

/**
 * 受講生の検索を行うコントローラークラスです
 */
@RestController

public class StudentSearchRestController {
    private final StudentSearchService service;

    public StudentSearchRestController(StudentSearchService service) {
        this.service = service;
    }
    /**
     * 受講生の検索を行います。
     *
     * <p>複数の検索条件を指定した場合はAND条件で検索されます。
     * nameおよびfuriganaは部分一致、それ以外は完全一致で検索されます。</p>
     *
     * <p>検索条件を指定しない場合は全件検索となります。</p>
     *
     * @param criteria 検索条件
     * @return 受講生サマリー情報のリスト
     */
    @GetMapping("/api/students/search")
    public List<StudentSearchSummary> searchStudentSummaries(
            @Validated  @ModelAttribute StudentSearchCriteria criteria) {
        return service.searchStudentSummaries(criteria);
    }
}

