CREATE TABLE SB_Statistic_History
(
    dateStat		varchar(10)		NOT NULL,
    heureStat		varchar(10)		NOT NULL,
    userId		varchar(100)		NOT NULL,
    objectId		int			NOT NULL,
    componentId		varchar(50)		NOT NULL,
    actionType		int			NOT NULL,
    objectType		varchar(50)	        NOT NULL    
);									

INSERT INTO SB_Statistic_History (dateStat, heureStat, userId, objectId, componentId, actionType, objectType)
	SELECT pubHistory.historyDate, '00:00', pubHistory.historyActorId, pubHistory.pubId, publi.instanceId, 1, 'Publication' 
	FROM SB_Publication_History pubHistory, SB_Publication_Publi publi
	WHERE pubHistory.pubId = publi.pubId;

DROP TABLE SB_Publication_History;
