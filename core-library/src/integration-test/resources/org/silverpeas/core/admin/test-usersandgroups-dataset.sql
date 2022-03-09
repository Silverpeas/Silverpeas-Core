/**
 * Silverpeas domains
 */
INSERT INTO st_domain (id, name, propFilename, className, authenticationServer, silverpeasServerURL)
VALUES (0, 'Silverpeas', 'org.silverpeas.domains.domainSP',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', ''),
       (1, 'Silverpeas 1', 'org.silverpeas.domains.domainSP1',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP1', '');

/**
 * Access levels
 */
INSERT INTO st_accesslevel (id, name) VALUES ('U', 'User');
INSERT INTO st_accesslevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO st_accesslevel (id, name) VALUES ('G', 'Guest');
INSERT INTO st_accesslevel (id, name) VALUES ('R', 'Removed');
INSERT INTO st_accesslevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO st_accesslevel (id, name) VALUES ('D', 'DomainManager');

/**
 * users
 */
INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (1, 0, '1', 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (2, 0, '2', 'Hetfield', 'jhetfield', 'U', 'VALID', '2013-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (3, 0, '3', 'Hammett', 'khammett', 'U', 'VALID', '2013-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (4, 0, '4', 'He', 'llo', 'U', 'REMOVED', '2013-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (10, 1, '10', 'LastName_10', 'Login_10', 'U', 'VALID', '2013-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (11, 1, '11', 'LastName_11', 'Login_11', 'U', 'REMOVED', '2013-02-11 00:00:00.000');

INSERT INTO domainsp_user (id, lastName, login) VALUES (1, 'Administrateur', 'SilverAdmin');
INSERT INTO domainsp_user (id, lastName, login) VALUES (2, 'Hetfield', 'jhetfield');
INSERT INTO domainsp_user (id, lastName, login) VALUES (3, 'Hammett', 'khammett');
INSERT INTO domainsp_user (id, lastName, login) VALUES (4, 'He', 'llo');

/**
 * user groups
 */
INSERT INTO st_group (id, domainId, specificId, name, state, stateSaveDate)
VALUES (1, 0, '1', 'Groupe 1', 'VALID', '2012-01-01 00:00:00.000');
INSERT INTO domainsp_group (id, name)
VALUES (1, 'Groupe 1');

INSERT INTO st_group (id, domainId, specificId, name, state, stateSaveDate)
VALUES (2, 0, '2', 'Groupe 2', 'REMOVED', '2012-01-01 00:00:00.000');
INSERT INTO domainsp_group (id, name)
VALUES (2, 'Groupe 2');

INSERT INTO st_group (id, domainId, specificId, name, state, stateSaveDate)
VALUES (10, 1, '10', 'Groupe 10', 'VALID', '2012-01-01 00:00:00.000');
INSERT INTO domainsp_group (id, name)
VALUES (10, 'Groupe 10');

INSERT INTO st_group (id, domainId, specificId, name, state, stateSaveDate)
VALUES (20, 1, '20', 'Groupe 20', 'REMOVED', '2012-01-01 00:00:00.000');
INSERT INTO domainsp_group (id, name)
VALUES (20, 'Groupe 20');


/**
 * Last Unique Id for tables used in tests
 */
INSERT INTO uniqueid (maxid, tableName) VALUES (20, 'domainsp_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (20, 'st_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (11, 'domainsp_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (11, 'st_user');
