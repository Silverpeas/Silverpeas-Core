ALTER TABLE ST_AccessLevel  ADD CONSTRAINT PK_AccessLevel PRIMARY KEY (id);
ALTER TABLE ST_AccessLevel ADD CONSTRAINT UN_AccessLevel_1 UNIQUE (name);

ALTER TABLE ST_User  ADD CONSTRAINT PK_User PRIMARY KEY (id);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_1 UNIQUE(specificId, domainId);
ALTER TABLE ST_User ADD CONSTRAINT UN_User_2 UNIQUE(login, domainId);
ALTER TABLE ST_User ADD CONSTRAINT FK_User_1 FOREIGN KEY (accessLevel) REFERENCES ST_AccessLevel(id);

ALTER TABLE ST_Group  ADD CONSTRAINT PK_Group PRIMARY KEY (id);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_1 UNIQUE(specificId, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT UN_Group_2 UNIQUE(superGroupId, name, domainId);
ALTER TABLE ST_Group ADD CONSTRAINT FK_Group_1 FOREIGN KEY (superGroupId) REFERENCES ST_Group(id);

ALTER TABLE ST_Group_User_Rel  ADD CONSTRAINT PK_Group_User_Rel PRIMARY KEY (groupId, userId);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES ST_Group(id);
ALTER TABLE ST_Group_User_Rel ADD CONSTRAINT FK_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_Space  ADD CONSTRAINT PK_Space PRIMARY KEY (id);
ALTER TABLE ST_Space ADD CONSTRAINT UN_Space_1 UNIQUE(domainFatherId, name);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User(id);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space(id);

ALTER TABLE ST_ComponentInstance  ADD CONSTRAINT PK_ComponentInstance PRIMARY KEY (id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT UN_ComponentInstance_1 UNIQUE(spaceId, name);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id);
ALTER TABLE ST_ComponentInstance ADD CONSTRAINT FK_ComponentInstance_2 FOREIGN KEY (createdBy) REFERENCES ST_User(id);

ALTER TABLE ST_Instance_Data  ADD CONSTRAINT PK_Instance_Data PRIMARY KEY (id);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT UN_Instance_Data_1 UNIQUE(componentId, name);
ALTER TABLE ST_Instance_Data ADD CONSTRAINT FK_Instance_Data_1 FOREIGN KEY (componentId) REFERENCES ST_ComponentInstance(id);

ALTER TABLE ST_UserRole  ADD CONSTRAINT PK_UserRole PRIMARY KEY (id);
ALTER TABLE ST_UserRole ADD CONSTRAINT UN_UserRole_1 UNIQUE(instanceId, roleName, isInherited, objectId, objectType);
ALTER TABLE ST_UserRole ADD CONSTRAINT FK_UserRole_1 FOREIGN KEY (instanceId) REFERENCES ST_ComponentInstance(id);

ALTER TABLE ST_UserRole_User_Rel  ADD CONSTRAINT PK_UserRole_User_Rel PRIMARY KEY (userRoleId, userId);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id);
ALTER TABLE ST_UserRole_User_Rel ADD CONSTRAINT FK_UserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_UserRole_Group_Rel  ADD CONSTRAINT PK_UserRole_Group_Rel PRIMARY KEY (userRoleId, groupId);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_1 FOREIGN KEY (userRoleId) REFERENCES ST_UserRole(id);
ALTER TABLE ST_UserRole_Group_Rel ADD CONSTRAINT FK_UserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id);

ALTER TABLE ST_SpaceUserRole  ADD CONSTRAINT PK_SpaceUserRole PRIMARY KEY (id);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT UN_SpaceUserRole_1 UNIQUE(spaceId, roleName, isInherited);
ALTER TABLE ST_SpaceUserRole ADD CONSTRAINT FK_SpaceUserRole_1 FOREIGN KEY (spaceId) REFERENCES ST_Space(id);

ALTER TABLE ST_SpaceUserRole_User_Rel  ADD CONSTRAINT PK_SpaceUserRole_User_Rel PRIMARY KEY (spaceUserRoleId, userId);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id);
ALTER TABLE ST_SpaceUserRole_User_Rel ADD CONSTRAINT FK_SpaceUserRole_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE ST_SpaceUserRole_Group_Rel  ADD CONSTRAINT PK_SpaceUserRole_Group_Rel PRIMARY KEY (spaceUserRoleId, groupId);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_1 FOREIGN KEY (spaceUserRoleId) REFERENCES ST_SpaceUserRole(id);
ALTER TABLE ST_SpaceUserRole_Group_Rel ADD CONSTRAINT FK_SpaceUserRole_Group_Rel_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id);

ALTER TABLE ST_UserSetType  ADD CONSTRAINT PK_UserSetType PRIMARY KEY (id);
ALTER TABLE ST_UserSetType ADD CONSTRAINT UN_UserSetType_1 UNIQUE(name);

ALTER TABLE ST_UserSet  ADD CONSTRAINT PK_UserSet PRIMARY KEY (userSetType, userSetId);
ALTER TABLE ST_UserSet ADD CONSTRAINT FK_UserSet_2 FOREIGN KEY (userSetType) REFERENCES ST_UserSetType(id);

ALTER TABLE ST_UserSet_UserSet_Rel  ADD CONSTRAINT PK_UserSet_UserSet_Rel PRIMARY KEY (superSetType, subSetType, superSetId, subSetId);
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT FK_UserSet_UserSet_Rel_1 FOREIGN KEY (superSetType, superSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT FK_UserSet_UserSet_Rel_2 FOREIGN KEY (subSetType, subSetId) REFERENCES ST_UserSet(userSetType, userSetId);
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

ALTER TABLE ST_UserSet_User_Rel  ADD CONSTRAINT PK_UserSet_User_Rel PRIMARY KEY (userSetType, userSetId, userId);
ALTER TABLE ST_UserSet_User_Rel ADD CONSTRAINT FK_UserSet_User_Rel_1 FOREIGN KEY (userSetType, userSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_User_Rel ADD CONSTRAINT FK_UserSet_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

ALTER TABLE DomainSP_Group  ADD CONSTRAINT PK_DomainSP_Group PRIMARY KEY (id);
ALTER TABLE DomainSP_Group ADD CONSTRAINT UN_DomainSP_Group_1 UNIQUE(superGroupId, name);
ALTER TABLE DomainSP_Group ADD CONSTRAINT FK_DomainSP_Group_1 FOREIGN KEY (superGroupId) REFERENCES DomainSP_Group(id);

ALTER TABLE DomainSP_User  ADD CONSTRAINT PK_DomainSP_User PRIMARY KEY (id);
ALTER TABLE DomainSP_User ADD CONSTRAINT UN_DomainSP_User_1 UNIQUE(login);

ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_1 FOREIGN KEY (groupId) REFERENCES DomainSP_Group(id);
ALTER TABLE DomainSP_Group_User_Rel ADD CONSTRAINT FK_DomainSP_Group_User_Rel_2 FOREIGN KEY (userId) REFERENCES DomainSP_User(id);

ALTER TABLE ST_Domain  ADD CONSTRAINT PK_ST_Domain PRIMARY KEY (id);

ALTER TABLE DomainSP_Group_User_Rel  ADD CONSTRAINT PK_DomainSP_Group_User_Rel PRIMARY KEY (groupId,userId);

ALTER TABLE ST_LongText ADD CONSTRAINT PK_ST_LongText PRIMARY KEY (id,orderNum);

ALTER TABLE st_instance_modelused
ADD CONSTRAINT PK_st_instance_modelused PRIMARY KEY
	(
		instanceId,
		modelId
	)   
;
