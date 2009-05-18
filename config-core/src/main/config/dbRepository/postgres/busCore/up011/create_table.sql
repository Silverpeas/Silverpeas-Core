ALTER TABLE DomainSP_User ALTER column password TYPE varchar(32)
;

ALTER TABLE ST_Group
ADD COLUMN synchroRule varchar(100) NULL
;

ALTER TABLE ST_Space
ADD COLUMN createTime varchar(20) NULL
;
ALTER TABLE ST_Space
ADD COLUMN updateTime varchar(20) NULL
;
ALTER TABLE ST_Space
ADD COLUMN removeTime varchar(20) NULL
;
ALTER TABLE ST_Space
ADD COLUMN spaceStatus char(1) NULL
;
ALTER TABLE ST_Space
ADD COLUMN updatedBy int NULL
;
ALTER TABLE ST_Space
ADD COLUMN removedBy int NULL
;

ALTER TABLE ST_ComponentInstance
ADD COLUMN createTime varchar(20) NULL
;
ALTER TABLE ST_ComponentInstance
ADD COLUMN updateTime varchar(20) NULL
;
ALTER TABLE ST_ComponentInstance
ADD COLUMN removeTime varchar(20) NULL
;
ALTER TABLE ST_ComponentInstance
ADD COLUMN componentStatus char(1) NULL
;
ALTER TABLE ST_ComponentInstance
ADD COLUMN updatedBy int NULL
;
ALTER TABLE ST_ComponentInstance
ADD COLUMN removedBy int NULL
;