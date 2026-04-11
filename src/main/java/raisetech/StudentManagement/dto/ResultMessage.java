package raisetech.StudentManagement.dto;

import lombok.Getter;
import lombok.Setter;


/**
 * APIの実行結果としてメッセージと受講生IDを返却するためのDTOです。
 */
@Getter
@Setter
public class ResultMessage {
    private String message;
    private String studentId;
}
