CREATE TABLE sb_agenda_import_settings
(
  userid int NOT NULL,
  hostname varchar(500) NOT NULL,
  synchrotype int NOT NULL,
  synchrodelay int NOT NULL
); 

ALTER TABLE calendarJournal
ADD externalid varchar(250) NULL
;

ALTER TABLE ST_FormDesigner_FormDesign 
DROP COLUMN SPACEID
;