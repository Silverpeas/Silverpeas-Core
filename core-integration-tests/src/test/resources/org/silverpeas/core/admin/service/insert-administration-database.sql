INSERT INTO st_accesslevel (id, name)
VALUES ('U', 'User'),
       ('A', 'Administrator'),
       ('G', 'Guest'),
       ('R', 'Removed'),
       ('K', 'KMManager'),
       ('D', 'DomainManager');

/*
 Users:
 # SP DOMAIN
 - Admin
 - 16 users, 1001 to 1016:
 - ... BLOCKED:
 - ... ... 1002
 - ... EXPIRED:
 - ... ... 1003
 - ... DEACTIVATED:
 - ... ... 1007
 - ... REMOVED:
 - ... ... 1011
 - ... DELETED:
 - ... ... 1015
 # SQL DOMAIN
 - 15 users, 2001 to 2015:
 - ... REMOVED:
 - ... ... 2006, 2012
 - ... DELETED:
 - ... ... 3003, 2008, 2015
 */

INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail,
                     accesslevel, state, stateSaveDate)
-- SP DOMAIN
VALUES (0, 0, '0', 'adm', 'Administrateur', 'adm@silverpeas.org', 'administrateur', '', 'A',
        'VALID', '2012-01-01 00:00:00.0'),
       (1001, 0, '1', 'spu1', 'SP USER 1', 'spu1@silverpeas.org', 'sp_user_1', '', 'A', 'VALID',
        '2012-01-01 00:00:00.0'),
       (1002, 0, '2', 'spu2', 'SP USER 2', 'spu2@silverpeas.org', 'sp_user_2', '', 'U', 'BLOCKED',
        '2012-01-01 00:00:00.0'),
       (1003, 0, '3', 'spu3', 'SP USER 3', 'spu3@silverpeas.org', 'sp_user_3', '', 'U', 'EXPIRED',
        '2012-01-01 00:00:00.0'),
       (1004, 0, '4', 'spu4', 'SP USER 4', 'spu4@silverpeas.org', 'sp_user_4', '', 'U', 'VALID',
        '2012-01-01 00:00:00.0'),
       (1005, 0, '5', 'spu5', 'SP USER 5', 'spu5@silverpeas.org', 'sp_user_5', '', 'U', 'VALID',
        '2012-01-01 00:00:00.0'),
       (1006, 0, '6', 'spu6', 'SP USER 6', 'spu6@silverpeas.org', 'sp_user_6', '', 'U', 'VALID',
        '2012-01-01 00:00:00.0'),
       (1007, 0, '7', 'spu7', 'SP USER 7', 'spu7@silverpeas.org', 'sp_user_7', '', 'U',
        'DEACTIVATED', '2012-01-01 00:00:00.0'),
       (1008, 0, '8', 'spu8', 'SP USER 8', 'spu8@silverpeas.org', 'sp_user_8', '', 'U', 'VALID',
        '2012-01-01 00:00:00.0'),
       (1009, 0, '9', 'spu9', 'SP USER 9', 'spu9@silverpeas.org', 'sp_user_9', '', 'U', 'VALID',
        '2012-01-01 00:00:00.0'),
       (1010, 0, '10', 'spu10', 'SP USER 10', 'spu10@silverpeas.org', 'sp_user_10', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (1011, 0, '11', 'spu11', 'SP USER 11', 'spu11@silverpeas.org', 'sp_user_11', '', 'U',
        'REMOVED', '2012-01-01 00:00:00.0'),
       (1012, 0, '12', 'spu12', 'SP USER 12', 'spu12@silverpeas.org', 'sp_user_12', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (1013, 0, '13', 'spu13', 'SP USER 13', 'spu13@silverpeas.org', 'sp_user_13', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (1014, 0, '14', 'spu14', 'SP USER 14', 'spu14@silverpeas.org', 'sp_user_14', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (1015, 0, '???REM???15', 'spu15', 'SP USER 15', 'spu15@silverpeas.org', '???REM???15', '',
        'U', 'DELETED', '2012-01-01 00:00:00.0'),
       (1016, 0, '31', 'spu16', 'SP USER 16', 'spu16@silverpeas.org', 'sp_user_16', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
-- SQL DOMAIN
       (2001, 1, '16', 'sqlu1', 'SQL USER 1', 'sqlu1@silverpeas.org', 'sql_user_1', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2002, 1, '17', 'sqlu2', 'SQL USER 2', 'sqlu2@silverpeas.org', 'sql_user_2', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2003, 1, '???REM???18', 'sqlu3', 'SQL USER 3', 'sqlu3@silverpeas.org', '???REM???18', '',
        'U', 'DELETED', '2012-01-01 00:00:00.0'),
       (2004, 1, '19', 'sqlu4', 'SQL USER 4', 'sqlu4@silverpeas.org', 'sql_user_4', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2005, 1, '20', 'sqlu5', 'SQL USER 5', 'sqlu5@silverpeas.org', 'sql_user_5', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2006, 1, '21', 'sqlu6', 'SQL USER 6', 'sqlu6@silverpeas.org', 'sql_user_6', '', 'U',
        'REMOVED', '2012-01-01 00:00:00.0'),
       (2007, 1, '22', 'sqlu7', 'SQL USER 7', 'sqlu7@silverpeas.org', 'sql_user_7', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2008, 1, '???REM???23', 'sqlu8', 'SQL USER 8', 'sqlu8@silverpeas.org', '???REM???23', '',
        'U', 'DELETED', '2012-01-01 00:00:00.0'),
       (2009, 1, '24', 'sqlu9', 'SQL USER 9', 'sqlu9@silverpeas.org', 'sql_user_9', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2010, 1, '25', 'sqlu10', 'SQL USER 10', 'sqlu10@silverpeas.org', 'sql_user_10', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2011, 1, '26', 'sqlu11', 'SQL USER 11', 'sqlu11@silverpeas.org', 'sql_user_11', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2012, 1, '27', 'sqlu12', 'SQL USER 12', 'sqlu12@silverpeas.org', 'sql_user_12', '', 'U',
        'REMOVED', '2012-01-01 00:00:00.0'),
       (2013, 1, '28', 'sqlu13', 'SQL USER 13', 'sqlu13@silverpeas.org', 'sql_user_13', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2014, 1, '29', 'sqlu14', 'SQL USER 14', 'sqlu14@silverpeas.org', 'sql_user_14', '', 'U',
        'VALID', '2012-01-01 00:00:00.0'),
       (2015, 1, '???REM???30', 'sqlu15', 'SQL USER 15', 'sqlu15@silverpeas.org', '???REM???30', '',
        'U', 'DELETED', '2012-01-01 00:00:00.0');

/*
 Groups:
 # MIXED DOMAIN
 - Group MIX 1
 - Group MIX 2
 - Group MIX 3
 - ... Group MIX 3-1
 - ... ... Group MIX 3-1-1
 - ... ... Group MIX 3-1-2
 # SP DOMAIN
 - Group SP 1
 - Group SP 2
 # SQL DOMAIN
 - Group SQL 1
 - ... Group SQL 1-2
 */

INSERT INTO st_group (id, domainid, specificid, name, description, synchrorule, supergroupid, state, stateSaveDate)
    VALUES (1, -1, '1', 'Group MIX 1', 'Group MIX 1 description', '', NULL, 'VALID', '2012-01-01 00:00:00.000'),
           (2, -1, '2', 'Group MIX 2', 'Group MIX 2 description', '', NULL, 'VALID', '2012-01-01 00:00:00.000'),
           (3, -1, '3', 'Group MIX 3', 'Group MIX 3 description', '', NULL, 'VALID', '2012-01-01 00:00:00.000'),
           (31, -1, '4', 'Group MIX 3-1', 'Group MIX 3-1 description', '', 3, 'VALID', '2012-01-01 00:00:00.000'),
           (311, -1, '5', 'Group MIX 3-1-1', 'Group MIX 3-1-1 description', '', 31, 'VALID', '2012-01-01 00:00:00.000'),
           (312, -1, '6', 'Group MIX 3-1-2', 'Group MIX 3-1-2 description', '', 31, 'VALID', '2012-01-01 00:00:00.000'),
           (1001, 0, '7', 'Group SP 1', 'Group SP 1 description', '', NULL, 'VALID', '2012-01-01 00:00:00.000'),
           (1002, 0, '8', 'Group SP 2', 'Group SP 2 description', '', NULL, 'VALID', '2012-01-01 00:00:00.000'),
           (2001, 1, '9', 'Group SQL 1', 'Group SQL 1 description', '', NULL, 'VALID', '2012-01-01 00:00:00.000'),
           (2011, 1, '10', 'Group SQL 1-1', 'Group SQL 1-1 description', '', 2001, 'VALID', '2012-01-01 00:00:00.000'),
           (2012, 1, '11', 'Group SQL 1-2 (REMOVED)', 'Group SQL 1-2 description', '', 2001, 'REMOVED', '2012-01-01 00:00:00.000');

INSERT INTO st_group_user_rel (groupid, userid)
    -- # MIXED DOMAIN
-- ... Group MIX 1: 1002 (BLOCKED)
VALUES (1, 1002),
-- ... Group MIX 2: 1006 (VALID), 1007 (DEACTIVATED), 1008 (VALID), 2014 (VALID)
       (2, 1006),
       (2, 1007),
       (2, 1008),
       (2, 2014),
-- ... Group MIX 3: 0 (VALID)
       (3, 0),
-- ... ... Group MIX 3-1: 1014 (VALID), 2014 (VALID)
       (31, 1014),
       (31, 2014),
-- ... ... ... Group MIX 3-1-1: 1014 (VALID), 2008 (DELETED), 1002 (BLOCKED), 1001 (VALID), 1010 (VALID), 1011 (REMOVED)
       (311, 1014),
       (311, 2008),
       (311, 1002),
       (311, 1001),
       (311, 1010),
       (311, 1011),
-- ... ... ... Group MIX 3-1-2: 2014 (VALID), 2015 (DELETED), 1007 (DEACTIVATED), 2001 (VALID), 2010 (VALID), 2012 (REMOVED)
       (312, 2014),
       (312, 2015),
       (312, 1007),
       (312, 2001),
       (312, 2010),
       (312, 2012),
-- # SP DOMAIN
-- ... Group SP 1: 1004 (VALID), 1005 (VALID)
       (1001, 1004),
       (1001, 1005),
-- ... Group SP 2: 1013 (VALID)
       (1002, 1013),
       (1002, 1016),
-- # SQL DOMAIN
-- ... Group SQL 1: 2005 (VALID), 2006 (REMOVED)
       (2001, 2005),
       (2001, 2006),
-- ... Group SQL 1-1: 2004 (VALID), 2011 (VALID), 2009 (VALID)
       (2011, 2004),
       (2011, 2011),
       (2011, 2009),
-- ... Group SQL 1-2 (REMOVED): 2001 (VALID), 2002 (VALID), 2002 (DELETED)
       (2012, 2001),
       (2012, 2002),
       (2012, 2003);

/*
Spaces
- Space-A_Level-1
- ...Space-A_Level_2
- Space-B_Level-1
 */
INSERT INTO st_space (id, domainFatherId, name, firstpagetype, ordernum, isinheritanceblocked)
VALUES (1, NULL, 'Space-A_Level-1', 2, 0, 0),
       (2, 1, 'Space-A_Level-2', 2, 0, 0),
       (10, NULL, 'Space-B_Level-1', 2, 1, 0);

/*
Components instances
- Space-A_Level-1
- ¤¤ kmelia_1
- ...Space-A_Level_2
- ¤¤¤¤ blog_2
- Space-B_Level-1
- ¤¤ almanach_3
 */
INSERT INTO st_componentinstance (id, spaceid, componentname, ordernum, ispublic, name,
                                  isinheritanceblocked, ishidden)
VALUES (1, 1, 'kmelia', 0, 0, 'kmelia-Space-A_Level-1', 0, 0),
       (2, 2, 'blog', 0, 0, 'blog-Space-A_Level-2', 0, 0),
       (3, 10, 'almanach', 1, 0, 'almanach-Space-B_Level-1', 0, 0);

/*
Node of kmelia-Space-A_Level-1
- Root
- ... Folder-1
- ...... Folder-1-1 (specific rights)
 */
INSERT INTO sb_node_node (nodeId, instanceid, nodeName, nodepath, nodefatherid, nodeCreationDate,
                          nodeCreatorId, rightsdependson, nodelevelnumber)
VALUES (10, '1', 'Root', '/', -1, '2014/11/06', '0', -1, 1),
       (101, '1', 'Folder-1', '/10/', 10, '2014/11/06', '0', -1, 2),
       (1011, '1', 'Folder-1-1', '/10/101', 101, '2014/11/06', '0', 1011, 3);

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
INSERT INTO st_spaceuserrole (id, spaceid, rolename, isinherited)
VALUES (10, 1, 'admin', 0),
       (100, 2, 'admin', 1),
       (11, 1, 'writer', 0),
       (30, 10, 'admin', 0),
       (31, 10, 'writer', 0),
       (32, 10, 'publisher', 0);
INSERT INTO st_userrole (id, instanceid, rolename, isinherited, objectid, objecttype)
VALUES (10, 1, 'admin', 0, NULL, NULL),
       (100, 1, 'admin', 1, NULL, NULL),
       (11, 1, 'writer', 0, NULL, NULL),
       (110, 1, 'writer', 1, NULL, NULL),
       (910, 1, 'admin', 0, 1011, 'O'),
       (9100, 1, 'admin', 1, 1011, 'O'),
       (911, 1, 'writer', 0, 1011, 'O'),
       (9110, 1, 'writer', 1, 1011, 'O'),
       (20, 2, 'admin', 0, NULL, NULL),
       (200, 2, 'admin', 1, NULL, NULL),
       (21, 2, 'writer', 0, NULL, NULL),
       (210, 2, 'writer', 1, NULL, NULL),
       (30, 3, 'admin', 0, NULL, NULL),
       (300, 3, 'admin', 1, NULL, NULL),
       (31, 3, 'writer', 0, NULL, NULL),
       (310, 3, 'writer', 1, NULL, NULL),
       (32, 3, 'publisher', 0, NULL, NULL),
       (320, 3, 'publisher', 1, NULL, NULL);

/* Space GROUP Role Relations */
INSERT INTO st_spaceuserrole_group_rel (spaceuserroleid, groupid)
-- 10 | 1 - Space-A_Level-1 | admin
--     1 - Group MIX 1
--     1001 - Group SP 1
VALUES (10, 1),
       (10, 1001),
-- 100 | 2 - Space-A_Level-2 | admin inherited
--     1 - Group MIX 1
--     1001 - Group SP 1
       (100, 1),
       (100, 1001),
-- 11 | 1 - Space-A_Level-1 | writer
--     31 - Group MIX 3-1
--     2001 - Group SQL 1
       (11, 31),
       (11, 2001),
-- 30 | 10 - Space-B_Level-1 | admin
--     1002 - Group SP 2
       (30, 1002),
-- 31 | 10 - Space-B_Level-1 | writer
--     3 - Group MIX 3
       (31, 3),
-- 32 | 10 - Space-B_Level-1 | publisher
--     1 - Group MIX 1
--     2011 - Group SQL 1-1
       (32, 1),
       (32, 2011);

/* Space USER roles Relations */
INSERT INTO st_spaceuserrole_user_rel (spaceuserroleid, userid)
-- 10 | 1 - Space-A_Level-1 | admin
--     0 - Administrateur - VALID
--     1001 - SP USER 1 - VALID
--     1011 - SP USER 11 - REMOVED
--     2015 - SQL USER 15 - DELETED
VALUES (10, 0),
       (10, 1001),
       (10, 1011),
       (10, 2015),
-- 100 | 2 - Space-A_Level-2 | admin inherited
--     0 - Administrateur - VALID
--     1001 - SP USER 1 - VALID
--     1011 - SP USER 11 - REMOVED
--     2015 - SQL USER 15 - DELETED
       (100, 0),
       (100, 1001),
       (100, 1011),
       (100, 2015),
-- 11 | 1 - Space-A_Level-1 | writer
--     1002 - SP USER 2 - BLOCKED
--     1006 - SP USER 6 - VALID
       (11, 1002),
       (11, 1006),
-- 30 | 10 - Space-B_Level-1 | admin
--     0 - Administrateur - VALID
--     2006 - SQL USER 6 - REMOVED
       (30, 0),
       (30, 2006),
-- 31 | 10 - Space-B_Level-1 | writer
--     1001 - SP USER 1 - VALID
--     2008 - SQL USER 8 - DELETED
       (31, 1001),
       (31, 2008),
-- 32 | 10 - Space-B_Level-1 | publisher
--     1004 - SP USER 4 - VALID
--     2003 - SQL USER 3 - DELETED
       (32, 1004),
       (32, 2003);

/* Component GROUP Role Relations */
INSERT INTO st_userrole_group_rel (userroleid, groupid)
-- 10 | 1 - kmelia-Space-A_Level-1 | admin
--     312 - Group MIX 3-1-2
--     1002 - Group SP 2
VALUES (10, 312),
       (10, 1002),
-- 100 | 1 - kmelia-Space-A_Level-1 | admin inherited
--     1 - Group MIX 1
--     1001 - Group SP 1
--     2012 - SQL 1-2 (REMOVED)
       (100, 1),
       (100, 1001),
       (100, 2012),
-- 110 | 1 - kmelia-Space-A_Level-1 | writer inherited
--     31 - Group MIX 3-1
--     2001 - Group SQL 1
       (110, 31),
       (110, 2001),
-- 200 | 2 - blog-Space-A_Level-2 | admin inherited
--     1 - Group MIX 1
--     1001 - Group SP 1
       (200, 1),
       (200, 1001),
-- 911 | 1 - kmelia-Space-A_Level-1 | Folder-1-1 (1011) with specific rights | writer
--     1002 - Group SP 2
       (911, 1002),
-- 21 | 2 - blog-Space-A_Level-2| writer
--     2012 - SQL 1-2 (REMOVED)
       (21, 2012),
-- 210 | 2 - blog-Space-A_Level-2 | writer inherited
--     31 - Group MIX 3-1
--     2001 - Group SQL 1
       (210, 31),
       (210, 2001),
-- 300 | 3 - almanach-Space-B_Level-1 | admin inherited
--     1002 - Group SP 2
--     2012 - SQL 1-2 (REMOVED)
       (300, 1002),
       (300, 2012),
-- 310 | 3 - almanach-Space-B_Level-1 | writer inherited
--     3 - Group MIX 3
       (310, 3),
-- 32 | 3 - almanach-Space-B_Level-1 | writer
--     1002 - Group SP 2
--     2012 - SQL 1-2 (REMOVED)
       (32, 1002),
       (32, 2012),
-- 320 | 3 - almanach-Space-B_Level-1 | publisher inherited
--     1 - Group MIX 1
--     2011 - Group SQL 1-1
       (320, 1),
       (320, 2011);

/* Component USER Role Relations */
INSERT INTO st_userrole_user_rel (userroleid, userid)
-- 10 | 1 - kmelia-Space-A_Level-1 | admin
--     1008 - SP USER 8 - VALID
--     1007 - SP USER 7 - DEACTIVATED
VALUES (10, 1008),
       (10, 1007),
-- 100 | 1 - kmelia-Space-A_Level-1 | admin inherited
--     0 - Administrateur - VALID
--     1001 - SP USER 1 - VALID
--     1011 - SP USER 11 - REMOVED
--     2015 - SQL USER 15 - DELETED
       (100, 0),
       (100, 1001),
       (100, 1011),
       (100, 2015),
-- 11 | 1 - kmelia-Space-A_Level-1 | writer
--     2006 - SQL USER 6 - REMOVED
--     2014 - SQL USER 14 - VALID
       (11, 2006),
       (11, 2014),
-- 910 | 1 - kmelia-Space-A_Level-1 | Folder-1-1 (1011) with specific rights | admin
--     1016 - SP USER 16 - VALID
       (910, 1016),
-- 911 | 1 - kmelia-Space-A_Level-1 | Folder-1-1 (1011) with specific rights | writer
--     2001 - SQL USER 1 - VALID
       (911, 2001),
-- 110 | 1 - kmelia-Space-A_Level-1 | writer inherited
--     1002 - SP USER 2 - BLOCKED
--     1006 - SP USER 6 - VALID
       (110, 1002),
       (110, 1006),
-- 20 | 2 - blog-Space-A_Level-2 | admin
--     2009 - SQL USER 9 - VALID
       (20, 2009),
-- 200 | 2 - blog-Space-A_Level-2 | admin inherited
--     0 - Administrateur - VALID
--     1001 - SP USER 1 - VALID
--     1011 - SP USER 11 - REMOVED
--     2015 - SQL USER 15 - DELETED
       (200, 0),
       (200, 1001),
       (200, 1011),
       (200, 2015),
-- 21 | 2 - blog-Space-A_Level-2 | writer
--     2013 - SQL USER 13 - VALID
       (21, 2013),
-- 210 | 2 - blog-Space-A_Level-2 | writer inherited
--     1002 - SP USER 2 - BLOCKED
--     1006 - SP USER 6 - VALID
       (210, 1002),
       (210, 1006),
-- 30 | 3 - almanach-Space-B_Level-1 | admin
--     2011 - SQL USER 11 - VALID
       (30, 2011),
-- 300 | 3 - almanach-Space-B_Level-1 | admin inherited
--     0 - Administrateur - VALID
--     2006 - SQL USER 6 - REMOVED
       (300, 0),
       (300, 2006),
-- 31 | 3 - almanach-Space-B_Level-1 | writer
--     1015 - SP USER 15 - DELETED
       (31, 1015),
-- 310 | 3 - almanach-Space-B_Level-1 | writer inherited
--     1001 - SP USER 1 - VALID
--     2008 - SQL USER 8 - DELETED
       (310, 1001),
       (310, 2008),
-- 32 | 3 - almanach-Space-B_Level-1 | publisher
--     2003 - SQL USER 3 - DELETED
       (32, 2003),
-- 320 | 3 - almanach-Space-B_Level-1 | publisher inherited
--     1004 - SP USER 4 - VALID
--     2003 - SQL USER 3 - DELETED
       (320, 1004),
       (320, 2003);
