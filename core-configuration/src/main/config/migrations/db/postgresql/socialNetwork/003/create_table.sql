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

CREATE TABLE sb_sn_externalaccount (
	profileId varchar(100) NOT NULL ,
	networkId varchar(10) not NULL,
	silverpeasUserId varchar(50) NULL
);