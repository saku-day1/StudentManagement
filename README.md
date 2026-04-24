# StudentManagement

Spring Bootを用いた受講生管理システムです。  
CRUD機能に加え、論理削除・例外処理・テスト実装まで行っています。

---

## 概要
受講生およびコース情報を管理するWebアプリケーションです。  
基本的なCRUD処理に加えて、論理削除や復元、バリデーション、例外処理を実装しています。

---

## 主な機能
- 受講生一覧・詳細取得
- 受講生登録・更新・削除（論理削除）
- 削除データの復元
- コース情報との紐づけ
- 入力バリデーション
- 例外処理（独自例外 + ControllerAdvice）

---

## 使用技術
- Java
- Spring Boot
- MyBatis
- MySQL
- Thymeleaf
- JUnit / Mockito
- Swagger（OpenAPI）
- Gradle
- Git / GitHub

---

## テスト
- Service層：Mockitoを用いたユニットテスト
- Controller層：WebMvcTestによるAPIテスト
- Repository層：DBを用いたテスト
- 正常系・異常系の両方を考慮してテストを作成

---

## 工夫した点
- Controller / Service / Repository の責務分離
- 独自例外クラスを用いたエラーハンドリング設計
- 論理削除・復元機能によりデータ整合性を担保
- テストコードを通して品質担保を意識した開発

---

## 今後の課題
- 申込処理機能の実装（現在開発中）
- Service層の責務分割の見直し
- テストケースの網羅性向上
- パフォーマンスや保守性を意識したリファクタリング

---

## 補足
DB接続情報などの機密情報は環境変数で管理し、公開リポジトリには含めていません。
