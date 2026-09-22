CREATE INDEX IDX_CONTRIBUTION_TRACKING
    ON SB_Contribution_Tracking (contrib_id, contrib_type, contrib_instanceId);

CREATE INDEX IDX_User_2FA_Recovery_User ON ST_User_2FA_Recovery (userId);