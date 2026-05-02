package raisetech.StudentManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
            summary = "受講生サマリー検索",
            description = "名前、コース名、申込状況を条件に受講生サマリー情報を検索します。複数条件指定時はAND検索を行います。名前は部分一致検索に対応しています。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = StudentSearchSummary.class)
                    ),
                    examples = @ExampleObject(
                            name = "一覧取得成功例",
                            value = """
                                    [
                                      {
                                        "studentId": 1,
                                        "studentCourseId": 1,
                                        "name": "田中啓介",
                                        "furigana": "タナカケイスケ",
                                        "email": "keisuke@example.com",
                                        "courseName": "Webデザインコース",
                                        "status": "仮申込"
                                      }
                                    ]
                                    """
                    )
            )
    )
    @GetMapping("/api/students/search")
    public List<StudentSearchSummary> searchStudentSummaries(
            @Validated @ModelAttribute StudentSearchCriteria criteria) {
        return service.searchStudentSummaries(criteria);
    }
}

