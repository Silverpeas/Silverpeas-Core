-- Admin data (users and the access level definition) required by the Silverpeas Calendar Engine to work
INSERT INTO st_accesslevel (id, name) VALUES
  ('U', 'User'),
  ('A', 'Administrator');

INSERT INTO st_user
  (id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate, notifManualReceiverLimit)
VALUES
  (0, 0, '0', NULL, 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000', 0),
  (1, 0, '1', 'Toto', 'Chez-les-Papoos', 'toto', 'U', 'VALID', '2012-01-01 00:00:00.000', 0),
  (2, 0, '2', 'Gustave', 'Eiffel', 'gustave', 'U', 'VALID', '2012-01-01 00:00:00.000', 0);

INSERT INTO Personalization
  (id, languages, zoneId, look, personalwspace, thesaurusstatus)
VALUES
  ('1', 'fr', 'Europe/Paris', 'Initial', '', 0),
  ('2', 'fr', 'Europe/Paris', 'Initial', '', 0);


-- Some Calendars
INSERT INTO sb_cal_calendar
  (id, instanceId, title, zoneid, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('CAL_ID_1', 'userCalendar1_PCI', 'Calendar User 1', 'UTC', '2010-07-28T16:50:00Z', '1', '2010-07-28T16:50:00Z', '1', 0),
  ('CAL_ID_2', 'userCalendar2_PCI', 'Calendar User 2', 'UTC', '2010-07-28T16:50:00Z', '2', '2010-07-28T16:55:00Z', '2', 1);

-- Some events
INSERT INTO sb_cal_components
  (id, calendarId, startDate, endDate, inDays, title, description, priority, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('ID_CMP_1', 'CAL_ID_1', '2011-07-08T12:00:00Z', '2011-07-08T13:00:00Z', FALSE, 'RDV1', 'bla blab', 0, '2011-07-01T16:50:00Z', '1', '2011-07-01T16:50:00Z', '1', 0),
  ('ID_CMP_2', 'CAL_ID_2', '2011-07-08T12:00:00Z', '2011-07-08T13:00:00Z', FALSE, 'RDV1', 'bla blab', 0, '2011-07-01T16:50:00Z', '2', '2011-07-01T16:50:00Z', '2', 0),
  ('ID_CMP_3', 'CAL_ID_1', '2011-07-09T13:00:00Z', '2011-07-09T14:00:00Z', FALSE, 'RDV3', 'bla2 blab2', 1, '2011-07-01T16:50:00Z', '1', '2011-07-01T16:50:00Z', '1', 0),
  ('ID_CMP_4', 'CAL_ID_1', '2011-07-09T07:00:00Z', '2011-07-09T08:00:00Z', FALSE, 'RDV4', 'bla4 blab4', 0, '2011-07-01T16:50:00Z', '1', '2011-07-01T16:50:00Z', '1', 0);

INSERT INTO sb_cal_event
  (id, componentId, visibility, recurrenceId)
VALUES
  ('ID_E_1', 'ID_CMP_1', 'PRIVATE', NULL),
  ('ID_E_2', 'ID_CMP_2', 'PRIVATE', NULL),
  ('ID_E_3', 'ID_CMP_3', 'PUBLIC', NULL),
  ('ID_E_4', 'ID_CMP_4', 'PUBLIC', NULL);
