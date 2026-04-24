package raisetech.StudentManagement.data;

/**
 * 申込状況の状態を表すenum。
 * 仮申込から本申込、受講中、終了までのステータスの変化を管理する。
 * labelで日本語の表示名を取得する。
 */
public enum ApplicationStatusType {
    PROVISIONAL("仮申込"),
    CONFIRMED("本申込"),
    ACTIVE("受講中"),
    COMPLETED("終了");

    private final String label;

    ApplicationStatusType(String label){
        this.label = label;
    }
    public String getLabel(){
        return label;
    }
}
