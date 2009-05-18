ALTER TABLE SB_ContentManager_Content
ADD beginDate varchar (10) NULL
;
ALTER TABLE SB_ContentManager_Content
ADD endDate varchar (10) NULL
;
ALTER TABLE SB_ContentManager_Content
ADD isVisible int NULL
;
UPDATE    SB_ContentManager_Content
SET              beginDate = '0000/00/00'
;
UPDATE    SB_ContentManager_Content
SET              endDate = '9999/99/99'
;
UPDATE    SB_ContentManager_Content
SET              isVisible = 1
;