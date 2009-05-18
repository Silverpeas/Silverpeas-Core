ALTER TABLE SB_Version_Version
	ADD instanceId varchar (50) default (-1) not null 
;

ALTER TABLE SB_Document_Readlist
	ADD instanceId varchar (50) default (-1) not null
;

ALTER TABLE SB_Document_Worklist
	ADD instanceId varchar (50) default (-1) not null
;

update SB_Version_Version  
set instanceId = 
(select svd.instanceId from SB_Version_Document svd where svd.documentid=SB_Version_Version.documentid)
;

update SB_Document_Readlist  
set instanceId = 
(select svd.instanceId from SB_Version_Document svd where svd.documentid=SB_Document_Readlist.documentid)
;

update SB_Document_Worklist  
set instanceId = 
(select svd.instanceId from SB_Version_Document svd where svd.documentid=SB_Document_Worklist.documentid)
;