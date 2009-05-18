ALTER TABLE SB_Pdc_Axis ADD 
	 CONSTRAINT PK_Pdc_Axis PRIMARY KEY   
	(
		id
	)
;

ALTER TABLE SB_Pdc_Utilization ADD 
	 CONSTRAINT PK_Pdc_Utilization PRIMARY KEY   
	(
		id
	)
;

ALTER TABLE SB_Pdc_AxisI18N ADD 
	 CONSTRAINT PK_Pdc_AxisI18N PRIMARY KEY   
	(
		id
	)
;

ALTER TABLE SB_Pdc_User_Rights 
ADD CONSTRAINT FK_Pdc_User_Rights_1 FOREIGN KEY (axisId) REFERENCES SB_Pdc_Axis(id)
;

ALTER TABLE SB_Pdc_User_Rights 
ADD CONSTRAINT FK_Pdc_User_Rights_2 FOREIGN KEY (userId) REFERENCES ST_User(id)
;

ALTER TABLE SB_Pdc_Group_Rights 
ADD CONSTRAINT FK_Pdc_Group_Rights_1 FOREIGN KEY (axisId) REFERENCES SB_Pdc_Axis(id)
;

ALTER TABLE SB_Pdc_Group_Rights 
ADD CONSTRAINT FK_Pdc_Group_Rights_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id)
;
