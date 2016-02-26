CREATE INDEX IN_SB_Pdc_Axis_1 ON SB_Pdc_Axis(AxisType);

CREATE INDEX IN_SB_Pdc_Utilization_1 ON SB_Pdc_Utilization(baseValue);
CREATE INDEX IN_SB_Pdc_Utilization_2 ON SB_Pdc_Utilization(instanceId);

create index IDX_PdcClassification_InstanceId on PdcClassification(instanceId);
create index IDX_PdcClassification_ContentId on PdcClassification(contentId);
