/**
 * Access levels
 */
INSERT INTO st_accesslevel (id, name)
VALUES ('U', 'User'),
       ('A', 'Administrator'),
       ('G', 'Guest'),
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
INSERT INTO st_componentinstance (id, spaceId, name, componentName, orderNum, isPublic, isHidden, isInheritanceBlocked)
VALUES (100, 1, 'A kmelia 100', 'kmelia', 1, 0, 0, 1),
       (200, 1, 'A kmelia 200', 'kmelia', 2, 1, 0, 0),
       (300, 1, 'A kmelia 300', 'kmelia', 3, 0, 1, 0);

/**
 * Publications in Kmelia instances
 */
INSERT INTO sb_publications (id, title, description, creationdate,
                                  creatorid, version, keywords, content,
                                  updatedate, instanceid, updaterid)
VALUES (100, 'Publication 1', 'Première publication de test', '2008-11-18T09:46:34.00',
        '100', '1.0', 'test', 'Contenu de la publication 1',
        '2009-11-18T14:09:12.00', 'kmelia200', '200'),
       (101, 'Publication 2', '2ème publication de test', '2008-11-18T15:23:04.00',
        '101', '5.0', 'test', 'Contenu de la publication 2',
        '2009-11-18T09:12:31.00', 'kmelia200', '200');