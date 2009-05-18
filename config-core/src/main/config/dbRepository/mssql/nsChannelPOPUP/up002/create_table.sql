ALTER TABLE ST_PopupMessage 
DROP COLUMN SUBJECT
;

ALTER TABLE ST_PopupMessage 
DROP COLUMN HEADER
;

ALTER TABLE ST_PopupMessage 
ADD SENDERID varchar(10) null
;

ALTER TABLE ST_PopupMessage 
ADD SENDERNAME varchar(200) null
;

ALTER TABLE ST_PopupMessage 
ADD ANSWERALLOWED char(1) default '0' not null
;


