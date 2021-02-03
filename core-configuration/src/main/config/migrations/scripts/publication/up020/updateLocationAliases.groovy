import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.sql.SQLException

/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

log.info 'Fix missing main location of some publications'

void unaliasLocation(Sql sql, GroovyRowResult location) {
  log.info "-> set main location of publication ${location.pubId} to node {${location.nodeId}, ${location.instanceId}}"
  int result = sql.executeUpdate('''
UPDATE sb_publication_publifather SET aliasuserid = NULL, aliasDate = NULL
WHERE pubId = ? AND nodeId = ? AND instanceId = ?
''', [location.pubId, location.nodeId, location.instanceId])

  if (result != 1) {
    throw new SQLException("Unable to set main location of publication ${location.pubId} to node {${location.nodeId}, ${location.instanceId}}")
  }
}

long start = System.currentTimeMillis()
Sql theSql = sql
List<GroovyRowResult> locations = theSql.rows('''
SELECT F.pubId, F.nodeId, F.instanceId, F.aliasUserId, F.aliasDate FROM sb_publication_publifather F
    INNER JOIN sb_publication_publi P ON F.pubid = P.pubid
WHERE F.instanceid = P.instanceid
    AND F.pubId NOT IN (SELECT pubId FROM sb_publication_publifather PF 
        WHERE PF.pubId = F.pubId AND (PF.aliasUserId IS NULL OR PF.aliasDate IS NULL))
ORDER BY pubId, aliasDate
''')

int count = 0
if (!locations.isEmpty()) {
  int i = 0
  while (i < locations.size()) {
    unaliasLocation(theSql, locations[i])
    count++
    int pubId = locations[i].pubId
    while (i < locations.size() && pubId == locations[i].pubId) {
      i = i + 1
    }
  }
}

log.info "=> Time to fix missing main location of ${count} publications: ${System.currentTimeMillis() - start}ms"
