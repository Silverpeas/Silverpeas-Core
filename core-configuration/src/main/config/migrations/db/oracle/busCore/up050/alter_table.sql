CREATE TABLE ST_User_2FA_Recovery
(
    id        NUMBER(19) PRIMARY KEY,
    userId    NUMBER(10) NOT NULL,
    hash      VARCHAR2(64) NOT NULL,
    used      NUMBER(1) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    usedAt    TIMESTAMP
);

ALTER TABLE ST_User_2FA_Recovery
    ADD CONSTRAINT FK_User_2FA_Recovery_User
        FOREIGN KEY (userId) REFERENCES ST_User(id);

CREATE INDEX IDX_User_2FA_Recovery_User
    ON ST_User_2FA_Recovery (userId);