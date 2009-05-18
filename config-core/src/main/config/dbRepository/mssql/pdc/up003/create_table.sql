ALTER TABLE SB_Pdc_Axis
ADD lang char(2)
;

CREATE TABLE SB_Pdc_AxisI18N
(
	id			int		NOT NULL ,
	AxisId			int		NOT NULL ,
	lang			char(2)		NOT NULL ,
	Name			varchar (255)	NOT NULL ,
	description             varchar (1000)  NULL
)
;
