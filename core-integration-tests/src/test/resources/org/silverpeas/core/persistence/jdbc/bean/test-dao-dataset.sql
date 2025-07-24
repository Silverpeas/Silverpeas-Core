INSERT INTO UniqueId (maxId, tableName)
VALUES (3, 'sb_person'), (2, 'sb_asso');

INSERT INTO SB_Person(id, firstName, lastName, age, address)
VALUES (1, 'John', 'Doo', 26, 'Street Tooper, Argentat'),
       (2, 'Toto', 'Dupont', 32, 'Bordel Street, Proot'),
       (3, 'Bart', 'Simpson', 16, 'Simpson City'),
       (4, 'Omer', 'Simpson', 40, 'Simpson City');

INSERT INTO SB_Asso(id, name, creation)
VALUES (1, 'AlpesJUG', '2009-12-03'),
       (2, 'SnowCamp', '2020-04-01');

INSERT INTO SB_Membership(assoId, personId)
VALUES (1, 3), (1, 4), (2, 4);