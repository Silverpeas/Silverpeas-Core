CREATE TABLE st_quota (
   id int8 NOT NULL ,
   quotaType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   minCount int8 NOT NULL ,
   maxCount int8 NOT NULL ,
   currentCount int8 NOT NULL ,
   saveDate timestamp NOT NULL
);