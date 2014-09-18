CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

ALTER TABLE UniqueId  ADD
	CONSTRAINT PK_UniqueId PRIMARY KEY
	(
		tableName
	);

CREATE TABLE sb_comment_comment
	(
	commentId int not null,
	commentOwnerId int not null,
	commentCreationDate char (10) not null,
	commentModificationDate char (10),
	commentComment varchar (2000) not null,
	foreignId int not null,
	instanceId varchar (50) not null
	);

ALTER TABLE sb_comment_comment  ADD
	 CONSTRAINT PK_Comment_Comment PRIMARY KEY
	(
		commentid
	);