alter table ST_Domain
add theTimeStamp varchar (100) NOT NULL DEFAULT ('0')
;
ALTER TABLE ST_UserSetType
ALTER COLUMN id nchar(1) NOT NULL
;
ALTER TABLE ST_UserSet
ALTER COLUMN userSetType nchar(1) NOT NULL
;
ALTER TABLE ST_UserSet_User_Rel
ALTER COLUMN userSetType nchar(1) NOT NULL
;
ALTER TABLE ST_UserSet_UserSet_Rel
ALTER COLUMN superSetType nchar(1) NOT NULL
;
ALTER TABLE ST_UserSet_UserSet_Rel
ALTER COLUMN subSetType nchar(1) NOT NULL
;
