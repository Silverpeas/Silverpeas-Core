CREATE TABLE SB_Notation_Notation
(
	id           int          not null,
	instanceId   varchar(50)  not null,
	externalId   varchar(50)  not null,
	externalType int          not null,
	author       varchar(50)  not null,
	note         int          not null
);