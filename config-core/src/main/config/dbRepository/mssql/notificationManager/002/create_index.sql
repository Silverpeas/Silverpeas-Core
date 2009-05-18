CREATE INDEX IN_NotifAddress_1 ON ST_NotifAddress(userId);

CREATE UNIQUE INDEX IN_NotifPreference_1 ON ST_NotifPreference(userId, componentInstanceId, messageType);
