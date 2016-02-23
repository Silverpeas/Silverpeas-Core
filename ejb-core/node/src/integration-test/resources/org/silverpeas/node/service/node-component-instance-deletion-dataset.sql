/*
3 component instances with nodes.
 */
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (0, 'Accueil', 'La Racine', '2016/01/14', '2', '/', 1, -1, '', 'Visible', 'kmelia382', NULL, 0,
   NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (1, 'Corbeille', 'Vous trouvez ici les publications que vous avez supprimé', '2016/01/14', '2',
   '/0/', 2, 0, '', 'Invisible', 'kmelia382', NULL, 0, NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (2, 'Déclassées', 'Vos publications inaccessibles se retrouvent ici', '2016/01/14', '2', '/0/', 2,
   0, '', 'Invisible', 'kmelia382', NULL, 0, NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES (11306, 'SD [kmelia382]', '', '2016/01/14', '2', '/0/', 2, 0, NULL, 'Invisible', 'kmelia382',
        'default', 3, 'fr', -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (0, 'Accueil', 'La Racine', '2016/01/14', '2', '/', 1, -1, '', 'Visible', 'kmelia383', NULL, 0,
   NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (1, 'Corbeille', 'Vous trouvez ici les publications que vous avez supprimé', '2016/01/14', '2',
   '/0/', 2, 0, '', 'Invisible', 'kmelia383', NULL, 0, NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (2, 'Déclassées', 'Vos publications inaccessibles se retrouvent ici', '2016/01/14', '2', '/0/', 2,
   0, '', 'Invisible', 'kmelia383', NULL, 0, NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES (11308, 'SD [kmelia383]', '', '2016/01/14', '2', '/0/', 2, 0, NULL, 'Invisible', 'kmelia383',
        'default', 3, 'fr', -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES (11309, 'SSD [kmelia383]', '', '2016/01/14', '2', '/0/11308/', 3, 11308, NULL, 'Invisible',
        'kmelia383', 'default', 1, 'fr', -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (0, 'Accueil', 'La Racine', '2016/01/14', '2', '/', 1, -1, '', 'Visible', 'kmelia384', NULL, 0,
   NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (1, 'Corbeille', 'Vous trouvez ici les publications que vous avez supprimé', '2016/01/14', '2',
   '/0/', 2, 0, '', 'Invisible', 'kmelia384', NULL, 0, NULL, -1);
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES
  (2, 'Déclassées', 'Vos publications inaccessibles se retrouvent ici', '2016/01/14', '2', '/0/', 2,
   0, '', 'Invisible', 'kmelia384', NULL, 0, NULL, -1);


/*
Translations
 */

INSERT INTO sb_node_nodei18n (id, nodeid, lang, nodename, nodedescription)
VALUES (11, 11306, 'en', '[EN] SD [kmelia382]', '');
INSERT INTO sb_node_nodei18n (id, nodeid, lang, nodename, nodedescription)
VALUES (12, 11306, 'de', '[DE] SD [kmelia382]', '');
INSERT INTO sb_node_nodei18n (id, nodeid, lang, nodename, nodedescription)
VALUES (13, 11308, 'en', '[EN] SD [kmelia383]', '');
INSERT INTO sb_node_nodei18n (id, nodeid, lang, nodename, nodedescription)
VALUES (14, 11309, 'en', '[EN] SSD [kmelia383]', '');
