# sample-cicd-springboot-gcp

Sample CI/CD Spring Boot Application on Google Cloud Platform

## プロジェクト構成

```
src/main/java/com/sn0326/cicddemo/
├── DemoApplication.java        # メインアプリケーションクラス
├── controller/
│   └── HelloController.java    # REST APIエンドポイント
└── model/
    └── AppInfo.java           # DTOモデル
```

## エンドポイント

- `GET /` - ウェルカムメッセージ
- `GET /health` - ヘルスチェック
- `GET /info` - アプリケーション情報（名前、バージョン、タイムスタンプ）

## 技術スタック

- Java 21
- Spring Boot 3.2.1
- Maven
- Google Cloud Run (デプロイ先)