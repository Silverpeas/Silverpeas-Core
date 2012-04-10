ALTER TABLE SB_Pdc_Axis ADD 
	 CONSTRAINT PK_Pdc_Axis PRIMARY KEY  CLUSTERED 
	(
		id
	)
;

ALTER TABLE SB_Pdc_Utilization ADD 
	 CONSTRAINT PK_Pdc_Utilization PRIMARY KEY  CLUSTERED 
	(
		id
	)
;

ALTER TABLE SB_Pdc_AxisI18N ADD 
	 CONSTRAINT PK_Pdc_AxisI18N PRIMARY KEY  CLUSTERED 
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

ALTER TABLE PdcAxisValue ADD CONSTRAINT PK_PdcAxisValue PRIMARY KEY  CLUSTERED (valueId, axisId);

ALTER TABLE PdcClassification ADD CONSTRAINT PK_PdcClassification PRIMARY KEY  CLUSTERED (id);

ALTER TABLE PdcClassification_PdcPosition ADD CONSTRAINT PK_PdcClassification_PdcPosition PRIMARY KEY CLUSTERED (PdcClassification_id, positions_id);
ALTER TABLE PdcClassification_PdcPosition ADD CONSTRAINT UQ_PdcClassification_PdcPosition UNIQUE (positions_id);

ALTER TABLE PdcPosition ADD CONSTRAINT PK_PdcPosition PRIMARY KEY CLUSTERED (id);

ALTER TABLE PdcPosition_PdcAxisValue ADD CONSTRAINT PK_PdcPosition_PdcAxisValue PRIMARY KEY CLUSTERED (PdcPosition_id, axisValues_valueId, axisValues_axisId);

alter table PdcClassification_PdcPosition 
  add constraint FK_PdcClassification_PdcPosition_PositionId
  foreign key (positions_id) 
  references PdcPosition;

alter table PdcClassification_PdcPosition 
  add constraint FK_PdcClassification_PdcPosition_PositionId_PdcClassificationId
  foreign key (PdcClassification_id) 
  references PdcClassification;

alter table PdcPosition_PdcAxisValue 
  add constraint FK_PdcPosition_PdcAxisValue_AxisValuesId
  foreign key (axisValues_valueId, axisValues_axisId) 
  references PdcAxisValue;

alter table PdcPosition_PdcAxisValue 
  add constraint FK_PdcPosition_PdcAxisValue_PdcPositionId
  foreign key (PdcPosition_id) 
  references PdcPosition;