ALTER TABLE SB_Publication_Publi
ADD COLUMN pubCloneId int
;
ALTER TABLE SB_Publication_Publi 
ALTER COLUMN pubCloneId set default -1
;
UPDATE SB_Publication_Publi 
SET pubCloneId=-1
;

ALTER TABLE SB_Publication_Publi
ADD pubCloneStatus varchar(50) NULL
;

CREATE TABLE SB_SeeAlso_Link 
(
	id			int		NOT NULL,
	objectId		int		NOT NULL,
	objectInstanceId	varchar (50)	NOT NULL,
	targetId		int		NOT NULL,
	targetInstanceId	varchar (50)	NOT NULL 
)
;

ALTER TABLE SB_SeeAlso_Link  ADD 
	 CONSTRAINT PK_SeeAlso_Link PRIMARY KEY   
	(
		id
	)   
;