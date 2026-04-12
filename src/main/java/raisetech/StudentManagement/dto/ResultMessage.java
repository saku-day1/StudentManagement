package raisetech.StudentManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * APIの実行結果としてメッセージと受講生IDを返却するためのDTOです。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultMessage {
    private String message;
    private String studentId;

}
