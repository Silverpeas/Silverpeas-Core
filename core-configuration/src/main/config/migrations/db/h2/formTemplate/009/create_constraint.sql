ALTER TABLE SB_FormTemplate_Template ADD
   CONSTRAINT PK_FormTemplate_Template
   PRIMARY KEY ( templateId )
;
ALTER TABLE SB_FormTemplate_Template ADD
   CONSTRAINT UN_FormTemplate_Template
   UNIQUE ( externalId )
;

ALTER TABLE SB_FormTemplate_TemplateField ADD
   CONSTRAINT PK_FormTemplate_TemplateField
   PRIMARY KEY ( templateId, fieldName )
;
ALTER TABLE SB_FormTemplate_TemplateField ADD
   CONSTRAINT UN_FormTemplate_TemplateField
   UNIQUE ( templateId, fieldIndex )
;
ALTER TABLE SB_FormTemplate_TemplateField ADD
   CONSTRAINT FK_FormTemplate_TemplateField
   FOREIGN KEY ( templateId )
   REFERENCES SB_FormTemplate_Template ( templateId )
;

ALTER TABLE SB_FormTemplate_Record ADD
   CONSTRAINT PK_FormTemplate_Record
   PRIMARY KEY ( recordId )
;
ALTER TABLE SB_FormTemplate_Record ADD
   CONSTRAINT UN_FormTemplate_Record
   UNIQUE ( templateId, externalId, lang )
;
ALTER TABLE SB_FormTemplate_Record ADD
   CONSTRAINT FK_FormTemplate_Record
   FOREIGN KEY ( templateId )
   REFERENCES SB_FormTemplate_Template ( templateId )
;

ALTER TABLE SB_FormTemplate_TextField ADD
   CONSTRAINT FK_FormTemplate_TextField
   FOREIGN KEY ( recordId )
   REFERENCES SB_FormTemplate_Record ( recordId )
;
