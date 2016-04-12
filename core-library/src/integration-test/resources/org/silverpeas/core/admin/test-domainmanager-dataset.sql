/**
 * Silverpeas domains
 */
INSERT INTO st_domain (id, name, propFilename, className, authenticationServer, silverpeasServerURL)
    VALUES (0, 'Silverpeas', 'org.silverpeas.domains.domainSP',
            'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', '');

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
    VALUES (4, 0, '4', 'Homer', 'homer', 'U', 'VALID', '2013-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (5, 0, '5', 'Bart', 'bart', 'D', 'VALID', '2013-02-11 00:00:00.000');


INSERT INTO domainsp_user (id, lastName, login) VALUES (1, 'Administrateur', 'SilverAdmin');
INSERT INTO domainsp_user (id, lastName, login) VALUES (2, 'Hetfield', 'jhetfield');
INSERT INTO domainsp_user (id, lastName, login) VALUES (3, 'Hammett', 'khammett');
INSERT INTO domainsp_user (id, lastName, login) VALUES (4, 'Homer', 'homer');
INSERT INTO domainsp_user (id, lastName, login) VALUES (5, 'Bart', 'bart');

/**
 * user groups
 */
INSERT INTO st_group (id, domainId, specificId, name) VALUES (1, 0, '1', 'Groupe 1');
INSERT INTO domainsp_group (id, name) VALUES (1, 'Groupe 1');

/**
 * Last Unique Id for tables used in tests
 */
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'domainsp_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'st_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (6, 'domainsp_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (6, 'st_user');
