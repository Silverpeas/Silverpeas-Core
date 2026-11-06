ALTER INDEX IND_st_instance_modelused RENAME TO IDX_st_instance_modelused;

CREATE INDEX IDX_User_2FA_Recovery_User ON ST_User_2FA_Recovery (userId);
CREATE INDEX IDX_User_2FA_Trusted_Device_User_Token ON ST_User_2FA_Trusted_Device (userId, tokenHash);
