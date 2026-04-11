package raisetech.StudentManagement.dto;

import lombok.Getter;
import lombok.Setter;
/**
 * APIの実行結果として追加でメッセージを返却するためのDTOです。
 */

@Getter
@Setter
public class ResultMessage {
    String message;
    String studentId;
}
