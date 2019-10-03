INSERT INTO st_user (id, domainId, specificId, lastName, firstName, login, accessLevel, state, stateSaveDate)
VALUES (1, 0, '1', 'Simpson', 'Lisa', 'lisa.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, firstName, login, accessLevel, state, stateSaveDate)
VALUES (2, 0, '2', 'Simpson', 'Bart', 'bart.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, firstName, login, accessLevel, state, stateSaveDate)
VALUES (3, 0, '3', 'Simpson', 'Marge', 'marge.simpson', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO sb_reminder (id, reminderType, contrib_id, contrib_instanceId, contrib_type, userId,
                         triggered, trigger_dateTime, process_name)
VALUES ('Reminder#1ed074deee814b6a8035b9ced02ff56d', 'datetime', '42', 'myApp42', 'EventContrib',
        '2', TRUE, '2016-01-08T15:30:00Z', 'TestReminderProcess');

INSERT INTO sb_reminder (id, reminderType, contrib_id, contrib_instanceId, contrib_type, userId,
                         triggered, trigger_dateTime, process_name)
VALUES ('Reminder#1ed074deee814b6a8035b9ced02ff56e', 'duration', '42', 'myApp42', 'EventContrib',
        '3', TRUE, '2016-01-08T15:30:00Z', 'TestReminderProcess');

INSERT INTO sb_reminder (id, reminderType, contrib_id, contrib_instanceId, contrib_type, userId,
                         triggered, trigger_dateTime, process_name)
VALUES ('Reminder#1ed074deee814b6a8035b9ced02ff56f', 'datetime', '12', 'myApp42', 'EventContrib',
        '3', TRUE, '2016-01-08T15:30:00Z', 'TestReminderProcess');