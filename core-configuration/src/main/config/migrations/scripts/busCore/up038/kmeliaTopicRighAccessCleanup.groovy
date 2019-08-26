import groovy.sql.GroovyRowResult
import groovy.sql.Sql

/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

Set<Integer> getParentsOfGroup(Sql sql, int groupId) {
  Set<Integer> groupIds = []
  sql.rows('''
SELECT DISTINCT supergroupid FROM st_group WHERE id = ? AND supergroupid IS NOT NULL
''', [groupId]).each { row ->
    groupIds.add(row.supergroupid)
    groupIds.addAll(getParentsOfGroup(sql, row.supergroupid))
  }
  return groupIds
}

Set<Integer> getAllGroupsOfUser(Sql sql, int userId) {
  Set<Integer> groups = []
  sql.rows('''
SELECT DISTINCT u.groupId FROM st_group_user_rel u INNER JOIN st_group g ON u.groupid = g.id 
WHERE u.userid = ?
''', [userId]).each { row ->
    groups.add(row.groupId)
    groups.addAll(getParentsOfGroup(sql, row.groupId))
  }
  return groups
}

boolean noAnyRightAccesses(Sql sql, int instanceId, Set<Integer> groups) {
  if (!groups.isEmpty()) {
    // take into account limitation in SQL IN clause with some the database systems.
    // we split the IN clause into smaller chunks of IN clauses
    String groupInclusionClause = ''
    groups.collate(1000).each {
      if (!groupInclusionClause.isEmpty()) {
        groupInclusionClause += ' OR '
      }
      groupInclusionClause += "g.groupId IN (${it.join(',')})"
    }
    GroovyRowResult row = sql.rows('''
SELECT COUNT(g.groupid) AS nb FROM st_userrole r INNER JOIN st_userrole_group_rel g ON r.id = g.userroleid
           WHERE r.objectid IS NULL AND r.instanceid = ?
''' + " AND (${groupInclusionClause})", [instanceId]).get(0)
    return row.nb == 0
  }
  return true
}

log.info 'Search users with right access on topics but not on the corresponding Kmelia instance'
sql.rows('''
SELECT u.userid, u.userroleid, r.instanceid, r.objectid FROM st_userrole r INNER JOIN st_userrole_user_rel u on r.id = u.userroleid
WHERE r.objectid IS NOT NULL
  AND r.instanceid IN (SELECT id FROM st_componentinstance c WHERE c.componentname = 'kmelia')
  AND 0 = (SELECT COUNT(rr.id) FROM st_userrole rr INNER JOIN st_userrole_user_rel uu ON rr.id = uu.userroleid
           WHERE rr.objectid IS NULL AND rr.instanceid = r.instanceid AND u.userid = uu.userid)
''').each { row ->
  Set<String> groupsOfUser = getAllGroupsOfUser(sql, row.userid)
  if (noAnyRightAccesses(sql, row.instanceid, groupsOfUser)) {
    log.info " -> Found user ${row.userid} with right access on topic ${row.objectid} but not on Kmelia instance ${row.instanceid}"
    sql.execute('''
DELETE FROM st_userrole_user_rel WHERE userid = ? AND userroleid = ?
''', [row.userid, row.userroleid])
  }
}

log.info 'Search user groups with right access on topics but not on the corresponding Kmelia instance'
sql.rows('''
SELECT g.groupid, g.userroleid, r.instanceid, r.objectid FROM st_userrole r INNER JOIN st_userrole_group_rel g on r.id = g.userroleid
WHERE r.objectid IS NOT NULL
  AND r.instanceid IN (SELECT id FROM st_componentinstance c WHERE c.componentname = 'kmelia')
  AND 0 = (SELECT COUNT(rr.id) FROM st_userrole rr INNER JOIN st_userrole_group_rel gg ON rr.id = gg.userroleid
           WHERE rr.objectid IS NULL AND rr.instanceid = r.instanceid AND g.groupid = gg.groupid)
''').each { row ->
  Set<String> parentGroups = getParentsOfGroup(sql, row.groupid)
  if (noAnyRightAccesses(sql, row.instanceid, parentGroups)) {
    log.info " -> Found group ${row.groupid} with right access on topic ${row.objectid} but not on Kmelia instance ${row.instanceid}"
    sql.execute('''
DELETE FROM st_userrole_group_rel WHERE groupid = ? AND userroleid = ?
''', [row.groupid, row.userroleid])
  }
}