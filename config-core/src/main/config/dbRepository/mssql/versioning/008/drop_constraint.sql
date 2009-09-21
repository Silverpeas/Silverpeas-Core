ALTER TABLE SB_Version_Document			DROP CONSTRAINT PK_Version_Document;
ALTER TABLE SB_Version_Version			DROP CONSTRAINT PK_Version_Version;
ALTER TABLE sb_doc_readers_acl_list			DROP CONSTRAINT pk_sb_doc_readers_acl_list;
ALTER TABLE sb_doc_readers_acl_list			DROP CONSTRAINT fk_sb_doc_readers_acl_list;
ALTER TABLE sb_doc_readers_acl			DROP CONSTRAINT pk_sb_doc_readers_acl;
