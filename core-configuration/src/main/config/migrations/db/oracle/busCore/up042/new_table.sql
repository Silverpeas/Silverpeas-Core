CREATE TABLE SB_Contribution_Tracking
(
    id                  VARCHAR(40)  NOT NULL,
    context             VARCHAR(255) DEFAULT '',
    contrib_id          VARCHAR(40)  NOT NULL,
    contrib_type        VARCHAR(40)  NOT NULL,
    contrib_instanceId  VARCHAR(50)  NOT NULL,
    action_type         VARCHAR(20)  NOT NULL,
    action_date         TIMESTAMP    NOT NULL,
    action_by           VARCHAR(50)  NOT NULL,
    CONSTRAINT PK_CONTRIBUTION_TRACKING PRIMARY KEY (id)
);

CREATE INDEX IDX_CONTRIBUTION_TRACKING
    ON SB_Contribution_Tracking (contrib_id, contrib_type, contrib_instanceId);