ALTER TABLE user_sessions
    ADD COLUMN max_expires_at DATETIME(6) NULL;

UPDATE user_sessions
SET max_expires_at = expires_at
WHERE max_expires_at IS NULL;

ALTER TABLE user_sessions
    MODIFY COLUMN max_expires_at DATETIME(6) NOT NULL;
