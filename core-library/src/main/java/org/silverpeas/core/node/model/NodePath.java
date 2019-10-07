/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.node.model;

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * List of {@link NodeDetail} which represents a path.
 * @author silveryocha
 */
public class NodePath extends ArrayList<NodeDetail> {
  private static final long serialVersionUID = -2389557818767894656L;
  private static final String SEP = " > ";
  private transient Map<String, Pair<String, String>> lastPathByLanguage = new HashMap<>();

  public NodePath() {
    super();
  }

  public NodePath(final int initialCapacity) {
    super(initialCapacity);
  }

  public NodePath(final Collection<? extends NodeDetail> c) {
    super(c);
  }

  /**
   * Formats a path from the node that the list contains.
   * @param language the aimed translation.
   * @return a string.
   */
  public String format(final String language) {
    return format(language, false);
  }

  /**
   * Formats a path from the node that the list contains.
   * @param language the aimed translation.
   * @param fullSpacePath if false, the space host is taken into account, if true the space host
   * and all parents are taken into account.
   * @return a string.
   */
  public String format(final String language, final boolean fullSpacePath) {
    final String currentNodeIdPath = stream().map(NodeDetail::getId).map(String::valueOf)
        .collect(Collectors.joining(","));
    Pair<String, String> lastPath = lastPathByLanguage
        .computeIfAbsent(language, l -> Pair.of("", ""));
    if (!currentNodeIdPath.equals(lastPath.getFirst())) {
      StringBuilder result = new StringBuilder();
      for (NodeDetail node : this) {
        if (result.length() > 0) {
          result.insert(0, SEP);
        }
        if (NodePK.ROOT_NODE_ID.equals(node.getNodePK().getId())) {
          result.insert(0, getPath(node, language, fullSpacePath));
        } else {
          result.insert(0, node.getName(language));
        }
      }
      lastPath = Pair.of(currentNodeIdPath, result.toString());
      lastPathByLanguage.put(language, lastPath);
    }
    return lastPath.getSecond();
  }

  private String getPath(final NodeDetail node, final String language,
      final boolean fullSpacePath) {
    final String instanceId = node.getNodePK().getInstanceId();
    final SilverpeasComponentInstance componentInstance = OrganizationController.get()
        .getComponentInstance(instanceId).orElseThrow(() -> new IllegalArgumentException(
            SilverpeasExceptionMessages.failureOnGetting("component instance", instanceId)));
    return getPath(componentInstance, language, fullSpacePath) + SEP +
        componentInstance.getLabel(language);
  }

  private String getPath(SilverpeasComponentInstance instance, final String language,
      final boolean fullSpacePath) {
    final List<SpaceInstLight> spaceList;
    final OrganizationController controller = OrganizationController.get();
    if (fullSpacePath) {
      spaceList = controller.getPathToComponent(instance.getId());
    } else {
      spaceList = singletonList(controller.getSpaceInstLightById(instance.getSpaceId()));
    }
    return spaceList.stream().map(s -> s.getName(language)).collect(Collectors.joining(SEP));
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
