/*
The links
 */
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (1, 'Name - 1', '/Component/kmelia4', 1, 0, '0', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (3, 'Name - 3', '/Publication/19', 1, 0, '0', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (4, 'Name - 4', '/Topic/10514?ComponentId=gallery89', 1, 0, '56', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (6, 'Name - 6', '/Publication/106', 1, 0, '3', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (7, 'Name - 7', '/Publication/97', 1, 0, '3', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (21, 'Name - 21', '/Component/suggestionBox269', 1, 0, '91', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (24, 'Name - 24', '/Component/suggestionBox269', 1, 0, '91', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (32, 'Name - 32', '/Component/kmelia188', 1, 0, '119', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (46, 'Name - 46', '/Publication/381', 1, 0, '3', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (51, 'Name - 51', '/Media/9e941fd2-40d3-42b8-8a83-90586b2d87f6', 1, 1, '3', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (69, 'Name - 69', '/Component/suggestionBox279', 1, 0, '3', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (78, 'Name - 78', '/Topic/10784?ComponentId=kmelia4', 1, 0, '2', NULL);
INSERT INTO SB_MyLinks_Link (linkid, name, url, visible, popup, userid, instanceid)
VALUES (79, 'Name - 79', '/Publication/26', 1, 0, '2', 'kmelia4');

INSERT INTO SB_MyLinks_Cat (catId, name, description, userId, position)
VALUES (1, 'catName_1', 'catDesc_1', '2', NULL),
       (2, 'catName_2', 'catDesc_2', '2', NULL),
       (3, 'catName_3', 'catDesc_3', '0', NULL),
       (4, 'catName_4', 'catDesc_4', '91', NULL),
       (5, 'catName_5', 'catDesc_5', '26', NULL);

INSERT INTO SB_MyLinks_LinkCat (catId, linkid)
VALUES (2, 78),
       (4, 24),
       (4, 21)
