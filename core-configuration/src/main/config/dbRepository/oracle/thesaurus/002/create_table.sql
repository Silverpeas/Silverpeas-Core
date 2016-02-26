CREATE TABLE SB_Thesaurus_Vocabulary
(
	id 			int 			NOT NULL,
	name 		varchar(100) 	NOT NULL,
	description varchar(2000) 	NOT NULL
)
;


CREATE TABLE SB_Thesaurus_Jargon
(
	id 			int 			NOT NULL,
	type 		int 			NOT NULL,
	idVoca 		int 			NOT NULL,
	idUser 		varchar(50) 	NOT NULL
)
;


CREATE TABLE SB_Thesaurus_Synonym
(
	id 			int 			NOT NULL,
	idVoca 		int 			NOT NULL,
	idTree 		int 			NOT NULL,
	idTerm 		int 			NOT NULL,
	name 		varchar(100)
)
;
