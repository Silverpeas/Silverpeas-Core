ALTER TABLE SB_Node_Node
ADD orderNumber int NULL DEFAULT (0)
;
update SB_Node_Node
set orderNumber = 9999
;
update SB_Node_Node
set nodeStatus = 'Visible'
where instanceId like 'kmelia%'
;
update SB_Node_Node
set nodeStatus = 'Invisible'
where instanceId like 'kmelia%'
and nodeId = 1
;
update SB_Node_Node
set nodeStatus = 'Invisible'
where instanceId like 'kmelia%'
and nodeId = 2
;
