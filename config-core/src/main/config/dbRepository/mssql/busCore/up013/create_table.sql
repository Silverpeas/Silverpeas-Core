ALTER TABLE ST_Space
ADD lang char(2)
;

ALTER TABLE ST_Space
ADD isInheritanceBlocked int NOT NULL default 1
; 

CREATE TABLE ST_SpaceI18N
(
    id			int		NOT NULL,
    spaceId		int		NOT NULL,
    lang		char(2)		NOT NULL,
    name		varchar(100)	NOT NULL,
    description		varchar(400)
);

ALTER TABLE ST_SpaceUserRole
ADD isInherited int NOT NULL default 0
;

ALTER TABLE ST_ComponentInstance
ADD isPublic int NOT NULL default 0
;

ALTER TABLE ST_ComponentInstance
ADD isHidden int NOT NULL default 0
;

ALTER TABLE ST_ComponentInstance
ADD lang char(2)
;

ALTER TABLE ST_ComponentInstance
ADD isInheritanceBlocked int NOT NULL default 0
; 

CREATE TABLE ST_ComponentInstanceI18N
(
    id			int		NOT NULL,
    componentId		int		NOT NULL,
    lang		char(2)		NOT NULL,
    name		varchar(100)	NOT NULL,
    description		varchar(400)
);

ALTER TABLE ST_UserRole
ADD isInherited int NOT NULL default 0
;

ALTER TABLE ST_UserRole
ADD objectId int
;

ALTER TABLE ST_UserSet_UserSet_Rel DROP CONSTRAINT no_cycle;

ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT no_cycle CHECK
(((subSetType = 'R' AND superSetType = 'I') OR
(subSetType = 'R' AND superSetType = 'S') OR
(subSetType = 'I' AND superSetType = 'S') OR
(subSetType = 'S' AND superSetType = 'S') OR
(subSetType = 'M' AND superSetType = 'S') OR
(subSetType = 'R' AND superSetType = 'O') OR
(subSetType = 'G'))
AND NOT (subSetType = superSetType AND subSetId = superSetId)
);

insert into ST_UserSetType(id, name) values ('O', 'Object');

ALTER TABLE ST_UserRole DROP CONSTRAINT UN_UserRole_1;

ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, objectId);