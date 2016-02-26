ALTER TABLE personalization DROP COLUMN onlineEditingStatus;
ALTER TABLE personalization ADD COLUMN  menuDisplay varchar(50) DEFAULT 'DEFAULT';

UPDATE personalization SET languages = 'fr' WHERE languages like '%fr%';
UPDATE personalization SET languages = 'en' WHERE languages like '%en%';
UPDATE personalization SET languages = 'de' WHERE languages like '%de%';