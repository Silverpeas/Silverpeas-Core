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

Set<Integer> getAllChildrenOfGroup(Sql sql, int groupId) {
  Set<Integer> groupIds = []
  sql.rows('SELECT DISTINCT id FROM st_group WHERE supergroupid = ?', [groupId]).each { row ->
    groupIds.add(row.id)
    groupIds.addAll(getAllChildrenOfGroup(sql, row.id))
  }
  return groupIds
}

Set<Integer> getAllGroupsAccessingResourcesWithUserSubscriptions(Sql sql) {
  Set<Integer> groups = []
  sql.rows('''
SELECT r.groupId FROM subscribe s, st_userrole_group_rel r INNER JOIN st_userrole u on r.userroleid = u.id 
WHERE s.instanceId LIKE CONCAT('%', u.instanceId) 
AND ((s.resourceId = '0' AND u.objectId IS NULL) OR CAST(u.objectId AS VARCHAR(100)) = s.resourceId) 
AND s.subscribertype = 'USER' 
''').each { row ->
    groups.add(row.groupId)
    groups.addAll(getAllChildrenOfGroup(sql, row.groupId))
  }
  return groups
}

Set<Integer> groups = getAllGroupsAccessingResourcesWithUserSubscriptions(sql)

// take into account limitation in SQL IN clause with some the database systems.
// we split the IN clause into smaller chunks of IN clauses/
String groupInclusionClause = ''
groups.collate(1000).each {
  if (!groupInclusionClause.isEmpty()) {
    groupInclusionClause += ' OR '
  }
  groupInclusionClause += "g.groupId IN (${it.join(',')})"
}
String findAllOrphanSubscriptions = """
SELECT s.subscriberId, s.subscriberType, s.instanceId, s.resourceId from subscribe s
WHERE (CAST(s.subscriberId AS INT) NOT IN
      (SELECT g.userId FROM st_group_user_rel g WHERE s.subscribertype = 'USER' AND ${groupInclusionClause}))
  AND (CAST(s.subscriberId AS INT) NOT IN
      (SELECT r.userid FROM st_userrole u INNER JOIN st_userrole_user_rel r ON u.id = r.userroleid
        WHERE s.subscriberType = 'USER' AND s.instanceId LIKE CONCAT('%', u.instanceId)
        AND ((s.resourceId = '0' AND u.objectId IS NULL) OR CAST(u.objectId AS VARCHAR(100)) = s.resourceId)))
  AND CAST(s.subscriberId AS INT) NOT IN
      (SELECT r.groupId FROM st_userrole u INNER JOIN st_userrole_group_rel r ON u.id = r.userroleid
      WHERE s.subscriberType = 'GROUP' AND s.instanceId LIKE CONCAT('%', u.instanceId)
        AND ((s.resourceId = '0' AND u.objectId IS NULL) OR CAST(u.objectId AS VARCHAR(100)) = s.resourceId))
"""

sql.eachRow(findAllOrphanSubscriptions) { row ->
  log.info "Found orphan subscription of ${row.subscriberType.toLowerCase()} ${row.creatorId} on instance ${row.instanceId} and resource ${row.resourceId}"
  sql.execute('DELETE FROM subscribe WHERE subscriberId = ? AND subscriberType = ? AND instanceId = ? AND creatorId = ? AND resourceid = ?',
      [row.subscriberId, row.subscriberType, row.instanceId, row.creatorId, row.resourceId])
}