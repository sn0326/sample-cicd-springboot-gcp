-- Initial user data
-- Passwords are encoded with BCrypt
-- user: password
-- admin: admin

INSERT INTO users (username, password, roles, enabled) VALUES
('user', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'USER', true),
('admin', '{bcrypt}$2a$10$qZ3zJL6LVXnXqN6Vg0ZJbeqYqBp/3pKxbqN1qY1hXqN1qY1hXqN1q', 'ADMIN,USER', true)
ON CONFLICT (username) DO NOTHING;
