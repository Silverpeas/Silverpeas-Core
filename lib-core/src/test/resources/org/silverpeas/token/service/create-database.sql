/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE st_token (
   id int8 NOT NULL ,
   tokenType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   token varchar(50) NOT NULL ,
   saveCount int NOT NULL ,
   saveDate timestamp NOT NULL
);

/* Constraints */
ALTER TABLE st_token ADD CONSTRAINT const_st_token_pk PRIMARY KEY (id);

/* Indexes */
CREATE UNIQUE INDEX idx_uc_st_token ON st_token(tokenType, resourceId);