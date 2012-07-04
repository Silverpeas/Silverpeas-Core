CREATE TABLE st_domain (
	id int NOT NULL ,
	name varchar(100) not NULL,
	description varchar(400) not NULL,
	propfilename varchar(100) not NULL,
	classname varchar(100) not NULL,
	authenticationserver varchar(100) not NULL,
	thetimestamp varchar(100) not NULL,
	silverpeasserverurl varchar(400) not NULL
);


ALTER TABLE st_domain  ADD
	CONSTRAINT PK_ST_Domain PRIMARY KEY
	(
		id
	);