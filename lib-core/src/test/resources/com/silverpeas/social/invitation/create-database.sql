create table sb_sn_invitation (
  id int not null,
  senderid int not null,
  receiverid int not null,
  message varchar(1000),
  invitationdate timestamp  not null
);

create table sb_sn_relationship (
  id int not null,
  user1id int not null,
  user2id int not null,
  typerelationshipid int ,
  acceptancedate timestamp not null,
  inviterid int not null
);

create table sb_sn_typerelationship (
  id int not null,
  designation varchar(10)
);

create table sb_sn_status (
  id int not null,
  userid int not null,  
  creationdate timestamp  not null,
  description varchar(1000) not null
);

alter table  sb_sn_invitation  add
	 constraint pk_sb_sn_invitation primary key
	(
		id
	)
;

alter table  sb_sn_relationship  add
	 constraint pk_sb_sn_relationship primary key
	(
		id
	)
;


alter table  sb_sn_typerelationship  add
	 constraint pk_sb_sn_typerelationship primary key (
            id
         )
;

alter table  sb_sn_status  add
	 constraint pk_sb_sn_status primary key (
            id
         )
;

CREATE TABLE IF NOT EXISTS UniqueId (
	maxId int NOT NULL ,
	tableName varchar(100) NOT NULL
);