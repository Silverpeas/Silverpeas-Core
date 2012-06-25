CREATE INDEX IN_NotifAddress_1 ON ST_NotifAddress(userId);

CREATE UNIQUE INDEX IN_NotifPreference_1 ON ST_NotifPreference(userId, componentInstanceId, messageType);

CREATE INDEX IN_NotifSended ON ST_NotifSended(notifId);

CREATE INDEX IN_NotifSendedReceiver ON ST_NotifSendedReceiver(notifId);

CREATE INDEX idx_st_dnus_id ON st_delayednotifusersetting(id);
CREATE INDEX idx_st_dnus_userId ON st_delayednotifusersetting(userId);
CREATE INDEX idx_st_dnus_channel ON st_delayednotifusersetting(channel);
CREATE UNIQUE INDEX idx_st_dnus_uc ON st_delayednotifusersetting(userId, channel);

CREATE INDEX idx_st_nr_id ON st_notificationresource(id);
CREATE INDEX idx_st_nr_resourceId ON st_notificationresource(resourceId);

CREATE INDEX idx_st_dn_id ON st_delayednotification(id);
CREATE INDEX idx_st_dn_userId ON st_delayednotification(userId);
CREATE INDEX idx_st_dn_channel ON st_delayednotification(channel);