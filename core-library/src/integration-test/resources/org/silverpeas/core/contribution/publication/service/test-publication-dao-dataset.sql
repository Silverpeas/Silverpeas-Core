insert into st_componentinstance (id, spaceId, name, componentName, orderNum, isPublic, isHidden, isInheritanceBlocked)
values (100, 1, 'A kmelia 100', 'kmelia', 1, 0, 0, 1),
       (200, 1, 'A kmelia 200', 'kmelia', 2, 1, 0, 0),
       (300, 1, 'A kmelia 300', 'kmelia', 3, 0, 1, 0);

insert into sb_publication_publifather (pubid, nodeid, instanceid, aliasuserid, aliasdate, puborder)
values (100, 110, 'kmelia200', 0, '2009/10/18', 0),
       (101, 110, 'kmelia200', 0, '2009/10/18', 5);

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