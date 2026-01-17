# sample-cicd-springboot-gcp

Sample CI/CD Spring Boot Application on Google Cloud Platform

エンタープライズレベルのセキュリティ機能を備えたSpring Bootサンプルアプリケーション。Google Cloud Platform上でCI/CDパイプラインを実装し、モダンな認証・認可機能を提供します。

## 📋 目次

- [技術スタック](#技術スタック)
- [主な機能](#主な機能)
- [プロジェクト構成](#プロジェクト構成)
- [セットアップ](#セットアップ)
- [エンドポイント](#エンドポイント)
- [セキュリティ機能](#セキュリティ機能)
- [データベース設計](#データベース設計)
- [設定](#設定)
- [デプロイ](#デプロイ)

## 技術スタック

### コアフレームワーク
- **Java**: 21
- **Spring Boot**: 4.0.1
- **ビルドツール**: Maven 3.9

### 主要な依存関係
| 依存関係 | 用途 |
|---------|------|
| spring-boot-starter-web | REST API・Webアプリケーション |
| spring-boot-starter-security | セキュリティ認証・認可 |
| spring-boot-starter-thymeleaf | テンプレートエンジン（HTMLレンダリング） |
| thymeleaf-extras-springsecurity6 | Thymeleafセキュリティ統合 |
| spring-boot-starter-validation | フォーム検証 |
| spring-boot-starter-oauth2-client | OAuth2/OIDC対応 |
| spring-boot-starter-data-jpa | データベースアクセス（JPA） |
| postgresql | PostgreSQLドライバー |
| lombok | ボイラープレートコード削減 |

### インフラストラクチャ
- **データベース**: PostgreSQL
- **デプロイ先**: Google Cloud Run
- **CI/CD**: GitHub Actions
- **コンテナ**: Docker（マルチステージビルド）

## 主な機能

### 🔐 セキュリティ機能
- **マルチ認証方式**
  - フォーム認証（ユーザー名・パスワード）
  - OAuth2/OIDC認証（Google連携）
- **Remember Me機能**: 永続トークンによる14日間の自動ログイン
- **アカウントロックアウト**: 5回の認証失敗で60分間ロック（TERASOLUNA準拠）
- **パスワードセキュリティ**: NIST SP 800-63B準拠の検証ルール
  - 8〜64文字の長さ制限
  - 弱いパスワード検出（83個のパターンリスト）
  - 連続文字数制限
  - ユーザー名含有チェック
- **強制パスワード変更**: 仮パスワードでのログイン後の本パスワード設定
- **セキュリティ対策**
  - ユーザー列挙攻撃対策
  - CSRF保護
  - XSS対策
  - セッション固定攻撃対策

### 👥 ユーザー管理
- ユーザーのCRUD操作（管理者権限）
- パスワードのリセット・変更
- アカウントの有効化・無効化
- ログイン履歴の記録

### 📧 メール機能
- プロバイダ選択可能（Mock/SendGrid）
- Thymeleafテンプレートレンダリング
- セキュリティ通知の送信

### ⏰ スケジューラー
- 弱いパスワードキャッシュの定期更新（1時間毎）
- 古い認証失敗記録の自動削除（7日以上前）
- Remember Meトークンの自動クリーンアップ（30日以上前）

## プロジェクト構成

```
src/main/java/com/sn0326/cicddemo/
├── DemoApplication.java                           # メインアプリケーション（@EnableScheduling）
├── config/                                        # 設定クラス
│   ├── SecurityConfig.java                        # セキュリティ設定
│   ├── UserDetailsConfig.java                     # ユーザー認証Bean定義
│   └── RememberMeProperties.java                  # Remember Me設定
├── controller/                                    # コントローラー（5個）
│   ├── WebController.java                         # ホーム・ログイン・パスワード変更
│   ├── AdminController.java                       # ユーザー管理（管理者専用）
│   ├── ProfileController.java                     # ユーザープロフィール
│   ├── ForcePasswordChangeController.java         # 強制パスワード変更
│   └── HelloController.java                       # 基本API
├── security/                                      # セキュリティ関連（9個）
│   ├── AccountLockoutUserDetailsChecker.java      # アカウントロック状態チェック
│   ├── CleanupJdbcTokenRepository.java            # Remember Meトークン自動削除
│   ├── PasswordChangeRequiredFilter.java          # 強制パスワード変更フィルター
│   ├── CustomOidcUserService.java                 # カスタムOIDCユーザーサービス
│   ├── FormAuthenticationSuccessHandler.java      # フォーム認証成功時処理
│   ├── FormAuthenticationFailureHandler.java      # フォーム認証失敗時処理
│   ├── OidcAuthenticationSuccessHandler.java      # OIDC認証成功時処理
│   ├── OidcAuthenticationFailureHandler.java      # OIDC認証失敗時処理
│   └── AuthenticationFailureEventListener.java    # 認証失敗イベントリスナー
├── service/                                       # ビジネスロジック（8個）
│   ├── AccountLockoutService.java                 # アカウントロックアウト管理
│   ├── WeakPasswordCacheService.java              # 弱いパスワードキャッシュ管理
│   ├── PasswordChangeService.java                 # パスワード変更
│   ├── ForcePasswordChangeService.java            # 強制パスワード変更
│   ├── AdminUserManagementService.java            # ユーザー管理（CRUD）
│   ├── LastLoginService.java                      # 前回ログイン情報取得
│   ├── OidcConnectionService.java                 # OIDC連携管理
│   └── notification/
│       └── SecurityNotificationService.java       # セキュリティ通知
├── validator/                                     # パスワード検証（8個）
│   ├── PasswordValidator.java                     # 統合検証エンジン
│   ├── PasswordValidationRule.java                # 検証ルールインターフェース
│   ├── PasswordStrength.java                      # 強度評価
│   ├── PasswordValidationResult.java              # 検証結果
│   └── rules/                                     # 検証ルール実装
│       ├── LengthValidationRule.java              # 長さチェック
│       ├── UsernameValidationRule.java            # ユーザー名含有チェック
│       ├── ConsecutiveCharsValidationRule.java    # 連続文字チェック
│       └── CommonPasswordValidationRule.java      # 弱いパスワードチェック
├── model/                                         # JPA エンティティ（6個）
│   ├── FailedAuthentication.java                  # 認証失敗記録
│   ├── UserLogin.java                             # ログイン履歴
│   ├── UserOidcConnection.java                    # OIDC連携情報
│   ├── WeakPassword.java                          # 弱いパスワードリスト
│   ├── OidcProvider.java                          # OIDCプロバイダ
│   └── AppInfo.java                               # アプリケーション情報DTO
├── dto/                                           # データ転送オブジェクト（5個）
│   ├── CreateUserRequest.java                     # ユーザー作成リクエスト
│   ├── UserInfo.java                              # ユーザー情報
│   ├── ChangePasswordRequest.java                 # パスワード変更リクエスト
│   ├── AdminResetPasswordRequest.java             # 管理者パスワードリセット
│   └── OidcConnectionInfo.java                    # OIDC連携情報DTO
├── repository/                                    # データアクセス層（6個）
│   ├── UserOidcConnectionRepository.java
│   ├── WeakPasswordRepository.java
│   ├── FailedAuthenticationRepository.java
│   ├── UserLoginRepository.java
│   ├── PasswordChangeRequirementRepository.java
│   └── JdbcPasswordChangeRequirementRepository.java
└── mail/                                          # メール送信機能
    ├── MailSender.java                            # メール送信インターフェース
    ├── MailMessage.java                           # メールメッセージ
    ├── MailTemplate.java                          # メールテンプレート
    ├── MailSendResult.java                        # 送信結果
    ├── config/
    │   ├── MailConfiguration.java                 # メール設定
    │   └── MailProperties.java                    # メールプロパティ
    ├── sender/
    │   └── MockMailSender.java                    # モックメール送信実装
    ├── template/
    │   └── MailTemplateRenderer.java              # テンプレートレンダリング
    └── exception/
        └── MailException.java                     # メール例外
```

**合計**: 60個のJavaファイル、約4,200行のコード

## セットアップ

### 前提条件
- Java 21
- Maven 3.9+
- PostgreSQL
- Google Cloud Platform アカウント（デプロイする場合）

### ローカル環境のセットアップ

1. **リポジトリのクローン**
```bash
git clone https://github.com/sn0326/sample-cicd-springboot-gcp.git
cd sample-cicd-springboot-gcp
```

2. **環境変数の設定**
```bash
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export PORT=8080
```

3. **データベースのセットアップ**
```bash
# PostgreSQLの起動とデータベース作成
createdb cicddemo
# スキーマとデータは自動的にロードされます（application.yamlのsql.init.mode: always）
```

4. **ビルドと実行**
```bash
# ビルド
mvn clean package

# 実行
java -jar target/demo-0.0.1-SNAPSHOT.jar

# または
mvn spring-boot:run
```

5. **アクセス**
- アプリケーション: http://localhost:8080
- ログインページ: http://localhost:8080/login

### テストユーザー

初期データとして以下のユーザーが登録されています：

| ユーザー名 | パスワード | 権限 |
|----------|---------|------|
| user | （デフォルト） | ROLE_USER |
| admin | （デフォルト） | ROLE_ADMIN, ROLE_USER |

## エンドポイント

### Web UIエンドポイント

| エンドポイント | メソッド | 認可 | 説明 |
|-------------|--------|------|------|
| `/login` | GET/POST | 公開 | ログインフォーム・処理 |
| `/home` | GET | 認証済み | ホームページ |
| `/admin` | GET | 認証済み | 管理者ページ |
| `/profile` | GET | 認証済み | ユーザープロフィール表示 |
| `/change-password` | GET/POST | 認証済み | パスワード変更 |
| `/force-change-password` | GET/POST | 認証済み | 強制パスワード変更 |
| `/admin/users` | GET | ADMIN | ユーザー一覧 |
| `/admin/users/new` | GET | ADMIN | ユーザー作成フォーム |
| `/admin/users` | POST | ADMIN | ユーザー作成 |
| `/admin/users/{username}/delete` | POST | ADMIN | ユーザー削除 |
| `/admin/users/{username}/reset-password` | GET/POST | ADMIN | パスワードリセット |
| `/admin/users/{username}/enable` | POST | ADMIN | ユーザー有効化 |
| `/admin/users/{username}/disable` | POST | ADMIN | ユーザー無効化 |
| `/logout` | GET | 認証済み | ログアウト |

### APIエンドポイント

| エンドポイント | メソッド | 認可 | 説明 |
|-------------|--------|------|------|
| `GET /` | GET | 公開 | ウェルカムメッセージ |
| `GET /health` | GET | 公開 | ヘルスチェック |
| `GET /info` | GET | 認証済み | アプリケーション情報（名前、バージョン、タイムスタンプ） |

## セキュリティ機能

### パスワードポリシー（NIST SP 800-63B準拠）

| 項目 | 値 | 説明 |
|------|-----|------|
| 最小文字数 | 8文字 | application.yaml設定 |
| 最大文字数 | 64文字 | application.yaml設定 |
| 最大連続文字数 | 3文字 | 同じ文字の最大連続数 |

**検証ルール**:
1. 長さチェック（8〜64文字）
2. ユーザー名を含まないか確認
3. 連続文字チェック（最大3文字）
4. 一般的な弱いパスワードリストとの照合（83個のパターン）
5. パスワード強度評価

### アカウントロック機能（TERASOLUNA準拠）

```yaml
セキュリティ設定:
  max-attempts: 5回
  duration-minutes: 60分
```

- 5回の認証失敗で60分間アカウントロック
- 古いレコードは7日以上前のものを定期削除
- 認証成功時に失敗記録をリセット
- 管理者による手動ロック解除機能

### Remember Me機能

```yaml
Remember Me設定:
  tokenValiditySeconds: 1209600（14日間）
  cleanup-days: 30日以上前のトークン自動削除
```

- 永続トークンによる自動ログイン
- トークン作成・取得時に自動クリーンアップ
- クッキー名: `remember-me`

### セキュリティ対策

- **ユーザー列挙攻撃対策**: エラーメッセージ隠蔽
- **アカウント情報隠蔽**: ロック状態を明示しない
- **CSRF保護**: Spring Securityのデフォルト有効
- **XSS対策**: Thymeleafによるエスケープ処理
- **セッション固定攻撃対策**: Spring Securityのデフォルト有効

## データベース設計

### テーブル一覧

**ユーザー認証テーブル**:
```sql
users                    -- Spring Security標準テーブル
  ├── username (PK)
  ├── password (bcrypt暗号化)
  ├── enabled (boolean)
  └── password_must_change (boolean)

authorities              -- 権限テーブル
  ├── username (FK)
  └── authority
```

**OIDC連携テーブル**:
```sql
user_oidc_connections   -- Google連携情報
  ├── id (PK)
  ├── username (FK)
  ├── provider (Google)
  ├── provider_id (UNIQUE)
  ├── email
  ├── enabled
  ├── created_at
  └── updated_at
```

**ログイン履歴テーブル**:
```sql
user_logins             -- ログイン記録
  ├── id (PK)
  ├── username (FK)
  ├── logged_in_at
  ├── login_method (FORM/OIDC)
  ├── oidc_provider
  ├── ip_address (IPv4/IPv6対応)
  ├── user_agent
  └── success (boolean)
```

**パスワード関連テーブル**:
```sql
weak_passwords          -- 弱いパスワードリスト（83件の初期データ）
  ├── id (PK)
  ├── password (UNIQUE)
  ├── description
  ├── created_at
  └── updated_at

failed_authentications  -- 認証失敗記録
  ├── username (PK,FK)
  ├── authentication_timestamp (PK)
  └── (複合主キー)
```

**Remember Meテーブル**:
```sql
persistent_logins      -- Spring Security標準テーブル
  ├── series (PK)
  ├── username (FK)
  ├── token
  └── last_used
```

## 設定

### application.yaml

主要な設定項目：

```yaml
# パスワードポリシー
password:
  min-length: 8
  max-length: 64
  max-consecutive-chars: 3

# セキュリティ設定
security:
  account:
    lockout:
      max-attempts: 5
      duration-minutes: 60
  remember-me:
    token-validity-seconds: 1209600  # 14日
    cleanup-days: 30

# メール設定
mail:
  provider: mock  # mock | sendgrid
  from: noreply@example.com

# ロギング（ECS形式）
logging:
  structured:
    format.console: ecs
```

### 環境変数

| 変数名 | 説明 | デフォルト |
|--------|------|----------|
| `GOOGLE_CLIENT_ID` | Google OAuth2クライアントID | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2シークレット | - |
| `PORT` | サーバーポート | 8080 |
| `SPRING_PROFILES_ACTIVE` | アクティブプロファイル | default |
| `SENDGRID_API_KEY` | SendGrid APIキー（将来使用） | - |

## デプロイ

### Docker

**イメージのビルド**:
```bash
docker build -t sample-cicd-springboot-gcp .
```

**コンテナの実行**:
```bash
docker run -p 8080:8080 \
  -e GOOGLE_CLIENT_ID=your-client-id \
  -e GOOGLE_CLIENT_SECRET=your-client-secret \
  sample-cicd-springboot-gcp
```

### Google Cloud Run

GitHub Actionsによる自動デプロイが設定されています。

**デプロイ設定**:
- リージョン: `asia-northeast1`
- インスタンス: min=0, max=3
- メモリ: 512Mi
- CPU: 1

**手動デプロイ**:
```bash
# Google Cloud認証
gcloud auth login

# Artifact Registryにプッシュ
gcloud builds submit --tag asia-northeast1-docker.pkg.dev/PROJECT_ID/REPOSITORY/sample-cicd-springboot-gcp

# Cloud Runにデプロイ
gcloud run deploy sample-cicd-springboot-gcp \
  --image asia-northeast1-docker.pkg.dev/PROJECT_ID/REPOSITORY/sample-cicd-springboot-gcp \
  --platform managed \
  --region asia-northeast1 \
  --allow-unauthenticated
```

## CI/CD

GitHub Actionsによるパイプライン:

**トリガー**:
- `main`ブランチへのPush: デプロイ実行
- Pull Request to `main`: テスト実行

**ワークフロー**:
1. JDK 21セットアップ
2. Maven依存関係キャッシング
3. テスト実行（`mvn test`）
4. ビルド実行（`mvn clean package`）
5. Google Cloud認証（Workload Identity）
6. Dockerイメージビルド＆プッシュ
7. Cloud Runデプロイ

## ライセンス

このプロジェクトはサンプルアプリケーションです。