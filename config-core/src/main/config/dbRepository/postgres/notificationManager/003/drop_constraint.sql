ALTER TABLE ST_NotifDefaultAddress DROP CONSTRAINT FK_NotifDefaultAddress_1;
ALTER TABLE ST_NotifPreference DROP CONSTRAINT FK_NotifPreference_1;
ALTER TABLE ST_NotifPreference DROP CONSTRAINT FK_NotifPreference_2;
ALTER TABLE ST_NotifAddress DROP CONSTRAINT FK_NotifAddress_1;
ALTER TABLE ST_NotifAddress DROP CONSTRAINT FK_NotifAddress_2;

ALTER TABLE ST_NotifChannel DROP CONSTRAINT PK_NotifChannel;
ALTER TABLE ST_NotifDefaultAddress DROP CONSTRAINT PK_ST_NotifDefaultAddress;
ALTER TABLE ST_NotifPreference DROP CONSTRAINT PK_NotifAddr_Component;
ALTER TABLE ST_NotifAddress DROP CONSTRAINT PK_NotifAddress;
