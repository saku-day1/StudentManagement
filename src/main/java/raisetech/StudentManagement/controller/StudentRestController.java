package raisetech.StudentManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.dto.ApiResult;
import raisetech.StudentManagement.dto.StudentIdResponse;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;

/**
 * 受講生情報の検索、登録、更新、削除、復元をREST APIとして受け付けるコントローラーです。
 */
@RestController
@RequestMapping("/api/students")
@Validated
public class StudentRestController {

    private final StudentService service;

    public StudentRestController(StudentService service) {
        this.service = service;
    }

    /**
     * 受講生詳細一覧を取得します。
     *
     * @return 受講生詳細一覧およびステータスとメッセージ含むレスポンス
     */
    @Operation(summary = "一覧取得", description = "受講生の一覧を取得します")
    @ApiResponse(responseCode = "200", description = "取得成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiResult.class),
                    examples = @ExampleObject(name = "一覧取得成功例",
                            value = """
                                    {
                                      "status": "success",
                                      "message": "受講生一覧を取得しました",
                                      "data": [
                                        {
                                          "student": {
                                            "id": "1",
                                            "name": "山田 太郎",
                                            "furigana": "ヤマダ タロウ",
                                            "nickname": "たろう",
                                            "email": "yamada@example.com",
                                            "area": "東京都",
                                            "age": 25,
                                            "gender": "男性",
                                            "remarks": "備考",
                                            "deleted": false
                                          },
                                          "studentCourseList": [
                                            {
                                              "id": "1",
                                              "studentId": "1",
                                              "courseName": "Javaコース",
                                              "courseStartAt": "2026-04-01T00:00:00",
                                              "courseEndAt": "2027-03-31T00:00:00"
                                            }
                                          ]
                                        }
                                      ]
                                    }
                                    """)))
    @GetMapping
    public ResponseEntity<ApiResult<List<StudentDetail>>> getStudentList() {
        List<StudentDetail> studentList = service.searchStudentList();
        return ResponseEntity.ok(
                new ApiResult<>(
                        "success",
                        "受講生一覧を取得しました",
                        studentList
                )
        );
    }

    /**
     * 指定した受講生IDに紐づく受講生詳細を取得します。
     *
     * @param id 受講生ID
     * @return 受講生詳細及びステータスとメッセージを含むレスポンス
     */
    @Operation(summary = "詳細検索", description = "指定した受講生の詳細情報を取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取得成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "詳細取得成功例",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "受講生詳細を取得しました",
                                              "data": {
                                                "student": {
                                                  "id": "1",
                                                  "name": "山田 太郎",
                                                  "furigana": "ヤマダ タロウ",
                                                  "nickname": "たろう",
                                                  "email": "yamada@example.com",
                                                  "area": "東京都",
                                                  "age": 25,
                                                  "gender": "男性",
                                                  "remarks": "備考",
                                                  "deleted": false
                                                },
                                                "studentCourseList": [
                                                  {
                                                    "id": "1",
                                                    "studentId": "1",
                                                    "courseName": "Javaコース",
                                                    "courseStartAt": "2026-04-01T00:00:00",
                                                    "courseEndAt": "2027-03-31T00:00:00"
                                                  }
                                                ]
                                              }
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "IDの形式が不正です",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "ID形式不正エラー例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "IDは数字で入力してください",
                                              "data": null
                                            }
                                            """))),
            @ApiResponse(responseCode = "404", description = "指定した受講生が存在しません",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "受講生未検出エラー例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "受講生ID：999 が見つかりません。",
                                              "data": null
                                            }
                                            """)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<StudentDetail>> getStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id) {
        StudentDetail student = service.searchStudent(id);
        return ResponseEntity.ok(
                new ApiResult<>(
                        "success",
                        "受講生詳細を取得しました",
                        student
                )
        );
    }

    /**
     * 受講生詳細を登録します。
     *
     * @param studentDetail 登録対象の受講生詳細情報
     * @return 処理成功メッセージと受講生ID
     */
    @Operation(summary = "受講生登録", description = "受講生情報を新規登録します。メールアドレスが重複している場合は登録できません。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "登録成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "登録成功例",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "登録処理が成功しました",
                                              "data": {
                                                "studentId": "1"
                                              }
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "不正な入力値",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "不正入力時のエラーメッセージ例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "student.name: 名前は必須です",
                                              "data": null
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "409", description = "メールアドレスの重複",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "重複したメールアドレス登録時",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "tani@example.com はすでに使われているメールアドレスです",
                                              "data": null
                                            }
                                            """)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ApiResult<StudentIdResponse>> registerStudent(
            @Valid @RequestBody StudentDetail studentDetail) {
        service.registerStudent(studentDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResult<>(
                        "success",
                        "登録処理が成功しました",
                        new StudentIdResponse(studentDetail.getStudent().getId())
                )
        );
    }

    /**
     * 指定したIDの受講生情報を更新します。
     *
     * @param id            受講生ID
     * @param studentDetail 更新対象の受講生詳細情報
     * @return 実行結果
     */
    @Operation(summary = "受講生更新", description = "指定した受講生情報を更新します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "更新成功例",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "受講生情報を更新しました",
                                              "data": {
                                                "studentId": "1"
                                              }
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "不正な入力値",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "不正入力時のエラーメッセージ例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "student.name: 名前は必須です",
                                              "data": null
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "指定した受講生が存在しません",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "受講生未検出エラー例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "受講生ID：999 が見つかりません。",
                                              "data": null
                                            }
                                            """)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<StudentIdResponse>> updateStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id,
            @Valid @RequestBody StudentDetail studentDetail) {
        studentDetail.getStudent().setId(id);
        service.updateStudent(studentDetail);
        return ResponseEntity.ok(
                new ApiResult<>(
                        "success",
                        "受講生情報を更新しました",
                        new StudentIdResponse(id)
                )
        );
    }

    /**
     * 指定したIDの受講生情報を論理削除します。
     *
     * @param id 受講生ID
     * @return 実行結果
     */
    @Operation(summary = "受講生を論理削除する", description = "指定した受講生を論理削除し、一覧表示から除外します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "論理削除",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "論理削除成功例",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "受講生を論理削除しました",
                                              "data": {
                                                "studentId": "1"
                                              }
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "指定した受講生が存在しません",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "受講生未検出例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "受講生ID：999 が見つかりません。",
                                              "data": null
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "409", description = "すでに削除済み",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "すでに論理削除済みの場合",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "受講生ID：1 はすでに論理削除済みです。",
                                              "data": null
                                            }
                                            """)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<StudentIdResponse>> deleteStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id) {
        service.deleteStudent(id);
        return ResponseEntity.ok(
                new ApiResult<>(
                        "success",
                        "受講生を論理削除しました",
                        new StudentIdResponse(id)
                )
        );
    }
    /**
     * 指定したIDの受講生情報を復元します。
     *
     * @param id 受講生ID
     * @return 実行結果
     */
    @Operation(summary = "受講生を復元する", description = "指定したIDの受講生を復元し、一覧表示の対象にします")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "復元処理成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "復元処理成功例",
                                    value = """
                                            {
                                              "status": "success",
                                              "message": "受講生情報を復元しました",
                                              "data": {
                                                "studentId": "1"
                                              }
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "指定した受講生が存在しません",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "受講生未検出例",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "受講生ID：999 が見つかりません。",
                                              "data": null
                                            }
                                            """)
                    )
            ),
            @ApiResponse(responseCode = "409", description = "すでに復元済み",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(name = "すでに復元済みの場合",
                                    value = """
                                            {
                                              "status": "error",
                                              "message": "受講生ID：1 はすでに有効状態のため復元できません。",
                                              "data": null
                                            }
                                            """)
                    )
            )
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResult<StudentIdResponse>> restoreStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id) {
        service.restoreStudent(id);
        return ResponseEntity.ok(
                new ApiResult<>(
                        "success",
                        "受講生情報を復元しました",
                        new StudentIdResponse(id)
                )
        );
    }

}