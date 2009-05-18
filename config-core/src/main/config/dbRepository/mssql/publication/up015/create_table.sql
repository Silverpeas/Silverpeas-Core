CREATE TABLE SB_Publication_Validation
(
	id				int			NOT NULL,
	pubId			int			NOT NULL,
	instanceId		varchar(50)	NOT NULL,
	userId			int			NOT NULL,
	decisionDate	varchar(20)	NOT NULL,
	decision		varchar(50)	NOT NULL
)
;

update ST_Instance_Data
set value='1' where name='targetValidation' and value='yes'
;

update ST_Instance_Data
set value='0' where name='targetValidation' and value='no'
;