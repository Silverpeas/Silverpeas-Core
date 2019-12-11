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

import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.Pair;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Processor of authorization to access a given personal user task. Usually a task is always related
 * to a given user (the owner) and it shouldn't be accessed or saw by another user unless he's also
 * involved in this task.
 * @author mmoquillon
 */
@Named
public class TodoComponentAuthorization implements ComponentAuthorization {

  @Inject
  private SilverpeasCalendar calendar;

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.matches("user@\\d+_todo");
  }

  @Override
  public <T> Stream<T> filter(final Collection<T> resources,
      final Function<T, ComponentResourceReference> converter, final String userId,
      final AccessControlOperation... operations) {
    return resources.stream()
        .map(r -> convert(r, converter))
        .filter(p -> p.getSecond().getType().equals("todo"))
        .filter(p -> isAuthorized(userId, p.getSecond()))
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
   * Gets a list of all the users involved in the specified personal task.
   * @param id the unique identifier of a personal user task.
   * @return a list of {@link Attendee} instances, each of them representing a user involved in
   * the task.
   */
  private List<Attendee> getInvolvedUsers(final String id) {
    return calendar.getToDoAttendees(id);
  }

  /**
   * The specified user is authorized to access the given task if and only if he's involved in the
   * task's realization.
   * @param userId the unique identifier of a user in Silverpeas.
   * @param todoRef a reference to the personal user task.
   * @return true if the specified user is authorized to access the personal task.
   */
  private boolean isAuthorized(final String userId, final ComponentResourceReference todoRef) {
    final List<Attendee> involvedUsers = getInvolvedUsers(todoRef.getLocalId());
    return involvedUsers.stream()
        .anyMatch(a -> a.getUserId().equals(userId));
  }
}
  