CREATE TABLE permalinks_document (
	documentId	INT		NOT NULL,
	documentUuid VARCHAR (50)	NOT NULL
);

ALTER TABLE permalinks_document  ADD CONSTRAINT PK_permalinks_document PRIMARY KEY (documentId);

INSERT INTO permalinks_document (documentId, documentUuid) VALUES (1, 'ilovesilverpeas');

CREATE TABLE permalinks_version (
	versionId		INT		NOT NULL,
	versionUuid VARCHAR (50)	NOT NULL
);

ALTER TABLE permalinks_version  ADD CONSTRAINT PK_permalinks_version PRIMARY KEY (versionId);


INSERT INTO permalinks_version (versionId, versionUuid) VALUES (5, 'ilovesilverpeas');