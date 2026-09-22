CREATE TABLE ST_UserTwoFactorAuthentication
(
    userId     INT          NOT NULL,
    secret     VARCHAR(512) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    createdAt  DATETIME2    NOT NULL,
    updatedAt  DATETIME2    NOT NULL,
    lastUsedAt DATETIME2
);