CREATE TABLE st_token (
   id int8 NOT NULL ,
   tokenType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   token varchar(50) NOT NULL ,
   saveCount int NOT NULL ,
   saveDate timestamp NOT NULL
);