create table PdcAxisValue (
  valueId int8 not null,
  axisId int8 not null,
  primary key (valueId, axisId)
);

create table PdcClassification (
  id int8 not null,
  contentId varchar(255),
  instanceId varchar(255) not null,
  modifiable bool not null,
  nodeId varchar(255),
  primary key (id)
);

create table PdcClassification_PdcPosition (
  PdcClassification_id int8 not null,
  positions_id int8 not null,
  primary key (PdcClassification_id, positions_id),
  unique (positions_id)
);

create table PdcPosition (
  id int8 not null,
  primary key (id)
);

create table PdcPosition_PdcAxisValue (
  PdcPosition_id int8 not null,
  axisValues_valueId int8 not null,
  axisValues_axisId bigint not null,
  primary key (PdcPosition_id, axisValues_valueId, axisValues_axisId)
);
