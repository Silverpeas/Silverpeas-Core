ALTER TABLE ST_Domain
add silverpeasServerURL varchar(400) NULL
;
UPDATE ST_Domain
SET silverpeasServerURL = '${URLSERVER}'
WHERE (id = 0)
;