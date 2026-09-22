ALTER TABLE ST_UserTwoFactorAuthentication
    ADD CONSTRAINT PK_UserTwoFactorAuthentication
        PRIMARY KEY (userId);

ALTER TABLE ST_UserTwoFactorAuthentication
    ADD CONSTRAINT FK_UserTwoFactorAuthentication_User
        FOREIGN KEY (userId)
            REFERENCES ST_User(id);