CREATE TABLE st_quota (
   id int8 NOT NULL ,
   quotaType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   minCount int NOT NULL ,
   maxCount int NOT NULL ,
   currentCount int NOT NULL ,
   saveDate timestamp NOT NULL
);