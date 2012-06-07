/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPEntry;
import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * This class manage groups that are described as follows : The group object contains an attribute
 * that point to each users and sub-groups DN Example (with the attribute 'member') : member
 * cn=user1,ou=people,o=stratelia member cn=user2,ou=people,o=stratelia .... THOSE GROUPS ARE ALL
 * CONSIDERED AS ROOT GROUPS ALL DESCENDENT USERS ARE PUT IN FIRST LEVEL USERS
 * @author tleroi
 */

public class LDAPGroupAllRoot extends AbstractLDAPGroup {
  protected List<String> getMemberGroupIds(String lds, String memberId, boolean isGroup)
      throws AdminException {
    List<String> groupsVector = new ArrayList<String>();

    if (StringUtil.isDefined(memberId)) {
      LDAPEntry[] theEntries;
      LDAPEntry memberEntry;
      int i;

      SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()",
          "root.MSG_GEN_ENTER_METHOD", "MemberId=" + memberId + ", isGroup=" + isGroup);
      if (isGroup) {
        memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            driverSettings.getGroupsIdFilter(memberId), driverSettings
            .getGroupAttributes());
      } else {
        memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
            .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
            .getUsersIdFilter(memberId), driverSettings.getGroupAttributes());
      }
      if (memberEntry == null) {
        throw new AdminException("LDAPGroupAllRoot.getMemberGroupIds",
            SilverpeasException.ERROR, "admin.EX_ERR_LDAP_USER_ENTRY_ISNULL",
            "Id=" + memberId + " IsGroup=" + isGroup);
      }
      String groupsMemberField = driverSettings.getGroupsMemberField();
      if ("memberUid".equals(groupsMemberField)) {
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), "(&"
            + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "=" + memberId + "))",
            driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
      } else {
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), "(&"
            + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "="
            + LDAPUtility.dblBackSlashesForDNInFilters(memberEntry.getDN())
            + "))", driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
      }
      for (i = 0; i < theEntries.length; i++) {
        SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()",
            "root.MSG_GEN_PARAM_VALUE", "GroupFound=" + theEntries[i].getDN());
        groupsVector.add(LDAPUtility.getFirstAttributeValue(theEntries[i],
            driverSettings.getGroupsIdField()));
      }
    }
    return groupsVector;
  }

  /**
   * All root groups, so, no group belongs to another...
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
    Set<String> groupsManaged = new HashSet<String>();

    List<String> groupsIdsSet = getMemberGroupIds(lds, userId, false);
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      Iterator<String> it = groupsIdsSet.iterator();
      List<String> groupsCur = new ArrayList<String>();
      while (it.hasNext()) {
        String grId = it.next();
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
   * @param lds
   * @param groupEntry
   * @return
   * @throws AdminException
   * @see
   */
  @Override
  protected String[] getUserIds(String lds, LDAPEntry groupEntry) throws AdminException {
    Set<String> usersManaged = new HashSet<String>();
    Set<String> groupsManaged = new HashSet<String>();
    List<LDAPEntry> groupsSet = new ArrayList<LDAPEntry>();
    String grId;

    groupsSet.add(groupEntry);
    while (groupsSet.size() > 0) {
      Iterator<LDAPEntry> it = groupsSet.iterator();
      List<LDAPEntry> groupsCur = new ArrayList<LDAPEntry>();
      while (it.hasNext()) {
        LDAPEntry curGroup = it.next();
        if (curGroup != null) {
          grId = "???";
          try {
            grId = LDAPUtility.getFirstAttributeValue(curGroup, driverSettings.getGroupsIdField());
            if (!groupsManaged.contains(grId)) {
              groupsManaged.add(grId);
              usersManaged.addAll(getTRUEUserIds(lds, curGroup));
              groupsCur.addAll(getTRUEChildGroupsEntry(lds, grId, curGroup));
            }
          } catch (AdminException e) {
            SilverTrace.info("admin", "LDAPGroupAllRoot.getUserIds()",
                "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : " + grId, e);
          }
        }
      }
      groupsSet = groupsCur;
    }
    return usersManaged.toArray(new String[usersManaged.size()]);
  }

  protected List<String> getTRUEUserIds(String lds, LDAPEntry groupEntry) throws AdminException {
    SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
        "root.MSG_GEN_ENTER_METHOD", "lds = " + lds + ", group = "
        + groupEntry.getDN());

    List<String> usersVector = new ArrayList<String>();
    LDAPEntry userEntry;
    int i;

    String groupsMemberField = driverSettings.getGroupsMemberField();

    String[] stringVals = LDAPUtility.getAttributeValues(groupEntry, groupsMemberField);
    for (i = 0; i < stringVals.length; i++) {
      SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
          "root.MSG_GEN_PARAM_VALUE", "stringVals[" + i + "] = "
          + stringVals[i]);
      try {
        if ("memberUid".equals(groupsMemberField)) {
          // Case of most common OpenLDAP implementation.
          // memberUid = specificId
          userEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
              .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
              .getUsersIdFilter(stringVals[i]), driverSettings.getGroupAttributes());
          if (userEntry != null) {
            usersVector.add(stringVals[i]);
          }
        } else {
          // Case of ActiveDirectory or NDS
          // member = dn
          userEntry = LDAPUtility.getFirstEntryFromSearch(lds, stringVals[i],
              driverSettings.getScope(), driverSettings.getUsersFullFilter(),
              driverSettings.getGroupAttributes());
          if (userEntry != null) {
            String userSpecificId = LDAPUtility.getFirstAttributeValue(
                userEntry, driverSettings.getUsersIdField());
            // Verify that the user exist in the scope
            if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
                .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
                .getUsersIdFilter(userSpecificId), driverSettings
                .getGroupAttributes()) != null)
              usersVector.add(userSpecificId);
          }
        }
      } catch (AdminException e) {
        SilverTrace.error("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
            "admin.MSG_ERR_LDAP_GENERAL", "USER NOT FOUND : "
            + LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]), e);
      }
    }
    SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
        "root.MSG_GEN_EXIT_METHOD");
    return usersVector;
  }

  /**
   * Method declaration THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   * @param lds
   * @param parentId
   * @return
   * @throws AdminException
   * @see
   */
  @Override
  protected LDAPEntry[] getChildGroupsEntry(String lds, String parentId,
      String extraFilter) throws AdminException {
    if ((parentId != null) && (parentId.length() > 0)) { // ALL ROOT GROUPS
      return ArrayUtil.EMPTY_LDAP_ENTRY_ARRAY;
    } else {
      LDAPEntry[] theEntries = null;
      String theFilter;

      if ((extraFilter != null) && (extraFilter.length() > 0)) {
        theFilter = "(&" + extraFilter + driverSettings.getGroupsFullFilter()
            + ")";
      } else {
        theFilter = driverSettings.getGroupsFullFilter();
      }
      try {
        SilverTrace.info("admin", "LDAPGroupAllRoot.getChildGroupsEntry()",
            "root.MSG_GEN_PARAM_VALUE", "Root Group Search");
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            theFilter, driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
        SynchroReport.debug("LDAPGroupAllRoot.getChildGroupsEntry()",
            "Récupération de " + theEntries.length + " groupes racine", null);
      } catch (AdminException e) {
        if (synchroInProcess) {
          SilverTrace.warn("admin", "LDAPGroupAllRoot.getChildGroupsEntry()",
              "admin.EX_ERR_CHILD_GROUPS", "ParentGroupId=" + parentId, e);
          append("PB getting Group's subgroups : ").append(parentId).append("\n");
          SynchroReport.error("LDAPGroupAllRoot.getChildGroupsEntry()",
              "Erreur lors de la récupération des groupes racine (parentId = "
              + parentId + ")", e);
        } else {
          throw e;
        }
      }
      return theEntries;
    }
  }

  /**
   * Method declaration THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
   * @param lds
   * @param parentId
   * @return
   * @throws AdminException
   * @see
   */
  protected List<LDAPEntry> getTRUEChildGroupsEntry(String lds, String parentId, LDAPEntry theEntry) {
    List<LDAPEntry> entryVector = new ArrayList<LDAPEntry>();
    int i;
    if ((parentId != null) && (parentId.length() > 0)) {
      SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEChildGroupsEntry()",
          "root.MSG_GEN_PARAM_VALUE", "Root Group Search : " + parentId);
       String[] stringVals = LDAPUtility.getAttributeValues(theEntry, driverSettings
          .getGroupsMemberField());
      for (i = 0; i < stringVals.length; i++) {
        try {
          LDAPEntry childGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds,
              stringVals[i], driverSettings.getScope(), driverSettings
              .getGroupsFullFilter(), driverSettings.getGroupAttributes());
          if (childGroupEntry != null) {
            // Verify that the group exist in the scope
            String groupSpecificId = LDAPUtility.getFirstAttributeValue(
                childGroupEntry, driverSettings.getGroupsIdField());
            if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
                .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
                driverSettings.getGroupsIdFilter(groupSpecificId),
                driverSettings.getGroupAttributes()) != null) {
              entryVector.add(childGroupEntry);
            }
          }
        } catch (AdminException e) {
          SilverTrace.error("admin",
              "LDAPGroupAllRoot.getTRUEChildGroupsEntry()",
              "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : "
              + stringVals[i], e);
        }
      }
    }
    return entryVector;
  }

  @Override
  public Group[] getAllChangedGroups(String lds, String extraFilter) throws AdminException {
    int i;
    Map<String, Group> groupsManaged = new HashMap<String, Group>();
    LDAPEntry[] les = getChildGroupsEntry(lds, null, extraFilter);
    List<LDAPEntry>  groupsIdsSet = new Vector<LDAPEntry>(les.length);
    for (LDAPEntry entry : les) {
      groupsIdsSet.add(entry);
      groupsManaged.put(entry.getDN(), translateGroup(lds, entry));
    }
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      Iterator<LDAPEntry> it = groupsIdsSet.iterator();
      List<LDAPEntry>  groupsCur = new ArrayList<LDAPEntry>();
      while (it.hasNext()) {
        LDAPEntry theGroup = it.next();
        SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()",
            "root.MSG_GEN_PARAM_VALUE", "GroupTraite2="
            + theGroup.getDN());
        les = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            "(&" + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "="
            + LDAPUtility.dblBackSlashesForDNInFilters(theGroup.getDN())
            + "))", driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
        for (LDAPEntry entry : les) {
          SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()",
              "root.MSG_GEN_PARAM_VALUE", "GroupFound2=" + entry.getDN());
          if (!groupsManaged.containsKey(entry.getDN())) {
            SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()",
                "root.MSG_GEN_PARAM_VALUE", "GroupAjoute2=" + entry.getDN());
            groupsCur.add(entry);
            groupsManaged.put(entry.getDN(), translateGroup(lds, entry));
          }
        }
      }
      groupsIdsSet = groupsCur;
    }
    return groupsManaged.values().toArray(new Group[ groupsManaged.size()]);
  }
}