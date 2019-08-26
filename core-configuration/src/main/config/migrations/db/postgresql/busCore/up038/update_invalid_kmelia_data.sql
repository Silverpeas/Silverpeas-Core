UPDATE subscribe
SET resourcetype = 'COMPONENT',
    space        = 'component'
WHERE instanceid like 'kmelia%'
  AND resourceid = '0';