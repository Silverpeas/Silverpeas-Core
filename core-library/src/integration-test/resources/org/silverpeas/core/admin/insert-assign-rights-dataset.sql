/* Access level */
INSERT INTO st_accesslevel (id, name) VALUES ('U', 'User');
INSERT INTO st_accesslevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO st_accesslevel (id, name) VALUES ('G', 'Guest');
INSERT INTO st_accesslevel (id, name) VALUES ('R', 'Removed');
INSERT INTO st_accesslevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO st_accesslevel (id, name) VALUES ('D', 'DomainManager');

/* Domains */
INSERT INTO st_domain (id, name, description, propFileName, className, authenticationServer)
VALUES (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-');
INSERT INTO st_domain (id, name, description, propFileName, className, authenticationServer)
VALUES (0, 'domainSilverpeas', 'default domain for Silverpeas',
        'org.silverpeas.domains.domainSP',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP');
INSERT INTO st_domain (id, name, description, propFileName, className, authenticationServer)
VALUES (1, 'SILVERPEAS', 'Zimbra Silverpeas', 'org.silverpeas.domains.domainSILVERPEAS',
        'org.silverpeas.domains.ldapdriver.LDAPDriver', 'autDomainSILVERPEAS');

/* Users */
INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, state, stateSaveDate)
VALUES (0, 0, '0', '', 'Administrateur', 'ehu@silverpeas.com', 'SilverAdmin', '', 'A', 'VALID',
        '2012-01-01 00:00:00.0');
INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, loginquestion, loginanswer, state, stateSaveDate)
VALUES (1, 0, '1', 'Bart', 'Simpson', 'ehu@silverpeas.com', 'user_a', '', 'U', '', '', 'VALID',
        '2012-01-01 00:00:00.0');
INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, loginquestion, loginanswer, state, stateSaveDate)
VALUES (2, 0, '2', 'Homer', 'Simpson', 'homer@simpson.com', 'user_b', '', 'U', '', '', 'VALID',
        '2012-01-01 00:00:00.0');
INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, loginquestion, loginanswer, state, stateSaveDate)
VALUES
  (3, 1, '3', 'Marge', 'Simpson', '', 'user_c', '', 'U', '', '', 'VALID', '2012-01-01 00:00:00.0');
INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, loginquestion, loginanswer, state, stateSaveDate)
VALUES
  (4, 1, 'f6632e21-9990-4393-9d65-4bdfebf7163f', 'Lisa', 'Simpson', 'lisa@simpson.com', 'user_d',
   '', 'U', '', '', 'VALID', '2012-01-01 00:00:00.0');
INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, loginquestion, loginanswer, state, stateSaveDate)
VALUES
  (38, 1, 'target_38', 'target', 'target', 'target@simpson.com', 'user_target', '', 'U', '', '',
   'VALID', '2012-01-01 00:00:00.0');


/*
Groups
- G1 (domain 0)
- ...G1-1
- ...G1-2
- G2 (domain 1)
- G_TARGET (domain 1)
 */
INSERT INTO st_group (id, domainid, specificid, name, description, synchrorule, supergroupid)
VALUES (1, 0, '1', 'G1_D0', 'G1_D0 description', '', NULL);
INSERT INTO st_group (id, domainid, specificid, name, description, synchrorule, supergroupid)
VALUES (2, 0, '2', 'G1-1_D0', 'G1-1_D0 description', '', '1');
INSERT INTO st_group (id, domainid, specificid, name, description, synchrorule, supergroupid)
VALUES (3, 0, '3', 'G1-2_D0', 'G1-2_D0 description', '', '1');
INSERT INTO st_group (id, domainid, specificid, name, description, synchrorule, supergroupid)
VALUES (10, 1, '10', 'G2_D1', 'G2_D1 description', '', NULL);
INSERT INTO st_group (id, domainid, specificid, name, description, synchrorule, supergroupid)
VALUES (26, 1, '26', 'G_TARGET_D1', 'G_TARGET_D1 description', '', NULL);


/*
G1-2 (domain 0)
- .....User id 1 (domain 0)
- .....User id 2 (domain 0)
- G2 (domain 1)
- ... User id 4 (domain 1)
 */
INSERT INTO st_group_user_rel (groupid, userid) VALUES (3, 1);
INSERT INTO st_group_user_rel (groupid, userid) VALUES (3, 2);
INSERT INTO st_group_user_rel (groupid, userid) VALUES (10, 4);


/*
Spaces
- Space-A_Level-1
- ...Space-A_Level_2
- Space-B_Level-1
 */
INSERT INTO st_space (id, domainFatherId, name, firstpagetype, ordernum, isinheritanceblocked)
VALUES (1, NULL, 'Space-A_Level-1', 2, 0, 0);
INSERT INTO st_space (id, domainFatherId, name, firstpagetype, ordernum, isinheritanceblocked)
VALUES (2, 1, 'Space-A_Level-2', 2, 0, 0);
INSERT INTO st_space (id, domainFatherId, name, firstpagetype, ordernum, isinheritanceblocked)
VALUES (10, NULL, 'Space-B_Level-1', 2, 1, 0);


/*
Components instances
- Space-A_Level-1
- ¤¤ kmelia_1
- ...Space-A_Level_2
- ¤¤¤¤ blog_2
- Space-B_Level-1
- ¤¤ almanach_3
 */
INSERT INTO st_componentinstance (id, spaceid, componentname, ordernum, ispublic, name, isinheritanceblocked, ishidden)
VALUES (1, 1, 'kmelia', 0, 0, 'kmelia-Space-A_Level-1', 0, 0);
INSERT INTO st_componentinstance (id, spaceid, componentname, ordernum, ispublic, name, isinheritanceblocked, ishidden)
VALUES (2, 2, 'blog', 0, 0, 'blog-Space-A_Level-2', 0, 0);
INSERT INTO st_componentinstance (id, spaceid, componentname, ordernum, ispublic, name, isinheritanceblocked, ishidden)
VALUES (3, 10, 'almanach', 1, 0, 'almanach-Space-B_Level-1', 0, 0);


INSERT INTO sb_node_node (nodeId, instanceid, nodeName, nodepath, nodefatherid, nodeCreationDate, nodeCreatorId, rightsdependson, nodelevelnumber)
VALUES (10, '1', 'Root', '/', -1, '2014/11/06', '0', -1, 1);
INSERT INTO sb_node_node (nodeId, instanceid, nodeName, nodePath, nodefatherid, nodeCreationDate, nodeCreatorId, rightsdependson, nodelevelnumber)
VALUES (101, '1', 'Folder-1', '/10/', 10, '2014/11/06', '0', -1, 2);
INSERT INTO sb_node_node (nodeId, instanceid, nodeName, nodePath, nodefatherid, nodeCreationDate, nodeCreatorId, rightsdependson, nodelevelnumber)
VALUES (1011, '1', 'Folder-1-1', '/10/101', 101, '2014/11/06', '0', 1011, 3);


/*
Node of kmelia-Space-A_Level-1
- Root
- ... Folder-1
- ...... Folder-1-1
 */
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited) VALUES (10, 1, 'admin', 0);
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited) VALUES (100, 2, 'admin', 1);
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited) VALUES (11, 1, 'writer', 0);
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited) VALUES (30, 10, 'admin', 0);
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited) VALUES (31, 10, 'writer', 0);
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited) VALUES (32, 10, 'publisher', 0);


/*
Space User roles
- - Space-A_Level-1:
- > group G1_D0
- > user id 3
- >>> InHeritage:
- ..... Space-A_Level_2
- ........ blog_2
- ..... kmelia_1
- - Space-B_Level-1:
 */
INSERT INTO st_userrole (id, instanceid, rolename, isinherited, objectid, objecttype)
VALUES (10, 1, 'admin', 0, NULL, NULL);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (100, 1, 'admin', 1);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (11, 1, 'writer', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (110, 1, 'writer', 1);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited, objectid, objecttype)
VALUES (910, 1, 'admin', 0, 101, 'O');
INSERT INTO st_userrole (id, instanceid, rolename, isinherited, objectid, objecttype)
VALUES (9100, 1, 'admin', 1, 101, 'O');
INSERT INTO st_userrole (id, instanceid, rolename, isinherited, objectid, objecttype)
VALUES (911, 1, 'writer', 0, 1011, 'O');
INSERT INTO st_userrole (id, instanceid, rolename, isinherited, objectid, objecttype)
VALUES (9110, 1, 'writer', 1, 1011, 'O');
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (20, 2, 'admin', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (200, 2, 'admin', 1);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (21, 2, 'writer', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (210, 2, 'writer', 1);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (30, 3, 'admin', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (300, 3, 'admin', 1);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (31, 3, 'writer', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (310, 3, 'writer', 1);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (41, 3, 'publisher', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited)
VALUES (410, 3, 'publisher', 1);


/* Space GROUP Role Relations */
INSERT INTO st_spaceuserrole_group_rel (spaceuserroleid, groupid) VALUES (10, 1);
INSERT INTO st_spaceuserrole_group_rel (spaceuserroleid, groupid) VALUES (100, 1);
INSERT INTO st_spaceuserrole_group_rel (spaceuserroleid, groupid) VALUES (30, 3);


/* Component User roles */
INSERT INTO st_spaceuserrole_user_rel (spaceuserroleid, userid) VALUES (32, 2);
INSERT INTO st_spaceuserrole_user_rel (spaceuserroleid, userid) VALUES (10, 3);
INSERT INTO st_spaceuserrole_user_rel (spaceuserroleid, userid) VALUES (100, 3);


/* Space GROUP Role Relations */
INSERT INTO st_userrole_group_rel (userroleid, groupid) VALUES (100, 1);
INSERT INTO st_userrole_group_rel (userroleid, groupid) VALUES (200, 1);
INSERT INTO st_userrole_group_rel (userroleid, groupid) VALUES (911, 1);
INSERT INTO st_userrole_group_rel (userroleid, groupid) VALUES (300, 3);
INSERT INTO st_userrole_group_rel (userroleid, groupid) VALUES (30, 10);

/* Component USER Role Relations */
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (11, 1);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (10, 2);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (410, 2);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (911, 2);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (100, 3);
INSERT INTO st_userrole_user_rel (userroleid, userid) VALUES (200, 3);
