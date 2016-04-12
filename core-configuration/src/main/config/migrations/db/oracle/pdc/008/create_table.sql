CREATE TABLE SB_Pdc_Axis
(
	id			int		NOT NULL ,
	RootId			int		NOT NULL ,
	Name			varchar (255)	NOT NULL ,
	AxisType		char(1)		NOT NULL ,
	AxisOrder		int		NOT NULL ,
	creationDate		varchar (10)	NULL ,
	creatorId		varchar (255)	NULL,
	description             varchar (1000)  NULL,
	lang			char(2)		NULL,
	PRIMARY KEY (id)
)
;

CREATE TABLE SB_Pdc_Utilization
(
	id			int		NOT NULL ,
	instanceId		varchar (100)	NOT NULL ,
	axisId			int		NOT NULL ,
	baseValue		int		NOT NULL ,
	mandatory		int		NOT NULL ,
	variant			int		NOT NULL ,
	PRIMARY KEY(id)
)
;

CREATE TABLE SB_Pdc_AxisI18N
(
	id			int		NOT NULL ,
	AxisId			int		NOT NULL ,
	lang			char(2)		NOT NULL ,
	Name			varchar (255)	NOT NULL ,
	description             varchar (1000)  NULL ,
	PRIMARY KEY (id)
)
;

CREATE TABLE SB_Pdc_User_Rights
(
	axisId	int	NOT NULL,
	valueId	int	NOT NULL,
	userId	int	NOT NULL,
	CONSTRAINT FK_Pdc_User_Rights_1 FOREIGN KEY (axisId) REFERENCES SB_Pdc_Axis(id),
	CONSTRAINT FK_Pdc_User_Rights_2 FOREIGN KEY (userId) REFERENCES ST_User(id)
)
;

CREATE TABLE SB_Pdc_Group_Rights
(
	axisId	int	NOT NULL,
	valueId	int	NOT NULL,
	groupId	int	NOT NULL,
  CONSTRAINT FK_Pdc_Group_Rights_1 FOREIGN KEY (axisId) REFERENCES SB_Pdc_Axis(id),
  CONSTRAINT FK_Pdc_Group_Rights_2 FOREIGN KEY (groupId) REFERENCES ST_Group(id)
)
;

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

create table PdcPosition (
  id number(19,0) not null,
  primary key (id)
);

create table PdcClassification_PdcPosition (
  PdcClassification_id number(19,0) not null,
  positions_id number(19,0) not null,
  primary key (PdcClassification_id, positions_id),
  unique (positions_id),
  constraint FK_PdcClass_PdcPos_1 foreign key (positions_id) references PdcPosition,
  constraint FK_PdcClass_PdcPos_2 foreign key (PdcClassification_id) references PdcClassification
 );

create table PdcPosition_PdcAxisValue (
  PdcPosition_id number(19,0) not null,
  axisValues_valueId number(19,0) not null,
  axisValues_axisId number(19,0) not null,
  primary key (PdcPosition_id, axisValues_valueId, axisValues_axisId),
  constraint FK_PdcAxisValue_1 foreign key (axisValues_valueId, axisValues_axisId) references PdcAxisValue,
  constraint FK_PdcAxisValue_2 foreign key (PdcPosition_id) references PdcPosition
);
