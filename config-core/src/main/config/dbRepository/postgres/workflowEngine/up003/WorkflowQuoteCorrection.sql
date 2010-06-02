ALTER table "sb_workflow_ProcessInstance" rename to sb_workflow_processinstance;
ALTER table "sb_workflow_ActiveState" rename to sb_workflow_activestate;
ALTER table "sb_workflow_HistoryStep" rename to sb_workflow_historystep;
ALTER table "sb_workflow_Undo_Step" rename to sb_workflow_undo_step;
ALTER table "sb_workflow_InterestedUser" rename to sb_workflow_interesteduser;
ALTER table "sb_workflow_LockingUser" rename to sb_workflow_lockinguser;
ALTER table "sb_workflow_WorkingUser" rename to sb_workflow_workinguser;
ALTER table "sb_workflow_Question" rename to sb_workflow_question;
ALTER table "sb_workflow_UserInfo" rename to sb_workflow_userInfo;
ALTER table "sb_workflow_UserSettings" rename to sb_workflow_usersettings;
ALTER table "sb_workflow_Error" rename to sb_workflow_error;

alter table sb_workflow_interesteduser drop constraint fk_workflow_interesteduser cascade;
alter table sb_workflow_activestate drop constraint fk_workflow_activestate cascade;
alter table sb_workflow_processinstance drop constraint pk_workflow_processinstance cascade;

alter table sb_workflow_processinstance rename "instanceId" to instanceid;
alter table sb_workflow_processinstance rename "modelId" to modelId;
alter table sb_workflow_processinstance rename "errorStatus" to errorStatus;
alter table sb_workflow_processinstance rename "timeoutStatus" to timeoutStatus;

alter table sb_workflow_activestate rename "instanceId" to instanceid;
alter table sb_workflow_activestate rename "backStatus" to backStatus;
alter table sb_workflow_activestate rename "timeoutStatus" to timeoutStatus;
alter table sb_workflow_processinstance add constraint pk_workflow_processinstance PRIMARY KEY (instanceId);
alter table sb_workflow_activestate add constraint fk_workflow_activestate FOREIGN KEY (instanceId)
      REFERENCES sb_workflow_processinstance (instanceId);

alter table sb_workflow_historystep rename "instanceId" to instanceId;
alter table sb_workflow_historystep rename "userId" to userId;
alter table sb_workflow_historystep rename "userRoleName" to userRoleName;
alter table sb_workflow_historystep rename "actionDate" to actionDate;
alter table sb_workflow_historystep rename "resolvedState" to resolvedState;
alter table sb_workflow_historystep rename "toState" to toState;
alter table sb_workflow_historystep rename "actionStatus" to actionStatus;
alter table sb_workflow_historystep add constraint fk_workflow_historystep FOREIGN KEY (instanceId)
      REFERENCES sb_workflow_processinstance (instanceId);


alter table sb_workflow_undo_step rename "stepId" to stepId;
alter table sb_workflow_undo_step rename "instanceId" to instanceId;

alter table sb_workflow_interesteduser rename "userId" to userId;
alter table sb_workflow_interesteduser rename "instanceId" to instanceId;
alter table sb_workflow_interesteduser add constraint fk_workflow_interesteduser FOREIGN KEY (instanceId)
      REFERENCES sb_workflow_processinstance (instanceId);

alter table sb_workflow_lockinguser rename "userId" to userId;
alter table sb_workflow_lockinguser rename "instanceId" to instanceId;
alter table sb_workflow_lockinguser rename "lockDate" to lockDate;
ALTER TABLE sb_workflow_lockinguser ADD 
	CONSTRAINT fk_workflow_lockinguser
	FOREIGN KEY ( instanceId )
	REFERENCES sb_workflow_processinstance ( instanceId );
	
alter table sb_workflow_workinguser rename "userId" to userId;
alter table sb_workflow_workinguser rename "instanceId" to instanceId;
ALTER TABLE sb_workflow_workinguser ADD 
	CONSTRAINT fk_workflow_workinguser
	FOREIGN KEY ( instanceId )
	REFERENCES sb_workflow_processinstance ( instanceId );

alter table sb_workflow_question rename "instanceId" to instanceId;
alter table sb_workflow_question rename "questionText" to questionText;
alter table sb_workflow_question rename "responseText" to responseText;
alter table sb_workflow_question rename "questionDate" to questionDate;
alter table sb_workflow_question rename "responseDate" to responseDate;
alter table sb_workflow_question rename "fromState" to fromState;
alter table sb_workflow_question rename "targetState" to targetState;
alter table sb_workflow_question rename "fromUserId" to fromUserId;
alter table sb_workflow_question rename "toUserId" to toUserId;
ALTER TABLE sb_workflow_question ADD 
	CONSTRAINT fk_workflow_question
	FOREIGN KEY ( instanceId )
	REFERENCES sb_workflow_processinstance ( instanceId );


alter table sb_workflow_userInfo drop constraint fk_workflow_userinfo cascade;
alter table sb_workflow_usersettings drop constraint pk_workflow_usersettings cascade;
alter table sb_workflow_userInfo rename "settingsId" to settingsId;
alter table sb_workflow_usersettings rename "settingsId" to settingsId;
alter table sb_workflow_usersettings rename "userId" to userId;
alter table sb_workflow_usersettings rename "peasId" to peasId;
alter table sb_workflow_usersettings add constraint pk_workflow_usersettings PRIMARY KEY (settingsId);
alter table sb_workflow_userInfo add constraint fk_workflow_userinfo FOREIGN KEY (settingsId)
      REFERENCES sb_workflow_usersettings (settingsId);


alter table sb_workflow_error rename "instanceId" to instanceId;
alter table sb_workflow_error rename "stepId" to stepId;
alter table sb_workflow_error rename "errorMessage" to errorMessage;
alter table sb_workflow_error rename "stackTrace" to stackTrace;
alter table sb_workflow_error rename "userId" to userId;
alter table sb_workflow_error rename "actionName" to actionName;
alter table sb_workflow_error rename "actionDate" to actionDate;
alter table sb_workflow_error rename "userRole" to userRole;
alter table sb_workflow_error rename "stateName" to stateName;