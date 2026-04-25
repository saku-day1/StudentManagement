package raisetech.StudentManagement.data;

import lombok.Getter;

/**
 * 申込状況の状態を表すenum。
 * 仮申込から本申込、受講中、終了までのステータスの変化を管理する。
 * labelで日本語の表示名を取得する。
 */
@Getter
public enum ApplicationStatusType {
    PROVISIONAL("仮申込"),
    CONFIRMED("本申込"),
    ACTIVE("受講中"),
    COMPLETED("終了");


    private final String label;

    ApplicationStatusType(String label){
        this.label = label;
    }

    public boolean canConfirm(){
        return this == PROVISIONAL;
    }
    public boolean canStart(){
        return this == CONFIRMED;
    }

    public boolean canComplete(){
        return this == ACTIVE;
    }

    public boolean canArchive(){return this == COMPLETED;}

    public boolean isCancelable() {return this == PROVISIONAL || this == CONFIRMED;}

    public static ApplicationStatusType fromLabel(String label){
        for (ApplicationStatusType type : ApplicationStatusType.values()){
            if (type.getLabel().equals(label)){
                return type;
            }
        }throw new IllegalArgumentException("不正な申込状況です: " + label);
    }

}
