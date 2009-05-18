ALTER TABLE SB_Version_Document 
ADD typeWorkList int not null default 0
;

ALTER TABLE SB_Version_Document 
ADD currentWorkListOrder int
;

CREATE TABLE SB_Document_ReadList
	(
	documentId	int	not null,
	userId		int	not null
	);

CREATE TABLE SB_Document_WorkList
	(
	documentId	int		not null,
	userId		int		not null,
	orderBy		int		not null,
	writer		varchar(100)	null,
	approval	varchar(100)	null
	);


