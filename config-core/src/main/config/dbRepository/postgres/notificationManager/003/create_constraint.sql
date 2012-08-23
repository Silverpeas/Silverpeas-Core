ALTER TABLE ST_NotifChannel ADD	CONSTRAINT PK_NotifChannel PRIMARY KEY(id) ;

ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT PK_ST_NotifDefaultAddress PRIMARY KEY(id);
ALTER TABLE ST_NotifDefaultAddress ADD CONSTRAINT FK_NotifDefaultAddress_1 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifPreference ADD CONSTRAINT PK_NotifAddr_Component PRIMARY KEY(id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_1 FOREIGN KEY(componentInstanceId) REFERENCES ST_ComponentInstance (id);
ALTER TABLE ST_NotifPreference ADD CONSTRAINT FK_NotifPreference_2 FOREIGN KEY(userId) REFERENCES ST_User(id);

ALTER TABLE ST_NotifAddress ADD CONSTRAINT PK_NotifAddress PRIMARY KEY(id);
ALTER TABLE ST_NotifAddress ADD CONSTRAINT FK_NotifAddress_1 FOREIGN KEY(notifChannelId) REFERENCES ST_NotifChannel(id);
ALTER TABLE ST_NotifAddress ADD	CONSTRAINT FK_NotifAddress_2 FOREIGN KEY(userId) REFERENCES ST_User(id);
