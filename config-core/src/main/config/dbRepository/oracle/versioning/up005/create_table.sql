CREATE TABLE sb_doc_readers_acl
(
  id integer  not null,
  componentid varchar(50)  not null
)
;
CREATE TABLE sb_doc_readers_acl_list
(
  id integer  not null,
  settype character(1)  not null,
  settypeid integer  not null,
  accessid integer  not null
)
;
ALTER TABLE sb_document_worklist ADD settype		character(1) DEFAULT 'U' NULL;
ALTER TABLE sb_document_worklist	ADD saved integer DEFAULT 0 NOT NULL;
ALTER TABLE sb_document_worklist	ADD used integer DEFAULT 1 NOT NULL;
ALTER TABLE sb_document_worklist	ADD listtype integer DEFAULT 0 NOT NULL;
