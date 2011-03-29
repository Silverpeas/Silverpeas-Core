CREATE TABLE Personalization (
	id varchar(100) NOT NULL ,
	languages varchar(100) NULL,
	look varchar(50) NULL,
	personalWSpace varchar(50) NULL,
	thesaurusStatus int NOT NULL,
	dragAndDropStatus int DEFAULT 1,
	onlineEditingStatus int DEFAULT 1,
    webdavEditingStatus int DEFAULT 0,
    menuDisplay varchar(50) DEFAULT 'DISABLE',
);

ALTER TABLE Personalization  ADD 
	CONSTRAINT PK_Personalization PRIMARY KEY   
	(
		id
	);