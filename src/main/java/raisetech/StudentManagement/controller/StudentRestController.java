package raisetech.StudentManagement.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import raisetech.StudentManagement.domain.StudentDetail;
import raisetech.StudentManagement.dto.ResultMessage;
import raisetech.StudentManagement.service.StudentService;

import java.util.List;

/**
 * 受講生情報の登録、検索、更新をREST　APIとして受け付けるControllerです
 */
@Validated
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
     * @return 受講生一覧
     */
    @GetMapping
    public List<StudentDetail> getStudentsList() {
        return service.searchStudentList();
    }

    /**
     * 受講生詳細の検索です。
     * 受講生IDに紐づく、受講生の詳細情報を取得します。
     * @param id 受講生ID
     * @return 受講生
     */
    @GetMapping("/{id}")
    public StudentDetail getStudent(@PathVariable  @NotBlank @Pattern(regexp = "^\\d+$") String id) {
        return service.searchStudent(id);
    }


    /**
     * 受講生詳細登録処理です。
     *
     * @param studentDetail 受講生詳細情報
     * @return 処理成功メッセージと自動採番されたID
     */
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
     * @param id 受講生ID
     * @param studentDetail 更新対象の受講生詳細情報
     * @return 実行結果
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody StudentDetail studentDetail) {

        studentDetail.getStudent().setId(id);
        service.updateStudent(studentDetail);
        return ResponseEntity.ok("受講生情報を更新しました");
    }

    /**
     * 指定したIDの受講生情報を論理削除します。
     * @param id 受講生ID
     * @return 実行結果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(
            @PathVariable String id){
        service.deleteStudent(id);
        return ResponseEntity.ok("受講生を論理削除しました");
    }

    /**
     * 削除したIDの受講生情報を復元します。
     * @param id 受講生ID
     * @return 実行結果
     */
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreStudent(
            @PathVariable String id){
        service.restoreStudent(id);
        return ResponseEntity.ok("受講生情報を復元しました");
    }

}