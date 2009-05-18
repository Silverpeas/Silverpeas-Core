ALTER TABLE Personalization
ADD COLUMN dragAndDropStatus int
;
ALTER TABLE Personalization
ALTER COLUMN dragAndDropStatus SET DEFAULT 1
;

ALTER TABLE Personalization
ADD COLUMN onlineEditingStatus int
;
ALTER TABLE Personalization
ALTER COLUMN onlineEditingStatus SET DEFAULT 1
;