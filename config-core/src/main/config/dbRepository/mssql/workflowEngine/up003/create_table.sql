ALTER TABLE SB_Workflow_InterestedUser
ADD usersrole varchar (50) NULL
;
ALTER TABLE SB_Workflow_InterestedUser
ALTER COLUMN userId varchar (50) NULL
;

ALTER TABLE SB_Workflow_WorkingUser
ADD usersrole varchar (50) NULL
;
ALTER TABLE SB_Workflow_WorkingUser
ALTER COLUMN userId varchar (50) NULL
;
