ALTER TABLE st_notificationresource
  ADD attachmentTargetId VARCHAR(500) NULL;

ALTER TABLE sb_workflow_historystep
  ADD COLUMN substituteId VARCHAR(40);