ALTER TABLE SB_Workflow_ActiveState 	
MODIFY state varchar (50);

ALTER TABLE SB_Workflow_InterestedUser 	
MODIFY state varchar (50);

ALTER TABLE SB_Workflow_LockingUser 	
MODIFY state varchar (50);

ALTER TABLE SB_Workflow_WorkingUser 	
MODIFY state varchar (50);

ALTER TABLE SB_Workflow_ActiveState 	
ADD timeoutDate	timestamp;
