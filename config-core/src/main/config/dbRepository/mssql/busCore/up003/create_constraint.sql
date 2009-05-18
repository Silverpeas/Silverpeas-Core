
ALTER TABLE ST_UserSetType WITH NOCHECK ADD CONSTRAINT PK_UserSetType PRIMARY KEY CLUSTERED(id);
ALTER TABLE ST_UserSetType ADD CONSTRAINT UN_UserSetType_1 UNIQUE(name);

ALTER TABLE ST_UserSet WITH NOCHECK ADD CONSTRAINT PK_UserSet PRIMARY KEY CLUSTERED(userSetType, userSetId);
ALTER TABLE ST_UserSet ADD CONSTRAINT FK_UserSet_2 FOREIGN KEY (userSetType) REFERENCES ST_UserSetType(id);

ALTER TABLE ST_UserSet_UserSet_Rel WITH NOCHECK ADD CONSTRAINT PK_UserSet_UserSet_Rel PRIMARY KEY CLUSTERED(superSetType, subSetType, superSetId, subSetId);
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT FK_UserSet_UserSet_Rel_1 FOREIGN KEY (superSetType, superSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT FK_UserSet_UserSet_Rel_2 FOREIGN KEY (subSetType, subSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_UserSet_Rel ADD CONSTRAINT no_cycle CHECK
(((subSetType = 'R' AND superSetType = 'I') OR
(subSetType = 'R' AND superSetType = 'S') OR
(subSetType = 'I' AND superSetType = 'S') OR
(subSetType = 'S' AND superSetType = 'S') OR
(subSetType = 'M' AND superSetType = 'S') OR
(subSetType = 'G'))
AND NOT (subSetType = superSetType AND subSetId = superSetId)
);

ALTER TABLE ST_UserSet_User_Rel WITH NOCHECK ADD CONSTRAINT PK_UserSet_User_Rel PRIMARY KEY CLUSTERED(userSetType, userSetId, userId);
ALTER TABLE ST_UserSet_User_Rel ADD CONSTRAINT FK_UserSet_User_Rel_1 FOREIGN KEY (userSetType, userSetId) REFERENCES ST_UserSet(userSetType, userSetId);
ALTER TABLE ST_UserSet_User_Rel ADD CONSTRAINT FK_UserSet_User_Rel_2 FOREIGN KEY (userId) REFERENCES ST_User(id);

