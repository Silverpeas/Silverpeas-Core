CREATE TABLE SB_Publication_PubliFatherTMP 
(
	pubId		int		NOT NULL ,
	nodeId		int		NOT NULL ,
	instanceId	varchar (50)	NOT NULL 
);

insert into SB_Publication_PubliFatherTMP
	select pub.pubId, father.nodeId, pub.instanceId from SB_Publication_Publi pub, SB_Publication_PubliFather father
		where pub.pubId = father.pubId;

ALTER TABLE SB_Publication_PubliFather DROP CONSTRAINT PK_Publication_PubliFather;
DROP TABLE SB_Publication_PubliFather;

CREATE TABLE SB_Publication_PubliFather 
(
	pubId		int		NOT NULL ,
	nodeId		int		NOT NULL ,
	instanceId	varchar (50)	NOT NULL 
);

ALTER TABLE SB_Publication_PubliFather ADD 
	 CONSTRAINT PK_Publication_PubliFather PRIMARY KEY 
	(
		pubId,
		nodeId
	)   
;

insert into SB_Publication_PubliFather
	select pubId, nodeId, instanceId from SB_Publication_PubliFatherTMP;

drop table SB_Publication_PubliFatherTMP;