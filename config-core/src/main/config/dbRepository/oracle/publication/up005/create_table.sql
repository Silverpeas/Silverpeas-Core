ALTER TABLE SB_Publication_Publi
ADD pubBeginHour varchar (5) DEFAULT ('00:00') NOT NULL
;
ALTER TABLE SB_Publication_Publi
ADD pubEndHour varchar (5) DEFAULT ('23:59') NOT NULL
;