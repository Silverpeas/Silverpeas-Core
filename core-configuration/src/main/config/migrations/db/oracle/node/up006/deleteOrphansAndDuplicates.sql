/* Delete orphans nodesi18n (does not exist any more in sb_node_node)... */
DELETE FROM sb_node_nodei18n WHERE nodeId NOT IN (SELECT nodeId from SB_NODE_NODE);

/* Delete duplicates nodesi18n. */
DELETE FROM sb_node_nodei18n a
WHERE a.id != (
    SELECT max(id) FROM sb_node_nodei18n b WHERE a.nodeid = b.nodeid AND a.lang = b.lang);

