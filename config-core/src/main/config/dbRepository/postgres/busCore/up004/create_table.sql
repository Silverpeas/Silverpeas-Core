alter table ST_ComponentInstance
add orderNum int DEFAULT (0) NOT NULL ;
alter table ST_Space
add orderNum int DEFAULT (0) NOT NULL ;
