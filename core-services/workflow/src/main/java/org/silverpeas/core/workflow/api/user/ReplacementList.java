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

package org.silverpeas.core.workflow.api.user;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.stream.StreamWrapper;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.silverpeas.core.util.CollectionUtil.intersection;

/**
 * A list dedicated to {@link Replacement} instances. It provides in particular the possibility
 * to filter on functional data about replacement by using the {@link Stream} API.
 * @author silveryocha
 */
public class ReplacementList <T extends Replacement> extends ArrayList<T> {
  private static final long serialVersionUID = 7833770055928190293L;

  private Map<String, List<String>> userRoleCache;

  ReplacementList(final List<T> source)  {
    super(source);
    this.userRoleCache = new ConcurrentHashMap<>(source.size());
  }

  @Override
  public FilterStream<T> stream() {
    return new FilterStream<>(super.stream());
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public class FilterStream <R extends Replacement> extends StreamWrapper<R> {

    FilterStream(final Stream<R> stream) {
      super(stream);
    }

    /**
     * Filters on the specified temporal. Only replacements which the period includes
     * the specified temporal will be kept.
     * @param temporal an {@link Temporal} instance.
     * @return new instance of {@link FilterStream}.
     */
    public FilterStream<R> filterAt(final Temporal temporal) {
      return new FilterStream<>(super.stream().filter(r -> r.getPeriod().includes(temporal)));
    }

    /**
     * Filters on the given roles. For each replacement, the substitute and the incumbent must have
     * one the given roles.
     * @param roles an array of roles as string.
     * @return new instance of {@link FilterStream}.
     */
    public FilterStream<R> filterOnAtLeastOneRole(String... roles) {
      return filterOnAtLeastOneRole(Arrays.asList(roles));
    }

    /**
     * Filters on the given roles. For each replacement, the substitute and the incumbent must have
     * one the given roles.
     * @param roles an list of roles as string.
     * @return new instance of {@link FilterStream}.
     */
    public FilterStream<R> filterOnAtLeastOneRole(List<String> roles) {
      final Stream<R> newStream = super.stream().filter(r -> {
        final List<String> incumbentRoles = getUserRoles(r.getIncumbent(), r.getWorkflowInstanceId());
        final List<String> substituteRoles = getUserRoles(r.getSubstitute(), r.getWorkflowInstanceId());
        final List<String> commonRoles = intersection(incumbentRoles, substituteRoles);
        return !intersection(roles, commonRoles).isEmpty();
      });
      return new FilterStream<>(newStream);
    }

    private List<String> getUserRoles(final User user, final String workflowInstanceId) {
      return userRoleCache.computeIfAbsent(user.getUserId(),
          i -> asList(OrganizationController.get().getUserProfiles(user.getUserId(), workflowInstanceId)));
    }
  }
}
