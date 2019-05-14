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
  (1, 0, '1', 'John', 'Anderton', 'john', 'U', 'VALID', '2012-01-01 00:00:00.000', 0),
  (2, 0, '2', 'Gustave', 'Eiffel', 'gustave', 'U', 'VALID', '2012-01-01 00:00:00.000', 0),
  (3, 0, '3', 'Bart', 'Simpson', 'bart', 'U', 'VALID', '2012-01-01 00:00:00.000', 0);

INSERT INTO st_space
(id, domainfatherid, name, description, createdby, firstpagetype, firstpageextraparam, ordernum, createtime, updatetime, removetime, spacestatus, updatedby, removedby, lang, isinheritanceblocked, look, displayspacefirst, ispersonal)
VALUES
  (0, null, 'Space for Integration Tests', '', 0, 0, '', 0, '1433237260318', '1443423990640', null, null, 0, null, 'fr', 0 , null, 1, null);

INSERT INTO st_componentinstance
(id, spaceid, name, componentname, description, createdby, ordernum, createtime, updatetime, removetime, componentstatus, updatedby, removedby, ispublic, ishidden, lang, isinheritanceblocked)
VALUES
  (24, 0, 'Workflow 24', 'workflow', '', 0, 1,
       '1433237280246', '1443424995948', null, null, 1, null, 1, 0, 'fr', 0),
  (42, 0, 'Workflow 42', 'workflow', '', 0, 1,
       '1433237280246', '1443424995948', null, null, 1, null, 1, 0, 'fr', 0);

INSERT INTO SB_Workflow_Replacements
(id, incumbentId, substituteId, workflowId, startDate, endDate, inDays, createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES
  ('c550ffb1-6e76-4947-9fe3-b69777d758b6', '1', '2', 'workflow42', '2018-04-09', '2018-04-13', true, '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('92b0fa2f-3287-4f99-a9b7-16424f82d607', '1', '2', 'workflow24', '2018-04-09', '2018-04-13', true, '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('64c8e712-e48a-4c63-b768-a5385f30a1ae', '1', '3', 'workflow42', '2018-04-09', '2018-04-13', true, '2018-03-09 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('62c4e5e4-73bc-47de-8e32-2717386b0278', '2', '1', 'workflow42', '2018-07-05', '2018-07-25', true, '2018-03-23 10:43:00.000', '1', '2018-03-23 10:43:00.000', '1', 0),
  ('dfbd67fa-6ee7-477f-ab52-8d973d3c8c60', '3', '2', 'workflow42', '2018-07-05', '2018-07-25', true, '2018-05-18 18:30:00.000', '1', '2018-05-18 18:30:00.000', '1', 0)