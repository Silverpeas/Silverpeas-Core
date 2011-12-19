alter table PdcClassification_PdcPosition 
  add constraint FK_PdcClass_PdcPos_1
  foreign key (positions_id) 
  references PdcPosition;

alter table PdcClassification_PdcPosition 
  add constraint FK_PdcClass_PdcPos_2
  foreign key (PdcClassification_id) 
  references PdcClassification;

alter table PdcPosition_PdcAxisValue 
  add constraint FK_PdcAxisValue_1
  foreign key (axisValues_valueId, axisValues_axisId) 
  references PdcAxisValue;

alter table PdcPosition_PdcAxisValue 
  add constraint FK_PdcAxisValue_2
  foreign key (PdcPosition_id) 
  references PdcPosition;
