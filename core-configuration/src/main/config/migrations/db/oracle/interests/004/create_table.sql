CREATE TABLE SB_Interests
(
	id			int		NOT NULL ,
	name			varchar (255)	NOT NULL ,
	criteria		varchar (255),
	workSpaceId		char(100),
	peasId			char(100),
	authorId		char(10),
	afterDate		varchar (10),
	beforeDate		varchar (10),
	ownerId			int		NOT NULL
)
;

CREATE TABLE SB_Interests_Axis
(
	id			int		NOT NULL ,
	icId			int		NOT NULL ,
	axisId			int		NOT NULL ,
	value			varchar (100)
)
;