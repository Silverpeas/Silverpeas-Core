ALTER TABLE ST_UserRole
ADD objectType char(1)
;

ALTER TABLE ST_UserRole DROP CONSTRAINT UN_UserRole_1;
ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, isInherited, objectId, objectType);

UPDATE ST_UserRole SET objectType = 'O' where objectId IS NOT NULL;

insert into ST_UserSetType(id, name) values ('D', 'Document');

ALTER TABLE ST_UserSet_UserSet_Rel drop constraint no_cycle;
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT no_cycle CHECK
(((subSetType = 'R' AND superSetType = 'I') OR
(subSetType = 'R' AND superSetType = 'S') OR
(subSetType = 'X' AND superSetType = 'I') OR
(subSetType = 'X' AND superSetType = 'S') OR
(subSetType = 'I' AND superSetType = 'S') OR
(subSetType = 'S' AND superSetType = 'S') OR
(subSetType = 'M' AND superSetType = 'S') OR
(subSetType = 'R' AND superSetType = 'O') OR
(subSetType = 'R' AND superSetType = 'D') OR
(subSetType = 'H' AND superSetType = 'G') OR
(subSetType = 'G'))
AND NOT (subSetType = superSetType AND subSetId = superSetId)
);