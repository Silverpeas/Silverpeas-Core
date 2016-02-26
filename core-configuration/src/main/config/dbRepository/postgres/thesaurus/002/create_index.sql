CREATE INDEX Jargon_idUser
ON SB_Thesaurus_Jargon  (idUser);


CREATE INDEX Synonym_TermVoca
ON SB_Thesaurus_Synonym  (idVoca, idTree, idTerm);
