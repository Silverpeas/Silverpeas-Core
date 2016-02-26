ALTER TABLE SB_QuestionContainer_Comment WITH NOCHECK ADD
	 CONSTRAINT PK_QuestionContainer_Comment PRIMARY KEY CLUSTERED
	(
		commentId
	)
;

ALTER TABLE SB_QuestionContainer_QC WITH NOCHECK ADD
	 CONSTRAINT PK_QuestionContainer_QC PRIMARY KEY  CLUSTERED
	(
		qcId
	)
;