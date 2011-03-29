ALTER TABLE personalization DROP COLUMN onlineEditingStatus;
ALTER TABLE personalization ADD menuDisplay varchar(50) DEFAULT('DISABLE');

UPDATE personalization SET languages = 'fr' WHERE languages like '%fr%';
UPDATE personalization SET languages = 'en' WHERE languages like '%en%';
UPDATE personalization SET languages = 'de' WHERE languages like '%de%';