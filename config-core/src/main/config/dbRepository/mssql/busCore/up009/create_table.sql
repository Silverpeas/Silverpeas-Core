insert into ST_AccessLevel(id, name) values ('D', 'DomainManager');

CREATE TABLE ST_LongText (
	id int NOT NULL ,
	orderNum int NOT NULL ,
	bodyContent nvarchar(2000) NOT NULL 
);

ALTER TABLE ST_LongText WITH NOCHECK ADD 
	CONSTRAINT PK_ST_LongText PRIMARY KEY  CLUSTERED 
	(
		id,
		orderNum
	);
