CREATE TABLE st_quota (
   id bigint NOT NULL ,
   quotaType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   minCount bigint NOT NULL ,
   maxCount bigint NOT NULL ,
   currentCount bigint NOT NULL ,
   saveDate datetime NOT NULL
);