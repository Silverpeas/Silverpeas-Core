CREATE INDEX IN_NotifAddress_1 ON ST_NotifAddress(userId);

CREATE UNIQUE INDEX IN_NotifPreference_1 ON ST_NotifPreference(userId, componentInstanceId, messageType);

CREATE INDEX IN_NotifSended ON ST_NotifSended(notifId);

CREATE INDEX IN_NotifSendedReceiver ON ST_NotifSendedReceiver(notifId);