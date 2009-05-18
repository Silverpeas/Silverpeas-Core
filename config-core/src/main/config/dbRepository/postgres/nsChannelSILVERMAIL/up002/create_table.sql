ALTER TABLE ST_SilverMailMessage
ADD SOURCE varchar (255) NULL
;
ALTER TABLE ST_SilverMailMessage
ADD URL varchar (255) NULL
;
ALTER TABLE ST_SilverMailMessage
ADD DATEMSG varchar (255) NULL
;
ALTER TABLE ST_SilverMailMessage
ADD READEN int default(1) NOT NULL 
;
