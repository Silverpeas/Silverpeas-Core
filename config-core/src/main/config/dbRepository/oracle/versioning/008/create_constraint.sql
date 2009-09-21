ALTER TABLE SB_Version_Document ADD 
	 CONSTRAINT PK_Version_Document PRIMARY KEY 
	(
		documentid
	)
;

ALTER TABLE SB_Version_Version ADD 
	 CONSTRAINT PK_Version_Version PRIMARY KEY  
	(
		versionid
	)
;

ALTER TABLE sb_doc_readers_acl ADD 
	 CONSTRAINT pk_sb_doc_readers_acl PRIMARY KEY 
	(
		id
	)
;

ALTER TABLE sb_doc_readers_acl_list ADD 
	 CONSTRAINT pk_sb_doc_readers_acl_list PRIMARY KEY  
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
