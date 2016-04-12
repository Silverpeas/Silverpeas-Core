CREATE TABLE SB_Statistic_History
(
    dateStat		varchar(10)		NOT NULL,
    heureStat		varchar(10)		NOT NULL,
    userId			varchar(100)	NOT NULL,
    resourceId		varchar(50)		NOT NULL,
    componentId		varchar(50)		NOT NULL,
    actionType		int				NOT NULL,
    resourceType	varchar(50)	    NOT NULL
);