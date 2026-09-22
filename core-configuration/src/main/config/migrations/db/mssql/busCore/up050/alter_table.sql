CREATE TABLE ST_User_2FA_Recovery
(
    id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    userId    INT          NOT NULL,
    hash      VARCHAR(64)  NOT NULL,
    used      BIT          NOT NULL DEFAULT (0),
    createdAt DATETIME2    NOT NULL,
    usedAt    DATETIME2
);

ALTER TABLE ST_User_2FA_Recovery
    ADD CONSTRAINT FK_User_2FA_Recovery_User
        FOREIGN KEY (userId) REFERENCES ST_User(id);

CREATE INDEX IDX_User_2FA_Recovery_User
    ON ST_User_2FA_Recovery (userId);