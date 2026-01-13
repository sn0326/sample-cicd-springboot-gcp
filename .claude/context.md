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
