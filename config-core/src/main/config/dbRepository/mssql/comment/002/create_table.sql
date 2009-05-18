CREATE TABLE SB_Comment_Comment 
	(
	commentId int not null, 
	commentOwnerId int not null,
	commentCreationDate char (10) not null,
	commentModificationDate char (10),
	commentComment varchar (2000) not null,
	foreignId int not null,
	instanceId varchar (50) not null
	);
