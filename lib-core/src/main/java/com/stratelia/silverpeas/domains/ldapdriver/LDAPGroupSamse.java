/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class manage groups that are described as follows : The group object contains an attribute
 * that point to each users and sub-groups DN Example (with the attribute 'memberUid') : memberUid
 * 003478 memberUid 004512 .... THOSE GROUPS ARE ALL CONSIDERED AS ROOT GROUPS ALL DESCENDENT USERS
 * ARE PUT IN FIRST LEVEL USERS
 * @author neysseri
 */

public class LDAPGroupSamse extends AbstractLDAPGroup {
  protected Vector<String> getMemberGroupIds(String lds, String memberId,
      boolean isGroup) throws AdminException {
    Vector<String> groupsVector = new Vector<String>();
    LDAPEntry[] theEntries = null;
    LDAPEntry memberEntry = null;
    int i;

    SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()",
        "root.MSG_GEN_ENTER_METHOD", "MemberId=" + memberId + ", isGroup="
        + isGroup);
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

    theEntries = LDAPUtility.search1000Plus(lds, driverSettings
        .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), "(&"
        + driverSettings.getGroupsFullFilter() + "("
        + driverSettings.getGroupsMemberField() + "=" + memberId + "))",
        driverSettings.getGroupsNameField(), driverSettings
        .getGroupAttributes());

    for (i = 0; i < theEntries.length; i++) {
      SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()",
          "root.MSG_GEN_PARAM_VALUE", "GroupFound=" + theEntries[i].getDN());
      groupsVector.add(LDAPUtility.getFirstAttributeValue(theEntries[i],
          driverSettings.getGroupsIdField()));
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
  public String[] getGroupMemberGroupIds(String lds, String groupId)
      throws AdminException {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public String[] getUserMemberGroupIds(String lds, String userId)
      throws AdminException {
    Vector<String> groupsCur;
    Iterator<String> it;
    String grId;
    HashSet<String> groupsManaged = new HashSet<String>();

    Vector<String> groupsIdsSet = getMemberGroupIds(lds, userId, false);
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      it = groupsIdsSet.iterator();
      groupsCur = new Vector<String>();
      while (it.hasNext()) {
        grId = it.next();
        if (!groupsManaged.contains(grId)) {
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
  protected String[] getUserIds(String lds, LDAPEntry groupEntry)
      throws AdminException {
    HashSet<String> usersManaged = new HashSet<String>();
    HashSet<String> groupsManaged = new HashSet<String>();
    Vector<LDAPEntry> groupsSet = new Vector<LDAPEntry>();
    Vector<LDAPEntry> groupsCur;
    Iterator<LDAPEntry> it;
    LDAPEntry curGroup;
    String grId;

    groupsSet.add(groupEntry);
    while (groupsSet.size() > 0) {
      it = groupsSet.iterator();
      groupsCur = new Vector<LDAPEntry>();
      while (it.hasNext()) {
        curGroup = it.next();
        if (curGroup != null) {
          grId = "???";
          try {
            grId = LDAPUtility.getFirstAttributeValue(curGroup, driverSettings
                .getGroupsIdField());
            if (!groupsManaged.contains(grId)) {
              groupsManaged.add(grId);
              usersManaged.addAll(getTRUEUserIds(lds, curGroup));
              // Pas de sous groupes chez SAMSE
              // groupsCur.addAll(getTRUEChildGroupsEntry(lds, grId, curGroup));
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

  protected Vector<String> getTRUEUserIds(String lds, LDAPEntry groupEntry)
      throws AdminException {
    SilverTrace.info("admin", "LDAPGroupSamse.getTRUEUserIds()",
        "root.MSG_GEN_ENTER_METHOD", "lds = " + lds + ", group = "
        + groupEntry.getDN());

    Vector<String> usersVector = new Vector<String>();
    String groupsMemberField = driverSettings.getGroupsMemberField();

    // retrieve all memberUid
    String[] stringVals = LDAPUtility.getAttributeValues(groupEntry, groupsMemberField);
    for (int i = 0; i < stringVals.length; i++) {
      SilverTrace.info("admin", "LDAPGroupSamse.getTRUEUserIds()",
          "root.MSG_GEN_PARAM_VALUE", "stringVals[" + i + "] = "
          + stringVals[i]);
      usersVector.add(stringVals[i]);
    }
    stringVals = null;
    SilverTrace.info("admin", "LDAPGroupSamse.getTRUEUserIds()",
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
        SilverTrace.info("admin", "LDAPGroupSamse.getChildGroupsEntry()",
            "root.MSG_GEN_PARAM_VALUE", "Root Group Search");
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            theFilter, driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
        SynchroReport.debug("LDAPGroupSamse.getChildGroupsEntry()",
            "Récupération de " + theEntries.length + " groupes racine", null);
      } catch (AdminException e) {
        if (synchroInProcess) {
          SilverTrace.warn("admin", "LDAPGroupSamse.getChildGroupsEntry()",
              "admin.EX_ERR_CHILD_GROUPS", "ParentGroupId=" + parentId, e);
          append("PB getting Group's subgroups : ").append(parentId).append("\n");
          SynchroReport.error("LDAPGroupSamse.getChildGroupsEntry()",
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
  protected Vector<LDAPEntry> getTRUEChildGroupsEntry(String lds, String parentId,
      LDAPEntry theEntry) {
    LDAPEntry childGroupEntry = null;
    Vector<LDAPEntry> entryVector = new Vector<LDAPEntry>();
    String[] stringVals = null;
    int i;

    if ((parentId != null) && (parentId.length() > 0)) {
      SilverTrace.info("admin", "LDAPGroupSamse.getTRUEChildGroupsEntry()",
          "root.MSG_GEN_PARAM_VALUE", "Root Group Search : " + parentId);
      stringVals = LDAPUtility.getAttributeValues(theEntry, driverSettings
          .getGroupsMemberField());
      for (i = 0; i < stringVals.length; i++) {
        try {
          childGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds,
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
              "LDAPGroupSamse.getTRUEChildGroupsEntry()",
              "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : "
              + stringVals[i], e);
        }
      }
    }
    return entryVector;
  }

  public Group[] getAllChangedGroups(String lds, String extraFilter)
      throws AdminException {
    Vector<LDAPEntry> groupsCur;
    Iterator<LDAPEntry> it;
    int i;
    Hashtable<String, Group> groupsManaged = new Hashtable<String, Group>();
    LDAPEntry[] les = getChildGroupsEntry(lds, null, extraFilter);
    LDAPEntry theGroup;

    Vector<LDAPEntry> groupsIdsSet = new Vector<LDAPEntry>(les.length);
    for (i = 0; i < les.length; i++) {
      groupsIdsSet.add(les[i]);
      groupsManaged.put(les[i].getDN(), translateGroup(lds, les[i]));
    }
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      it = groupsIdsSet.iterator();
      groupsCur = new Vector<LDAPEntry>();
      while (it.hasNext()) {
        theGroup = it.next();
        SilverTrace.info("admin", "LDAPGroupSamse.getAllChangedGroups()",
            "root.MSG_GEN_PARAM_VALUE", "GroupTraite2="
            + theGroup.getDN());
        les = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            "(&" + driverSettings.getGroupsFullFilter() + "("
            + driverSettings.getGroupsMemberField() + "="
            + LDAPUtility.dblBackSlashesForDNInFilters(theGroup.getDN())
            + "))", driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
        for (i = 0; i < les.length; i++) {
          SilverTrace.info("admin", "LDAPGroupSamse.getAllChangedGroups()",
              "root.MSG_GEN_PARAM_VALUE", "GroupFound2=" + les[i].getDN());
          if (!groupsManaged.containsKey(les[i].getDN())) {
            SilverTrace.info("admin", "LDAPGroupSamse.getAllChangedGroups()",
                "root.MSG_GEN_PARAM_VALUE", "GroupAjoute2="
                + les[i].getDN());
            groupsCur.add(les[i]);
            groupsManaged.put(les[i].getDN(), translateGroup(lds,
                les[i]));
          }
        }
      }
      groupsIdsSet = groupsCur;
    }
    return groupsManaged.values().toArray(new Group[groupsManaged.size()]);
  }
}
