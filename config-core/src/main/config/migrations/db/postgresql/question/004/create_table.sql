CREATE TABLE SB_Question_Answer
(
	answerId		int		NOT NULL ,
	questionId		int		NOT NULL ,
	answerLabel		varchar (1000) NULL,
	answerNbPoints		int		NULL ,
	answerIsSolution	int		NOT NULL ,
	answerComment		varchar (2000)	NULL ,
	answerNbVoters		int		NOT NULL ,
	answerIsOpened		int		NOT NULL ,
	answerImage		varchar (2000)	NULL ,
	answerQuestionLink	varchar (100)	NULL
)
;

CREATE TABLE SB_Question_Question
(
	questionId		int		NOT NULL ,
	qcId			int		NOT NULL ,
	questionLabel		varchar (1000)	NOT NULL ,
	questionDescription	varchar (2000)	NULL ,
	questionClue		varchar (2000)	NULL ,
	questionImage		varchar (1000)	NULL ,
	questionIsQCM		int		NOT NULL ,
	questionType		int		NOT NULL ,
	questionIsOpen		int		NOT NULL ,
	questionCluePenalty	int		NOT NULL ,
	questionMaxTime		int		NOT NULL ,
	questionDisplayOrder	int		NOT NULL ,
	questionNbPointsMin	int		NOT NULL ,
	questionNbPointsMax	int		NOT NULL ,
	instanceId		varchar (50)	NOT NULL ,
	style			varchar (50)	NULL
)
;

CREATE TABLE SB_Question_QuestionResult
(
	qrId			int		NOT NULL ,
	questionId		int		NOT NULL ,
	userId			varchar (100)	NOT NULL ,
	answerId		int		NOT NULL ,
	qrOpenAnswer		varchar (2000)	NULL ,
	qrNbPoints		int		NOT NULL ,
	qrPollDate		varchar (10)	NOT NULL ,
	qrElapsedTime		varchar (100)	NULL ,
	qrParticipationId	int		NULL
)
;

CREATE TABLE SB_Question_Score
(
	scoreId			int		NOT NULL ,
	qcId			int		NOT NULL ,
	userId			varchar (100)	NOT NULL ,
	scoreParticipationId	int		NOT NULL ,
	scoreScore		int		NOT NULL ,
	scoreElapsedTime	varchar (100)	NULL ,
	scoreParticipationDate	varchar (10)	NOT NULL ,
	scoreSuggestion		varchar (2000)	NULL
)
;
