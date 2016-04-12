CREATE TABLE SB_Comment_Comment
	(
	commentId int not null,
	commentOwnerId int not null,
	commentCreationDate char (10) not null,
	commentModificationDate char (10),
	commentComment varchar (2000) not null,
	instanceId varchar (50) not null,
	resourceType varchar (50) not null,
	resourceId varchar (50) not null
	);
