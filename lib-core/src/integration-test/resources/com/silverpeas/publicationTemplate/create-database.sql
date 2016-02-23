CREATE TABLE SB_FormTemplate_Template
(
  templateId   INT          NOT NULL,
  externalId   VARCHAR(250) NOT NULL,
  templateName VARCHAR(250) NULL
);

ALTER TABLE SB_FormTemplate_Template ADD
CONSTRAINT PK_FormTemplate_Template
PRIMARY KEY (templateId);
ALTER TABLE SB_FormTemplate_Template ADD
CONSTRAINT UN_FormTemplate_Template
UNIQUE (externalId);

CREATE TABLE SB_FormTemplate_TemplateField
(
  templateId  INT         NOT NULL,
  fieldName   VARCHAR(50) NOT NULL,
  fieldIndex  INT         NOT NULL,
  fieldType   VARCHAR(50) NOT NULL,
  isMandatory SMALLINT DEFAULT 0,
  isReadOnly  SMALLINT DEFAULT 0,
  isHidden    SMALLINT DEFAULT 0
);

ALTER TABLE SB_FormTemplate_TemplateField ADD
CONSTRAINT PK_FormTemplate_TemplateField
PRIMARY KEY (templateId, fieldName);
ALTER TABLE SB_FormTemplate_TemplateField ADD
CONSTRAINT UN_FormTemplate_TemplateField
UNIQUE (templateId, fieldIndex);
ALTER TABLE SB_FormTemplate_TemplateField ADD
CONSTRAINT FK_FormTemplate_TemplateField
FOREIGN KEY (templateId)
REFERENCES SB_FormTemplate_Template (templateId);

CREATE TABLE SB_FormTemplate_Record
(
  recordId   INT          NOT NULL,
  templateId INT          NOT NULL,
  externalId VARCHAR(250) NOT NULL,
  lang       CHAR(2)
);

ALTER TABLE SB_FormTemplate_Record ADD
CONSTRAINT PK_FormTemplate_Record
PRIMARY KEY (recordId);
ALTER TABLE SB_FormTemplate_Record ADD
CONSTRAINT UN_FormTemplate_Record
UNIQUE (templateId, externalId, lang);
ALTER TABLE SB_FormTemplate_Record ADD
CONSTRAINT FK_FormTemplate_Record
FOREIGN KEY (templateId)
REFERENCES SB_FormTemplate_Template (templateId);

CREATE TABLE SB_FormTemplate_TextField
(
  RecordId        INT          NOT NULL,
  fieldName       VARCHAR(100) NOT NULL,
  fieldValue      VARCHAR(4000),
  fieldValueIndex INT
);

ALTER TABLE SB_FormTemplate_TextField ADD
CONSTRAINT FK_FormTemplate_TextField
FOREIGN KEY (recordId)
REFERENCES SB_FormTemplate_Record (recordId);

CREATE INDEX IDX_SB_FORMTEMPLATE_TEXTFIELD ON SB_FORMTEMPLATE_TEXTFIELD (RECORDID);
