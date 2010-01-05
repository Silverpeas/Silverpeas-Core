CREATE INDEX IND_ST_User1 ON ST_User (lastName)
;
CREATE INDEX IND_ST_UserSet_User_Rel1 ON ST_UserSet_User_Rel (userSetId)
;
CREATE INDEX IND_ST_UserSet_User_Rel2 ON ST_UserSet_User_Rel (userSetId, userId)
;
CREATE INDEX IND_st_instance_modelused ON st_instance_modelused (instanceId,modelId)
;
