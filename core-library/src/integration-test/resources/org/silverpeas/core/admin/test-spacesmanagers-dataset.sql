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

/**
 * users in the default Silverpeas domain
 */
INSERT INTO domainsp_user (id, lastName, login) VALUES (1, 'Administrateur', 'SilverAdmin');
INSERT INTO domainsp_user (id, lastName, login) VALUES (2, 'SimpleUser', 'SimpleUser');

/**
 * user groups
 */
INSERT INTO st_group (id, domainId, specificId, name) VALUES (1, 0, '1', 'Groupe 1');
INSERT INTO domainsp_group (id, name) VALUES (1, 'Groupe 1');
INSERT INTO st_group_user_rel(groupid, userid) VALUES (1, 1);

/**
 * spaces
 */
INSERT INTO st_space (id, domainFatherId, name, lang, firstPageType, isInheritanceBlocked)
    VALUES (1, NULL, 'Space 1', 'fr', 0, 0);
INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (2, 1, 'Space 1-1', 0, 0);

INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (3, 2, 'Space 1-1-1', 0, 0);

INSERT INTO st_space (id, domainFatherId, name, firstPageType, isInheritanceBlocked)
    VALUES (4, 1, 'Space 1-2', 0, 0);

/**
 * component instances (application instances)
 */
INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (1, 1, 'GED', 'kmelia', 0);

INSERT INTO st_componentinstance (id, spaceId, name, componentName, isInheritanceBlocked)
    VALUES (2, 2, 'Dates clés', 'almanach', 0);

/**
 * Managers of space
 */
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (1, 3, 'Manager', 0);
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (2, 4, 'Manager', 0);
INSERT INTO st_spaceuserrole (id, spaceId, roleName, isInherited) VALUES (3, 1, 'Manager', 0);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (1, 2);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (2, 1);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (2, 2);
INSERT INTO st_spaceuserrole_user_rel (spaceUserRoleId, userId) VALUES (3, 2);
INSERT INTO st_spaceuserrole_group_rel (spaceuserroleid, groupid) VALUES (1, 1);
INSERT INTO st_spaceuserrole_group_rel (spaceuserroleid, groupid) VALUES (2, 1);

/**
 * Last Unique Id for tables used in tests
 */
INSERT INTO uniqueid (maxid, tableName) VALUES (3, 'st_spaceuserrole');
INSERT INTO uniqueid (maxid, tableName) VALUES (2, 'st_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (2, 'st_componentinstance');
INSERT INTO uniqueid (maxid, tableName) VALUES (2, 'domainsp_user');
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'st_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (1, 'domainsp_group');
INSERT INTO uniqueid (maxid, tableName) VALUES (4, 'st_space');
