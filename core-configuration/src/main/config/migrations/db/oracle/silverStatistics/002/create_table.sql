CREATE TABLE SB_Stat_Connection
(
    dateStat        varchar(10)		not null,
    userId          integer		not null,
    countConnection decimal(19)	        not null,
    duration        decimal(19)	        not null
);


CREATE TABLE SB_Stat_Access
(
    dateStat        	varchar(10)	not null,
    userId          	integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countAccess		decimal(19)     not null
);

CREATE TABLE SB_Stat_SizeDir
(
    dateStat        varchar(10)	        not null,
    fileDir         varchar(256)        not null,
    sizeDir         decimal(19)		not null
);

CREATE TABLE SB_Stat_Volume
(
    dateStat        varchar(10)		not null,
    userId          integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countVolume		decimal(19)     not null
);

CREATE TABLE SB_Stat_ConnectionCumul
(
    dateStat        varchar(10)  	not null,
    userId          integer		not null,
    countConnection decimal(19)	        not null,
    duration        decimal(19)	        not null
);


CREATE TABLE SB_Stat_AccessCumul
(
    dateStat        	varchar(10)	not null,
    userId          	integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countAccess		decimal(19)     not null
);


CREATE TABLE SB_Stat_SizeDirCumul
(
    dateStat        varchar(10)	        not null,
    fileDir         varchar(256)        not null,
    sizeDir         decimal(19)		not null
);

CREATE TABLE SB_Stat_VolumeCumul
(
    dateStat        varchar(10)		not null,
    userId          integer	        not null,
    peasType		varchar(50)     not null,
    spaceId		varchar(50)         not null,
    componentId		varchar(50)		not null,
    countVolume		decimal(19)     not null
);
