INSERT INTO DomainSP_User (id, firstName, lastName, login, password)
    VALUES (32, 'John', 'Doo', 'jdoo', '$6$vme7lSiF2QeO9$c41qC73E7MqpvFdO4GnHT0WjjUlbJ90YpopfGUeifP3yFAy5QIq9KB6iRwnqSiLltz5bkPLCHKKADFIx52DSt/');

INSERT INTO ST_USER (id, domainId, specificId, firstName, lastName, login, state, stateSaveDate)
    VALUES (32, 0, 32, 'John', 'Doo', 'jdoo', 'VALID', '2012-01-01 00:00:00.0');

INSERT INTO ST_TOKEN (id, tokenType, resourceId, token, saveCount, saveDate)
    VALUES (4, 'USER', '32', 'd3097ea159644125a21c60dd3bceb197', 1, '2020-09-15 12:00:34.864');