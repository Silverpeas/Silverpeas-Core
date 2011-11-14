alter table PdcClassification_PdcPosition 
  add constraint FK_PdcClassification_PdcPosition_PositionId
  foreign key (positions_id) 
  references PdcPosition;

alter table PdcClassification_PdcPosition 
  add constraint FK_PdcClassification_PdcPosition_PositionId_PdcClassificationId
  foreign key (PdcClassification_id) 
  references PdcClassification;

alter table PdcPosition_PdcAxisValue 
  add constraint FK_PdcPosition_PdcAxisValue_PdcAxisValueId
  foreign key (axisValues_valueId, axisValues_axisId) 
  references PdcAxisValue;

alter table PdcPosition_PdcAxisValue 
  add constraint FK_PdcPosition_PdcAxisValue_PdcPositionId
  foreign key (PdcPosition_id) 
  references PdcPosition;