ALTER TABLE ST_NotifSended ADD CONSTRAINT PK_NotifSended PRIMARY KEY(notifId);

ALTER TABLE ST_NotifSendedReceiver ADD CONSTRAINT PK_NotifSendedReceiver PRIMARY KEY(notifId, userId);
