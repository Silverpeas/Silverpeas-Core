create table PdcAxisValue (
  id numeric(19,0) not null,
  axisId numeric(19,0) null,
  primary key (id)
);

create table PdcClassification (
  id number(19,0) not null,
  contentId varchar2(255 char),
  instanceId varchar2(255 char) not null,
  modifiable number(1,0) not null,
  nodeId varchar2(255 char),
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
  axisValues_id number(19,0) not null,
  primary key (PdcPosition_id, axisValues_id)
);
