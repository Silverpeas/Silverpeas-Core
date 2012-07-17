ALTER TABLE st_delayednotifusersetting
        ADD CONSTRAINT const_st_dnus_pk
        PRIMARY KEY CLUSTERED(id);
ALTER TABLE st_delayednotifusersetting
        ADD CONSTRAINT const_st_dnus_fk_userId
		FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE st_notificationresource
        ADD CONSTRAINT const_st_nr_pk
        PRIMARY KEY CLUSTERED(id);

ALTER TABLE st_delayednotification
        ADD CONSTRAINT const_st_dn_pk
        PRIMARY KEY CLUSTERED(id);
ALTER TABLE st_delayednotification
		ADD CONSTRAINT const_st_dn_fk_nrId
		FOREIGN KEY (notificationResourceId) REFERENCES st_notificationresource(id);
ALTER TABLE st_delayednotification
        ADD CONSTRAINT const_st_dn_fk_userId
		FOREIGN KEY (userId) REFERENCES ST_User(id);