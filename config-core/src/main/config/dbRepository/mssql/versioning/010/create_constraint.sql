ALTER TABLE SB_Version_Document WITH NOCHECK ADD 
	 CONSTRAINT PK_Version_Document PRIMARY KEY  CLUSTERED 
	(
		documentid
	)
;

ALTER TABLE SB_Version_Version WITH NOCHECK ADD 
	 CONSTRAINT PK_Version_Version PRIMARY KEY  CLUSTERED 
	(
		versionid
	)
;

ALTER TABLE sb_doc_readers_acl ADD 
	 CONSTRAINT pk_sb_doc_readers_acl PRIMARY KEY CLUSTERED
	(
		id
	)
;

ALTER TABLE sb_doc_readers_acl_list ADD 
	 CONSTRAINT pk_sb_doc_readers_acl_list PRIMARY KEY CLUSTERED
	(
		id
	)
;

ALTER TABLE sb_doc_readers_acl_list ADD 
	 CONSTRAINT fk_sb_doc_readers_acl_list FOREIGN KEY 
	(
		accessid
	)
	REFERENCES sb_doc_readers_acl (id)
;

