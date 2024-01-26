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

import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class manage groups that are described as follows : The group object contains an attribute
 * that point to each users and sub-groups DN Example (with the attribute 'member') : member
 * cn=user1,ou=people,o=stratelia member cn=user2,ou=people,o=stratelia ....
 * @author tleroi
 */

@SuppressWarnings("unused")
public class LDAPGroupUniqueDescriptor extends AbstractLDAPGroup {

  private static final String LDAPGROUP_UNIQUE_DESCRIPTOR_GET_CHILD_GROUPS_ENTRY =
      "LDAPGroupUniqueDescriptor.getChildGroupsEntry()";

  private String[] getMemberGroupIds(String lds, String memberId, boolean isGroup)
      throws AdminException {
    List<String> groups = new ArrayList<>();
    LDAPEntry[] theEntries;
    int i;
    LDAPEntry memberEntry = getMemberEntry(lds, memberId, isGroup);
    theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
        driverSettings.getScope(),
        "(&" + driverSettings.getGroupsFullFilter() + "(" + driverSettings.getGroupsMemberField() +
            "=" + LDAPUtility.normalizeFilterValue(memberEntry.getDN()) + "))",
        driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
    for (i = 0; i < theEntries.length; i++) {
      groups.add(
          LDAPUtility.getFirstAttributeValue(theEntries[i], driverSettings.getGroupsIdField()));
    }
    return groups.toArray(new String[0]);
  }

  @Override
  public String[] getGroupMemberGroupIds(String lds, String groupId) throws AdminException {
    return getMemberGroupIds(lds, groupId, true);
  }

  @Override
  public String[] getUserMemberGroupIds(String lds, String userId) throws AdminException {
    return getMemberGroupIds(lds, userId, false);
  }

  @Override
  protected String[] getUserIds(String lds, LDAPEntry groupEntry) {
    List<String> users = new ArrayList<>();
    final String[] stringVals =
        LDAPUtility.getAttributeValues(groupEntry, driverSettings.getGroupsMemberField());
    for (final String stringVal : stringVals) {
      try {
        final LDAPEntry userEntry =
            LDAPUtility.getFirstEntryFromSearch(lds, stringVal, driverSettings.getScope(),
                driverSettings.getUsersFullFilter(), driverSettings.getGroupAttributes());
        final String userSpecificId = getSpecificId(lds, userEntry);
        if (userSpecificId != null) {
          users.add(userSpecificId);
        }
      } catch (AdminException e) {
        SilverLogger.getLogger(this)
            .error("USER NOT FOUND : " + LDAPUtility.dblBackSlashesForDNInFilters(stringVal), e);
      }
    }

    return users.toArray(new String[0]);
  }

  @Override
    protected LDAPEntry[] getChildGroupsEntry(String lds, String parentId, String extraFilter)
      throws AdminException {
    List<LDAPEntry> entries = new ArrayList<>();
    try {
      if ((parentId != null) && (!parentId.isEmpty())) {
        final LDAPEntry theEntry = getGroupEntry(lds, parentId);
        final String[] stringVals =
            LDAPUtility.getAttributeValues(theEntry, driverSettings.getGroupsMemberField());
        entries = Stream.of(stringVals)
            .map(v -> findGroupEntry(lds, v, extraFilter))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
      } else {
        // Retreives the ROOT groups : the groups that are under the base
        // DN but that are not member of another group...
        final String theFilter;
        if ((extraFilter != null) && (!extraFilter.isEmpty())) {
          theFilter = "(&" + extraFilter + driverSettings.getGroupsFullFilter() + ")";
        } else {
          theFilter = driverSettings.getGroupsFullFilter();
        }
        final LDAPEntry[] theEntries =
            LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
            driverSettings.getScope(), theFilter, driverSettings.getGroupsNameField(),
            driverSettings.getGroupAttributes());
        SynchroDomainReport.debug(LDAPGROUP_UNIQUE_DESCRIPTOR_GET_CHILD_GROUPS_ENTRY,
            "Récupération de " + theEntries.length +
                " groupes en tout, recherche des groupes racine...");
        entries = Stream.of(theEntries)
            .map(e -> findGroupWithMember(lds, e).orElse(e))
            .collect(Collectors.toList());
      }
    } catch (AdminException e) {
      if (synchroInProcess) {
        SilverLogger.getLogger(this).error(e);
        append("PB getting Group's subgroups : ").append(parentId).append("\n");
        if (parentId == null) {
          SynchroDomainReport.error(LDAPGROUP_UNIQUE_DESCRIPTOR_GET_CHILD_GROUPS_ENTRY,
              "Erreur lors de la récupération des groupes racine", e);
        } else {
          SynchroDomainReport.error(LDAPGROUP_UNIQUE_DESCRIPTOR_GET_CHILD_GROUPS_ENTRY,
              "Erreur lors de la récupération des groupes fils du groupe " + parentId, e);
        }
      } else {
        throw e;
      }
    }
    return entries.toArray(new LDAPEntry[0]);
  }

  private Optional<LDAPEntry> findGroupWithMember(final String lds, final LDAPEntry theEntry) {
    try {
      return Optional.ofNullable(
          LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
              driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "(" +
                  driverSettings.getGroupsMemberField() + "=" +
                  LDAPUtility.normalizeFilterValue(theEntry.getDN()) + "))",
              driverSettings.getGroupAttributes()));
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error("IS ROOT GROUP ? : " + theEntry.getDN(), e);
    }
    return Optional.empty();
  }

  private Optional<LDAPEntry> findGroupEntry(final String lds, final String stringVal,
      final String extraFilter) {
    final String theFilter;
    final LDAPEntry childGroupEntry;
    try {
      if ((extraFilter != null) && (!extraFilter.isEmpty())) {
        theFilter = "(&" + extraFilter + driverSettings.getGroupsFullFilter() + ")";
      } else {
        theFilter = driverSettings.getGroupsFullFilter();
      }
      childGroupEntry =
          LDAPUtility.getFirstEntryFromSearch(lds, stringVal, driverSettings.getScope(), theFilter,
              driverSettings.getGroupAttributes());
      if (childGroupEntry != null) {
        // Verify that the group exist in the scope
        String groupSpecificId =
            LDAPUtility.getFirstAttributeValue(childGroupEntry, driverSettings.getGroupsIdField());
        if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(),
            driverSettings.getScope(), driverSettings.getGroupsIdFilter(groupSpecificId),
            driverSettings.getGroupAttributes()) != null) {
          return Optional.of(childGroupEntry);
        }
      }
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error("GROUP NOT FOUND : " + stringVal, e);
    }
    return Optional.empty();
  }
}