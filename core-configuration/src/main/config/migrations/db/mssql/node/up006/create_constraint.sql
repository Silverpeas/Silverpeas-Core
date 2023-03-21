ALTER TABLE SB_Node_NodeI18N ADD
    CONSTRAINT UN_Node_NodeI18N
    UNIQUE
    (
     nodeId,lang
    )
;