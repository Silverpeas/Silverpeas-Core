ALTER TABLE SB_Publication_PubliFather
ADD pubOrder int DEFAULT (0) NULL
;

UPDATE SB_Publication_PubliFather
SET pubOrder = 0
;