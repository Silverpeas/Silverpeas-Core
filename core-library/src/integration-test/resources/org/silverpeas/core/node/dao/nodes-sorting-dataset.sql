/*
 * Two component instances whose nodes bear the very same identifiers: the root node of every
 * instance is numbered 0, and the ones below it can bear the same numbers from an instance to
 * another. Sorting the nodes of an instance must leave the ones of the other instances untouched.
 */
INSERT INTO sb_node_node
(nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber,
 nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (0, 'Accueil', 'La Racine', '2026/09/22', '7', '/', 1, -1, NULL, 'Visible', 'kmelia1',
   'default', 0, 'fr', -1),
  (1, 'Premier theme', '', '2026/09/22', '7', '/0/', 2, 0, NULL, 'Visible', 'kmelia1',
   'default', 0, 'fr', -1),
  (2, 'Second theme', '', '2026/09/22', '7', '/0/', 2, 0, NULL, 'Visible', 'kmelia1',
   'default', 1, 'fr', -1),
  (0, 'Accueil', 'La Racine', '2026/09/22', '7', '/', 1, -1, NULL, 'Visible', 'kmelia2',
   'default', 0, 'fr', -1),
  (1, 'Theme d une autre instance', '', '2026/09/22', '7', '/0/', 2, 0, NULL, 'Visible', 'kmelia2',
   'default', 7, 'fr', -1),
  (2, 'Autre theme d une autre instance', '', '2026/09/22', '7', '/0/', 2, 0, NULL, 'Visible',
   'kmelia2', 'default', 8, 'fr', -1);
