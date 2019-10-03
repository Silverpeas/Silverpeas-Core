CREATE TABLE sb_reminder (
  id                    VARCHAR(41) NOT NULL,
  reminderType          VARCHAR(40) NOT NULL,
  contrib_id            VARCHAR(40) NOT NULL,
  contrib_instanceId    VARCHAR(30) NOT NULL,
  contrib_type          VARCHAR(40) NOT NULL,
  userId                VARCHAR(40) NOT NULL,
  text                  VARCHAR(255),
  triggered             NUMBER(1) DEFAULT 0,
  trigger_datetime      TIMESTAMP,
  trigger_durationTime  INTEGER,
  trigger_durationUnit  VARCHAR(12),
  trigger_prop          VARCHAR(30),
  process_name          VARCHAR(200) NOT NULL,
  CONSTRAINT PK_REMINDER PRIMARY KEY (id)
);

CREATE INDEX IDX_REMINDER_CONTRIB
  ON sb_reminder (contrib_id, contrib_instanceId, contrib_type);

CREATE INDEX IDX_REMINDER_USER
  ON sb_reminder (userId);

CREATE INDEX IDX_REMINDER_CONTRIB_USER
  ON sb_reminder (contrib_id, contrib_instanceId, contrib_type, userId);