ALTER TABLE SB_Version_Version
	ADD instanceId varchar (50) not null default -1
;

ALTER TABLE SB_Document_Readlist
	ADD instanceId varchar (50) not null default -1
;

ALTER TABLE SB_Document_Worklist
	ADD instanceId varchar (50) not null default -1
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