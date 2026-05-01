package raisetech.StudentManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 受講生情報・コース情報・申込状況をまとめて返却するDTO
 * 検索結果は一覧表示用のサマリー情報として返却する。
 * 詳細情報が必要な場合は、既存の受講生IDによる詳細検索APIを利用する。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentSearchSummary {
    private String studentId;
    private String studentCourseId;
    private String name;
    private String furigana;
    private String email;
    private String courseName;
    private String status;
}
