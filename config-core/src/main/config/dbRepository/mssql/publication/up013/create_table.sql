ALTER TABLE SB_Publication_PubliFather
ADD aliasUserId int
;

ALTER TABLE SB_Publication_PubliFather
ADD aliasDate varchar(20)
;

update sb_publication_publi
set pubstatus = 'Valid' where instanceid like 'toolbox%'
;