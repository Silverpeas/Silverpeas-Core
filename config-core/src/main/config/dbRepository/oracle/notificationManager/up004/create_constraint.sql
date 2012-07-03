ALTER TABLE st_delayednotifusersetting
        ADD CONSTRAINT const_st_delayednotifusersetting_pk
        PRIMARY KEY (id);
ALTER TABLE st_delayednotifusersetting
        ADD CONSTRAINT const_st_delayednotifusersetting_fk_userId
		FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE st_notificationresource
        ADD CONSTRAINT const_st_notificationresource_pk
        PRIMARY KEY (id);

ALTER TABLE st_delayednotification
        ADD CONSTRAINT const_st_delayednotification_pk
        PRIMARY KEY (id);
ALTER TABLE st_delayednotification
		ADD CONSTRAINT const_st_delayednotification_fk_notificationResourceId
		FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id);
ALTER TABLE st_delayednotification
        ADD CONSTRAINT const_st_delayednotification_fk_userId
		FOREIGN KEY (userId) REFERENCES ST_User(id);