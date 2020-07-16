/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.personalorganizer.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.Pair;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * Processor of authorization to access a given personal user task. Usually a task is always related
 * to a given user (the owner) and it shouldn't be accessed or saw by another user unless he's also
 * involved in this task.
 * @author mmoquillon
 */
@Service
public class TodoComponentAuthorization implements ComponentAuthorization {

  private static final Pattern PATTERN = Pattern.compile("user@\\d+_todo");

  @Inject
  private SilverpeasCalendar calendar;

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return PATTERN.matcher(instanceId).matches();
  }

  @Override
  public <T> Stream<T> filter(final Collection<T> resources,
      final Function<T, ComponentResourceReference> converter, final String userId,
      final AccessControlOperation... operations) {
    final Set<String> todoIds = new HashSet<>(resources.size());
    final Set<ComponentResourceReference> authorized = new HashSet<>(resources.size());
    final List<Pair<T, ComponentResourceReference>> convertedResources = resources.stream()
        .map(r -> {
          final Pair<T, ComponentResourceReference> convertedResource = convert(r, converter);
          final ComponentResourceReference resourceRef = convertedResource.getSecond();
          if ("todo".equals(resourceRef.getType())) {
            todoIds.add(resourceRef.getLocalId());
          } else {
            authorized.add(resourceRef);
          }
          return convertedResource;
        })
        .collect(Collectors.toList());
    final Map<String, List<Attendee>> involvedUsers = getInvolvedUsers(todoIds);
    return convertedResources.stream()
        .filter(p -> authorized.contains(p.getSecond()) || isAuthorized(userId, p.getSecond(), involvedUsers))
        .map(Pair::getFirst);
  }

  /**
   * Converts the specified resource into a reference to a personal user task. The source object and
   * the converted one are both returned within a {@link Pair} instance.
   * @param resource the resource to convert.
   * @param converter the converter to use for converting the resource.
   * @param <T> the concrete type of the resource to convert.
   * @return a {@link Pair} instance with the resource to convert and the converted object that is
   * a reference to the personal user task in the personal organizer.
   */
  private <T> Pair<T, ComponentResourceReference> convert(final T resource,
      final Function<T, ComponentResourceReference> converter) {
    return Pair.of(resource, converter.apply(resource));
  }

  /**
   * Gets list of all the users involved in the specified personal tasks.
   * @param ids the unique identifiers of personal user tasks.
   * @return a map of list of {@link Attendee} instances each of them representing a user
   * involved in the task.
   */
  private Map<String, List<Attendee>> getInvolvedUsers(final Collection<String> ids) {
    return calendar.getToDoAttendees(ids);
  }

  /**
   * The specified user is authorized to access the given task if and only if he's involved in the
   * task's realization.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param todoRef a reference to the personal user task.
   * @param allInvolvedUsers all involved users of all tasks.
   * @return true if the specified user is authorized to access the personal task.
   */
  private boolean isAuthorized(final String userId, final ComponentResourceReference todoRef,
      final Map<String, List<Attendee>> allInvolvedUsers) {
    final List<Attendee> involvedUsers = allInvolvedUsers.getOrDefault(todoRef.getLocalId(), emptyList());
    return involvedUsers.stream().anyMatch(a -> a.getUserId().equals(userId));
  }
}
  