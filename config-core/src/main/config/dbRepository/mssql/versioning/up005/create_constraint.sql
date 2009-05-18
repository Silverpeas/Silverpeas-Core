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

