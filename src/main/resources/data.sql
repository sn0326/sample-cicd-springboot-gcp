-- 初期ユーザーデータ
-- パスワードはbcryptでエンコード済み

INSERT INTO users (username, password, enabled) VALUES
('user', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', true),
('admin', '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6RyFO', true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO authorities (username, authority) VALUES
('user', 'ROLE_USER'),
('admin', 'ROLE_ADMIN'),
('admin', 'ROLE_USER')
ON CONFLICT DO NOTHING;

-- 弱いパスワード初期データ（NIST SP 800-63B準拠）
-- 最も一般的な弱いパスワードのリスト
INSERT INTO weak_passwords (password, description) VALUES
-- 数字のみ
('12345678', '数字のみ'),
('123456789', '数字のみ'),
('1234567890', '数字のみ'),
('00000000', '数字のみ'),
('11111111', '数字のみ'),
-- 一般的な単語
('password', '一般的な単語'),
('password123', '一般的な単語+数字'),
('password1', '一般的な単語+数字'),
-- キーボードパターン
('qwerty', 'キーボードパターン'),
('qwertyui', 'キーボードパターン'),
('qwertyuiop', 'キーボードパターン'),
('asdfghjk', 'キーボードパターン'),
('asdfghjkl', 'キーボードパターン'),
('zxcvbnm', 'キーボードパターン'),
('qweasd', 'キーボードパターン'),
('qweasdzxc', 'キーボードパターン'),
-- 一般的な単語
('welcome', '一般的な単語'),
('welcome1', '一般的な単語'),
('admin', '一般的な単語'),
('admin123', '一般的な単語'),
('administrator', '一般的な単語'),
('root', '一般的な単語'),
('root123', '一般的な単語'),
('test', '一般的な単語'),
('test123', '一般的な単語'),
('user', '一般的な単語'),
('user123', '一般的な単語'),
('guest', '一般的な単語'),
('guest123', '一般的な単語'),
('login', '一般的な単語'),
('login123', '一般的な単語'),
-- バリエーション
('passw0rd', 'パスワードのバリエーション'),
('p@ssw0rd', 'パスワードのバリエーション'),
('p@ssword', 'パスワードのバリエーション'),
-- 日本語由来
('sakura', '日本語由来'),
('yamada', '日本語由来'),
('tanaka', '日本語由来'),
('suzuki', '日本語由来'),
-- 季節・月
('spring', '季節'),
('summer', '季節'),
('autumn', '季節'),
('winter', '季節'),
('january', '月'),
('february', '月'),
('march', '月'),
('april', '月'),
-- その他
('letmein', '一般的なフレーズ'),
('monkey', '一般的な単語'),
('dragon', '一般的な単語'),
('master', '一般的な単語'),
('sunshine', '一般的な単語'),
('princess', '一般的な単語'),
('football', '一般的な単語'),
('iloveyou', '一般的なフレーズ'),
('trustno1', '一般的なフレーズ'),
('baseball', '一般的な単語'),
('superman', '一般的な単語'),
('batman', '一般的な単語')
ON CONFLICT (password) DO NOTHING;
