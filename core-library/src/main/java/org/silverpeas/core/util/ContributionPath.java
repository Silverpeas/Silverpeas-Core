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
import org.silverpeas.core.admin.component.model.ComponentInstPath;
import org.silverpeas.core.contribution.model.Contribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The path of a resource in the organizational resources tree of Silverpeas. It provides a
 * centralized point to compute such a path according to the type of the concerned resource.
 * Each segment of the path refers a given resource of the tree by its localized name.
 * @author silveryocha
 */
public abstract class ContributionPath<T extends Contribution> extends ArrayList<T>
    implements ResourcePath<T>, Serializable {

  private static final long serialVersionUID = 9091158323736803705L;

  protected final transient Map<String, Pair<String, String>> lastPathByLanguage = new HashMap<>();

  public ContributionPath() {
    super();
  }

  public ContributionPath(final int initialCapacity) {
    super(initialCapacity);
  }

  public ContributionPath(@NotNull final Collection<? extends T> c) {
    super(c);
  }

  protected abstract boolean isRoot(final T resource);

  protected boolean rootIsComponentInstance() {
    return false;
  }

  protected abstract String getLabel(final T resource, final String language);

  /**
   * Formats a path from the node that the list contains by using the specified path separator.
   * @param language the aimed translation.
   * @param fullSpacePath if false, the space host is taken into account, if true the space host
   * and all of its parents are taken into account.
   * @param pathSep the path separator to use.
   * @return a string.
   */
  public String format(final String language, final boolean fullSpacePath, final String pathSep) {
    final String currentResourceIdPath =
        stream().map(c -> c.getIdentifier().getLocalId()).collect(Collectors.joining(","));
    Pair<String, String> lastPath =
        lastPathByLanguage.computeIfAbsent(language, l -> Pair.of("", ""));
    if (!currentResourceIdPath.equals(lastPath.getFirst())) {
      StringBuilder result = new StringBuilder();
      for (T resource : this) {
        if (result.length() > 0) {
          result.insert(0, pathSep);
        }
        if (isRoot(resource)) {
          if (!rootIsComponentInstance()) {
            result.insert(0, pathSep + getLabel(resource, language));
          }
          result.insert(0, getPath(resource, language, fullSpacePath, pathSep));
        } else {
          result.insert(0, getLabel(resource, language));
        }
      }
      lastPath = Pair.of(currentResourceIdPath, result.toString());
      lastPathByLanguage.put(language, lastPath);
    }
    return lastPath.getSecond();
  }

  private String getPath(final T resource, final String language, final boolean fullSpacePath,
      final String pathSep) {
    final String instanceId = resource.getIdentifier().getComponentInstanceId();
    ComponentInstPath path = ComponentInstPath.getPath(instanceId);
    return path.format(language, fullSpacePath, pathSep);
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
