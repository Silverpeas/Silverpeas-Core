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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getThreadCacheService;

/**
 * This class manage groups that are described as follows : The group object contains an attribute
 * that point to each users and sub-groups DN Example (with the attribute 'member') : member
 * cn=user1,ou=people,o=stratelia member cn=user2,ou=people,o=stratelia .... THOSE GROUPS ARE ALL
 * CONSIDERED AS ROOT GROUPS ALL DESCENDENT USERS ARE PUT IN FIRST LEVEL USERS
 */
public class LDAPGroupAllRoot extends AbstractLDAPGroup {

  private static final String LDAPGROUP_ALL_ROOT_GET_CHILD_GROUPS_ENTRY =
      "LDAPGroupAllRoot.getChildGroupsEntry()";
  private static final String GROUP_CYCLIC_PROCESS_SECURITY_CACHE_KEY =
      LDAPGroupAllRoot.class.getName() + "#getUserSpecificIds";

  private List<String> getMemberGroupIds(String lds, String memberId, boolean isGroup)
      throws AdminException {
    List<String> groupsVector = new ArrayList<>();

    if (StringUtil.isDefined(memberId)) {
      LDAPEntry memberEntry = getMemberEntry(lds, memberId, isGroup);
      String groupsMemberField = driverSettings.getGroupsMemberField();
      LDAPEntry[] theEntries;
      if ("memberUid".equalsIgnoreCase(groupsMemberField)) {
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
            driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "=" + memberId + "))",
            driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
      } else {
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
            driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "="
            + memberEntry.getDN() + "))",
            driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
      }
      for (LDAPEntry currentEntry : theEntries) {

        groupsVector.add(getGroupId(currentEntry));
      }
    }
    return groupsVector;
  }

  @Override
  public String[] getGroupMemberGroupIds(String lds, String groupId) {
    return ArrayUtil.emptyStringArray();
  }

  @Override
  public String[] getUserMemberGroupIds(String lds, String userId) throws AdminException {
    Set<String> groupsManaged = new HashSet<>();

    List<String> groupsIdsSet = getMemberGroupIds(lds, userId, false);
    // Go recurs to all group's ancestors
    while (!groupsIdsSet.isEmpty()) {
      List<String> groupsCur = new ArrayList<>();
      for (String grId : groupsIdsSet) {
        if (StringUtil.isDefined(grId) && !groupsManaged.contains(grId)) {
          groupsManaged.add(grId);
          groupsCur.addAll(getMemberGroupIds(lds, grId, true));
        }
      }
      groupsIdsSet = groupsCur;
    }
    return groupsManaged.toArray(new String[0]);
  }

  @Override
  protected String[] getUserIds(String lds, LDAPEntry groupEntry) {
    final Set<String> usersManaged = new HashSet<>();
    final Set<String> groupsManaged = new HashSet<>();
    List<LDAPEntry> groupsSet = new ArrayList<>();
    groupsSet.add(groupEntry);
    while (!groupsSet.isEmpty()) {
      final List<LDAPEntry> groupsCur = new ArrayList<>();
      for (LDAPEntry curGroup : groupsSet) {
        if (curGroup != null) {
          final String grId = getGroupId(curGroup);
          if (!groupsManaged.contains(grId)) {
            groupsManaged.add(grId);
            usersManaged.addAll(getUserSpecificIds(lds, curGroup));
            groupsCur.addAll(getChildGroupsEntry(lds, curGroup));
          }
        }
      }
      groupsSet = groupsCur;
    }
    return usersManaged.toArray(new String[0]);
  }

  @SuppressWarnings("unchecked")
  private List<String> getUserSpecificIds(String lds, LDAPEntry groupEntry) {
    final Set<String> processedGroups = getThreadCacheService().getCache().computeIfAbsent(GROUP_CYCLIC_PROCESS_SECURITY_CACHE_KEY, Set.class, HashSet::new);
    final boolean isFirstCall = processedGroups.isEmpty();
    final List<String> users = new ArrayList<>();
    try {
      if (!processedGroups.add(groupEntry.getDN())) {
        final String warning = format(
            "Users of group with DN ''{0}'' have already been retrieved (please verify a potential circular case)",
            groupEntry.getDN());
        SilverLogger.getLogger(this).warn(warning);
        SynchroDomainReport.warn(LDAPGROUP_ALL_ROOT_GET_CHILD_GROUPS_ENTRY, warning);
      } else {
        final String groupsMemberField = driverSettings.getGroupsMemberField();
        final String[] stringVals = LDAPUtility.getAttributeValues(groupEntry, groupsMemberField);
        for (final String memberFieldValue : stringVals) {
          try {
            if ("memberUid".equals(groupsMemberField)) {
              // Case of most common OpenLDAP implementation.  memberUid = specificId
              Optional.ofNullable(getUserEntryByUID(lds, memberFieldValue))
                      .ifPresent(e -> users.add(memberFieldValue));
            } else {
              // Case of ActiveDirectory, NDS, OpenDS or OpenDJ (member = dn)
              // a group member can be a user of a group
              // first case, get member as user
              findUserIdsByDNOrUID(lds, memberFieldValue, users);
            }
          } catch (AdminException e) {
            SilverLogger.getLogger(this).error("USER NOT FOUND : " + LDAPUtility.
                dblBackSlashesForDNInFilters(memberFieldValue), e);
          }
        }
      }
    } finally {
      if (isFirstCall) {
        getThreadCacheService().getCache().remove(GROUP_CYCLIC_PROCESS_SECURITY_CACHE_KEY);
      }
    }
    return users;
  }

  private void findUserIdsByDNOrUID(final String lds, final String memberFieldValue,
      final List<String> users) throws AdminException {
    LDAPEntry userEntry = getUserEntryByDN(lds, memberFieldValue);
    if (userEntry != null) {
      String userSpecificId = getUserId(userEntry);
      // Verify that the user exist in the scope
      userEntry = getUserEntryByUID(lds, userSpecificId);
      if (userEntry != null) {
        users.add(userSpecificId);
      }
    } else {
      // second case, get member as group
      // users of this group must be added to current group
      LDAPEntry gEntry = getGroupEntryByDN(lds, memberFieldValue);
      if (gEntry != null) {
        users.addAll(getUserSpecificIds(lds, gEntry));
      }
    }
  }

  @Override
  protected LDAPEntry[] getChildGroupsEntry(String lds, String parentId, String extraFilter) throws
      AdminException {
    if (StringUtil.isDefined(parentId)) { // ALL ROOT GROUPS
      return new LDAPEntry[0];
    } else {
      LDAPEntry[] theEntries = null;
      String theFilter = driverSettings.getGroupsFullFilter();

      if (StringUtil.isDefined(extraFilter)) {
        theFilter = "(&" + extraFilter + driverSettings.getGroupsFullFilter() + ")";
      }
      try {

        theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
            driverSettings.getScope(),
            theFilter, driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
        SynchroDomainReport.debug(LDAPGROUP_ALL_ROOT_GET_CHILD_GROUPS_ENTRY,
            "Récupération de " + theEntries.length + " groupes racine");
      } catch (AdminException e) {
        if (synchroInProcess) {
          SilverLogger.getLogger(this).warn(e);
          append("PB getting Group's subgroups : ").append(parentId).append("\n");
          SynchroDomainReport.error(LDAPGROUP_ALL_ROOT_GET_CHILD_GROUPS_ENTRY,
              "Erreur lors de la récupération des groupes racine (parentId = " + parentId + ")", e);
          theEntries = new LDAPEntry[0];
        } else {
          throw e;
        }
      }
      return theEntries;
    }
  }

  private List<LDAPEntry> getChildGroupsEntry(String lds, LDAPEntry theEntry) {
    try {
      LDAPEntry[] entries = LDAPUtility.search1000Plus(lds, theEntry.getDN(),
          driverSettings.getScope(), driverSettings.getGroupsFullFilter(),
          driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
      if(entries != null) {
        List<LDAPEntry> subGroups = new ArrayList<>();
        for (LDAPEntry entry : entries) {
          if (!entry.getDN().equals(theEntry.getDN())) {
            subGroups.add(entry);
          }
        }
        return subGroups;
      }
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error("GETTING SUBGROUPS FAILED FOR: " + theEntry.getDN(), e);
    }
    return new ArrayList<>();
  }

  @Override
  public GroupDetail[] getAllChangedGroups(String lds, String extraFilter) throws AdminException {
    Map<String, Group> groupsManaged = new HashMap<>();
    LDAPEntry[] les = getChildGroupsEntry(lds, null, extraFilter);

    List<LDAPEntry> groupsIdsSet = new ArrayList<>(les.length);
    for (LDAPEntry childGroupEntry : les) {
      groupsIdsSet.add(childGroupEntry);
      groupsManaged.put(childGroupEntry.getDN(), translateGroup(lds, childGroupEntry));
    }
    // Go recurs to all group's ancestors
    while (!groupsIdsSet.isEmpty()) {
      List<LDAPEntry> groupsCur = new ArrayList<>();
      for ( LDAPEntry theGroup : groupsIdsSet) {

        les = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
            driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "=" + theGroup.getDN()
            + "))", driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
        for (LDAPEntry childGroupEntry : les) {

          if (!groupsManaged.containsKey(childGroupEntry.getDN())) {

            groupsCur.add(childGroupEntry);
            groupsManaged.put(childGroupEntry.getDN(), translateGroup(lds, childGroupEntry));
          }
        }
      }
      groupsIdsSet = groupsCur;
    }
    return groupsManaged.values().toArray(new GroupDetail[0]);
  }

  private String getGroupId(LDAPEntry entry) {
    return LDAPUtility.getFirstAttributeValue(entry, driverSettings.getGroupsIdField());
  }

  private String getUserId(LDAPEntry entry) {
    return LDAPUtility.getFirstAttributeValue(entry, driverSettings.getUsersIdField());
  }

  private LDAPEntry getUserEntryByUID(String lds, String uid) throws AdminException {
    return LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
        driverSettings.getScope(), driverSettings.getUsersIdFilter(uid),
        driverSettings.getUserAttributes());
  }

  private LDAPEntry getUserEntryByDN(String lds, String dn) throws AdminException {
    return LDAPUtility.getFirstEntryFromSearch(lds, dn, driverSettings.getScope(),
        driverSettings.getUsersFullFilter(), driverSettings.getUserAttributes());
  }

  private LDAPEntry getGroupEntryByDN(String lds, String dn) throws AdminException {
    return LDAPUtility.getFirstEntryFromSearch(lds, dn, driverSettings.getScope(),
        driverSettings.getGroupsFullFilter(), driverSettings.getGroupAttributes());
  }
}