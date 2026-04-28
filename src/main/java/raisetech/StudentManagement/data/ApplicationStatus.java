package raisetech.StudentManagement.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 申込状況を保持するクラス
 */
@Getter
@Setter
@Schema(description = "申込状況")
public class ApplicationStatus {

    @Schema(description = "申込状況ID", example = "1")
    private String id;

    @Schema(description = "受講生コースID", example = "10")
    private String studentCourseId;

    @Schema(description =
            "申込ステータス（仮申込・本申込・受講中・終了）", example = "仮申込")
    private String status;

    @Schema(description = "論理削除フラグ", example = "false")
    private boolean deleted;

    @Schema(description = "論理削除日時", example = "2026-01-01T00:00:00")
    private LocalDateTime deletedAt;
}
