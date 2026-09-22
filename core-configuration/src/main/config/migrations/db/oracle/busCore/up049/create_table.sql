CREATE TABLE ST_User_2FA
(
    userId     INT           NOT NULL,
    secret     VARCHAR2(1024) NOT NULL,
    status     VARCHAR2(20)  NOT NULL,
    createdAt  TIMESTAMP     NOT NULL,
    updatedAt  TIMESTAMP     NOT NULL,
    lastUsedAt TIMESTAMP
);