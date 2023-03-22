/* Delete orphans nodesi18n (does not exist any more in sb_node_node)... */
DELETE FROM sb_node_nodei18n
WHERE nodeId NOT IN (SELECT nodeId from SB_NODE_NODE);

/* Delete duplicates nodesi18n. */
DELETE sb_node_nodei18n FROM sb_node_nodei18n
LEFT OUTER JOIN (
SELECT MAX(id) as id, nodeId, lang
FROM sb_node_nodei18n
GROUP BY nodeId, lang
) as t1
ON sb_node_nodei18n.id = t1.id
WHERE t1.id IS NULL;

