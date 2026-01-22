# トークンクリーンアップ戦略

## 背景

このアプリケーションでは、以下のトークンと試行記録を使用しています：

- **パスワードリセットトークン** (`password_reset_tokens`)
- **パスワードリセット試行記録** (`password_reset_attempts`)
- **メールアドレス変更トークン** (`email_change_tokens`)
- **メールアドレス変更試行記録** (`email_change_attempts`)

これらのデータは時間経過とともに不要になるため、定期的なクリーンアップが必要です。

### 問題

通常、Spring Bootアプリケーションでは`@Scheduled`アノテーションを使用して定期的なタスクを実行しますが、以下の理由で使用できません：

- **サーバーレス環境**: Cloud RunやApp Engineなどの環境では、アプリケーションが常駐しないためスケジューラーが動作しない
- **スケールダウン**: アクセスがない時間帯にインスタンスが停止するため、定期実行が保証されない

## 検討した代替案

### 案1: リクエスト時確率的クリーンアップ ⭐️ **採用**

**概要**: 各リクエスト処理時に一定確率でクリーンアップを実行

**メリット**:
- ✅ 追加のインフラ不要
- ✅ 実装が簡単
- ✅ パフォーマンス影響が最小限（確率的なので負荷分散）
- ✅ アプリケーション内で完結
- ✅ 追加コストなし

**デメリット**:
- ❌ クリーンアップのタイミングが不確定
- ❌ アクセスが極端に少ない場合、クリーンアップが実行されない可能性

### 案2: Cloud Scheduler + 専用HTTPエンドポイント

**概要**: Cloud Schedulerから定期的にクリーンアップエンドポイントを呼び出す

**メリット**:
- ✅ 確実に定期実行される
- ✅ クリーンアップタイミングを制御できる
- ✅ 実行履歴がCloud Schedulerで確認可能

**デメリット**:
- ❌ 追加のGCPリソース（Cloud Scheduler）が必要
- ❌ 追加コスト発生
- ❌ エンドポイントのセキュリティ対策が必要（IP制限やトークン認証）
- ❌ インフラ設定の複雑化

### 案3: データベース側でクリーンアップ

**概要**: PostgreSQLのパーティショニングやTTL機能を使用

**メリット**:
- ✅ アプリケーションコード不要
- ✅ データベースレベルで最適化

**デメリット**:
- ❌ データベース設定が複雑
- ❌ PostgreSQL固有の機能に依存
- ❌ 移植性が低下

### 案4: アプリケーション起動時クリーンアップ

**概要**: サーバー起動時に毎回クリーンアップを実行

**メリット**:
- ✅ 実装が簡単
- ✅ 確実に定期的（再起動のたびに）実行される

**デメリット**:
- ❌ 起動時間が長くなる
- ❌ Cloud Runなどのスケールダウン/アップが頻繁な環境では不要な実行が多い
- ❌ コールドスタート時のレスポンスが遅くなる

### 案5: 手動クリーンアップ

**概要**: 必要に応じて管理者が手動でクリーンアップ

**メリット**:
- ✅ 追加実装不要
- ✅ パフォーマンス影響なし

**デメリット**:
- ❌ 定期的にメンテナンスが必要
- ❌ 忘れるとデータが溜まる
- ❌ 運用負荷が高い

## 採用した案と理由

### 案1: リクエスト時確率的クリーンアップを採用

以下の理由から**案1**を採用しました：

1. **サーバーレス環境に最適**: 追加のインフラや設定が不要
2. **コストゼロ**: 既存のリソースのみで実現可能
3. **シンプル**: 実装が簡単でメンテナンスしやすい
4. **パフォーマンス影響が小さい**: 確率的実行により負荷が分散される
5. **柔軟性**: 設定で確率を調整可能

アクセス頻度が低い場合のクリーンアップ遅延は許容範囲と判断しました。トークンの有効期限は30分、試行記録の保持期間は7日間であり、数日程度のクリーンアップ遅延は実用上問題ありません。

## 実装詳細

### クリーンアップの実行タイミング

以下のメソッド実行時に確率的にクリーンアップが実行されます：

1. **EmailChangeService**
   - `requestEmailChange()`: メールアドレス変更リクエスト時
   - `verifyEmail()`: メールアドレス確認時

2. **PasswordResetService**
   - `requestPasswordReset()`: パスワードリセットリクエスト時
   - `resetPassword()`: パスワードリセット実行時

### クリーンアップの内容

各サービスの`cleanupExpiredData()`メソッドで以下を実行：

- **期限切れトークンの削除**: 有効期限が過ぎたトークンを削除
- **古い試行記録の削除**: 7日以上前の試行記録を削除

### 確率的実行の仕組み

```java
private void maybeCleanup() {
    if (random.nextDouble() < cleanupProbability) {
        log.debug("Probabilistic cleanup triggered");
        cleanupExpiredData();
    }
}
```

デフォルトでは**10%の確率**で実行されます。

### 設定方法

`application.yaml`で確率を調整できます：

```yaml
security:
  email-change:
    cleanup-probability: 0.1  # デフォルト10%（0.0～1.0）
  password-reset:
    cleanup-probability: 0.1  # デフォルト10%（0.0～1.0）
```

**設定例**:
- `0.1`: 10%（デフォルト、推奨）
- `0.05`: 5%（アクセスが多い場合）
- `0.2`: 20%（アクセスが少ない場合）
- `1.0`: 100%（毎回実行、パフォーマンステスト用）
- `0.0`: 無効化（手動クリーンアップのみ）

### 手動クリーンアップ

必要に応じて以下のメソッドを直接呼び出すことも可能です：

```java
@Autowired
private EmailChangeService emailChangeService;

@Autowired
private PasswordResetService passwordResetService;

// 手動クリーンアップ
emailChangeService.cleanupExpiredData();
passwordResetService.cleanupExpiredData();
```

管理画面や内部エンドポイントから実行することも検討できます。

## 将来の改善案

アクセス頻度が極端に低い場合や、より確実なクリーンアップが必要になった場合は、以下を検討：

1. **Cloud Schedulerの導入** (案2)
   - 月次コスト: 約$0.10
   - 確実に定期実行される
   - 実装例: `POST /internal/cleanup`エンドポイントを追加

2. **ハイブリッドアプローチ**
   - 確率的クリーンアップ + Cloud Scheduler（週次など低頻度）
   - 通常時は確率的、念のため週1回確実に実行

3. **監視の追加**
   - 古いトークンの件数を監視
   - 一定数を超えたらアラート

## 関連ファイル

- `src/main/java/com/sn0326/cicddemo/service/EmailChangeService.java`
- `src/main/java/com/sn0326/cicddemo/service/PasswordResetService.java`
- `src/main/java/com/sn0326/cicddemo/repository/EmailChangeTokenRepository.java`
- `src/main/java/com/sn0326/cicddemo/repository/EmailChangeAttemptRepository.java`
- `src/main/java/com/sn0326/cicddemo/repository/PasswordResetTokenRepository.java`
- `src/main/java/com/sn0326/cicddemo/repository/PasswordResetAttemptRepository.java`

## 更新履歴

- **2026-01-22**: 初版作成（案1を採用）
