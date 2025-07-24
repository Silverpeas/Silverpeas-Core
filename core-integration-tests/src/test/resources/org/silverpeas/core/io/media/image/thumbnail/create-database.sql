CREATE TABLE SB_Thumbnail_Thumbnail
(
	instanceId varchar (50)	NOT NULL ,
	objectId int	NOT NULL ,
	objectType int	NOT NULL ,
	originalAttachmentName		varchar(250)  NOT NULL ,
	modifiedAttachmentName		varchar(250)  NULL ,
  mimeType		varchar(250)  NULL ,
	xStart int NULL ,
	yStart int NULL ,
	xLength	int NULL ,
	yLength int NULL
);