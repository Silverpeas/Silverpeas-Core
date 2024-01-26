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

package org.silverpeas.web.jobdomain.servlets;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.SelectableUIEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * UI item for a {@link Group} instance.
 */
public class GroupUIEntity extends SelectableUIEntity<Group> {

  private String path;

  GroupUIEntity(final Group data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  @Override
  public String getId() {
    return String.valueOf(getData().getId());
  }

  public String getPath() {
    return path;
  }

  void setPath(final String path) {
    this.path = path;
  }

  protected static <T extends GroupUIEntity, G extends Group> void computePathsWith(
      final SilverpeasList<T> uiList, final List<G> allGroups) {
    final Map<String, Group> groupMapping = allGroups.stream().collect(toMap(Group::getId, g -> g));
    uiList.forEach(g -> computePathsFor(g, groupMapping));
  }

  private static <T extends GroupUIEntity> void computePathsFor(final T uiGroup,
      final Map<String, Group> groupMapping) {
    final OrganizationController controller = OrganizationController.get();
    final Group group = uiGroup.getData();
    final StringBuilder path = new StringBuilder();
    String superGroupId = group.getSuperGroupId();
    while (isDefined(superGroupId)) {
      Group superGroup = groupMapping.get(superGroupId);
      if (superGroup == null) {
        superGroup = controller.getGroup(superGroupId);
      }
      if (path.length() > 0) {
        path.insert(0, " > ");
      }
      path.insert(0, superGroup.getName());
      superGroupId = superGroup.getSuperGroupId();
    }
    uiGroup.setPath(path.toString());
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * Group}.
   * @param values the list of {@link Group}.
   * @return the {@link SilverpeasList} of {@link GroupUIEntity}.
   */
  public static <G extends Group> SilverpeasList<GroupUIEntity> convertList(
      final SilverpeasList<G> values, final Set<String> selectedIds) {
    final Function<Group, GroupUIEntity> converter = c -> new GroupUIEntity(c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }
}