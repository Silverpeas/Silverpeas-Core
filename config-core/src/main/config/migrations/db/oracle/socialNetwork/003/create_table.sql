create table sb_sn_invitation (
  id integer not null,
  senderid integer not null,
  receiverid integer not null,
  message varchar(1000),
  invitationdate timestamp  not null
);

create table sb_sn_relationship (
  id integer not null,
  user1id integer not null,
  user2id integer not null,
  typerelationshipid integer ,
  acceptancedate timestamp not null,
  inviterid integer not null
);

create table sb_sn_typerelationship (
  id integer not null,
  designation varchar(10)
);

create table sb_sn_status (
  id integer not null,
  userid integer not null,
  creationdate timestamp  not null,
  description varchar(1000) not null
);

create table sb_sn_externalaccount (
	profileId varchar(100) not null,
	networkId varchar(10) not null,
	silverpeasUserId varchar(50)
);
