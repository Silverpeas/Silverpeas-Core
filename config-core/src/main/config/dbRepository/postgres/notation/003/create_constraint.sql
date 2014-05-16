ALTER TABLE SB_Notation_Notation
ADD CONSTRAINT PK_SB_Notation_Notation
PRIMARY KEY (id);

ALTER TABLE SB_Notation_Notation
ADD CONSTRAINT UN_SB_Notation_Notation
UNIQUE (instanceId, externalId, externalType, author);