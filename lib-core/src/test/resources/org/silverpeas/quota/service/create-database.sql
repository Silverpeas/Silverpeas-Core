/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);

CREATE TABLE st_quota (
   id int8 NOT NULL ,
   quotaType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   minCount int8 NOT NULL ,
   maxCount int8 NOT NULL ,
   currentCount int8 NOT NULL ,
   saveDate timestamp NOT NULL
);

/* Constraints */
ALTER TABLE st_quota ADD CONSTRAINT const_st_quota_pk PRIMARY KEY (id);

/* Indexes */
CREATE UNIQUE INDEX idx_uc_st_quota ON st_quota(quotaType, resourceId);