ALTER TABLE SB_FormTemplate_Record 
DROP CONSTRAINT UN_FormTemplate_Record
;

ALTER TABLE SB_FormTemplate_Record 
ADD CONSTRAINT UN_FormTemplate_Record
   UNIQUE ( templateId, externalId, lang )   
;