ALTER TABLE Personalization
ADD personalWSpace varchar (50) NULL
;
ALTER TABLE Personalization
ADD thesaurusStatus int default(0) NOT NULL 
;
