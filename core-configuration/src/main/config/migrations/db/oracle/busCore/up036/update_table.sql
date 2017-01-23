ALTER TABLE Personalization
  ADD zoneId VARCHAR(100) NULL;

UPDATE Personalization
SET zoneId = CASE
             WHEN languages = 'de'
               THEN 'Europe/Berlin'
             WHEN languages = 'en'
               THEN 'Europe/London'
             ELSE 'Europe/Paris' END;