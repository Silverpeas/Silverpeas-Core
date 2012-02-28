create table PdcAxisValue (
  valueId numeric(19,0) not null,
  axisId numeric(19,0) not null,
  primary key (valueId, axisId)
);

create table PdcClassification (
  id numeric(19,0) IDENTITY(1,1) not null,
  contentId varchar(255),
  instanceId varchar(255) not null,
  modifiable numeric(1,0) not null,
  nodeId varchar(255),
  primary key (id)
);

create table PdcClassification_PdcPosition (
  PdcClassification_id numeric(19,0) not null,
  positions_id numeric(19,0) not null,
  primary key (PdcClassification_id, positions_id),
  unique (positions_id)
 );

create table PdcPosition (
  id numeric(19,0) IDENTITY(1,1) not null,
  primary key (id)
);

create table PdcPosition_PdcAxisValue (
  PdcPosition_id numeric(19,0) not null,
  axisValues_valueId numeric(19,0) not null,
  axisValues_axisId numeric(19,0) not null,
  primary key (PdcPosition_id, axisValues_valueId, axisValues_axisId)
);
