INSERT INTO st_accesslevel (id, name) VALUES ('U', 'User');
INSERT INTO st_accesslevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO st_accesslevel (id, name) VALUES ('G', 'Guest');
INSERT INTO st_accesslevel (id, name) VALUES ('R', 'Removed');
INSERT INTO st_accesslevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO st_accesslevel (id, name) VALUES ('D', 'DomainManager');

INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, state, stateSaveDate)
VALUES
    (0, 0, '0', '', 'Administrateur', 'ehu@silverpeas.com', 'SilverAdmin', '', 'A', 'VALID', '2012-01-01 00:00:00.0'),
    (1, 0, '1', 'Bart', 'Simpson', 'ehu@silverpeas.com', 'user_a', '', 'U', 'VALID', '2012-01-01 00:00:00.0'),
    (2, 0, '2', 'Homer', 'Simpson', 'homer@simpson.com', 'user_b', '', 'U', 'VALID', '2012-01-01 00:00:00.0'),
    (10, 0, '10', 'Marge', 'Simpson', '', 'user_c', '', 'U', 'VALID', '2012-01-01 00:00:00.0'),
    (11, 0, '11', 'Lisa', 'Simpson', 'lisa@simpson.com', 'user_d','', 'U', 'VALID', '2012-01-01 00:00:00.0');

INSERT INTO st_group (id, domainid, specificid, name, supergroupid, state, stateSaveDate)
VALUES
    (1, -1, '1', 'G1', NULL, 'VALID', '2012-01-01 00:00:00.000'),
    (2, -1, '2', 'G2', NULL, 'VALID', '2012-01-01 00:00:00.000'),
    (3, -1, '3', 'G3', NULL, 'VALID', '2012-01-01 00:00:00.000'),
    (4, -1, '4', 'G3-1', 3, 'VALID', '2012-01-01 00:00:00.000'),
    (5, -1, '5', 'G3-2', 3, 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_group_user_rel (groupid, userid)
VALUES (1, 1),
       (1, 10),
       (1, 11),
       (2, 0),
       (2, 11),
       (4, 1),
       (4, 2),
       (5, 10),
       (5, 11);

INSERT INTO st_space (id, domainFatherId, name, firstpagetype, ordernum, isinheritanceblocked)
VALUES
    (1, NULL, 'Space-A_Level-1', 2, 0, 0),
    (2, 1, 'Space-A_Level-2', 2, 0, 0),
    (10, NULL, 'Space-B_Level-1', 2, 1, 0);

INSERT INTO st_componentinstance (id, spaceid, componentname, ordernum, ispublic, name, isinheritanceblocked, ishidden)
VALUES
    (1, 1, 'myComponent', 0, 0, 'My Component', 0, 0),
    (2, 2, 'myComponent', 0, 0, 'My Another Component', 1, 0);

INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited)
VALUES
    (1, 1, 'admin', 0),
    (2, 1, 'publisher', 0),
    (3, 1, 'writer', 0),
    (4, 1, 'reader', 0);

INSERT INTO st_userrole (id, instanceid, name, rolename, isinherited)
VALUES
    (1, 1,  'Managers','admin', 0),
    (2, 1, 'Validators', 'validator', 0),
    (3, 1, 'Readers', 'booker', 0),
    (10, 2,  'Managers','admin', 1),
    (11, 2, 'Validators', 'validator', 1),
    (12, 2, 'Readers', 'reader', 1);

INSERT INTO st_userrole_group_rel (userroleId, groupId)
VALUES (11, 1);

INSERT INTO SC_MyComponent_Resources (id, instanceId, name, creationDate, updateDate, creator, updater)
VALUES
    (1, 'myComponent1', 'Salle Chartreuse', '2025-03-03T14:40:21.440869Z', '2025-03-03T14:40:21.440869Z', '5', '5'),
    (2, 'myComponent1', 'Salle Belledonne', '2025-03-03T14:40:21.440869Z', '2025-03-03T14:40:21.440869Z', '5', '5'),
    (3, 'myComponent1', 'Twingo verte', '2025-03-03T14:40:21.440869Z', '2025-03-03T14:40:21.440869Z', '5', '5');

INSERT INTO SC_MyComponent_Validators (resourceId, validatorid)
VALUES (1, '0'),
       (1, '1'),
       (1, '2'),
       (3, '0');

INSERT INTO UniqueId (maxId, tableName)
VALUES ('4', 'sc_mycomponent_resources');