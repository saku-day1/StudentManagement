package raisetech.StudentManagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagement.data.ApplicationStatus;
import raisetech.StudentManagement.data.ApplicationStatusType;
import raisetech.StudentManagement.exception.ApplicationStatusAlreadyActiveException;
import raisetech.StudentManagement.exception.ApplicationStatusAlreadyDeletedException;
import raisetech.StudentManagement.exception.InvalidApplicationException;
import raisetech.StudentManagement.exception.StudentCourseNotFoundException;
import raisetech.StudentManagement.handler.GlobalExceptionHandler;
import raisetech.StudentManagement.service.ApplicationStatusService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationStatusRestController.class)
@Import(GlobalExceptionHandler.class)
class ApplicationStatusRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationStatusService service;

    @Test
    void 検索処理_申込状況が検索できること() throws Exception {
        String studentCourseId = "1";

        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStudentCourseId(studentCourseId);
        applicationStatus.setStatus(ApplicationStatusType.PROVISIONAL.getLabel());
        applicationStatus.setDeleted(false);

        when(service.searchApplicationStatus(studentCourseId)).thenReturn(applicationStatus);

        mockMvc.perform(get("/api/student-courses/{studentCourseId}/application-status", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("申込状況を取得しました"))
                .andExpect(jsonPath("$.data.studentCourseId").value(studentCourseId))
                .andExpect(jsonPath("$.data.status").value("仮申込"))
                .andExpect(jsonPath("$.data.deleted").value(false));

        verify(service, times(1)).searchApplicationStatus(studentCourseId);
    }

    @Test
    void 検索処理_受講生コースIDが存在しない場合に404エラーが返ること() throws Exception {
        String studentCourseId = "999";

        when(service.searchApplicationStatus(studentCourseId))
                .thenThrow(new StudentCourseNotFoundException(studentCourseId));

        mockMvc.perform(get("/api/student-courses/{studentCourseId}/application-status", studentCourseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生コースID：" + studentCourseId + " が見つかりません。"));

        verify(service, times(1)).searchApplicationStatus(studentCourseId);
    }

    @Test
    void 検索処理_受講生コースIDが不正な形式の場合に400エラーが返ること() throws Exception {
        String studentCourseId = "abc";

        mockMvc.perform(get("/api/student-courses/{studentCourseId}/application-status", studentCourseId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("IDは数字で入力してください"));

        verify(service, never()).searchApplicationStatus(any());
    }

    @Test
    void 仮申込作成処理_仮申込を作成したときに201が返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(post("/api/student-courses/{studentCourseId}/application-status", studentCourseId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("申込状況を作成しました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).createApplicationStatus(studentCourseId);
    }

    @Test
    void 仮申込作成処理_受講生コースIDが存在しない場合に404エラーが返ること() throws Exception {
        String studentCourseId = "999";

        doThrow(new StudentCourseNotFoundException(studentCourseId))
                .when(service).createApplicationStatus(studentCourseId);

        mockMvc.perform(post("/api/student-courses/{studentCourseId}/application-status", studentCourseId))
                .andExpect(status().isNotFound());

        verify(service, times(1)).createApplicationStatus(studentCourseId);
    }

    @Test
    void 仮申込作成処理_受講生コースIDが不正な形式の場合に400エラーが返ること() throws Exception {
        String studentCourseId = "abc";

        mockMvc.perform(post("/api/student-courses/{studentCourseId}/application-status", studentCourseId))
                .andExpect(status().isBadRequest());

        verify(service, never()).createApplicationStatus(any());
    }

    @Test
    void 本申込処理_本申込状態へ更新したときに成功メッセージが返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/confirm", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("本申込状況に更新しました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).confirmApplicationStatus(studentCourseId);
    }

    @Test
    void 本申込_仮申込以外の状態の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new InvalidApplicationException(
                studentCourseId,
                "仮申込状態のみ本申込に変更できます。"
        )).when(service).confirmApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/confirm", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + "：仮申込状態のみ本申込に変更できます。"));

        verify(service, times(1)).confirmApplicationStatus(studentCourseId);
    }

    @Test
    void 本申込_受講生コースIDが存在しない場合に404エラーが返ること() throws Exception {
        String studentCourseId = "999";

        doThrow(new StudentCourseNotFoundException(studentCourseId))
                .when(service).confirmApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/confirm", studentCourseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生コースID：" + studentCourseId + " が見つかりません。"));

        verify(service, times(1)).confirmApplicationStatus(studentCourseId);
    }

    @Test
    void 受講中処理_受講中状態へ更新したときに成功メッセージが返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/start", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("受講開始状況に更新しました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).startApplicationStatus(studentCourseId);
    }

    @Test
    void 受講中_本申込以外の状態の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new InvalidApplicationException(
                studentCourseId,
                "本申込状態のみ受講中に変更できます。"
        )).when(service).startApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/start", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + "：本申込状態のみ受講中に変更できます。"));

        verify(service, times(1)).startApplicationStatus(studentCourseId);
    }

    @Test
    void 受講中_受講生コースIDが存在しない場合に404エラーが返ること() throws Exception {
        String studentCourseId = "999";

        doThrow(new StudentCourseNotFoundException(studentCourseId))
                .when(service).startApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/start", studentCourseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生コースID：" + studentCourseId + " が見つかりません。"));

        verify(service, times(1)).startApplicationStatus(studentCourseId);
    }

    @Test
    void 終了_終了状態へ更新したときに成功メッセージが返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/complete", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("終了状況に更新しました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).completeApplicationStatus(studentCourseId);
    }

    @Test
    void 終了_受講中以外の状態の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new InvalidApplicationException(
                studentCourseId,
                "受講中のみ終了にできます。"
        )).when(service).completeApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/complete", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + "：受講中のみ終了にできます。"));

        verify(service, times(1)).completeApplicationStatus(studentCourseId);
    }

    @Test
    void 終了_受講生コースIDが存在しない場合に404エラーが返ること() throws Exception {
        String studentCourseId = "999";

        doThrow(new StudentCourseNotFoundException(studentCourseId))
                .when(service).completeApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/complete", studentCourseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("受講生コースID：" + studentCourseId + " が見つかりません。"));

        verify(service, times(1)).completeApplicationStatus(studentCourseId);
    }

    @Test
    void 申込キャンセル_申込のキャンセルを行ったとき成功メッセージが返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/cancel", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("申込処理をキャンセルしました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).cancelApplicationStatus(studentCourseId);
    }

    @Test
    void 申込キャンセル_すでにキャンセル済みの場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new ApplicationStatusAlreadyDeletedException(studentCourseId))
                .when(service).cancelApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/cancel", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + " の申込状況は論理削除済みです。"));

        verify(service, times(1)).cancelApplicationStatus(studentCourseId);
    }

    @Test
    void 申込キャンセル_仮申込または本申込以外の状態の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new InvalidApplicationException(
                studentCourseId,
                "この申込状況はキャンセルできません。"
        )).when(service).cancelApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/cancel", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + "：この申込状況はキャンセルできません。"));

        verify(service, times(1)).cancelApplicationStatus(studentCourseId);
    }

    @Test
    void 申込状況の論理削除_申込状況の論理削除を行ったとき成功メッセージが返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(delete("/api/student-courses/{studentCourseId}/application-status/delete", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("申込状況を論理削除しました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).archiveCompletedApplicationStatus(studentCourseId);
    }

    @Test
    void 申込状況の論理削除_すでに論理削除済みの場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new ApplicationStatusAlreadyDeletedException(studentCourseId))
                .when(service).archiveCompletedApplicationStatus(studentCourseId);

        mockMvc.perform(delete("/api/student-courses/{studentCourseId}/application-status/delete", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + " の申込状況は論理削除済みです。"));

        verify(service, times(1)).archiveCompletedApplicationStatus(studentCourseId);
    }

    @Test
    void 申込状況の論理削除_終了状態以外の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new InvalidApplicationException(
                studentCourseId,
                "終了状態のみ非表示化できます。"
        )).when(service).archiveCompletedApplicationStatus(studentCourseId);

        mockMvc.perform(delete("/api/student-courses/{studentCourseId}/application-status/delete", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + "：終了状態のみ非表示化できます。"));

        verify(service, times(1)).archiveCompletedApplicationStatus(studentCourseId);
    }

    @Test
    void 申込状況の復元_申込状況の復元を行ったとき成功メッセージが返ること() throws Exception {
        String studentCourseId = "1";

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/restore", studentCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("申込状況を復元し、仮申込状態に戻しました"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service, times(1)).restoreApplicationStatus(studentCourseId);
    }

    @Test
    void 申込状況の復元_すでに有効状態の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new ApplicationStatusAlreadyActiveException(studentCourseId))
                .when(service).restoreApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/restore", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + " の申込状況は有効状態です。"));

        verify(service, times(1)).restoreApplicationStatus(studentCourseId);
    }

    @Test
    void 申込状況の復元_仮申込または本申込以外の状態の場合に409エラーが返ること() throws Exception {
        String studentCourseId = "1";

        doThrow(new InvalidApplicationException(
                studentCourseId,
                "仮申込または本申込状態のみ復元できます。"
        )).when(service).restoreApplicationStatus(studentCourseId);

        mockMvc.perform(patch("/api/student-courses/{studentCourseId}/application-status/restore", studentCourseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("受講生コースID：" + studentCourseId + "：仮申込または本申込状態のみ復元できます。"));

        verify(service, times(1)).restoreApplicationStatus(studentCourseId);
    }


}


