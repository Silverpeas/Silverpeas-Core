ALTER TABLE SB_Pdc_Axis		DROP CONSTRAINT PK_Pdc_Axis;
ALTER TABLE SB_Pdc_Utilization	DROP CONSTRAINT PK_Pdc_Utilization;
ALTER TABLE SB_Pdc_AxisI18N	DROP CONSTRAINT PK_Pdc_AxisI18N;

alter table PdcPosition_PdcAxisValue drop FK_PdcAxisValue_1;
alter table PdcPosition_PdcAxisValue drop FK_PdcAxisValue_2;
alter table PdcClassification_PdcPosition drop constraint FK_PdcClass_PdcPos_1;
alter table PdcClassification_PdcPosition drop constraint FK_PdcClass_PdcPos_2;
