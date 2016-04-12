/**
 * Silverpeas domains
 */
INSERT INTO st_domain (id, name, propFilename, className, authenticationServer, silverpeasServerURL)
    VALUES (0, 'Silverpeas', 'org.silverpeas.domains.domainSP',
            'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP', '');

/**
 * users
 */
INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (1, 0, '1', 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (2, 0, '2', 'SimpleUser', 'SimpleUser', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (3, 0, '3', 'Hetfield', 'Hetfield', 'U', 'VALID', '2012-01-01 00:00:00.000');

/**
 * user groups
 */
INSERT INTO st_group (id, domainId, specificId, name) VALUES (1, 0, '1', 'Groupe 1');

/**
 * users in the default Silverpeas domain
 */
INSERT INTO domainsp_user (id, lastName, login) VALUES (1, 'Administrateur', 'SilverAdmin');
INSERT INTO domainsp_user (id, lastName, login) VALUES (2, 'SimpleUser', 'SimpleUser');
INSERT INTO domainsp_user (id, lastName, login) VALUES (3, 'Hetfield', 'Hetfield');

/**
 * groups in the default Silverpeas domain
 */
INSERT INTO domainsp_group (id, name) VALUES (1, 'Groupe 1');

/**
 * spaces
 */
INSERT INTO st_space (id, domainFatherId, name, lang, firstPageType, isInheritanceBlocked)
    VALUES (1, NULL, 'Space 1', 'fr', 0, 0);
INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (2, 1, 'Space 1-2', 0, 0);

INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (3, NULL, 'Space 2', 0, 0);

INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (4, NULL, 'Space 4', 0, 0);
INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (5, 4, '4.1 (Inheritance blocked)', 0, 1);
INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (6, 4, '4.2 (Inheritance enabled)', 0, 0);

INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (100, NULL, 'Space 100', 0, 0);
INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (110, 100, 'Space 110', 0, 0);

/**
 * multilang support in spaces
 */
INSERT INTO st_spacei18n (id, spaceId, lang, name) VALUES (1, 2, 'en', 'Space 1-2 in english');

/**
 * component instances (application instances)
 */
INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (1, 1, 'GED', 'kmelia', 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (4, 1, 'GED (Inheritance blocked)', 'kmelia', 1);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (2, 2, 'Dates clés', 'almanach', 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (3, 3, 'Documents', 'kmelia', 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (200, 100, 'Documents In root Space', 'kmelia', 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (210, 110, 'Documents in sub space', 'kmelia', 0);

/**
 * roles the users play in the spaces
 */
// Administrateur is publisher and user on space 1
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (1, 1, 'publisher', 0);
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (2, 1, 'reader', 0);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (1, 1);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (2, 1);

// SimpleUser is publisher on space 3 and subspace 2
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (3, 3, 'publisher', 0);
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (4, 2, 'publisher', 0);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (3, 2);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (4, 2);

// Inherited profiles from space 1
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (5, 2, 'publisher', 1);
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (6, 2, 'reader', 1);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (5, 1);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (6, 1);

/**
 * roles the users play in the component instances
 */
// Application GED inherits rights of its space
INSERT INTO st_userrole (id, instanceId, roleName, isInherited) VALUES (1, 1, 'publisher', 1);
INSERT INTO st_userrole (id, instanceId, roleName, isInherited) VALUES (2, 1, 'user', 1);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (1, 1);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (2, 1);

// Application Almanach inherits rights of its space
INSERT INTO st_userrole (id, instanceId, roleName, isInherited) VALUES (3, 2, 'publisher', 1);
INSERT INTO st_userrole (id, instanceId, roleName, isInherited) VALUES (4, 2, 'user', 1);
INSERT INTO st_userrole (id, instanceId, roleName, isInherited) VALUES (5, 2, 'user', 0);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (3, 1);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (3, 2);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (4, 1);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (5, 3);

// Application Documents inherits rights of its space
INSERT INTO st_userrole (id, instanceId, roleName, isInherited) VALUES (6, 3, 'publisher', 1);
INSERT INTO st_userrole_user_rel (userRoleId, userId) VALUES (6, 2);

/**
 * Last Unique Id for tables used in tests
 */
INSERT INTO uniqueid (maxid, tableName) VALUES (3, 'st_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (0, 'st_domain');
INSERT INTO uniqueid (maxid, tableName) VALUES (3, 'domainsp_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'st_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'domainsp_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (199, 'st_space');
INSERT INTO uniqueid (maxid, tableName) VALUES (4, 'st_componentinstance');
INSERT INTO uniqueid (maxid, tableName) VALUES (6, 'st_spaceuserrole');
INSERT INTO uniqueid (maxid, tableName) VALUES (6, 'st_userrole');
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'st_spacei18n');
