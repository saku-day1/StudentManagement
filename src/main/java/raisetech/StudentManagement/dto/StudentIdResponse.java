package raisetech.StudentManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 受講生IDを返却するためのDTOです。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentIdResponse {
    private String studentId;
}