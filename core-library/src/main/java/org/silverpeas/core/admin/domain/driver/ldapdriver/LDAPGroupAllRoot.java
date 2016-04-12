/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class manage groups that are described as follows : The group object contains an attribute
 * that point to each users and sub-groups DN Example (with the attribute 'member') : member
 * cn=user1,ou=people,o=stratelia member cn=user2,ou=people,o=stratelia .... THOSE GROUPS ARE ALL
 * CONSIDERED AS ROOT GROUPS ALL DESCENDENT USERS ARE PUT IN FIRST LEVEL USERS
 */
public class LDAPGroupAllRoot extends AbstractLDAPGroup {

  protected List<String> getMemberGroupIds(String lds, String memberId, boolean isGroup)
      throws AdminException {
    List<String> groupsVector = new ArrayList<>();

    if (StringUtil.isDefined(memberId)) {
      LDAPEntry memberEntry;

      if (isGroup) {
        memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.
            getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            driverSettings.getGroupsIdFilter(memberId), driverSettings.getGroupAttributes());
      } else {
        memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
            driverSettings.getScope(), driverSettings.getUsersIdFilter(memberId), driverSettings.
            getGroupAttributes());
      }
      if (memberEntry == null) {
        throw new AdminException("LDAPGroupAllRoot.getMemberGroupIds",
            SilverpeasException.ERROR, "admin.EX_ERR_LDAP_USER_ENTRY_ISNULL",
            "Id=" + memberId + " IsGroup=" + isGroup);
      }
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

  /**
   * All root groups, so, no group belongs to another...
   *
   * @param lds
   * @param groupId the group's Id
   * @return
   * @throws AdminException
   */
  @Override
  public String[] getGroupMemberGroupIds(String lds, String groupId) throws AdminException {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  public String[] getUserMemberGroupIds(String lds, String userId) throws AdminException {
    Set<String> groupsManaged = new HashSet<>();

    List<String> groupsIdsSet = getMemberGroupIds(lds, userId, false);
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      List<String> groupsCur = new ArrayList<>();
      for (String grId : groupsIdsSet) {
        if (StringUtil.isDefined(grId) && !groupsManaged.contains(grId)) {
          groupsManaged.add(grId);
          groupsCur.addAll(getMemberGroupIds(lds, grId, true));
        }
      }
      groupsIdsSet = groupsCur;
    }
    return groupsManaged.toArray(new String[groupsManaged.size()]);
  }

  /**
   * Method declaration
   *
   * @param lds
   * @param groupEntry
   * @return
   * @throws AdminException
   * @see
   */
  @Override
  protected String[] getUserIds(String lds, LDAPEntry groupEntry) throws AdminException {
    Set<String> usersManaged = new HashSet<>();
    Set<String> groupsManaged = new HashSet<>();
    List<LDAPEntry> groupsSet = new ArrayList<>();

    groupsSet.add(groupEntry);
    while (groupsSet.size() > 0) {
      List<LDAPEntry> groupsCur = new ArrayList<>();
      for (LDAPEntry curGroup : groupsSet) {
        if (curGroup != null) {
          String grId = "???";
          try {
            grId = getGroupId(curGroup);
            if (!groupsManaged.contains(grId)) {
              groupsManaged.add(grId);
              usersManaged.addAll(getTRUEUserIds(lds, curGroup));
              groupsCur.addAll(getTRUEChildGroupsEntry(lds, curGroup));
            }
          } catch (AdminException e) {

          }
        }
      }
      groupsSet = groupsCur;
    }
    return usersManaged.toArray(new String[usersManaged.size()]);
  }

  protected List<String> getTRUEUserIds(String lds, LDAPEntry groupEntry) throws AdminException {

    List<String> usersVector = new ArrayList<>();
    String groupsMemberField = driverSettings.getGroupsMemberField();
    String[] stringVals = LDAPUtility.getAttributeValues(groupEntry, groupsMemberField);
    for (String memberFieldValue : stringVals) {

      try {
        LDAPEntry userEntry;
        if ("memberUid".equals(groupsMemberField)) {
          // Case of most common OpenLDAP implementation.  memberUid = specificId
          userEntry = getUserEntryByUID(lds, memberFieldValue);
          if (userEntry != null) {
            usersVector.add(memberFieldValue);
          }
        } else {
          // Case of ActiveDirectory, NDS, OpenDS or OpenDJ (member = dn)
          // a group member can be a user of a group
          // first case, get member as user
          userEntry = getUserEntryByDN(lds, memberFieldValue);
          if (userEntry != null) {
            String userSpecificId = getUserId(userEntry);
            // Verify that the user exist in the scope
            userEntry = getUserEntryByUID(lds, userSpecificId);
            if (userEntry != null) {
              usersVector.add(userSpecificId);
            }
          } else {
            // second case, get member as group
            // users of this group must be added to current group
            LDAPEntry gEntry = getGroupEntryByDN(lds, memberFieldValue);
            if (gEntry != null) {
              usersVector.addAll(getTRUEUserIds(lds, gEntry));
            }
          }
        }
      } catch (AdminException e) {
        SilverTrace.error("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
            "admin.MSG_ERR_LDAP_GENERAL", "USER NOT FOUND : " + LDAPUtility.
            dblBackSlashesForDNInFilters(memberFieldValue), e);
      }
    }

    return usersVector;
  }

  /**
   * Method declaration THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds
   * @param parentId
   * @return
   * @throws AdminException
   * @see
   */
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
        SynchroDomainReport.debug("LDAPGroupAllRoot.getChildGroupsEntry()",
            "Récupération de " + theEntries.length + " groupes racine");
      } catch (AdminException e) {
        if (synchroInProcess) {
          SilverTrace.warn("admin", "LDAPGroupAllRoot.getChildGroupsEntry()",
              "admin.EX_ERR_CHILD_GROUPS", "ParentGroupId=" + parentId, e);
          append("PB getting Group's subgroups : ").append(parentId).append("\n");
          SynchroDomainReport.error("LDAPGroupAllRoot.getChildGroupsEntry()",
              "Erreur lors de la récupération des groupes racine (parentId = " + parentId + ")", e);
        } else {
          throw e;
        }
      }
      return theEntries;
    }
  }

  /**
   * Method declaration THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   *
   * @param lds
   * @param theEntry
   * @return
   * @throws AdminException
   * @see
   */
  protected List<LDAPEntry> getTRUEChildGroupsEntry(String lds, LDAPEntry theEntry) {
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
      SilverTrace.error("admin", "LDAPGroupAllRoot.getTRUEChildGroupsEntry()",
          "admin.MSG_ERR_LDAP_GENERAL", "GETTING SUBGROUPS FAILED FOR : "
          + theEntry.getDN(), e);
    }
    return new ArrayList<>();
  }

  @Override
  public Group[] getAllChangedGroups(String lds, String extraFilter) throws AdminException {
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
    return groupsManaged.values().toArray(new Group[groupsManaged.size()]);
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