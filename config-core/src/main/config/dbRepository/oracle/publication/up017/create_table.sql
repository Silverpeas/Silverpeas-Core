CREATE TABLE SB_Thumbnail_Thumbnail 
(
	instanceId		          varchar (50)	NOT NULL ,
	objectId              	          int	NOT NULL ,
	objectType              	          int	NOT NULL ,
	originalAttachmentName		varchar(250)  NOT NULL ,
	modifiedAttachmentName		varchar(250)  NULL ,
        mimeType		varchar(250)  NULL ,
	xStart	                int NULL ,
	yStart	                int NULL ,
	xLength	                int NULL ,
	yLength	                int NULL
) 
;

ALTER TABLE SB_Thumbnail_Thumbnail ADD 
	 CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY   
	(
		objectId,
		objectType,
		instanceId
	)   
;

INSERT INTO sb_thumbnail_thumbnail (instanceId, objectId, objectType, originalAttachmentName, mimeType) 
 (SELECT instanceid, pubid, '1', pubImage, pubImageMimeType FROM SB_Publication_Publi WHERE pubImage IS NOT NULL);

ALTER TABLE SB_Publication_Publi DROP COLUMN pubImage;
ALTER TABLE SB_Publication_Publi DROP COLUMN pubImageMimeType;