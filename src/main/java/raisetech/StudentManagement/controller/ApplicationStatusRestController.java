package raisetech.StudentManagement.controller;

import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.dto.ApiResult;
import raisetech.StudentManagement.service.ApplicationStatusService;

/**
 * 申込情報の検索、更新、論理削除をREST APIとして受け付けるコントローラーです。
 */
@RestController
@RequestMapping("/api/student-courses")
@Validated
public class ApplicationStatusRestController {
    private final ApplicationStatusService service;

    public ApplicationStatusRestController(ApplicationStatusService service) {
        this.service = service;
    }

    /**
     * 申込状況を検索します
     *
     * @param studentCourseId 受講生コースID
     * @return 申込状況および成功メッセージを含むレスポンス
     */
    @GetMapping("/{studentCourseId}/application-status")
    public ApiResult<ApplicationStatus> searchApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください") String studentCourseId) {
        ApplicationStatus applicationStatus = service.searchApplicationStatus(studentCourseId);

        return new ApiResult<>(
                "success",
                "申込状況を取得しました",
                applicationStatus
        );
    }

    /**
     * 申込状況を新規作成します。
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @PostMapping("/{studentCourseId}/application-status")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<Void> createApplicationStatus(
            @PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
            String studentCourseId) {

        service.createApplicationStatus(studentCourseId);

        return new ApiResult<>(
                "success",
                "申込状況を作成しました",
                null
        );
    }

    /**
     * 申込状況を本申込へ更新します
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @PatchMapping("/{studentCourseId}/application-status/confirm")
    public ApiResult<Void> confirmApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
     String studentCourseId) {
        service.confirmApplicationStatus(studentCourseId);
        return new ApiResult<>(
                "success",
                "本申込状況に更新しました",
                null
        );
    }

    /**
     * 申込状況を受講開始へ更新します
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @PatchMapping("/{studentCourseId}/application-status/start")
    public ApiResult<Void> startApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
     String studentCourseId) {
        service.startApplicationStatus(studentCourseId);
        return new ApiResult<>(
                "success",
                "受講開始状況に更新しました",
                null
        );
    }

    /**
     * 申込状況を受講終了へ更新します
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @PatchMapping("/{studentCourseId}/application-status/complete")
    public ApiResult<Void> completeApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
     String studentCourseId) {
        service.completeApplicationStatus(studentCourseId);
        return new ApiResult<>(
                "success",
                "受講終了状況に更新しました",
                null
        );
    }

    /**
     * 申込状況のキャンセルを行います
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @PatchMapping("/{studentCourseId}/application-status/cancel")
    public ApiResult<Void> cancelApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
     String studentCourseId) {
        service.cancelApplicationStatus(studentCourseId);
        return new ApiResult<>(
                "success",
                "申込処理をキャンセルしました",
                null
        );
    }

    /**
     * 申込状況の論理削除を行います
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @DeleteMapping("/{studentCourseId}/application-status/delete")
    public ApiResult<Void> deleteApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
     String studentCourseId) {
        service.archiveCompletedApplicationStatus(studentCourseId);
        return new ApiResult<>(
                "success",
                "申込状況を論理削除しました",
                null
        );
    }

    /**
     * 申込状況の復元を行います
     *
     * @param studentCourseId 受講生コースID
     * @return 成功メッセージを含むレスポンス
     */
    @PatchMapping("/{studentCourseId}/application-status/restore")
    public ApiResult<Void> restoreApplicationStatus
    (@PathVariable @Pattern(regexp = "^\\d+$", message = "IDは数字で入力してください")
     String studentCourseId) {
        service.restoreApplicationStatus(studentCourseId);
        return new ApiResult<>(
                "success",
                "申込状況を復元し、仮申込状態に戻しました",
                null
        );
    }
}
