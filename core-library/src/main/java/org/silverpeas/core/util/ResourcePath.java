/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.util;

import org.jetbrains.annotations.NotNull;
import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * Capitalizes some code in order to represent a path to a resource.
 * @author silveryocha
 */
public abstract class ResourcePath<T> extends ArrayList<T> {
  private static final long serialVersionUID = 9091158323736803705L;

  protected static final String SEP = " > ";
  protected final transient Map<String, Pair<String, String>> lastPathByLanguage = new HashMap<>();

  public ResourcePath() {
    super();
  }

  public ResourcePath(final int initialCapacity) {
    super(initialCapacity);
  }

  public ResourcePath(@NotNull final Collection<? extends T> c) {
    super(c);
  }

  protected abstract String getInstanceId(final T resource);

  protected abstract <R> R getId(final T resource);

  protected abstract boolean isRoot(final T resource);

  protected boolean rootIsComponentInstance() {
    return false;
  }

  protected abstract String getLabel(final T resource, final String language);

  /**
   * Formats a path from the resource that the list contains.
   * @param language the aimed translation.
   * @return a string.
   */
  public String format(final String language) {
    return format(language, false);
  }

  /**
   * Formats a path from the resource that the list contains.
   * @param language the aimed translation.
   * @param fullSpacePath if false, the space host is taken into account, if true the space host
   * and all parents are taken into account.
   * @return a string.
   */
  public String format(final String language, final boolean fullSpacePath) {
    final String currentResourceIdPath = stream().map(this::getId).map(String::valueOf)
        .collect(Collectors.joining(","));
    Pair<String, String> lastPath = lastPathByLanguage
        .computeIfAbsent(language, l -> Pair.of("", ""));
    if (!currentResourceIdPath.equals(lastPath.getFirst())) {
      StringBuilder result = new StringBuilder();
      for (T resource : this) {
        if (result.length() > 0) {
          result.insert(0, SEP);
        }
        if (isRoot(resource)) {
          if (!rootIsComponentInstance()) {
            result.insert(0, SEP + getLabel(resource, language));
          }
          result.insert(0, getPath(resource, language, fullSpacePath));
        } else {
          result.insert(0, getLabel(resource, language));
        }
      }
      lastPath = Pair.of(currentResourceIdPath, result.toString());
      lastPathByLanguage.put(language, lastPath);
    }
    return lastPath.getSecond();
  }

  private String getPath(final T resource, final String language,
      final boolean fullSpacePath) {
    final String instanceId = getInstanceId(resource);
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
