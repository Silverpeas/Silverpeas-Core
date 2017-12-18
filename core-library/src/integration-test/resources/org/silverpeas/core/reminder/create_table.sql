CREATE TABLE IF NOT EXISTS sb_reminder (
  id                    VARCHAR(40) NOT NULL,
  reminderType          VARCHAR(40) NOT NULL,
  contrib_id            VARCHAR(40) NOT NULL,
  contrib_instanceId    VARCHAR(30) NOT NULL,
  contrib_type          VARCHAR(40) NOT NULL,
  userId                VARCHAR(40) NOT NULL,
  text                  VARCHAR(255),
  trigger_datetime      TIMESTAMP,
  trigger_durationTime  INTEGER,
  trigger_durationUnit  VARCHAR(12),
  trigger_durationProp  VARCHAR(30),
  CONSTRAINT PK_REMINDER PRIMARY KEY (id)
);

CREATE INDEX IDX_REMINDER_USER_CONTRIB
  ON sb_reminder (userId, contrib_id, contrib_instanceId, contrib_type);