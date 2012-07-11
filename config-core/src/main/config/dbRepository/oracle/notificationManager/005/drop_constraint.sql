ALTER TABLE ST_NotifDefaultAddress DROP CONSTRAINT FK_NotifDefaultAddress_1;
ALTER TABLE ST_NotifPreference DROP CONSTRAINT FK_NotifPreference_1;
ALTER TABLE ST_NotifPreference DROP CONSTRAINT FK_NotifPreference_2;
ALTER TABLE ST_NotifAddress DROP CONSTRAINT FK_NotifAddress_1;
ALTER TABLE ST_NotifAddress DROP CONSTRAINT FK_NotifAddress_2;

ALTER TABLE ST_NotifChannel DROP CONSTRAINT PK_NotifChannel;
ALTER TABLE ST_NotifDefaultAddress DROP CONSTRAINT PK_ST_NotifDefaultAddress;
ALTER TABLE ST_NotifPreference DROP CONSTRAINT PK_NotifAddr_Component;
ALTER TABLE ST_NotifAddress DROP CONSTRAINT PK_NotifAddress;

ALTER TABLE ST_NotifSended DROP CONSTRAINT PK_NotifSended;

ALTER TABLE ST_NotifSendedReceiver DROP CONSTRAINT PK_NotifSendedReceiver;

ALTER TABLE st_delayednotification DROP CONSTRAINT const_st_dn_pk;
ALTER TABLE st_delayednotification DROP CONSTRAINT const_st_dn_fk_nrId;
ALTER TABLE st_delayednotification DROP CONSTRAINT const_st_dn_fk_userId;

ALTER TABLE st_notificationresource DROP CONSTRAINT const_st_nr_pk;

ALTER TABLE st_delayednotifusersetting DROP CONSTRAINT const_st_dnus_pk;
ALTER TABLE st_delayednotifusersetting DROP CONSTRAINT const_st_dnus_fk_userId;