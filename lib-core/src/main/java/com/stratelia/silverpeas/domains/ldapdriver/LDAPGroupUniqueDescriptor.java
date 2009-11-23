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
package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.Vector;

import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class manage groups that are described as follows : The group object contains an attribute
 * that point to each users and sub-groups DN Example (with the attribute 'member') : member
 * cn=user1,ou=people,o=stratelia member cn=user2,ou=people,o=stratelia ....
 * @author tleroi
 */

public class LDAPGroupUniqueDescriptor extends AbstractLDAPGroup {
  protected String[] getMemberGroupIds(String lds, String memberId,
      boolean isGroup) throws AdminException {
    Vector groupsVector = new Vector();
    LDAPEntry[] theEntries = null;
    LDAPEntry memberEntry = null;
    int i;

    SilverTrace.info("admin", "LDAPGroupUniqueDescriptor.getMemberGroupIds()",
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
      throw new AdminException("LDAPGroupUniqueDescriptor.getMemberGroupIds",
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
      SilverTrace.info("admin",
          "LDAPGroupUniqueDescriptor.getMemberGroupIds()",
          "root.MSG_GEN_PARAM_VALUE", "GroupFound=" + theEntries[i].getDN());
      groupsVector.add(LDAPUtility.getFirstAttributeValue(theEntries[i],
          driverSettings.getGroupsIdField()));
    }
    return (String[]) groupsVector.toArray(new String[0]);
  }

  public String[] getGroupMemberGroupIds(String lds, String groupId)
      throws AdminException {
    return getMemberGroupIds(lds, groupId, true);
  }

  public String[] getUserMemberGroupIds(String lds, String userId)
      throws AdminException {
    return getMemberGroupIds(lds, userId, false);
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
    Vector usersVector = new Vector();
    LDAPEntry userEntry = null;
    String[] stringVals = null;
    int i;

    SilverTrace.info("admin", "LDAPGroupUniqueDescriptor.getUserIds()",
        "root.MSG_GEN_ENTER_METHOD");
    stringVals = LDAPUtility.getAttributeValues(groupEntry, driverSettings
        .getGroupsMemberField());
    for (i = 0; i < stringVals.length; i++) {
      try {
        // userEntry = LDAPUtility.getFirstEntryFromSearch(lds,
        // driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(), "(&" +
        // driverSettings.getUsersFullFilter() + "(distinguishedName=" +
        // LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]) + "))");
        userEntry = LDAPUtility.getFirstEntryFromSearch(lds, stringVals[i],
            driverSettings.getScope(), driverSettings.getUsersFullFilter(),
            driverSettings.getGroupAttributes());
        if (userEntry != null) {
          String userSpecificId = LDAPUtility.getFirstAttributeValue(userEntry,
              driverSettings.getUsersIdField());
          // Verify that the user exist in the scope
          if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
              .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
              .getUsersIdFilter(userSpecificId), driverSettings
              .getGroupAttributes()) != null) {
            usersVector.add(userSpecificId);
          }
        }
      } catch (AdminException e) {
        SilverTrace.error("admin", "LDAPGroupUniqueDescriptor.getUserIds()",
            "admin.MSG_ERR_LDAP_GENERAL", "USER NOT FOUND : "
            + LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]), e);
      }
    }
    stringVals = null;
    SilverTrace.info("admin", "LDAPGroupUniqueDescriptor.getUserIds()",
        "root.MSG_GEN_EXIT_METHOD");
    return (String[]) usersVector.toArray(new String[0]);
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
    LDAPEntry theEntry = null;
    LDAPEntry childGroupEntry = null;
    LDAPEntry parentGroupEntry = null;
    Vector entryVector = new Vector();
    String[] stringVals = null;
    LDAPEntry[] theEntries = null;
    int i;
    String theFilter;

    try {
      if ((parentId != null) && (parentId.length() > 0)) {
        SilverTrace.info("admin",
            "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
            "root.MSG_GEN_PARAM_VALUE", "Root Group Search : " + parentId);
        theEntry = getGroupEntry(lds, parentId);
        stringVals = LDAPUtility.getAttributeValues(theEntry, driverSettings
            .getGroupsMemberField());
        for (i = 0; i < stringVals.length; i++) {
          try {
            if ((extraFilter != null) && (extraFilter.length() > 0)) {
              // theFilter = "(&" + extraFilter + "(&" +
              // driverSettings.getGroupsFullFilter() + "(distinguishedName=" +
              // LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]) + "))"
              // + ")";
              theFilter = "(&" + extraFilter
                  + driverSettings.getGroupsFullFilter() + ")";
            } else {
              // theFilter = "(&" + driverSettings.getGroupsFullFilter() +
              // "(distinguishedName=" +
              // LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]) + "))";
              theFilter = driverSettings.getGroupsFullFilter();
            }
            // childGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds,
            // driverSettings.getGroupsSpecificGroupsBaseDN(),
            // driverSettings.getScope(), theFilter);
            childGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds,
                stringVals[i], driverSettings.getScope(), theFilter,
                driverSettings.getGroupAttributes());
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
                "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
                "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : "
                + stringVals[i], e);
          }
        }
      } else // Retreives the ROOT groups : the groups that are under the base
      // DN but that are not member of another group...
      {
        if ((extraFilter != null) && (extraFilter.length() > 0)) {
          theFilter = "(&" + extraFilter + driverSettings.getGroupsFullFilter()
              + ")";
        } else {
          theFilter = driverSettings.getGroupsFullFilter();
        }
        SilverTrace.info("admin",
            "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
            "root.MSG_GEN_PARAM_VALUE", "Root Group Search");
        theEntries = LDAPUtility.search1000Plus(lds, driverSettings
            .getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(),
            theFilter, driverSettings.getGroupsNameField(), driverSettings
            .getGroupAttributes());
        SynchroReport.debug("LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
            "Récupération de " + theEntries.length
            + " groupes en tout, recherche des groupes racine...", null);
        for (i = 0; i < theEntries.length; i++) {
          // Search for groups that have at least one member attribute that
          // point to the group
          try {
            parentGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds,
                driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings
                .getScope(), "(&"
                + driverSettings.getGroupsFullFilter()
                + "("
                + driverSettings.getGroupsMemberField()
                + "="
                + LDAPUtility.dblBackSlashesForDNInFilters(theEntries[i]
                .getDN()) + "))", driverSettings.getGroupAttributes());
          } catch (AdminException e) {
            SilverTrace.error("admin",
                "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
                "admin.MSG_ERR_LDAP_GENERAL", "IS ROOT GROUP ? : "
                + theEntries[i].getDN(), e);
            parentGroupEntry = null; // If query failed, set this group as a
            // root group
          }
          if (parentGroupEntry == null) // No parent...
          {
            entryVector.add(theEntries[i]);
          }
        }
        theEntries = null;
      }
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverTrace.warn("admin",
            "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
            "admin.EX_ERR_CHILD_GROUPS", "ParentGroupId=" + parentId, e);
        synchroReport.append("PB getting Group's subgroups : " + parentId
            + "\n");
        if (parentId == null)
          SynchroReport.error(
              "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
              "Erreur lors de la récupération des groupes racine", e);
        else
          SynchroReport.error(
              "LDAPGroupUniqueDescriptor.getChildGroupsEntry()",
              "Erreur lors de la récupération des groupes fils du groupe "
              + parentId, e);
      } else {
        throw e;
      }
    }
    return (LDAPEntry[]) entryVector.toArray(new LDAPEntry[0]);
  }
}