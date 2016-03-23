/* The group of users */
INSERT INTO st_group (id, domainId, specificId, superGroupId, name, description, synchroRule)
VALUES (1, 0, 'A', NULL, 'Simpsons', '', '');

INSERT INTO st_group (id, domainId, specificId, superGroupId, name, description, synchroRule)
VALUES (2, 0, 'B', 1, 'Children', '', '');

INSERT INTO st_group (id, domainId, specificId, superGroupId, name, description, synchroRule)
VALUES (3, 0, 'C', 1, 'Parents', '', '');

/* the users */
INSERT INTO st_user (id, domainId, specificId, firstName, lastName, email, login, loginMail,
                     accessLevel, loginQuestion, loginAnswer, state, stateSaveDate)
VALUES (1, 0, '1', 'Bart', 'Simpson', 'bart.simpson@silverpeas.org', 'bart.simpson', '', 'U',
        '', '', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, email, login, loginMail,
                     accessLevel, loginQuestion, loginAnswer, state, stateSaveDate)
VALUES (2, 0, '2', 'Lisa', 'Simpson', 'lisa.simpson@silverpeas.org', 'lisa.simpson', '', 'U',
        '', '', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, email, login, loginMail,
                     accessLevel, loginQuestion, loginAnswer, state, stateSaveDate)
VALUES (3, 0, '3', 'Maggie', 'Simpson', 'maggie.simpson@silverpeas.org', 'maggie.simpson', '', 'U',
        '', '', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, email, login, loginMail,
                     accessLevel, loginQuestion, loginAnswer, state, stateSaveDate)
VALUES (4, 0, '2', 'Homer', 'Simpson', 'homer.simpson@silverpeas.org', 'homer.simpson', '', 'U',
        '', '', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, firstName, lastName, email, login, loginMail,
                     accessLevel, loginQuestion, loginAnswer, state, stateSaveDate)
VALUES (5, 0, '5', 'Marge', 'Simpson', 'marge.simpson@silverpeas.org', 'marge.simpson', '', 'U',
        '', '', 'VALID', '2012-01-01 00:00:00.000');

/* the relationships between users and groups */
INSERT INTO st_group_user_rel (groupId, userId) VALUES (1, 1);
INSERT INTO st_group_user_rel (groupId, userId) VALUES (2, 1);
INSERT INTO st_group_user_rel (groupId, userId) VALUES (2, 2);
INSERT INTO st_group_user_rel (groupId, userId) VALUES (2, 3);
INSERT INTO st_group_user_rel (groupId, userId) VALUES (3, 4);
INSERT INTO st_group_user_rel (groupId, userId) VALUES (3, 5);

/* the statistics */
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-01-01', 1, 34, 353282954);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-01-01', 2, 5, 10077813);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-01-01', 3, 71, 839936043);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-01-01', 4, 97, 890435611);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-01-01', 5, 16, 33485629);

INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-02-01', 1, 10, 19393516);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-02-01', 2, 8, 17248937);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-02-01', 3, 49, 137319674);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-02-01', 4, 57, 145001011);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-02-01', 5, 5, 12604658);

INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-03-01', 1, 44, 480061201);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-03-01', 2, 64, 795077447);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-03-01', 3, 41, 600340240);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-03-01', 4, 139, 349477599);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-03-01', 5, 1, 14423032);

INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-04-01', 1, 3, 9504000);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-04-01', 2, 2, 3721031);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-04-01', 3, 53, 796772435);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-04-01', 4, 48, 333543547);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-04-01', 5, 288, 1589079961);

INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-05-01', 1, 1, 1864859);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-05-01', 2, 51, 119062229);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-05-01', 3, 12, 28180342);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-05-01', 4, 16, 29557344);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-05-01', 5, 35, 133440623);

INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-06-01', 1, 13, 30242735);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-06-01', 2, 17, 85030220);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-06-01', 3, 67, 161696874);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-06-01', 4, 14, 65769156);
INSERT INTO sb_stat_connectioncumul (datestat, userId, countConnection, duration)
    VALUES ('2011-06-01', 5, 4, 7086469);
