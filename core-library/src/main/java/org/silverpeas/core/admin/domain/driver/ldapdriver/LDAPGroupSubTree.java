/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPDN;
import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * This class manage groups that are described as follows : The group object are root to their
 * childs Method used to retreive users and groups that are direct childs of a given group : GroupG
 * (i.e. childs that have no groups between group node to their node) : 1) Search all child groups
 * of GroupG 2) Sort the result by DN 3) Remove from the search all the groups that are not
 * directly
 * under GroupG (i.e. there is another group between). Those groups are the direct children of
 * GroupG (result of "getChildGroupsEntryByLDAPEntry" call) 4) Performs a search of the users that
 * have for baseDN GroupG's DN and that have NOT : (DN substring of any direct child groups' DN)
 * @author tleroi
 */

public class LDAPGroupSubTree extends AbstractLDAPGroup {

  private String[] getMemberGroupIds(String lds, String memberId, boolean isGroup)
      throws AdminException {
    List<String> groupEntries = new ArrayList<>();
    LDAPEntry memberEntry;
    LDAPEntry groupEntry;
    int i;
    String[] baseGroupDN;
    String[] memberDN;
    StringBuilder newDN;


    if (isGroup) {
      memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
          driverSettings.getScope(), driverSettings.getGroupsIdFilter(memberId),
          driverSettings.getGroupAttributes());
    } else {
      memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
          driverSettings.getScope(), driverSettings.getUsersIdFilter(memberId),
          driverSettings.getGroupAttributes());
    }
    if (memberEntry == null) {
      throw new AdminException(unknown("LDAP group id", memberId));
    }
    baseGroupDN = LDAPDN.explodeDN(driverSettings.getGroupsSpecificGroupsBaseDN(), false);
    memberDN = LDAPDN.explodeDN(memberEntry.getDN(), false);
    if ((memberDN.length - baseGroupDN.length) > 1) {
      newDN = new StringBuilder();
      for (i = 1; i < memberDN.length; i++) {
        if (i > 1) {
          newDN.append(",");
        }
        newDN.append(memberDN[i]);
      }
      groupEntry = LDAPUtility
          .getFirstEntryFromSearch(lds, newDN.toString(), LDAPConnection.SCOPE_BASE,
              driverSettings.getGroupsFullFilter(), driverSettings.getGroupAttributes());
      groupEntries
          .add(LDAPUtility.getFirstAttributeValue(groupEntry, driverSettings.getGroupsIdField()));
    }
    return groupEntries.toArray(new String[0]);
  }

  public String[] getGroupMemberGroupIds(String lds, String groupId) throws AdminException {
    return getMemberGroupIds(lds, groupId, true);
  }

  public String[] getUserMemberGroupIds(String lds, String userId) throws AdminException {
    return getMemberGroupIds(lds, userId, false);
  }

  protected String[] getUserIds(String lds, LDAPEntry groupEntry) throws AdminException {
    List<String> userEntries = new ArrayList<>();
    LDAPEntry[] theEntries =
        LDAPUtility.search1000Plus(lds, groupEntry.getDN(), LDAPConnection.SCOPE_ONE,
        driverSettings.getUsersFullFilter(), driverSettings.getUsersLoginField(),
        driverSettings.getGroupAttributes());
    for (final LDAPEntry theEntry : theEntries) {
      final String userSpecificId = getSpecificId(lds, theEntry);
      if (userSpecificId != null) {
        userEntries.add(userSpecificId);
      }
    }
    return userEntries.toArray(new String[0]);
  }

  protected LDAPEntry[] getChildGroupsEntry(String lds, String parentId, String extraFilter)
      throws AdminException {
    LDAPEntry parentEntry = null;

    try {
      String theFilter;
      if ((parentId != null) && (parentId.length() > 0)) {
        if ((extraFilter != null) && (extraFilter.length() > 0)) {
          theFilter = "(&" + extraFilter + driverSettings.getGroupsIdFilter(parentId) + ")";
        } else {
          theFilter = driverSettings.getGroupsIdFilter(parentId);
        }
        parentEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(),
            LDAPConnection.SCOPE_SUB, theFilter, driverSettings.getGroupAttributes());
      }
      return getChildGroupsEntryByLDAPEntry(lds, parentEntry);
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverLogger.getLogger(this).warn(e);
        append("PB getting Group's subgroups : ").append(parentId).append("\n");
        return new LDAPEntry[0];
      } else {
        throw e;
      }
    }
  }

  private LDAPEntry[] getChildGroupsEntryByLDAPEntry(String lds, LDAPEntry parentEntry)
      throws AdminException {
    List<LDAPEntry> childGroupEntries = new ArrayList<>();
    final String searchDN = getSearchDN(parentEntry);
    final LDAPEntry[] theEntries =
        LDAPUtility.search1000Plus(lds, searchDN, driverSettings.getScope(),
        driverSettings.getGroupsFullFilter(), driverSettings.getGroupsNameField(),
        driverSettings.getGroupAttributes());
    final LDAPEntry[] sortedEntries = sortReversedDN(theEntries);
    String previousDN = "";
    for (final LDAPEntry entry : sortedEntries) {
      if (notMatchSearchDN(entry, searchDN) && notReachEndDN(entry, previousDN)) {
        final boolean includeGroup;
        if (!driverSettings.getGroupsIncludeEmptyGroups()) {
          includeGroup = isIncludeGroup(lds, entry);
        } else {
          includeGroup = false;
        }
        if (includeGroup) {
          childGroupEntries.add(entry);
          previousDN = entry.getDN();
        }
      }
    }
    return childGroupEntries.toArray(new LDAPEntry[0]);
  }

  private boolean isIncludeGroup(final String lds, final LDAPEntry entry) {
    boolean includeGroup = true;
    try {
      final LDAPEntry theFirstEntry =
          LDAPUtility.getFirstEntryFromSearch(lds, entry.getDN(), driverSettings.getScope(),
              driverSettings.getUsersFullFilter(), driverSettings.getGroupAttributes());
      if (theFirstEntry == null) {
        includeGroup = false;
      }
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return includeGroup;
  }

  private String getSearchDN(final LDAPEntry parentEntry) {
    final String searchDN;
    if (parentEntry == null) {
      searchDN = driverSettings.getLDAPUserBaseDN();
    } else {
      searchDN = parentEntry.getDN();
    }
    return searchDN;
  }

  private boolean notReachEndDN(final LDAPEntry entry, final String previousDN) {
    return !entry.getDN().endsWith(previousDN) || StringUtil.isNotDefined(previousDN);
  }

  private boolean notMatchSearchDN(final LDAPEntry entry, final String searchDN) {
    return !entry.getDN().equalsIgnoreCase(searchDN);
  }

  private LDAPEntry[] sortReversedDN(LDAPEntry[] theEntries) {
    TreeMap<String, LDAPEntry> theMap = new TreeMap<>();
    StringBuilder forReversing;
    LDAPEntry groupEntry;
    int i;

    if (theEntries == null) {
      return new LDAPEntry[0];
    }
    for (i = 0; i < theEntries.length; i++) {
      groupEntry = theEntries[i];
      forReversing = new StringBuilder(groupEntry.getDN());
      forReversing.reverse();
      theMap.put(forReversing.toString(), groupEntry);
    }
    return theMap.values().toArray(new LDAPEntry[0]);
  }

}
