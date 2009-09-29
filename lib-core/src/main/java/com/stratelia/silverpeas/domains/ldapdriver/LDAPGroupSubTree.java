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

import java.util.TreeMap;
import java.util.Vector;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPDN;
import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class manage groups that are described as follows : The group object are
 * root to their childs
 * 
 * Method used to retreive users and groups that are direct childs of a given
 * group : GroupG (i.e. childs that have no groups between group node to their
 * node) : 1) Search all child groups of GroupG 2) Sort the result by DN 3)
 * Remove from the search all the groups that are not directly under GroupG
 * (i.e. there is another group between) => Those groups are the direct childs
 * of GroupG (result of "getChildGroupsEntryByLDAPEntry" call) 4) Performs a
 * search of the users that have for baseDN GroupG's DN and that have NOT : (DN
 * substring of any direct child groups' DN)
 * 
 * @author tleroi
 */

public class LDAPGroupSubTree extends AbstractLDAPGroup {
  protected String[] getMemberGroupIds(String lds, String memberId,
      boolean isGroup) throws AdminException {
    Vector groupsVector = new Vector();
    LDAPEntry memberEntry = null;
    LDAPEntry groupEntry = null;
    int i;
    String[] baseGroupDN;
    String[] memberDN;
    StringBuffer newDN;

    SilverTrace.info("admin", "LDAPGroupSubTree.getMemberGroupIds()",
        "root.MSG_GEN_ENTER_METHOD", "MemberId=" + memberId + ", isGroup="
            + isGroup);
    if (isGroup) {
      memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
          .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
          .getGroupsIdFilter(memberId), driverSettings.getGroupAttributes());
    } else {
      memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
          .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
          .getUsersIdFilter(memberId), driverSettings.getGroupAttributes());
    }
    if (memberEntry == null) {
      throw new AdminException("LDAPGroupSubTree.getMemberGroupIds",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_USER_ENTRY_ISNULL",
          "Id=" + memberId + " IsGroup=" + isGroup);
    }
    baseGroupDN = LDAPDN.explodeDN(driverSettings
        .getGroupsSpecificGroupsBaseDN(), false);
    memberDN = LDAPDN.explodeDN(memberEntry.getDN(), false);
    if ((memberDN.length - baseGroupDN.length) > 1) {
      newDN = new StringBuffer();
      for (i = 1; i < memberDN.length; i++) {
        if (i > 1) {
          newDN.append(",");
        }
        newDN.append(memberDN[i]);
      }
      groupEntry = LDAPUtility.getFirstEntryFromSearch(lds, newDN.toString(),
          LDAPConnection.SCOPE_BASE, driverSettings.getGroupsFullFilter(),
          driverSettings.getGroupAttributes());
      groupsVector.add(LDAPUtility.getFirstAttributeValue(groupEntry,
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
    LDAPEntry[] theEntries = null;
    Vector usersVector = new Vector();
    int i;

    SilverTrace.info("admin", "LDAPGroupSubTree.getUserIds()",
        "root.MSG_GEN_ENTER_METHOD", "GroupDN=" + groupEntry.getDN());
    theEntries = LDAPUtility.search1000Plus(lds, groupEntry.getDN(),
        LDAPConnection.SCOPE_ONE, driverSettings.getUsersFullFilter(),
        driverSettings.getUsersLoginField(), driverSettings
            .getGroupAttributes());
    for (i = 0; i < theEntries.length; i++) {
      String userSpecificId = LDAPUtility.getFirstAttributeValue(theEntries[i],
          driverSettings.getUsersIdField());
      // Verify that the user exist in the scope
      if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
          .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
          .getUsersIdFilter(userSpecificId), driverSettings
          .getGroupAttributes()) != null) {
        usersVector.add(userSpecificId);
      }
    }
    return (String[]) usersVector.toArray(new String[0]);
  }

  /**
   * Method declaration
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
    LDAPEntry parentEntry = null;

    try {
      String theFilter;

      SilverTrace.info("admin", "LDAPGroupSubTree.getChildGroupsEntry()",
          "root.MSG_GEN_ENTER_METHOD", "parentId=" + parentId);
      if ((parentId != null) && (parentId.length() > 0)) {
        if ((extraFilter != null) && (extraFilter.length() > 0)) {
          theFilter = "(&" + extraFilter
              + driverSettings.getGroupsIdFilter(parentId) + ")";
        } else {
          theFilter = driverSettings.getGroupsIdFilter(parentId);
        }
        parentEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
            .getLDAPUserBaseDN(), LDAPConnection.SCOPE_SUB, theFilter,
            driverSettings.getGroupAttributes());
      }
      return getChildGroupsEntryByLDAPEntry(lds, parentEntry);
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverTrace.warn("admin", "LDAPGroupSubTree.getChildGroupsEntry()",
            "admin.EX_ERR_CHILD_GROUPS", "ParentGroupId=" + parentId, e);
        synchroReport.append("PB getting Group's subgroups : " + parentId
            + "\n");
        return new LDAPEntry[0];
      } else {
        throw e;
      }
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param lds
   * @param parentEntry
   * 
   * @return
   * 
   * @throws AdminException
   * 
   * @see
   */
  private LDAPEntry[] getChildGroupsEntryByLDAPEntry(String lds,
      LDAPEntry parentEntry) throws AdminException {
    LDAPEntry[] theEntries = null;
    LDAPEntry[] sortResult = null;
    LDAPEntry theFirstEntry = null;
    Vector entryVector = new Vector();
    String previousDN = "";
    String searchDN = null;
    int i;
    boolean includeGroup = true;

    if (parentEntry == null) {
      searchDN = driverSettings.getLDAPUserBaseDN();
    } else {
      searchDN = parentEntry.getDN();
    }
    SilverTrace.info("admin", "LDAPGroupSubTree.getGroupEntry()",
        "root.MSG_GEN_ENTER_METHOD", "groupDN=" + searchDN);
    theEntries = LDAPUtility.search1000Plus(lds, searchDN, driverSettings
        .getScope(), driverSettings.getGroupsFullFilter(), driverSettings
        .getGroupsNameField(), driverSettings.getGroupAttributes());
    sortResult = sortReversedDN(theEntries);
    for (i = 0; i < sortResult.length; i++) {
      SilverTrace
          .info("admin", "LDAPGroupSubTree.getChildGroupsEntryByLDAPEntry()",
              "root.MSG_GEN_PARAM_VALUE", "GROUP Found  : "
                  + sortResult[i].getDN());
      if ((sortResult[i].getDN().equalsIgnoreCase(searchDN) == false)
          && ((sortResult[i].getDN().endsWith(previousDN) == false) || (previousDN
              .length() <= 0))) {
        includeGroup = true;
        if (driverSettings.getGroupsIncludeEmptyGroups() == false) {
          try {
            theFirstEntry = LDAPUtility.getFirstEntryFromSearch(lds,
                sortResult[i].getDN(), driverSettings.getScope(),
                driverSettings.getUsersFullFilter(), driverSettings
                    .getGroupAttributes());
            if (theFirstEntry == null) {
              includeGroup = false;
            }
          } catch (AdminException e) {
            SilverTrace.error("admin",
                "LDAPGroupSubTree.getChildGroupsEntryByLDAPEntry()",
                "admin.MSG_ERR_LDAP_GENERAL", "USERS SEARCH FAILED", e);
          }
        }
        if (includeGroup) {
          SilverTrace.info("admin",
              "LDAPGroupSubTree.getChildGroupsEntryByLDAPEntry()",
              "root.MSG_GEN_PARAM_VALUE", "GROUP ADDED !!!");
          entryVector.add(sortResult[i]);
          previousDN = sortResult[i].getDN();
        }
      }
    }
    return (LDAPEntry[]) entryVector.toArray(new LDAPEntry[0]);
  }

  /**
   * Method declaration
   * 
   * 
   * @param res
   * 
   * @return
   * 
   * @throws LDAPException
   * 
   * @see
   */
  private LDAPEntry[] sortReversedDN(LDAPEntry[] theEntries) {
    TreeMap theMap = new TreeMap();
    StringBuffer forReversing;
    LDAPEntry groupEntry;
    int i;

    if (theEntries == null) {
      return new LDAPEntry[0];
    }
    for (i = 0; i < theEntries.length; i++) {
      groupEntry = theEntries[i];
      forReversing = new StringBuffer(groupEntry.getDN());
      forReversing.reverse();
      theMap.put(forReversing.toString(), groupEntry);
    }
    return (LDAPEntry[]) (theMap.values().toArray(new LDAPEntry[0]));
  }

}
