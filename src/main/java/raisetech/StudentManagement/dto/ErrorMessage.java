package raisetech.StudentManagement.dto;

import lombok.Getter;
import lombok.Setter;
/**
 * APIの実行結果として、エラーメッセージを返却するためのDTOです。
 */
@Getter
@Setter
public class ErrorMessage {
    private String message;

}