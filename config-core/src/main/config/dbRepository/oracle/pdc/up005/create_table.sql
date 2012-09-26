create table PdcAxisValue (
  valueId number(19,0) not null,
  axisId number(19,0) not null,
  primary key (valueId, axisId)
);

create table PdcClassification (
  id number(19,0) not null,
  contentId varchar2(255),
  instanceId varchar2(255) not null,
  modifiable number(1,0) not null,
  nodeId varchar2(255),
  primary key (id)
);

create table PdcClassification_PdcPosition (
  PdcClassification_id number(19,0) not null,
  positions_id number(19,0) not null,
  primary key (PdcClassification_id, positions_id),
  unique (positions_id)
 );

create table PdcPosition (
  id number(19,0) not null,
  primary key (id)
);

create table PdcPosition_PdcAxisValue (
  PdcPosition_id number(19,0) not null,
  axisValues_valueId number(19,0) not null,
  axisValues_axisId number(19,0) not null,
  primary key (PdcPosition_id, axisValues_valueId, axisValues_axisId)
);
