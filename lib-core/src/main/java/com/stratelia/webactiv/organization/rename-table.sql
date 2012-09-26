-- TEMPORAIRE
--
-- les tables ont malencontreusement été nommées ST_xxx pour SilverPeas
-- mais cela correspond à Stored Procedure ...
-- d'où ce script qui déplace les tables en ST_xxx pour SilverpeasTable

insert into ST_User select * from SP_User;
insert into ST_Group select * from SP_Group;
insert into ST_Group_User_Rel select * from SP_Group_User_Rel;
insert into ST_Space select * from SP_Space;
insert into ST_ComponentInstance select * from SP_ComponentInstance;
insert into ST_UserRole select * from SP_UserRole;
insert into ST_UserRole_User_Rel select * from SP_UserRole_User_Rel;
insert into ST_UserRole_Group_Rel select * from SP_UserRole_Group_Rel;

insert into ST_UserSet select * from SP_UserSet;
insert into ST_UserSet_User_Rel select * from SP_UserSet_User_Rel;
insert into ST_UserSet_UserSet_Rel select * from SP_UserSet_UserSet_Rel;
