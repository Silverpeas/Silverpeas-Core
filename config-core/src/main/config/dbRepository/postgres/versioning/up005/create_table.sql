CREATE TABLE sb_doc_readers_acl
(
  id integer NOT NULL,
  componentid character varying(50) NOT NULL
)
;
CREATE TABLE sb_doc_readers_acl_list
(
  id integer NOT NULL,
  settype character(1) NOT NULL,
  settypeid integer NOT NULL,
  accessid integer NOT NULL
)
;
ALTER TABLE sb_document_worklist 
	ADD settype		character(1) NULL DEFAULT 'U',
	ADD saved integer NOT NULL DEFAULT 0,
	ADD used integer NOT NULL DEFAULT 1,
	ADD listtype integer NOT NULL DEFAULT 0
;