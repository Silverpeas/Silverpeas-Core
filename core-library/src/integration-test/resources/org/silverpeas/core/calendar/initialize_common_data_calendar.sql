INSERT INTO sb_calendar
  (id, instanceId, title, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_3', 'instance_A', 'title 3', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_2', 'instance_B', 'title 2', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_1', 'instance_A', 'title 1', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:55:00Z', '1', 1);

INSERT INTO sb_calendar_event
(id, calendarId, inDays, startDate, endDate, title, description, attributes, visibility, priority, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_E_1', 'ID_3', TRUE, '2016-01-08 00:00:00', '2016-01-20 23:59:59', 'title A',
             'description A', '"location":"location A"', 'PUBLIC', 0, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_2', 'ID_2', FALSE, '2016-01-05 08:00:00', '2016-01-21 16:50:00', 'title B',
             'description B', '"location":"location B"', 'PUBLIC', 0, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_3', 'ID_1', FALSE, '2016-01-08 18:30:00', '2016-01-22 13:38:22', 'title C',
             'description C', '"location":"location C"', 'PUBLIC', 1, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_4', 'ID_3', FALSE, '2016-01-01 00:00:00', '2016-01-23 00:00:00', 'title D',
             'description D',
             '"location":"location D"', 'PRIVATE', 0, '2016-07-29T16:50:00Z', '0', '2016-07-29T16:50:00Z', '0',
   0),
  ('ID_E_5', 'ID_1', FALSE, '2016-01-09 00:00:00', '2016-01-24 00:00:00', 'title E',
             'description E',
             '"location":"location E"', 'PUBLIC', 1, '2016-07-29T16:50:00Z', '0', '2016-07-29T16:55:00Z', '1',
   1);