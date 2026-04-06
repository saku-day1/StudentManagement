package raisetech.StudentManagement.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 受講生コース情報を扱うオブジェクト
 */
@JsonPropertyOrder({
        "id",
        "studentId",
        "courseName",
        "courseStartAt",
        "courseEndAt"
})

@Getter
@Setter

public class StudentCourse {
    private String id;
    private String studentId;
    private String courseName;
    private LocalDateTime courseStartAt;
    private LocalDateTime courseEndAt;
}

