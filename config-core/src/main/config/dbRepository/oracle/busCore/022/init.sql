insert into ST_AccessLevel(id, name) values ('U', 'User');
insert into ST_AccessLevel(id, name) values ('A', 'Administrator');
insert into ST_AccessLevel(id, name) values ('G', 'Guest');
insert into ST_AccessLevel(id, name) values ('R', 'Removed');
insert into ST_AccessLevel(id, name) values ('K', 'KMManager');
insert into ST_AccessLevel(id, name) values ('D', 'DomainManager');

insert into ST_UserSetType(id, name) values ('G', 'Group');
insert into ST_UserSetType(id, name) values ('R', 'UserRole');
insert into ST_UserSetType(id, name) values ('I', 'ComponentInstance');
insert into ST_UserSetType(id, name) values ('S', 'Space');
insert into ST_UserSetType(id, name) values ('M', 'SpaceProfile');
insert into ST_UserSetType(id, name) values ('O', 'Object');
insert into ST_UserSetType(id, name) values ('H', 'Group Manager');
insert into ST_UserSetType(id, name) values ('X', 'SpaceUserRole');
insert into ST_UserSetType(id, name) values ('D', 'Document');

insert into ST_User(id, specificId, domainId, lastName, login, accessLevel)
values             (0, '0', 0, 'Administrateur', '${ADMINLOGIN}', 'A');

insert into DomainSP_User(id, lastName, login, password)
values             (0, 'Administrateur', '${ADMINLOGIN}', '${ADMINPASSWD}');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-', '0', '');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (0, 'domainSilverpeas', 'default domain for Silverpeas', 'com.stratelia.silverpeas.domains.domainSP', 'com.stratelia.silverpeas.domains.silverpeasdriver.SilverpeasDriver', 'autDomainSP', '0', '${URLSERVER}');
