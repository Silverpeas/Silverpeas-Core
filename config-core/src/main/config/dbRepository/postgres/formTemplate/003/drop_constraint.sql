ALTER TABLE SB_FormTemplate_TemplateField DROP 
	CONSTRAINT FK_FormTemplate_TemplateField
;
ALTER TABLE SB_FormTemplate_Record DROP 
	CONSTRAINT FK_FormTemplate_Record
;
ALTER TABLE SB_FormTemplate_TextField DROP 
	CONSTRAINT FK_FormTemplate_TextField
;

ALTER TABLE SB_FormTemplate_Template DROP 
	CONSTRAINT UN_FormTemplate_Template
;
ALTER TABLE SB_FormTemplate_TemplateField DROP 
	CONSTRAINT UN_FormTemplate_TemplateField
;
ALTER TABLE SB_FormTemplate_Record DROP 
	CONSTRAINT UN_FormTemplate_Record
;

ALTER TABLE SB_FormTemplate_TemplateField DROP 
	CONSTRAINT PK_FormTemplate_TemplateField
;
ALTER TABLE SB_FormTemplate_Template DROP 
	CONSTRAINT PK_FormTemplate_Template
;

ALTER TABLE SB_FormTemplate_Record DROP 
	CONSTRAINT PK_FormTemplate_Record
;
