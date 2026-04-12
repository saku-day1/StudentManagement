# 論理削除の状態遷移図

```mermaid
stateDiagram-v2
    [*] --> Active : 受講生登録
    Active --> Deleted : 論理削除
    Deleted --> Active : 復元
    Active --> Active : 再復元は不可
    Deleted --> Deleted : 再削除は不可
    Active --> Active : StudentAlreadyActiveException
    Deleted --> Deleted : StudentAlreadyDeletedException