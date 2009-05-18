ALTER TABLE model
ADD COLUMN partId int
;
UPDATE model SET partId = 1
;
ALTER TABLE model
ALTER COLUMN partId set default 1 
;