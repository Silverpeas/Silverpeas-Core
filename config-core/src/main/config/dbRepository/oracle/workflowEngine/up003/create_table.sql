ALTER TABLE SB_Workflow_InterestedUser
ADD usersrole varchar (50) NULL
;
ALTER TABLE SB_Workflow_InterestedUser
MODIFY userId varchar (50) NULL
;

ALTER TABLE SB_Workflow_WorkingUser
ADD usersrole varchar (50) NULL
;
ALTER TABLE SB_Workflow_WorkingUser
MODIFY userId varchar (50) NULL
;
