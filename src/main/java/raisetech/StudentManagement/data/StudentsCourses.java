package raisetech.StudentManagement.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "studentId",
        "courseName",
        "courseStartAt",
        "courseEndAt"
})

@Getter
@Setter

public class StudentsCourses {
    private String id;
    private String studentId;
    private String courseName;
    private LocalDateTime courseStartAt;
    private LocalDateTime courseEndAt;
}

