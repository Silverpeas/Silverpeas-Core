INSERT INTO sb_calendar
  (id, instanceId, title, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_3', 'instance_A', 'title 3', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_2', 'instance_B', 'title 2', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_1', 'instance_A', 'title 1', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:55:00Z', '1', 1);

INSERT INTO sb_calendar_event
(id, calendarId, inDays, startDate, endDate, title, description, location, visibility, priority, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_E_1', 'ID_3', TRUE, '2016-01-08T00:00:00Z', '2016-01-20T23:59:59Z', 'title A',
             'description A', 'location A', 'PUBLIC', 0, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_2', 'ID_2', FALSE, '2016-01-05T08:00:00Z', '2016-01-21T16:50:00Z', 'title B',
             'description B', 'location B', 'PUBLIC', 0, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_3', 'ID_1', FALSE, '2016-01-08T18:30:00Z', '2016-01-22T13:38:22Z', 'title C',
             'description C', 'location C', 'PUBLIC', 1, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_4', 'ID_3', FALSE, '2016-01-01T00:00:00Z', '2016-01-23T00:00:00Z', 'title D',
             'description D',
             'location D', 'PRIVATE', 0, '2016-07-29T16:50:00Z', '0', '2016-07-29T16:50:00Z', '0',
   0),
  ('ID_E_5', 'ID_1', FALSE, '2016-01-09T00:00:00Z', '2016-01-24T00:00:00Z', 'title E',
             'description E',
             'location E', 'PUBLIC', 1, '2016-07-29T16:50:00Z', '0', '2016-07-29T16:55:00Z', '1',
   1);