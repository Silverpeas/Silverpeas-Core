ALTER TABLE SB_Publication_PubliFather
ADD aliasUserId int	NULL
;

ALTER TABLE SB_Publication_PubliFather
ADD aliasDate varchar(20) NULL
;

update sb_publication_publi
set pubstatus = 'Valid' where instanceid like 'toolbox%'
;