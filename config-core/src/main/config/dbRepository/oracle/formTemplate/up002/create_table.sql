alter table SB_FormTemplate_TextField
ADD fieldName varchar(100) default('defaultValue') NOT NULL 
;

UPDATE    SB_FormTemplate_TextField
SET       SB_FormTemplate_TextField.fieldName = (
select SB_FormTemplate_TemplateField.fieldName
from	  SB_FormTemplate_TemplateField, SB_FormTemplate_Record
WHERE     SB_FormTemplate_TemplateField.templateId = SB_FormTemplate_Record.templateId
AND	  SB_FormTemplate_Record.recordId = SB_FormTemplate_TextField.recordId
AND	  SB_FormTemplate_TemplateField.fieldIndex = SB_FormTemplate_TextField.fieldIndex
);

alter table SB_FormTemplate_TextField
DROP column fieldIndex
;

ALTER TABLE SB_FormTemplate_TemplateField ADD 
   CONSTRAINT UN_FormTemplate_TemplateField
   UNIQUE ( templateId, fieldName )
;

ALTER TABLE SB_FormTemplate_Template
ADD templateName varchar (250) NULL
;

UPDATE    SB_FormTemplate_Template
SET              templateName = 'whitepages/annuaire.xml'
WHERE     (externalId like 'whitePages%')
;
UPDATE    SB_FormTemplate_Template
SET              templateName = 'documentation/documentation.xml'
WHERE     (externalId like 'documentation%')
;
UPDATE    SB_FormTemplate_Template
SET              templateName = 'expertLocator/expertLocator.xml'
WHERE     (externalId like 'expertLocator%')
;
UPDATE    SB_FormTemplate_Template
SET              templateName = 'infoTracker/infoTracker.xml'
WHERE     (externalId like 'infoTracker%')
;
UPDATE    SB_FormTemplate_Template
SET              templateName = 'incidents/incidents.xml'
WHERE     (externalId like 'incidents%')
;
UPDATE    SB_FormTemplate_Template
SET              templateName = 'trucsAstuces/trucsAstuces.xml'
WHERE     (externalId like 'trucsAstuces%')
;