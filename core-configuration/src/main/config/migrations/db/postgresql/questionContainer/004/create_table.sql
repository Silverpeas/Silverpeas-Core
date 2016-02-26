CREATE TABLE SB_QuestionContainer_Comment
(
	commentId		int		NOT NULL ,
	commentFatherId		int		NOT NULL ,
	userId			varchar (100)	NOT NULL ,
	commentComment		varchar (2000)  NULL,
	commentIsAnonymous	int		NOT NULL ,
	commentDate		varchar (10)	NOT NULL
);

CREATE TABLE SB_QuestionContainer_QC
(
	qcId					int		NOT NULL ,
	qcTitle					varchar (1000)	NOT NULL ,
	qcDescription				varchar (2000)	NULL ,
	qcComment				varchar (2000)	NULL ,
	qcCreatorId				varchar (100)	NOT NULL ,
	qcCreationDate				varchar (10)	NOT NULL ,
	qcBeginDate				varchar (10)	NOT NULL ,
	qcEndDate				varchar (10)	NOT NULL ,
	qcIsClosed				int		NOT NULL ,
	qcNbVoters				int		NOT NULL ,
	qcNbQuestionsPage			int		NOT NULL ,
	qcNbMaxParticipations			int		NULL ,
	qcNbTriesBeforeSolution			int		NULL ,
	qcMaxTime				int		NULL ,
	instanceId				varchar (50)	NOT NULL ,
	anonymous				int		NOT NULL ,
	resultMode				int		NOT NULL ,
	resultView				int		NOT NULL
);
