insert into ST_AccessLevel(id, name) values ('U', 'User');
insert into ST_AccessLevel(id, name) values ('A', 'Administrator');
insert into ST_AccessLevel(id, name) values ('G', 'Guest');
insert into ST_AccessLevel(id, name) values ('R', 'Removed');
insert into ST_AccessLevel(id, name) values ('K', 'KMManager');
insert into ST_AccessLevel(id, name) values ('D', 'DomainManager');

INSERT INTO ST_User (id, specificId, domainId, lastName, login, accessLevel, state, stateSaveDate)
  VALUES (0, '0', 0, '${SILVERPEAS_ADMIN_NAME}', '${SILVERPEAS_ADMIN_LOGIN}', 'A', 'VALID', CURRENT_TIMESTAMP);

insert into DomainSP_User(id, lastName, login, password)
values             (0, '${SILVERPEAS_ADMIN_NAME}', '${SILVERPEAS_ADMIN_LOGIN}', '${SILVERPEAS_ADMIN_PASSWORD}');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-', '0', '');

insert into ST_Domain(id, name, description, propFileName, className, authenticationServer, theTimeStamp, silverpeasServerURL)
values             (0, 'domainSilverpeas', 'default domain for Silverpeas', 'org.silverpeas.domains.domainSP', 'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', '0', '${SERVER_URL}');
