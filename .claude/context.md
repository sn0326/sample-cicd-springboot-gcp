# プロジェクトコンテキスト

## GitHub CLI (gh) の使用方法

このリポジトリはClaude Code Web経由でアクセスされており、git remoteがローカルプロキシを指しています。

そのため、`gh` コマンドを使用する際は **必ず `--repo` オプション** を指定してください:

```bash
gh pr create --repo sn0326/sample-cicd-springboot-gcp --head <branch> --base main ...
gh pr view --repo sn0326/sample-cicd-springboot-gcp <pr-number>
gh pr merge --repo sn0326/sample-cicd-springboot-gcp <pr-number>
gh issue create --repo sn0326/sample-cicd-springboot-gcp ...
```

### 理由
- git remote: `http://127.0.0.1:59445/git/sn0326/sample-cicd-springboot-gcp`
- `gh` CLIはこのローカルプロキシをGitHubホストとして認識できない
- `--repo` オプションで明示的にGitHubリポジトリを指定することで回避可能

## プロジェクト情報

- **リポジトリ**: sn0326/sample-cicd-springboot-gcp
- **Java バージョン**: 21
- **Spring Boot バージョン**: 3.2.1
- **デプロイ先**: Google Cloud Run (asia-northeast1)

## アーキテクチャ

### パッケージ構成

```
com.sn0326.cicddemo/
├── DemoApplication.java        # @SpringBootApplication メインクラス
├── controller/                 # REST Controller層
│   └── HelloController.java   # @RestController - API エンドポイント
└── model/                      # データモデル層
    └── AppInfo.java           # record - DTO
```

### 設計方針

- **レイヤー分離**: Controller、Model を専用パッケージに配置
- **Spring Boot ベストプラクティス**: パッケージ構造を標準的な設計に準拠
- **保守性**: 各レイヤーの責任を明確に分離し、拡張しやすい構造を維持
