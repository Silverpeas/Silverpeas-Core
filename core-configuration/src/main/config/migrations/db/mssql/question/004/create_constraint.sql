ALTER TABLE SB_Question_Answer WITH NOCHECK ADD
	 CONSTRAINT PK_Question_Answer PRIMARY KEY  CLUSTERED
	(
		answerId
	)
;

ALTER TABLE SB_Question_Question WITH NOCHECK ADD
	 CONSTRAINT PK_Question_Question PRIMARY KEY  CLUSTERED
	(
		questionId
	)
;

ALTER TABLE SB_Question_QuestionResult WITH NOCHECK ADD
	 CONSTRAINT PK_Question_QuestionResult PRIMARY KEY  CLUSTERED
	(
		qrId
	)
;

ALTER TABLE SB_Question_Score WITH NOCHECK ADD
	 CONSTRAINT PK_Question_Score PRIMARY KEY  CLUSTERED
	(
		scoreId
	)
;