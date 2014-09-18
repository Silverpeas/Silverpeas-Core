create table sb_sn_status (
  id int not null,
  userid int not null,
  creationdate timestamp  not null,
  description varchar(1000) not null
);


alter table  sb_sn_status  add
	 constraint pk_sb_sn_status primary key (
            id
         )
;

CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);