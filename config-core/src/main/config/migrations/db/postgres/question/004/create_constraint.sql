ALTER TABLE SB_Question_Answer ADD
	 CONSTRAINT PK_Question_Answer PRIMARY KEY
	(
		answerId
	)
;

ALTER TABLE SB_Question_Question ADD
	 CONSTRAINT PK_Question_Question PRIMARY KEY
	(
		questionId
	)
;

ALTER TABLE SB_Question_QuestionResult ADD
	 CONSTRAINT PK_Question_QuestionResult PRIMARY KEY
	(
		qrId
	)
;

ALTER TABLE SB_Question_Score ADD
	 CONSTRAINT PK_Question_Score PRIMARY KEY
	(
		scoreId
	)
;
