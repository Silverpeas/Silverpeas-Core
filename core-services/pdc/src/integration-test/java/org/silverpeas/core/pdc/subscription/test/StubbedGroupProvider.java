/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription.test;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.service.GroupProvider;
import org.silverpeas.core.annotation.Provider;

import java.util.List;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A provider of groups of users answering any identifier with a valid group, so that the validity
 * of the subscribers can be checked without requiring the whole administration of Silverpeas.
 * @author mmoquillon
 */
@Provider
@Alternative
@Priority(APPLICATION + 10)
public class StubbedGroupProvider implements GroupProvider {

  @Override
  public Group getGroup(final String groupId) {
    final GroupDetail group = new GroupDetail();
    group.setId(groupId);
    group.setName("Group " + groupId);
    return group;
  }

  @Override
  public List<Group> getAllRootGroups() {
    return List.of();
  }

  @Override
  public List<Group> getAllRootGroupsInDomain(final String domainId) {
    return List.of();
  }
}
