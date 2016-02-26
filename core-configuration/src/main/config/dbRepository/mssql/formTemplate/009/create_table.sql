CREATE TABLE SB_FormTemplate_Template
(
   templateId	int		NOT NULL,
   externalId	varchar(250)	NOT NULL,
   templateName varchar(250)	NULL
)
;

CREATE TABLE SB_FormTemplate_TemplateField
(
   templateId  int		NOT NULL,
   fieldName   varchar(50)	NOT NULL,
   fieldIndex  int		NOT NULL,
   fieldType   varchar(50)	NOT NULL,
   isMandatory smallint		DEFAULT 0,
   isReadOnly  smallint		DEFAULT 0,
   isHidden    smallint		DEFAULT 0
)
;

CREATE TABLE SB_FormTemplate_Record
(
   recordId    int		NOT NULL,
   templateId  int		NOT NULL,
   externalId  varchar(250)	NOT NULL,
   lang		char(2)
)
;

CREATE TABLE SB_FormTemplate_TextField
(
   RecordId	int		NOT NULL,
   fieldName	varchar(100)	NOT NULL,
   fieldValue	varchar(4000),
   fieldValueIndex int
)
;
