CREATE TABLE SB_Pdc_Axis
(
	id			int		NOT NULL ,
	RootId			int		NOT NULL ,
	Name			varchar (255)	NOT NULL ,
	AxisType		char(1)		NOT NULL ,
	AxisOrder		int		NOT NULL ,
	creationDate		varchar (10)	NULL ,
	creatorId		varchar (255)	NULL 
)
;

CREATE TABLE SB_Pdc_Utilization
(
	id			int		NOT NULL ,
	instanceId		varchar (100)	NOT NULL ,
	axisId			int		NOT NULL ,
	baseValue		int		NOT NULL ,
	mandatory		int		NOT NULL ,
	variant			int		NOT NULL
)
;