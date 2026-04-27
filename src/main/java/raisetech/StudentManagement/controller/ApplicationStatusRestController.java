package raisetech.StudentManagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "申込状況API", description = "受講生コースに紐づく申込状況の検索・作成・更新・削除・復元を行うAPI")
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
    @Operation(
            summary = "申込状況検索",
            description = "受講生コースIDに紐づく申込状況を取得します。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "申込状況の取得に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "申込状況を取得しました",
                              "data": {
                                "id": "1",
                                "studentCourseId": "1",
                                "status": "仮申込",
                                "deleted": false,
                                "deletedAt": null
                              }
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 の申込状況が見つかりません。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況新規作成",
            description = "受講生コースIDに紐づく申込状況を新規作成します。初期状態は仮申込です。"
    )
    @ApiResponse(
            responseCode = "201",
            description = "申込状況の作成に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "申込状況を作成しました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースが存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "申込状況がすでに存在している",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：1 の申込状況はすでに存在しています。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況を本申込へ更新",
            description = "仮申込状態の申込状況を本申込状態へ遷移させます。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "本申込への更新に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "本申込状況に更新しました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースまたは申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "仮申込以外の状態のため本申込へ更新できない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "現在の申込状況では本申込に更新できません。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況を受講開始へ更新",
            description = "本申込状態の申込状況を受講開始状態へ遷移させます。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "受講開始への更新に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "受講開始状況に更新しました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースまたは申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "本申込以外の状態のため受講開始へ更新できない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "現在の申込状況では受講開始に更新できません。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況を受講終了へ更新",
            description = "受講開始状態の申込状況を受講終了状態へ遷移させます。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "受講終了への更新に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "受講終了状況に更新しました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースまたは申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "受講開始以外の状態のため受講終了へ更新できない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "現在の申込状況では受講終了に更新できません。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況をキャンセル",
            description = "仮申込または本申込状態の申込状況をキャンセルします。受講開始後の申込状況はキャンセルできません。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "申込状況のキャンセルに成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "申込処理をキャンセルしました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースまたは申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "キャンセルできない申込状態",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "現在の申込状況ではキャンセルできません。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況の論理削除",
            description = "受講終了状態の申込状況を論理削除します。削除された申込状況は履歴として保持されます。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "申込状況の論理削除に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "申込状況を論理削除しました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースまたは申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "受講終了以外の状態のため削除できない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "現在の申込状況では削除できません。"
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "申込状況の復元",
            description = "論理削除された申込状況を復元し、仮申込状態に戻します。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "申込状況の復元に成功",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": "success",
                              "message": "申込状況を復元し、仮申込状態に戻しました",
                              "data": null
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "IDの形式が不正",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "IDは数字で入力してください"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "受講生コースまたは申込状況が存在しない",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "受講生コースID：999 が見つかりません。"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "復元できない申込状態",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "message": "申込状況はすでに有効状態です。"
                            }
                            """)
            )
    )
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
