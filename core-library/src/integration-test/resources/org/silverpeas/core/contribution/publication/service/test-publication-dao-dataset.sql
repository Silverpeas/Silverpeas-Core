/**
 * Access levels
 */
INSERT INTO st_accesslevel (id, name)
VALUES ('U', 'User'),
       ('A', 'Administrator'),
       ('G', 'Guest'),
       ('R', 'Removed'),
       ('K', 'KMManager'),
       ('D', 'DomainManager');

/**
 * users
 */
INSERT INTO st_user (id, domainId, specificId, lastName, firstName, login, accessLevel, state, stateSaveDate)
VALUES (0, 0, '0', 'Administrateur', '', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000'),
       (1, 0, '1', 'Foo', 'John','jfoo', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (2, 0, '2', 'Hetfield', 'John', 'jhetfield', 'U', 'VALID', '2013-02-11 00:00:00.000'),
       (3, 0, '3', 'Hammett', 'Karl', 'khammett', 'U', 'VALID', '2013-02-11 00:00:00.000'),
       (100, 0, '100', 'Simpson', 'Bart', 'bsimpson', 'U', 'VALID', '2013-02-11 00:00:00.000'),
       (101, 0, '101', 'Simpson', 'Emma', 'esimpson','U', 'VALID', '2013-02-11 00:00:00.000'),
       (200, 0, '200', 'Dalton', 'Joe', 'jdalton','U', 'VALID', '2013-02-11 00:00:00.000');

/**
 * Component instances
 */
insert into st_componentinstance (id, spaceId, name, componentName, orderNum, isPublic, isHidden, isInheritanceBlocked)
values (100, 1, 'A kmelia 100', 'kmelia', 1, 0, 0, 1),
       (200, 1, 'A kmelia 200', 'kmelia', 2, 1, 0, 0),
       (300, 1, 'A kmelia 300', 'kmelia', 3, 0, 1, 0);

/**
 * Nodes in Kmelia instances
 */
INSERT INTO sb_node_node (nodeid, nodename, nodedescription, nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, nodestatus, instanceid, type, ordernumber, lang, rightsdependson)
VALUES (0, 'Accueil', 'La Racine', '2016/01/14', '2', '/', 1, -1, '', 'Visible', 'kmelia200', NULL, 0, NULL, -1),
       (1, 'Corbeille', 'Vous trouvez ici les publications que vous avez supprimé', '2016/01/14', '2', '/0/', 2, 0, '', 'Invisible', 'kmelia200', NULL, 0, NULL, -1),
       (2, 'Déclassées', 'Vos publications inaccessibles se retrouvent ici', '2016/01/14', '2', '/0/', 2, 0, '', 'Invisible', 'kmelia200', NULL, 0, NULL, -1),
       (110, 'Les publications', '', '2016/01/14', '2', '/0/', 2, 0, NULL, 'Invisible', 'kmelia200', 'default', 3, 'fr', -1);

/**
 * Links between nodes and publications in Kmelia instances
 */
insert into sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
values (100, 110, 'kmelia200', NULL, NULL, 0),
       (101, 110, 'kmelia200', NULL, NULL, 5);

/**
 * Publications in Kmelia instances
 */
insert into sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate,
                                  pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus,
                                  pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid,
                                  pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang)
values (100, '0', 'Publication 1', 'Première publication de test', '2008/11/18', '2009/10/18', '2120/12/18',
        '100', 1, NULL, 'test', 'Contenu de la publication 1', 'Valid',
        '2009/11/18', 'kmelia200', '200', '2008/11/18', '300',
        '00:00', '23:59', 'Homer Simpson', NULL, -1, NULL, 'fr'),
       (101, '0', 'Publication 2', '2ème publication de test', '2008/11/18', '2009/10/18', '2120/12/18',
        '101', '5', NULL, 'test', 'Contenu de la publication 2', 'Valid',
        '2009/11/18', 'kmelia200', '200', '2008/11/18', '300',
        '01:10', '20:35', 'Bart Simpson', NULL, -1, NULL, 'fr');