CREATE INDEX IND_st_instance_modelused ON st_instance_modelused (instanceId,modelId)
;

CREATE INDEX IDX_User_2FA_Recovery_User
    ON ST_User_2FA_Recovery (userId);