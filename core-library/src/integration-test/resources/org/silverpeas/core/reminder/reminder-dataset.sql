INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (2, 0, '2', 'SimpleUser', 'SimpleUser', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO sb_reminder (id, reminderType, contrib_id, contrib_instanceId, contrib_type, userId,
                         triggered, trigger_dateTime)
VALUES ('Reminder#1ed074deee814b6a8035b9ced02ff56d', 'datetime', '42', 'myApp42', 'EventContrib',
        '2', TRUE, '2016-01-08T15:30:00Z');