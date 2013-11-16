CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);
CREATE TABLE SB_Publication_Publi
(
	pubId			int		NOT NULL ,
	infoId			varchar (50) 	NULL ,
	pubName			varchar (400)	NOT NULL ,
	pubDescription		varchar (2000)	NULL ,
	pubCreationDate		varchar (10)	NOT NULL ,
	pubBeginDate		varchar (10)	NOT NULL ,
	pubEndDate		varchar (10)	NOT NULL ,
	pubCreatorId		varchar (100)	NOT NULL ,
	pubImportance		int		NULL ,
	pubVersion		varchar (100)	NULL ,
	pubKeywords		varchar (1000)	NULL ,
	pubContent		varchar (2000)	NULL ,
	pubStatus		varchar (100)	NULL ,
	pubUpdateDate		varchar (10)	NULL ,
	instanceId		varchar (50)	NOT NULL ,
	pubUpdaterId            varchar (100)	NULL ,
	pubValidateDate		varchar (10)	NULL ,
	pubValidatorId		varchar (50)	NULL ,
	pubBeginHour		varchar (5)	NULL ,
	pubEndHour		varchar (5)	NULL ,
	pubAuthor		varchar (50)	NULL,
	pubTargetValidatorId	varchar (50)	NULL,
	pubCloneId		int		DEFAULT (-1),
	pubCloneStatus		varchar (50)	NULL,
	lang			char(2)		NULL,
	pubdraftoutdate		varchar (10)	NULL
);

CREATE TABLE SB_Publication_PubliFather
(
	pubId		int		NOT NULL ,
	nodeId		int		NOT NULL ,
	instanceId	varchar (50)	NOT NULL,
	aliasUserId	int,
	aliasDate	varchar (20),
	pubOrder	int		DEFAULT (0) NULL
);

