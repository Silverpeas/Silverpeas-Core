-- This select shows all direct rights (not the transitive ones for users that are linked to groups)
SELECT
  result.domainId,
  rpad(result.name, 25, ' '),
  (CASE WHEN result.isinherited = 1 THEN '^'
   ELSE '*' END) || ' ' || result.role
FROM (SELECT
        g.domainid                                                         AS domainId,
        '(G) - ' || g.name                                                 AS name,
        rpad('SPACE', 16, ' ') || ' - ' || (sur.rolename || '@' || s.name) AS ROLE,
        sur.isinherited
      FROM st_group g
        JOIN st_spaceuserrole_group_rel sur_gr
          ON sur_gr.groupid = g.id
        JOIN st_spaceuserrole sur
          ON sur.id = sur_gr.spaceuserroleid
        JOIN st_space s
          ON s.id = sur.spaceid
      -- #############################
      UNION
      SELECT
        g.domainid         AS domainId,
        '(G) - ' || g.name AS NAME,
        rpad('COMPONENT' || (CASE WHEN node.nodename IS NOT NULL THEN ('-OBJECT')
                             ELSE '' END), 16, ' ') || ' - ' ||
        (ur.rolename || '@' || C.name ||
         (CASE WHEN node.nodename IS NOT NULL THEN (
           '#' ||
           node.nodename)
          ELSE '' END))    AS ROLE,
        ur.isinherited
      FROM st_group g
        JOIN st_userrole_group_rel ur_gr
          ON ur_gr.groupid = g.id
        JOIN st_userrole ur
          ON ur.id = ur_gr.userroleid
        JOIN st_componentinstance C
          ON C.id = ur.instanceid
        LEFT OUTER JOIN sb_node_node node
          ON node.nodeid = ur.objectid
      -- #############################
      UNION
      SELECT
        u.domainid                                                           AS domainId,
        '(U) - ' || u.login                                                  AS name,
        rpad('SPACE', 16, ' ') || ' - ' || (sur2.rolename || '@' || s2.name) AS role,
        sur2.isinherited
      FROM st_user u
        JOIN st_spaceuserrole_user_rel sur_ur
          ON sur_ur.userid = u.id
        JOIN st_spaceuserrole sur2
          ON sur2.id = sur_ur.spaceuserroleid
        JOIN st_space s2
          ON s2.id = sur2.spaceid
      WHERE u.state = 'VALID'
      -- #############################
      UNION
      SELECT
        u.domainid          AS domainId,
        '(U) - ' || u.login AS name,
        rpad('COMPONENT' || (CASE WHEN node.nodename IS NOT NULL THEN ('-OBJECT')
                             ELSE '' END), 16, ' ') || ' - ' ||
        (ur2.rolename || '@' || c2.name ||
         (CASE WHEN node.nodename IS NOT NULL THEN (
           '#' ||
           node.nodename)
          ELSE '' END))     AS compRole,
        ur2.isinherited
      FROM st_user u
        JOIN st_userrole_user_rel ur_ur
          ON ur_ur.userid = u.id
        JOIN st_userrole ur2
          ON ur2.id = ur_ur.userroleid
        JOIN st_componentinstance c2
          ON c2.id = ur2.instanceid
        LEFT OUTER JOIN sb_node_node node
          ON node.nodeid = ur2.objectid
      WHERE u.state = 'VALID'
     ) AS result
ORDER BY result.domainid, result.name, result.role