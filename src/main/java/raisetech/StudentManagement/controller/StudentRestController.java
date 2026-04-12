package raisetech.StudentManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.dto.ResultMessage;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;

/**
 * 受講生情報の検索、登録、更新、削除、復元をREST APIとして受け付けるコントローラーです。
 */

@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentService service;

    public StudentRestController(StudentService service) {
        this.service = service;
    }

    /**
     * 受講生詳細一覧を取得します。
     *
     * @return 受講生詳細一覧
     */
    @Operation(summary = "一覧検索",description = "受講生の一覧を取得します")
    @GetMapping
    public List<StudentDetail> getStudentList() {
        return service.searchStudentList();
    }

    /**
     * 指定した受講生IDに紐づく受講生詳細を取得します。
     *
     * @param id 受講生ID
     * @return 受講生詳細
     */
    @Operation(summary = "詳細検索",description = "受講生の一覧の詳細を取得します")
    @GetMapping("/{id}")
    public StudentDetail getStudent(
            @PathVariable
            @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
            String id) {

        return service.searchStudent(id);
    }


    /**
     * 受講生詳細を登録します。
     *
     * @param studentDetail 登録対象の受講生詳細情報
     * @return 処理成功メッセージと受講生ID
     */
    @Operation(summary = "受講生登録",description = "受講生を登録します")
    @PostMapping
    public ResponseEntity<ResultMessage> registerStudent(
            @Valid @RequestBody StudentDetail studentDetail) {
        service.registerStudent(studentDetail);
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setMessage("登録処理が成功しました");
        resultMessage.setStudentId(studentDetail.getStudent().getId());
        return ResponseEntity.ok(resultMessage);
    }

    /**
     * 指定したIDの受講生情報を更新します。
     *
     * @param id            受講生ID
     * @param studentDetail 更新対象の受講生詳細情報
     * @return 実行結果
     */
    @Operation(summary = "受講生更新",description = "受講生の更新を行います")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id,
            @Valid @RequestBody StudentDetail studentDetail) {
        studentDetail.getStudent().setId(id);
        service.updateStudent(studentDetail);
        return ResponseEntity.ok("受講生情報を更新しました");
    }

    /**
     * 指定したIDの受講生情報を論理削除します。
     *
     * @param id 受講生ID
     * @return 実行結果
     */
    @Operation(summary = "受講生を論理削除する", description = "指定したIDの受講生を論理削除し、一覧表示の対象外にします。")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id) {
        service.deleteStudent(id);
        return ResponseEntity.ok("受講生を論理削除しました");
    }

    /**
     * 指定したIDの受講生情報を復元します。
     *
     * @param id 受講生ID
     * @return 実行結果
     */
    @Operation(summary = "受講生を復元する", description = "指定したIDの受講生を復元し、一覧表示の対象にします。")
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreStudent(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String id) {
        service.restoreStudent(id);
        return ResponseEntity.ok("受講生情報を復元しました");
    }


}