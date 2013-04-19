IF NOT EXISTS (SELECT [name] FROM sys.tables WHERE [name] = 'permalinks_document') CREATE TABLE permalinks_document 
(documentId INT NOT NULL, documentUuid VARCHAR (50)	NOT NULL, CONSTRAINT PK_permalinks_document PRIMARY KEY (documentId));
IF NOT EXISTS (SELECT [name] FROM sys.tables WHERE [name] = 'permalinks_version') CREATE TABLE permalinks_version 
(versionId	INT	NOT NULL, versionUuid VARCHAR (50)	NOT NULL, CONSTRAINT PK_permalinks_version PRIMARY KEY (versionId));