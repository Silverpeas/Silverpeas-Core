ALTER TABLE DomainSP_User MODIFY (password varchar (32))
;

ALTER TABLE ST_Group
ADD synchroRule varchar(100) NULL
;

ALTER TABLE ST_Space
ADD createTime varchar(20) NULL
;
ALTER TABLE ST_Space
ADD updateTime varchar(20) NULL
;
ALTER TABLE ST_Space
ADD removeTime varchar(20) NULL
;
ALTER TABLE ST_Space
ADD spaceStatus char(1) NULL
;
ALTER TABLE ST_Space
ADD updatedBy int NULL
;
ALTER TABLE ST_Space
ADD removedBy int NULL
;

ALTER TABLE ST_ComponentInstance
ADD createTime varchar(20) NULL
;
ALTER TABLE ST_ComponentInstance
ADD updateTime varchar(20) NULL
;
ALTER TABLE ST_ComponentInstance
ADD removeTime varchar(20) NULL
;
ALTER TABLE ST_ComponentInstance
ADD componentStatus char(1) NULL
;
ALTER TABLE ST_ComponentInstance
ADD updatedBy int NULL
;
ALTER TABLE ST_ComponentInstance
ADD removedBy int NULL
;