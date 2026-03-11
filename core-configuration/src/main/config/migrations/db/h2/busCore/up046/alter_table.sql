UPDATE ST_User SET accessLevel = 'U' WHERE accessLevel = 'R';

DELETE FROM ST_AccessLevel WHERE id = 'R';