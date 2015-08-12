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
