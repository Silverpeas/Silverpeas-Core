CREATE TABLE ST_User_2FA
(
    userId     INT          NOT NULL,
    secret     VARCHAR(1024) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    createdAt  TIMESTAMP    NOT NULL,
    updatedAt  TIMESTAMP    NOT NULL,
    lastUsedAt TIMESTAMP,
    failedAttempts INT NOT NULL DEFAULT 0,
    lockedUntil TIMESTAMP
);