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
	variant			int		NOT NULL,
	PRIMARY KEY (id)
)
;

CREATE TABLE SB_Pdc_AxisI18N
(
	id			int		NOT NULL ,
	AxisId			int		NOT NULL ,
	lang			char(2)		NOT NULL ,
	Name			varchar (255)	NOT NULL ,
	description             varchar (1000)  NULL,
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
  valueId bigint not null,
  axisId bigint not null,
);
ALTER TABLE PdcAxisValue ADD CONSTRAINT PK_PdcAxisValue PRIMARY KEY  CLUSTERED (valueId, axisId);

create table PdcClassification (
  id bigint identity not null,
  contentId varchar(255) null,
  instanceId varchar(255) not null,
  modifiable bit not null,
  nodeId varchar(255) null
);
ALTER TABLE PdcClassification ADD CONSTRAINT PK_PdcClassification PRIMARY KEY  CLUSTERED (id);

create table PdcPosition (
  id bigint identity not null
);
ALTER TABLE PdcPosition ADD CONSTRAINT PK_PdcPosition PRIMARY KEY CLUSTERED (id);

create table PdcClassification_PdcPosition (
  PdcClassification_id bigint not null,
  positions_id bigint not null,
  constraint FK_PdcClassification_PdcPosition_PositionId foreign key (positions_id) references PdcPosition,
  constraint FK_PdcClassification_PdcPosition_PositionId_PdcClassificationId foreign key (PdcClassification_id) references PdcClassification
);
ALTER TABLE PdcClassification_PdcPosition ADD CONSTRAINT PK_PdcClassification_PdcPosition PRIMARY KEY CLUSTERED (PdcClassification_id, positions_id);
ALTER TABLE PdcClassification_PdcPosition ADD CONSTRAINT UQ_PdcClassification_PdcPosition UNIQUE (positions_id);

create table PdcPosition_PdcAxisValue (
  PdcPosition_id bigint not null,
  axisValues_valueId bigint not null,
  axisValues_axisId bigint not null,
  constraint FK_PdcPosition_PdcAxisValue_AxisValuesId foreign key (axisValues_valueId, axisValues_axisId) references PdcAxisValue,
  constraint FK_PdcPosition_PdcAxisValue_PdcPositionId foreign key (PdcPosition_id) references PdcPosition
);
ALTER TABLE PdcPosition_PdcAxisValue ADD CONSTRAINT PK_PdcPosition_PdcAxisValue PRIMARY KEY CLUSTERED (PdcPosition_id, axisValues_valueId, axisValues_axisId);
