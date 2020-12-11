UPDATE ST_USER SET firstname = '_Anonymous_', email = ''
WHERE (firstname = 'Anonyme' or firstname = 'anonym' or firstname = 'Anonymous')
  AND lastName = '' AND state = 'DELETED';