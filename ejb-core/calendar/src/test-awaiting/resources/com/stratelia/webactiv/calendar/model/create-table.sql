CREATE TABLE calendarJournal (
	id int NOT NULL ,
	name varchar(2000) NOT NULL ,
	description varchar(4000) NULL ,
	delegatorId varchar(100) NOT NULL ,
	startDay varchar(50) NOT NULL ,
	endDay varchar(50) NULL ,
	startHour varchar(50) NULL ,
	endHour varchar(50) NULL ,
	classification varchar(20) NULL ,
	priority int NULL ,
	lastModification varchar(50) NULL,
	externalid varchar(50) NULL
);