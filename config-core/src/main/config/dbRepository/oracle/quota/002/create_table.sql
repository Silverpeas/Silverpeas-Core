CREATE TABLE st_quota (
   id number(19,0) NOT NULL ,
   quotaType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   minCount number(19,0) NOT NULL ,
   maxCount number(19,0) NOT NULL ,
   currentCount number(19,0) NOT NULL ,
   saveDate timestamp NOT NULL
);