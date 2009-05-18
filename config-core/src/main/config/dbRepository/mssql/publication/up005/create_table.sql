ALTER TABLE SB_Publication_Publi
ADD pubBeginHour varchar (5) NOT NULL DEFAULT ('00:00')
;
ALTER TABLE SB_Publication_Publi
ADD pubEndHour varchar (5) NOT NULL DEFAULT ('23:59')
;