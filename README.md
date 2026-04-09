# StudentManagement

Spring Bootを用いて作成している受講生管理アプリです。  
Java・Spring Boot・MyBatis・Thymeleafを用いたWebアプリケーション開発の学習を目的として制作しています。

## 概要
受講生情報の登録・一覧表示・更新・削除を行うCRUD機能を実装したアプリです。  
受講生とコース情報を管理し、画面操作を通して基本的なデータ管理ができるようにしています。

## 実装機能
- 受講生一覧表示
- 受講生詳細表示
- 受講生登録
- 受講生情報更新
- 受講生情報削除（論理削除）
- コース情報との紐づけ

## 使用技術
- Java
- Spring Boot
- MyBatis
- Thymeleaf
- HTML / CSS
- Gradle
- Git / GitHub

## 工夫した点
- Controller / Service / Repository の役割を分けて実装
- 受講生情報とコース情報を分けて管理
- CRUD処理の流れを意識しながら画面遷移を実装
- .gitignoreを設定し、設定ファイルや不要ファイルをGit管理対象から除外

## 学習を通して理解したこと
- Spring Bootにおける基本的な画面遷移
- CRUD処理の実装方法
- MyBatisを用いたデータベース操作
- Thymeleafによるフォーム送信と画面表示
- Git / GitHub を用いたブランチ運用、Pull Request、Merge の流れ

## 今後の課題
- バリデーションの追加
- 例外処理の整理
- コードの可読性向上
- READMEの改善
- テストコードの追加

## 補足
DB接続情報などの機密情報は環境変数で管理し、公開リポジトリに含めないようにしています。
