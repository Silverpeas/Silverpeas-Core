-- Mettre la ligne suivante en commentaire si patch manuel du P407 déjà installé. Commentaire = --
insert into ST_UserSetType(id, name) values ('X', 'SpaceUserRole');

ALTER TABLE ST_Space
ADD look varchar(50)
;

CREATE TABLE ST_GroupUserRole
(
    id            int           NOT NULL,
    groupId	  int           NOT NULL,
    roleName      varchar(100)  NOT NULL
);

CREATE TABLE ST_GroupUserRole_User_Rel
(
    groupUserRoleId   int NOT NULL,
    userId            int NOT NULL
);

CREATE TABLE ST_GroupUserRole_Group_Rel
(
    groupUserRoleId   int NOT NULL,
    groupId           int NOT NULL
);

insert into ST_UserSetType(id, name) values ('H', 'Group Manager');

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
(subSetType = 'H' AND superSetType = 'G') OR
(subSetType = 'G'))
AND NOT (subSetType = superSetType AND subSetId = superSetId)
);

ALTER TABLE ST_UserSet_UserSet_Rel drop constraint FK_UserSet_UserSet_Rel_1;
ALTER TABLE ST_UserSet_UserSet_Rel drop constraint FK_UserSet_UserSet_Rel_2;
ALTER TABLE ST_UserSet_User_Rel DROP CONSTRAINT FK_UserSet_User_Rel_1;

update ST_UserSet_UserSet_Rel
set subSetType = 'X' where (subSetType = 'R' and subSetId IN (select id from ST_SpaceUserRole where roleName <> 'Manager'));

update ST_UserSet_UserSet_Rel
set superSetType = 'X' where (superSetType = 'R' and superSetId IN (select id from ST_SpaceUserRole where roleName <> 'Manager'));

update ST_UserSet_User_Rel
set userSetType = 'X' where (userSetType = 'R' and userSetId IN (select id from ST_SpaceUserRole where roleName <> 'Manager'));

update ST_UserSet
set userSetType = 'X' where (userSetType = 'R' and userSetId IN (select id from ST_SpaceUserRole where roleName <> 'Manager'));

ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT FK_UserSet_UserSet_Rel_1 FOREIGN KEY (superSetType, superSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT FK_UserSet_UserSet_Rel_2 FOREIGN KEY (subSetType, subSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_User_Rel ADD CONSTRAINT FK_UserSet_User_Rel_1 FOREIGN KEY (userSetType, userSetId) REFERENCES ST_UserSet(userSetType, userSetId);

ALTER TABLE ST_UserRole DROP CONSTRAINT UN_UserRole_1;
ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, isInherited, objectId);

ALTER TABLE ST_SpaceUserRole DROP CONSTRAINT UN_SpaceUserRole_1;
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT UN_SpaceUserRole_1 UNIQUE(spaceId, roleName, isInherited);