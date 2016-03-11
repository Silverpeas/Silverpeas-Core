/*
3 component instances with publications:
- kmelia382: 5 publications, 2 published (1153, 1155), 1 in the trash (1154), 1 in draft (1156), 1 in validation (1157)
- kmelia383: 3 publications, 1 published (1158), 1 in validation (1159), 1 in draft (1160)
- kmelia384: 1 validated publication (1161)
 */
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1153, '0', 'Pub [kmelia382] - A', '', '2016/01/14', '2016/01/14', '9999/99/99', '2', 1, '', '',
   '', 'Valid', '2016/01/14', 'kmelia382', '2', NULL, NULL, '00:00', '23:59', NULL, NULL, -1, NULL,
   'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1154, '0', 'Pub [kmelia382] - A', '', '2016/01/14', '2016/01/14', '9999/99/99', '2', 1, '', '',
   '', 'Draft', '2016/01/14', 'kmelia382', '2', NULL, NULL, '00:00', '23:59', NULL, NULL, -1, '',
   'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1155, '0', 'Pub [kmelia382] - B - Contrib.', '', '2016/01/14', '2016/01/14', '9999/99/99', '119',
   1, '', '', '', 'Valid', '2016/01/14', 'kmelia382', '119', '2016/01/14', '2', '00:00', '23:59',
   NULL, NULL, -1, NULL, 'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1156, '0', 'Pub [kmelia382] - C - Contrib.', '', '2016/01/14', '2016/01/14', '9999/99/99', '119',
   1, '', '', '', 'Draft', '2016/01/14', 'kmelia382', '119', NULL, NULL, '00:00', '23:59', NULL,
   NULL, -1, NULL, 'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1157, '0', 'Pub [kmelia382] - D - Contrib.', '', '2016/01/14', '2016/01/14', '9999/99/99', '119',
   1, '', '', '', 'ToValidate', '2016/01/14', 'kmelia382', '119', NULL, NULL, '00:00', '23:59',
   NULL, NULL, -1, NULL, 'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1158, '0', 'Pub [kmelia383] - A ', '', '2016/01/14', '2016/01/14', '9999/99/99', '2', 1, '', '',
   '', 'Valid', '2016/01/14', 'kmelia383', '2', NULL, NULL, '00:00', '23:59', NULL, NULL, -1, NULL,
   'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1159, '0', 'Pub [kmelia383] - B - Contrib.', '', '2016/01/14', '2016/01/14', '9999/99/99', '119',
   1, '', '', '', 'ToValidate', '2016/01/14', 'kmelia383', '119', NULL, NULL, '00:00', '23:59',
   NULL, NULL, -1, NULL, 'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1160, '0', 'Pub [kmelia383] - C - Contrib.', '', '2016/01/14', '2016/01/14', '9999/99/99', '119',
   1, '', '', '', 'Draft', '2016/01/14', 'kmelia383', '119', NULL, NULL, '00:00', '23:59', NULL,
   NULL, -1, NULL, 'fr', NULL);
INSERT INTO sb_publication_publi (pubid, infoid, pubname, pubdescription, pubcreationdate, pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords, pubcontent, pubstatus, pubupdatedate, instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour, pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus, lang, pubdraftoutdate)
VALUES
  (1161, '0', 'Pub [kmelia384] - A', '', '2016/01/14', '2016/01/14', '9999/99/99', '2', 1, '', '',
   '', 'Valid', '2016/01/14', 'kmelia384', '2', NULL, NULL, '00:00', '23:59', NULL, NULL, -1, NULL,
   'fr', NULL);

/*
Translations
 */

INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (1, 1153, 'en', 'en translation of 1153');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (2, 1154, 'fr', 'fr translation of 1154');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (3, 1155, 'en', 'en translation of 1155');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (4, 1156, 'fr', 'fr translation of 1156');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (5, 1157, 'en', 'en translation of 1157');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (6, 1158, 'fr', 'fr translation of 1158');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (7, 1159, 'en', 'en translation of 1159');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (8, 1160, 'fr', 'fr translation of 1160');
INSERT INTO sb_publication_publii18n (id, pubid, lang, name)
VALUES (9, 1161, 'en', 'en translation of 1161');

/*
Linked with nodes. pubid-nodeid-instanceid.
The original location of a publication can be found by getting pubid from sb_publication_publi table first.
 */
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1153, 11306, 'kmelia382', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1153, 11309, 'kmelia383', 2, '1452768260374', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1153, 0, 'kmelia384', 2, '1452768260376', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1154, 1, 'kmelia382', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1155, 0, 'kmelia382', 119, '1452768299528', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1155, 11306, 'kmelia382', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1155, 11308, 'kmelia383', 2, '1452768543119', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1155, 0, 'kmelia384', 2, '1452768543121', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1156, 11306, 'kmelia382', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1157, 11306, 'kmelia382', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1158, 11306, 'kmelia382', 2, '1452768736135', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1158, 0, 'kmelia383', 2, '1452768736134', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1158, 11309, 'kmelia383', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1158, 0, 'kmelia384', 2, '1452768736136', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1159, 11308, 'kmelia383', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1160, 11309, 'kmelia383', NULL, NULL, 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1161, 11306, 'kmelia382', 2, '1452769187440', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1161, 0, 'kmelia383', 2, '1452769187440', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1161, 11308, 'kmelia383', 2, '1452769187441', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1161, 11309, 'kmelia383', 2, '1452769187441', 0);
INSERT INTO sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
VALUES (1161, 0, 'kmelia384', NULL, NULL, 0);

/*
Validation
 */
INSERT INTO sb_publication_validation (id, pubid, instanceid, userid, decisiondate, decision)
VALUES (1, 1157, 'kmelia382', 2, '1338809530241', 'Valid');
INSERT INTO sb_publication_validation (id, pubid, instanceid, userid, decisiondate, decision)
VALUES (2, 1157, 'kmelia382', 3, '1338809530241', 'Valid');
INSERT INTO sb_publication_validation (id, pubid, instanceid, userid, decisiondate, decision)
VALUES (3, 1159, 'kmelia383', 2, '1338809530241', 'Valid');

/*
See Also
 */
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (1, 1153, 'kmelia382', 1155, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (2, 1155, 'kmelia382', 1153, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (3, 1157, 'kmelia382', 1155, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (4, 1157, 'kmelia382', 1158, 'kmelia383');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (5, 1158, 'kmelia383', 1153, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (6, 1158, 'kmelia383', 1155, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (7, 1160, 'kmelia383', 1155, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (8, 1160, 'kmelia383', 1158, 'kmelia383');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (9, 1161, 'kmelia384', 1155, 'kmelia382');
INSERT INTO sb_seealso_link (id, objectid, objectinstanceid, targetid, targetinstanceid)
VALUES (10, 1161, 'kmelia384', 1158, 'kmelia383');
