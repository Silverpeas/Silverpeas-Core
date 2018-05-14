INSERT INTO ST_AccessLevel (id, name) VALUES
  ('U', 'User'),
  ('A', 'Administrator'),
  ('G', 'Guest'),
  ('R', 'Removed'),
  ('K', 'KMManager'),
  ('D', 'DomainManager');

INSERT INTO ST_User
(id, domainId, specificId, firstName, lastName, login, accessLevel, state, stateSaveDate, notifManualReceiverLimit)
VALUES
  (0, 0, '0', NULL, 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000', 0),
  (1, 0, '1', 'Toto', 'Chez-les-Papoos', 'toto', 'U', 'VALID', '2012-01-01 00:00:00.000', 0),
  (2, 0, '2', 'Gustave', 'Eiffel', 'gustave', 'U', 'VALID', '2012-01-01 00:00:00.000', 0),
  (3, 0, '3', 'Bart', 'Simpson', 'bart', 'U', 'VALID', '2012-01-01 00:00:00.000', 0);

INSERT INTO SB_DELEGATIONS
(id, delegatorId, delegateId, instanceId, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('c550ffb1-6e76-4947-9fe3-b69777d758b6', '1', '2', 'workflow32', '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('92b0fa2f-3287-4f99-a9b7-16424f82d607', '1', '2', 'workflow12', '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('64c8e712-e48a-4c63-b768-a5385f30a1ae', '1', '3', 'workflow32', '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('62c4e5e4-73bc-47de-8e32-2717386b0278', '2', '1', 'kmelia42', '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0)