package raisetech.StudentManagement.data;


import lombok.Getter;
import lombok.Setter;

/**
 * 申込状況を保持するクラス
 */
@Getter
@Setter

public class ApplicationStatus {
    private String id;
    private String studentCourseId;
    private String status;
    private boolean isDeleted;
}
