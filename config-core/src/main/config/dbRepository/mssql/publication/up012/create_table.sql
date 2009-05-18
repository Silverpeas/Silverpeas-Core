ALTER TABLE SB_Publication_Publi
ADD lang char(2) NULL
;

CREATE TABLE SB_Publication_PubliI18N
(
	id		int		NOT NULL,
	pubId		int		NOT NULL,
	lang		char (2)	NOT NULL,
	name		varchar (400)	NOT NULL,
	description	varchar (2000),
	keywords	varchar (1000)
);
