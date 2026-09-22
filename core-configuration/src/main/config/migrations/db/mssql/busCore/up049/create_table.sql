CREATE TABLE ST_User_2FA
(
    userId     INT          NOT NULL,
    secret     VARCHAR(1024) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    createdAt  DATETIME2    NOT NULL,
    updatedAt  DATETIME2    NOT NULL,
    lastUsedAt DATETIME2,
    failedAttempts INT NOT NULL DEFAULT 0,
    lockedUntil DATETIME2
);

CREATE TABLE ST_User_2FA_Recovery
(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    userId INT NOT NULL,
    hash VARCHAR(64) NOT NULL,
    used BIT NOT NULL DEFAULT (0),
    createdAt DATETIME2 NOT NULL,
    usedAt DATETIME2
);