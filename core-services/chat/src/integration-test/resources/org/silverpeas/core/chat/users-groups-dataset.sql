/**
 * Silverpeas domains
 */
INSERT INTO st_domain (id, name, propFilename, className, authenticationServer, silverpeasServerURL)
    VALUES (0, 'Silverpeas', 'org.silverpeas.domains.domainSP',
            'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', '');

INSERT INTO st_domain (id, name, propFilename, className, authenticationServer, silverpeasServerURL)
    VALUES (1, 'Goldpeas', 'org.silverpeas.domains.domainSP',
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
    VALUES (0, 0, '0', 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate)
VALUES (1, 0, '1', 'John', 'Doo1', 'jdoo1', 'U', 'VALID', '2013-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (2, 0, '2', 'John', 'Doo2', 'jdoo2', 'U', 'VALID', '2013-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (3, 1, '3', 'Tom', 'Bombadil1', 'tbombadil1', 'U', 'VALID', '2014-02-11 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (4, 1, '4', 'Tom', 'Bombadil2', 'tbombadil2', 'U', 'VALID', '2014-02-11 00:00:00.000');

INSERT INTO domainsp_user (id, lastName, login) VALUES (1, 'Administrateur', 'SilverAdmin');
INSERT INTO domainsp_user (id, lastName, login) VALUES (2, 'Hetfield', 'jhetfield');
INSERT INTO domainsp_user (id, lastName, login) VALUES (3, 'Hammett', 'khammett');

/**
 * user groups
 */
INSERT INTO st_group (id, domainId, specificId, name) VALUES (1, 0, '1', 'Group 1');
INSERT INTO domainsp_group (id, name) VALUES (1, 'Group 1');

INSERT INTO st_group (id, domainId, specificId, name) VALUES (2, 0, '2', 'Group 2');
INSERT INTO domainsp_group (id, name) VALUES (2, 'Group 2');

INSERT INTO st_group (id, domainId, specificId, name) VALUES (3, 0, '3', 'Group 3');
INSERT INTO domainsp_group (id, name) VALUES (3, 'Group 3');

INSERT INTO st_group (id, domainId, specificId, name) VALUES (4, 0, '4', 'Group 4');
INSERT INTO domainsp_group (id, name) VALUES (4, 'Group 4');

/**
 * Last Unique Id for tables used in tests
 */
INSERT INTO uniqueid (maxid, tableName) VALUES (4, 'domainsp_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (4, 'st_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (3, 'domainsp_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (4, 'st_user');
