CREATE TABLE st_token (
   id bigint NOT NULL ,
   tokenType varchar(50) NOT NULL ,
   resourceId varchar(50) NOT NULL ,
   token varchar(50) NOT NULL ,
   saveCount int NOT NULL ,
   saveDate datetime NOT NULL
);