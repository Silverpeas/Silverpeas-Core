/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/
package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class manage groups that are described as follows : The group object
 * contains an attribute that point to each users and sub-groups DN Example
 * (with the attribute 'member') : member cn=user1,ou=people,o=stratelia member
 * cn=user2,ou=people,o=stratelia .... THOSE GROUPS ARE ALL CONSIDERED AS ROOT
 * GROUPS ALL DESCENDENT USERS ARE PUT IN FIRST LEVEL USERS
 *
 * For CTI : Add new functionality : - Build Name from path (dn) - Use dn as key
 *
 * @author tleroi
 */

public class LDAPGroupCTI extends AbstractLDAPGroup {
  Hashtable TRUEChildGroupsEntrys = null;
  Hashtable TRUEUserIds = null;

  protected Vector getMemberGroupIds(String lds, String memberId,
      boolean isGroup) throws AdminException {
    Vector groupsVector = new Vector();
    LDAPEntry[] theEntries = null;
    LDAPEntry memberEntry = null;
    int i;

    SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()",
        "root.MSG_GEN_ENTER_METHOD", "MemberId=" + memberId + ", isGroup="
            + isGroup);
    if (isGroup) {
      memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, memberId,
          driverSettings.getScope(), driverSettings.getGroupsFullFilter(),
          driverSettings.getGroupAttributes());
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
        + driverSettings.getGroupsMemberField() + "="
        + LDAPUtility.dblBackSlashesForDNInFilters(memberEntry.getDN()) + "))",
        driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
    for (i = 0; i < theEntries.length; i++) {
      SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()",
          "root.MSG_GEN_PARAM_VALUE", "GroupFound=" + theEntries[i].getDN());
      groupsVector.add(theEntries[i].getDN());
    }
    return groupsVector;
  }

  public String[] getGroupMemberGroupIds(String lds, String groupId)
      throws AdminException {
    // All root groups, so, no group belongs to another...
    return new String[0];
  }

  public String[] getUserMemberGroupIds(String lds, String userId)
      throws AdminException {
    Vector groupsIdsSet;
    Vector groupsCur;
    Iterator it;
    String grId;
    HashSet groupsManaged = new HashSet();

    groupsIdsSet = getMemberGroupIds(lds, userId, false);
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      it = groupsIdsSet.iterator();
      groupsCur = new Vector();
      while (it.hasNext()) {
        grId = (String) it.next();
        if (!groupsManaged.contains(grId)) {
          groupsManaged.add(grId);
          groupsCur.addAll(getMemberGroupIds(lds, grId, true));
        }
      }
      groupsIdsSet = groupsCur;
    }
    return (String[]) groupsManaged.toArray(new String[0]);
  }

  /**
   * Method declaration
   *
   *
   * @param lds
   * @param groupEntry
   *
   * @return
   *
   * @throws AdminException
   *
   * @see
   */
  protected String[] getUserIds(String lds, LDAPEntry groupEntry)
      throws AdminException {
    HashSet usersManaged = new HashSet();
    HashSet groupsManaged = new HashSet();
    Vector groupsSet = new Vector();
    Vector groupsCur;
    Iterator it;
    LDAPEntry curGroup;
    String grId;

    groupsSet.add(groupEntry);
    while (groupsSet.size() > 0) {
      it = groupsSet.iterator();
      groupsCur = new Vector();
      while (it.hasNext()) {
        curGroup = (LDAPEntry) it.next();
        if (curGroup != null) {
          grId = "???";
          try {
            grId = curGroup.getDN();
            if (!groupsManaged.contains(grId)) {
              groupsManaged.add(grId);
              usersManaged.addAll(getTRUEUserIds(lds, curGroup));
              groupsCur.addAll(getTRUEChildGroupsEntry(lds, grId, curGroup));
            }
          } catch (AdminException e) {
            SilverTrace.error("admin", "LDAPGroupAllRoot.getUserIds()",
                "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : " + grId, e);
          }
        }
      }
      groupsSet = groupsCur;
    }
    return (String[]) usersManaged.toArray(new String[0]);
  }

  protected Vector getTRUEUserIds(String lds, LDAPEntry groupEntry)
      throws AdminException {
    Vector usersVector = new Vector();
    LDAPEntry userEntry = null;
    String[] stringVals = null;
    String userSpecificId = null;
    int i;

    SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
        "root.MSG_GEN_ENTER_METHOD");

    // Check cache
    if ((TRUEUserIds != null) && (TRUEUserIds.containsKey(groupEntry)))
      return (Vector) TRUEUserIds.get(groupEntry);

    stringVals = LDAPUtility.getAttributeValues(groupEntry, driverSettings
        .getGroupsMemberField());
    for (i = 0; i < stringVals.length; i++) {
      try {
        userSpecificId = synchroCache.getUserId(stringVals[i]);
        if (userSpecificId != null) {
          usersVector.add(userSpecificId);
        } else // Not in cache or cache unavailable
        {
          userEntry = LDAPUtility.getFirstEntryFromSearch(lds, stringVals[i],
              driverSettings.getScope(), driverSettings.getUsersFullFilter(),
              driverSettings.getGroupAttributes());
          if (userEntry != null) {
            userSpecificId = LDAPUtility.getFirstAttributeValue(userEntry,
                driverSettings.getUsersIdField());
            // Verify that the user exist in the scope
            if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
                .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
                .getUsersIdFilter(userSpecificId), driverSettings
                .getGroupAttributes()) != null) {
              usersVector.add(userSpecificId);
              synchroCache.addUser(stringVals[i], userSpecificId);
            }
          }
        }
      } catch (AdminException e) {
        SilverTrace.error("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
            "admin.MSG_ERR_LDAP_GENERAL", "USER NOT FOUND : "
                + LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]), e);
      }
    }
    stringVals = null;
    SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()",
        "root.MSG_GEN_EXIT_METHOD");

    // store result in cache
    if (TRUEUserIds != null)
      TRUEUserIds.put(groupEntry, usersVector);

    return usersVector;
  }

  /**
   * Method declaration THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS
   * RUNNING
   *
   *
   * @param lds
   * @param parentId
   *
   * @return
   *
   * @throws AdminException
   *
   * @see
   */
  protected LDAPEntry[] getChildGroupsEntry(String lds, String parentId,
      String extraFilter) throws AdminException {
    if ((parentId != null) && (parentId.length() > 0)) { // ALL ROOT GROUPS
      return new LDAPEntry[0];
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
          synchroReport.append("PB getting Group's subgroups : " + parentId
              + "\n");
          SynchroReport.error("LDAPGroupAllRoot.getChildGroupsEntry()",
              "Erreur lors de la récupération des groupes racine", e);
        } else {
          throw e;
        }
      }
      return theEntries;
    }
  }

  /**
   * Method declaration THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS
   * RUNNING
   *
   *
   * @param lds
   * @param parentId
   *
   * @return
   *
   * @throws AdminException
   *
   * @see
   */
  protected Vector getTRUEChildGroupsEntry(String lds, String parentId,
      LDAPEntry theEntry) {
    LDAPEntry childGroupEntry = null;
    Vector entryVector = new Vector();
    String[] stringVals = null;
    int i;

    if ((parentId != null) && (parentId.length() > 0)) {
      SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEChildGroupsEntry()",
          "root.MSG_GEN_PARAM_VALUE", "Root Group Search : " + parentId);

      // Checks cache
      if ((TRUEChildGroupsEntrys != null)
          && (TRUEChildGroupsEntrys.containsKey(parentId)))
        return (Vector) TRUEChildGroupsEntrys.get(parentId);

      stringVals = LDAPUtility.getAttributeValues(theEntry, driverSettings
          .getGroupsMemberField());
      for (i = 0; i < stringVals.length; i++) {
        try {
          childGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds,
              stringVals[i], driverSettings.getScope(), driverSettings
                  .getGroupsFullFilter(), driverSettings.getGroupAttributes());
          entryVector.add(childGroupEntry);
        } catch (AdminException e) {
          SilverTrace.error("admin",
              "LDAPGroupAllRoot.getTRUEChildGroupsEntry()",
              "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : "
                  + stringVals[i], e);
        }
      }

      // store result in cache
      if (TRUEChildGroupsEntrys != null)
        TRUEChildGroupsEntrys.put(parentId, entryVector);
    }

    return entryVector;
  }

  public Group[] getAllChangedGroups(String lds, String extraFilter)
      throws AdminException {
    Vector groupsIdsSet;
    Vector groupsCur;
    Iterator it;
    int i;
    Hashtable groupsManaged = new Hashtable();
    LDAPEntry[] les = getChildGroupsEntry(lds, null, extraFilter);
    LDAPEntry theGroup;

    groupsIdsSet = new Vector(les.length);
    for (i = 0; i < les.length; i++) {
      groupsIdsSet.add(les[i]);
    }
    // Go recurs to all group's ancestors
    while (groupsIdsSet.size() > 0) {
      it = groupsIdsSet.iterator();
      groupsCur = new Vector();
      while (it.hasNext()) {
        theGroup = (LDAPEntry) it.next();
        groupsManaged.put(theGroup.getDN(), translateGroup(lds, theGroup));
        les = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            "(&" + driverSettings.getGroupsFullFilter() + "("
                + driverSettings.getGroupsMemberField() + "="
                + LDAPUtility.dblBackSlashesForDNInFilters(theGroup.getDN())
                + "))", driverSettings.getGroupsNameField(), driverSettings
                .getGroupAttributes());
        for (i = 0; i < les.length; i++) {
          SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()",
              "root.MSG_GEN_PARAM_VALUE", "GroupFound=" + les[i].getDN());
          if (!groupsManaged.contains(les[i].getDN())) {
            groupsCur.add(les[i]);
          }
        }
      }
      groupsIdsSet = groupsCur;
    }
    return (Group[]) groupsManaged.values().toArray(new Group[0]);
  }

  // Surcharge de la fonction de la classe abstraite pour pouvoir construire le
  // nom
  protected Group translateGroup(String lds, LDAPEntry groupEntry)
      throws AdminException {
    Group groupInfos = new Group();
    String theName = "";

    if (groupEntry == null) {
      throw new AdminException("AbstractLDAPGroup.translateGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GROUP_ENTRY_ISNULL");
    }

    // We don't set : GroupParentID, DomainId and Id...
    // ------------------------------------------------
    groupInfos.setSpecificId(groupEntry.getDN());

    // Build the name
    if (driverSettings.getGroupsNamingDepth() > 0) {
      StringTokenizer theDN = new StringTokenizer(groupEntry.getDN(), ",=");
      int i = 0;

      // Remove the cn and his value
      while (theDN.hasMoreTokens()
          && (i < driverSettings.getGroupsNamingDepth())) {
        theDN.nextToken();
        if (i > 0) {
          theName = theDN.nextToken() + "-" + theName;
        } else {
          theName = theDN.nextToken();
        }
        i++;
      }
    } else {
      theName = LDAPUtility.getFirstAttributeValue(groupEntry, driverSettings
          .getGroupsNameField());
    }
    groupInfos.setName(theName);
    groupInfos.setDescription(LDAPUtility.getFirstAttributeValue(groupEntry,
        driverSettings.getGroupsDescriptionField()));
    try {
      groupInfos.setUserIds(getUserIds(lds, groupEntry));
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverTrace.warn("admin", "AbstractLDAPGroup.translateGroup",
            "admin.EX_ERR_CHILD_USERS", "Group=" + groupInfos.getName(), e);
        synchroReport.append("PB getting Group's childs : "
            + groupInfos.getName() + "\n");
        SynchroReport.error("AbstractLDAPGroup.translateGroup()",
            "Pb de récupération des membres utilisateurs du groupe "
                + groupInfos.getSpecificId(), e);
      } else {
        throw e;
      }
    }
    return groupInfos;
  }

  protected LDAPEntry getGroupEntry(String lds, String groupId)
      throws AdminException {
    SilverTrace.info("admin", "AbstractLDAPGroup.getGroupEntry()",
        "root.MSG_GEN_ENTER_METHOD", "groupId=" + groupId);
    return LDAPUtility.getFirstEntryFromSearch(lds, groupId, driverSettings
        .getScope(), driverSettings.getGroupsFullFilter(), driverSettings
        .getGroupAttributes());
  }

  protected LDAPEntry getGroupEntryByName(String lds, String groupName)
      throws AdminException {
    SilverTrace.info("admin", "AbstractLDAPGroup.getGroupEntryByName()",
        "root.MSG_GEN_ENTER_METHOD", "groupName=" + groupName);
    return LDAPUtility.getFirstEntryFromSearch(lds, groupName, driverSettings
        .getScope(), driverSettings.getGroupsFullFilter(), driverSettings
        .getGroupAttributes());
  }

  /**
   * Called when Admin starts the synchronization overidded to manage local
   * cache
   */
  public void beginSynchronization() throws Exception {
    super.beginSynchronization();
    TRUEChildGroupsEntrys = new Hashtable();
    TRUEUserIds = new Hashtable();
  }

  /**
   * Called when Admin ends the synchronization overidded to manage local cache
   */
  public String endSynchronization() throws Exception {
    String result = super.endSynchronization();
    TRUEChildGroupsEntrys.clear();
    TRUEUserIds.clear();
    TRUEChildGroupsEntrys = null;
    TRUEUserIds = null;

    return result;
  }
}