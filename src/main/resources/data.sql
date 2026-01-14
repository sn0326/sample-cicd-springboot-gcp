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
