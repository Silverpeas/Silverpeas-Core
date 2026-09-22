CREATE TABLE ST_UserTwoFactorAuthentication
(
    userId     INT           NOT NULL,
    secret     VARCHAR2(512) NOT NULL,
    status     VARCHAR2(20)  NOT NULL,
    createdAt  TIMESTAMP     NOT NULL,
    updatedAt  TIMESTAMP     NOT NULL,
    lastUsedAt TIMESTAMP
);