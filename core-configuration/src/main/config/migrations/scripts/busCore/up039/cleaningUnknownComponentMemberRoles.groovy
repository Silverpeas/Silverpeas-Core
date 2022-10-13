/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Represents a role from 'st_userrole' database table.
 */
class Role {
  int id
  String instanceId
  String name
  String rolename
  String description
  Integer isinherited
  Integer objectId
  String objecttype

  Role(row) {
    this.id = row.id
    this.instanceId = row.instanceId
    this.name = row.name
    this.rolename = row.rolename
    this.description = row.description
    this.isinherited = row.isinherited
    this.objectId = row.objectId
    this.objecttype = row.objecttype
  }

  /**
   * Indicates if the role is an unknown one
   * @return true if unknown, false otherwise
   */
  boolean isUnknownRole() {
    return objecttype != null && objecttype != 'O'
  }

  @Override
  String toString() {
    return "${String.valueOf(id)} - ${instanceId} - ${name} - ${rolename} - ${description} - ${isinherited} - ${objectId} - ${objecttype}"
  }
}

/**
 * Represents a user or a group associated to a role.
 */
class MemberRole {
  int memberId
  Role role

  MemberRole(row, Role role) {
    this.memberId = row.memberId
    this.role = role
  }

  @Override
  String toString() {
    return "${String.valueOf(memberId)} # ${role}"
  }
}

/*
 * Context which MUST have to be instantiated one time only.
 */
class Context {
  public def log
  public def sql
  public int nbRoleDeletion = 0
  public int nbUserRoleDeletion = 0
  public int nbGroupRoleDeletion = 0
  public Set<String> instanceIdsWithDeletions = new HashSet<>()

  Context(def log, def sql) {
    this.log = log
    this.sql = sql
  }
}

static void deleteUnknownRole(Context c, Role role) {
  if (role.isUnknownRole()) {
    c.log.info " -> Deleting role ${role}"
    c.instanceIdsWithDeletions.add(role.instanceId)
    c.sql.rows('SELECT userroleid, groupid AS memberId FROM st_userrole_group_rel WHERE userroleid = ?', [role.id]).each {row ->
      MemberRole groupRole = new MemberRole(row, role)
      deleteUnknownGroupRole(c, groupRole)
    }
    c.sql.rows('SELECT userroleid, userid AS memberId FROM st_userrole_user_rel WHERE userroleid = ?', [role.id]).each { row ->
      MemberRole userRole = new MemberRole(row, role)
      deleteUnknownUserRole(c, userRole)
    }
    c.nbRoleDeletion += c.sql.executeUpdate('DELETE FROM st_userrole WHERE id = ?', [role.id])
  } else {
    c.log.warn "trying to delete role [${role}] which is not an unknown role!!!"
  }
}

static void deleteUnknownUserRole(Context c, MemberRole memberRole) {
  if (memberRole.role.isUnknownRole()) {
    c.log.info " -> Deleting user role ${memberRole}"
    c.instanceIdsWithDeletions.add(memberRole.role.instanceId)
    c.nbUserRoleDeletion += c.sql.executeUpdate('DELETE FROM st_userrole_user_rel WHERE userroleid = ? AND userid = ?', [memberRole.role.id, memberRole.memberId])
  } else {
    c.log.warn "trying to delete user role [${memberRole}] which is not an unknown role!!!"
  }
}

static void deleteUnknownGroupRole(Context c, MemberRole memberRole) {
  if (memberRole.role.isUnknownRole()) {
    c.log.info " -> Deleting group role ${memberRole}"
    c.instanceIdsWithDeletions.add(memberRole.role.instanceId)
    c.nbGroupRoleDeletion += c.sql.executeUpdate('DELETE FROM st_userrole_group_rel WHERE userroleid = ? AND groupid = ?', [memberRole.role.id, memberRole.memberId])
  } else {
    c.log.warn "trying to delete user role [${memberRole}] which is not an unknown role!!!"
  }
}

Context context = new Context(log, sql)
log.info 'Search and cleaning unknown rights on component instances'
long start = System.currentTimeMillis()
List<Role> unknownRoles = context.sql.rows('''
SELECT *
FROM st_userrole r
WHERE objecttype IS not null
  AND objecttype <> 'O'
ORDER BY instanceid, objectid, id
''').stream()
    .map({row -> new Role(row)})
    .collect()
log.info "Identifying ${unknownRoles.size()} unknown roles"
unknownRoles.forEach({ r ->
  deleteUnknownRole(context, r)
})
long end = System.currentTimeMillis()
log.info "...search and cleaning in ${end - start}ms"

log.info "Stats:"
log.info "  ${unknownRoles.size()} concerned instances at all"
log.info "  ${context.instanceIdsWithDeletions.size()} instances for which roles have been deleted"
log.info "  ${context.nbRoleDeletion} role deletions, ${context.nbUserRoleDeletion} user role deletions, ${context.nbGroupRoleDeletion} group role deletions"
