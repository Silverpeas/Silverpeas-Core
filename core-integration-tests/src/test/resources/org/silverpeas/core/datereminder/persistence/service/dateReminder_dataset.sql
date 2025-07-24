INSERT INTO st_dateReminder (id, resourceType, resourceId, dateReminder, message, processStatus,
  createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES ('dateReminer_4', 'TYPE_THAT_DOESNT_EXIST', '38', '2015-07-08 00:00:00.0', 'unknown',
  0, '2015-07-21 17:34:00.0', '1', '2015-07-21 17:34:00.0', '1', 0);

INSERT INTO st_dateReminder (id, resourceType, resourceId, dateReminder, message, processStatus,
  createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES ('dateReminer_24', 'STRING', '38', '2015-07-08 00:00:00.0',
  'Modifier le contenu de la publication', 0, '2015-07-21 17:34:00.0', '1',
  '2015-07-21 17:34:00.0', '1', 0);

INSERT INTO st_dateReminder (id, resourceType, resourceId, dateReminder, message, processStatus,
  createDate, createdBy, lastUpdateDate, lastUpdatedBy, version)
VALUES ('dateReminer_6', 'STRING', '7', '2015-07-08 00:00:00.0',
  'Modifier la version de la publication', 0, '2015-07-21 17:34:00.0', '1',
  '2015-07-21 17:34:00.0', '1', 0);

INSERT INTO st_domain (id, name, description, propFileName, className, authenticationServer)
VALUES (-1, 'internal', 'Do not remove - Used by Silverpeas engine', '-', '-', '-');

INSERT INTO st_domain (id, name, description, propFileName, className, authenticationServer)
VALUES (0, 'domainSilverpeas', 'default domain for Silverpeas',
        'org.silverpeas.domains.domainSP',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP');

INSERT INTO st_user (id, domainid, specificid, firstname, lastname, email, login, loginmail, accesslevel, state, stateSaveDate)
VALUES (0, 0, '0', '', 'Administrateur', 'cecile.bonin@silverpeas.com', 'SilverAdmin', '', 'A', 'VALID',
        '2012-01-01 00:00:00.0');
