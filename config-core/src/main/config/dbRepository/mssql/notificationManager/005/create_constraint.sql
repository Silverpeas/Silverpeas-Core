ALTER TABLE ST_NotifChannel WITH NOCHECK ADD CONSTRAINT PK_NotifChannel PRIMARY KEY CLUSTERED(id) ;

ALTER TABLE ST_NotifDefaultAddress WITH NOCHECK ADD CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY CLUSTERED(id);
ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifPreference WITH NOCHECK ADD CONSTRAINT PK_NotifAddr_Component PRIMARY KEY CLUSTERED(id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_1 FOREIGN KEY(componentInstanceId) REFERENCES ST_ComponentInstance (id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_2 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifAddress WITH NOCHECK ADD CONSTRAINT PK_NotifAddress PRIMARY KEY CLUSTERED(id);
ALTER TABLE ST_NotifAddress ADD CONSTRAINT FK_NotifAddress_1 FOREIGN KEY(notifChannelId) REFERENCES ST_NotifChannel(id);
ALTER TABLE ST_NotifAddress ADD	CONSTRAINT FK_NotifAddress_2 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifSended ADD CONSTRAINT PK_NotifSended PRIMARY KEY CLUSTERED(notifId);

ALTER TABLE ST_NotifSendedReceiver ADD CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY CLUSTERED(notifId, userId);

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
