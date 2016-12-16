/*
  Admin data (users and the access level definition) required by the Silverpeas Calendar Engine to
  work.
 */
INSERT INTO st_accesslevel (id, name) VALUES
  ('U', 'User'),
  ('A', 'Administrator'),
  ('G', 'Guest'),
  ('R', 'Removed'),
  ('K', 'KMManager'),
  ('D', 'DomainManager');

INSERT INTO st_user
  (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate, notifManualReceiverLimit)
VALUES
  (0, 0, '0', NULL, 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000', 0),
  (1, 0, '1', 'Toto', 'Chez-les-Papoos', 'toto', 'U', 'VALID', '2012-01-01 00:00:00.000', 0),
  (2, 0, '2', 'Gustave', 'Eiffel', 'gustave', 'U', 'VALID', '2012-01-01 00:00:00.000', 0);

/*
  Some Calendars.
 */
INSERT INTO sb_cal_calendar
  (id, instanceId, title, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_3', 'instance_A', 'title 3', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_2', 'instance_B', 'title 2', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:50:00Z', '0', 0),
  ('ID_1', 'instance_A', 'title 1', '2016-07-28T16:50:00Z', '0', '2016-07-28T16:55:00Z', '1', 1);

/*
  The events' recurrence.
 */
INSERT INTO sb_cal_recurrence (id, recur_periodInterval, recur_periodUnit, recur_count, recur_endDate)
VALUES
  ('ID_R_5', 1, 'WEEK', 8, NULL);

INSERT INTO SB_Cal_Recurrence_DayOfWeek (recurrenceId, recur_nth, recur_dayOfWeek) VALUES
  ('ID_R_5', 1, 5);

INSERT INTO SB_Cal_Recurrence_Exception (recurrenceId, recur_exceptionDate) VALUES
  ('ID_R_5', '2016-01-16T00:00:00Z'),
  ('ID_R_5', '2016-01-30T00:00:00Z');

/*
  Some events.
 */
INSERT INTO sb_cal_event
  (id, calendarId, inDays, startDate, endDate, title, description, visibility, priority, recurrenceId,
   createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_E_1', 'ID_3', TRUE, '2016-01-08T00:00:00Z', '2016-01-20T23:59:00Z', 'title A',
             'description A', 'PUBLIC', 0, NULL, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_2', 'ID_2', FALSE, '2016-01-05T08:00:00Z', '2016-01-21T16:50:00Z', 'title B',
             'description B', 'PUBLIC', 0, NULL, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_3', 'ID_1', FALSE, '2016-01-08T18:30:00Z', '2016-01-22T13:38:00Z', 'title C',
             'description C', 'PUBLIC', 1, NULL, '2016-07-29T16:50:00Z', '0',
   '2016-07-29T16:50:00Z', '0', 0),
  ('ID_E_4', 'ID_3', FALSE, '2016-01-01T00:00:00Z', '2016-01-23T00:00:00Z', 'title D',
             'description D',
             'PRIVATE', 0, NULL, '2016-07-29T16:50:00Z', '1', '2016-07-29T16:50:00Z', '1',
   0),
  ('ID_E_5', 'ID_1', TRUE, '2016-01-09T00:00:00Z', '2016-01-09T23:59:00Z', 'title E',
             'description E',
             'PUBLIC', 1, 'ID_R_5', '2016-07-29T16:50:00Z', '0', '2016-07-29T16:55:00Z', '1',
   1);

INSERT INTO sb_cal_attributes (id, name, value) VALUES
  ('ID_E_1', 'location', 'location A'),
  ('ID_E_2', 'location', 'location B'),
  ('ID_E_3', 'location', 'location C');

INSERT INTO sb_cal_attendees (id, attendeeId, eventId, type, participation, presence,
                              createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_A_E1_1', '1', 'ID_E_1', 0, 'ACCEPTED', 'OPTIONAL',
                '2016-07-29T16:50:00Z', '0', '2016-07-29T16:50:00Z', '0', 0),
  ('ID_A_E1_2', 'john.doe@silverpeas.org', 'ID_E_1', 1, 'TENTATIVE', 'REQUIRED',
                '2016-07-29T16:50:00Z', '0', '2016-07-29T16:50:00Z', '0', 0),
  ('ID_A_E5_1', '1', 'ID_E_5', 0, 'AWAITING', 'OPTIONAL',
                '2016-07-29T16:50:00Z', '0', '2016-07-29T16:50:00Z', '0', 0);

INSERT INTO SB_Cal_Attendees_PartDate (id, startDate, participation) VALUES
  ('ID_A_E5_1', '2016-01-16T00:00:00Z', 'DECLINED');