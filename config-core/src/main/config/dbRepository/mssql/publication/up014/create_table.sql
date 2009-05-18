ALTER TABLE SB_Publication_PubliFather
ADD pubOrder int NULL DEFAULT (0)
;

UPDATE SB_Publication_PubliFather
SET pubOrder = 0
;