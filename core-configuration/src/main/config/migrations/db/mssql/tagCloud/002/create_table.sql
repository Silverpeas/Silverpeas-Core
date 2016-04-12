CREATE TABLE SB_TagCloud_TagCloud
(
	id           int          not null,
	tag          varchar(100) not null,
	label        varchar(100) not null,
	instanceId   varchar(50)  not null,
	externalId   varchar(50)  not null,
	externalType int          not null
);