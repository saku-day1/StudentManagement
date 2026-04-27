package raisetech.StudentManagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
/**
 * 受講生登録後に受講生IDと受講生コースIDを返却するためのDTOです。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "受講生登録後に返却するID情報")
public class StudentRegisterResponse {

    @Schema(description = "受講生ID", example = "1")
    private String studentId;

    @Schema(description = "受講生コースID一覧", example = "[\"10\", \"11\"]")
    private List<String> studentCourseIds;
}
