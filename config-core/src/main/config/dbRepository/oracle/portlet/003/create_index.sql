CREATE INDEX IN_ST_PortletColumn_1 ON ST_PortletColumn(spaceId);

CREATE INDEX IN_ST_PortletRow_1 ON ST_PortletRow(portletColumnId);

CREATE INDEX IN_ST_PortState_1 ON ST_PortletState(portletRowId);
