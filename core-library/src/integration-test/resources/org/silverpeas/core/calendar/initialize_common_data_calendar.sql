INSERT INTO st_accesslevel (id, name) VALUES ('U', 'User');
INSERT INTO st_accesslevel (id, name) VALUES ('A', 'Administrator');
INSERT INTO st_accesslevel (id, name) VALUES ('G', 'Guest');
INSERT INTO st_accesslevel (id, name) VALUES ('R', 'Removed');
INSERT INTO st_accesslevel (id, name) VALUES ('K', 'KMManager');
INSERT INTO st_accesslevel (id, name) VALUES ('D', 'DomainManager');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate, notifManualReceiverLimit)
VALUES (0, 0, '0', 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000', 0);
INSERT INTO st_user (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate, notifManualReceiverLimit)
VALUES (1, 0, '1', 'Toto', 'Chez-les-Papoos', 'toto', 'U', 'VALID', '2012-01-01 00:00:00.000', 0);

INSERT INTO sb_calendar
  (id, instanceId, title, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_3', 'instance_A', 'title 3', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_2', 'instance_B', 'title 2', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_1', 'instance_A', 'title 1', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:55:00Z', '1', 1);

INSERT INTO sb_calendar_event
(id, calendarId, inDays, startDate, endDate, title, description, visibility, priority, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_E_1', 'ID_3', TRUE, '2016-01-08 00:00:00', '2016-01-20 23:59:59', 'title A',
             'description A', 'PUBLIC', 0, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_2', 'ID_2', FALSE, '2016-01-05 08:00:00', '2016-01-21 16:50:00', 'title B',
             'description B', 'PUBLIC', 0, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_3', 'ID_1', FALSE, '2016-01-08 18:30:00', '2016-01-22 13:38:22', 'title C',
             'description C', 'PUBLIC', 1, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_4', 'ID_3', FALSE, '2016-01-01 00:00:00', '2016-01-23 00:00:00', 'title D',
             'description D',
             'PRIVATE', 0, '2016-07-29T16:50:00Z', '0', '2016-07-29T16:50:00Z', '0',
   0),
  ('ID_E_5', 'ID_1', FALSE, '2016-01-09 00:00:00', '2016-01-24 00:00:00', 'title E',
             'description E',
             'PUBLIC', 1, '2016-07-29T16:50:00Z', '0', '2016-07-29T16:55:00Z', '1',
   1);

INSERT INTO sb_calendar_attributes (event_id, name, value) VALUES ('ID_E_1', 'location', 'location A');
INSERT INTO sb_calendar_attributes (event_id, name, value) VALUES ('ID_E_2', 'location', 'location B');
INSERT INTO sb_calendar_attributes (event_id, name, value) VALUES ('ID_E_3', 'location', 'location C');