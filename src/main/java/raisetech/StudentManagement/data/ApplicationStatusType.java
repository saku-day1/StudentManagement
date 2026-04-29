package raisetech.StudentManagement.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 申込状況の状態を表すenum。
 * 仮申込から本申込、受講中、終了までのステータスの変化を管理する。
 * labelで日本語の表示名を取得する。
 */
@Getter
@Schema(description = "申込状況のステータス")
public enum ApplicationStatusType {

    @Schema(description = "仮申込")
    PROVISIONAL("仮申込"),

    @Schema(description = "本申込")
    CONFIRMED("本申込"),

    @Schema(description = "受講中")
    ACTIVE("受講中"),

    @Schema(description = "終了")
    COMPLETED("終了");

    private final String label;

    ApplicationStatusType(String label) {
        this.label = label;
    }

    public boolean canConfirm() {
        return this == PROVISIONAL;
    }

    public boolean canStart() {
        return this == CONFIRMED;
    }

    public boolean canComplete() {
        return this == ACTIVE;
    }

    public boolean canArchive() {
        return this == COMPLETED;
    }

    public boolean isCancelable() {
        return this == PROVISIONAL || this == CONFIRMED;
    }

    public static ApplicationStatusType fromLabel(String label) {
        for (ApplicationStatusType type : ApplicationStatusType.values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不正な申込状況です: " + label);
    }
}
