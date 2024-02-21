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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.util.Mutable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class is able to register the differences between two components in order to apply them
 * on other components.
 * <p>BE CAREFUL: id and period are not handled by this class</p>
 * @author Yohann Chastagnier
 */
class CalendarComponentDiffDescriptor {

  private static final String TITLE_ATTR = "title";
  private static final String DESCRIPTION_ATTR = "description";
  private static final String LOCATION_ATTR = "location";
  private static final String PRIORITY_ATTR = "priority";
  private static final String SAVE_ATTRIBUTE_ATTR = "save_attribute";
  private static final String REMOVE_ATTRIBUTE_ATTR = "remove_attribute";
  private static final String SAVE_ATTENDEE_ATTR = "save_attendee";
  private static final String REMOVE_ATTENDEE_ATTR = "remove_attendee";
  private static final String UPDATE_ATTENDEE_STATUS_ATTR = "status_attendee";

  private final Map<String, Object> diff = new HashMap<>();

  /**
   * Hidden constructor
   */
  private CalendarComponentDiffDescriptor() {
  }

  /**
   * Gets the differences between the two given components which first one contains potential value
   * changes and the second one contains the reference values.
   * @param changes the component containing the changes.
   * @param reference the component containing the reference data.
   * @return the description of all detected differences.
   */
  static CalendarComponentDiffDescriptor diffBetween(CalendarComponent changes,
      CalendarComponent reference) {
    CalendarComponentDiffDescriptor diffDescriptor = new CalendarComponentDiffDescriptor();
    diffDescriptor.analyze(changes, reference);
    return diffDescriptor;
  }

  /**
   * Indicates if it exists detected differences.
   * @return true if it exists differences, false if there is not.
   */
  boolean existsDiff() {
    return !diff.isEmpty();
  }

  /**
   * Merges the detected differences into the given component.
   * @param component the component to merge.
   * @return true if something has been merged, false otherwise.
   */
  @SuppressWarnings("unchecked")
  boolean mergeInto(CalendarComponent component) {
    Mutable<Boolean> dataMerged = Mutable.of(false);
    if (diff.containsKey(TITLE_ATTR)) {
      component.setTitle((String) diff.get(TITLE_ATTR));
      dataMerged.set(true);
    }
    if (diff.containsKey(DESCRIPTION_ATTR)) {
      component.setDescription((String) diff.get(DESCRIPTION_ATTR));
      dataMerged.set(true);
    }
    if (diff.containsKey(LOCATION_ATTR)) {
      component.setLocation((String) diff.get(LOCATION_ATTR));
      dataMerged.set(true);
    }
    if (diff.containsKey(PRIORITY_ATTR)) {
      component.setPriority((Priority) diff.get(PRIORITY_ATTR));
      dataMerged.set(true);
    }
    if (diff.containsKey(SAVE_ATTRIBUTE_ATTR)) {
      Map<String, String> attributesToSave = (Map<String, String>) diff.get(SAVE_ATTRIBUTE_ATTR);
      attributesToSave.forEach((key, value) -> component.getAttributes().set(key, value));
      dataMerged.set(true);
    }
    if (diff.containsKey(REMOVE_ATTRIBUTE_ATTR)) {
      Set<String> attributesToRemove = (Set<String>) diff.get(REMOVE_ATTRIBUTE_ATTR);
      attributesToRemove.forEach(a -> component.getAttributes().remove(a));
      dataMerged.set(true);
    }
    if (diff.containsKey(SAVE_ATTENDEE_ATTR)) {
      Set<Attendee> attendeesToSave = (Set<Attendee>) diff.get(SAVE_ATTENDEE_ATTR);
      attendeesToSave.forEach(a -> {
        Optional<Attendee> attendee = component.getAttendees().get(a.getId());
        if (attendee.isPresent()) {
          attendee.get().setPresenceStatus(a.getPresenceStatus());
        } else {
          component.getAttendees().add(a.copyFor(component));
        }
      });
      dataMerged.set(true);
    }
    if (diff.containsKey(REMOVE_ATTENDEE_ATTR)) {
      Set<Attendee> attendeesToRemove = (Set<Attendee>) diff.get(REMOVE_ATTENDEE_ATTR);
      attendeesToRemove
          .forEach(atr -> component.getAttendees().removeIf(a -> a.getId().equals(atr.getId())));
      dataMerged.set(true);
    }
    if (diff.containsKey(UPDATE_ATTENDEE_STATUS_ATTR)) {
      Set<Attendee> attendeeStatusesToUpdate = (Set<Attendee>) diff.get(UPDATE_ATTENDEE_STATUS_ATTR);
      attendeeStatusesToUpdate.forEach(aS -> {
        Optional<Attendee> attendee = component.getAttendees().get(aS.getId());
        attendee.ifPresent(a -> a.setParticipationStatus(aS.getParticipationStatus()));
        dataMerged.set(attendee.isPresent());
      });
    }
    return dataMerged.is(true);
  }

  private void analyze(final CalendarComponent changes, final CalendarComponent reference) {
    if (areNotEquals(changes.getTitle(), reference.getTitle())) {
      diff.put(TITLE_ATTR, changes.getTitle());
    }
    if (areNotEquals(changes.getDescription(), reference.getDescription())) {
      diff.put(DESCRIPTION_ATTR, changes.getDescription());
    }
    if (areNotEquals(changes.getLocation(), reference.getLocation())) {
      diff.put(LOCATION_ATTR, changes.getLocation());
    }
    if (areNotEquals(changes.getPriority(), reference.getPriority())) {
      diff.put(PRIORITY_ATTR, changes.getPriority());
    }
    analyseAttributes(changes, reference);
    analyseAttendees(changes, reference);
  }

  private void analyseAttendees(final CalendarComponent left, final CalendarComponent right) {
    final Set<Attendee> attendeeToSave = new HashSet<>();
    final Set<Attendee> attendeeToRemove = new HashSet<>();
    final Set<Attendee> attendeeStatusToUpdate = new HashSet<>();
    left.getAttendees().forEach(aLeft -> {
      Optional<Attendee> aRight = right.getAttendees().get(aLeft.getId());
      if (aRight.isEmpty()) {
        attendeeToSave.add(aLeft);
      } else {
        if (areNotEquals(aLeft.getParticipationStatus(), aRight.get().getParticipationStatus())) {
          attendeeStatusToUpdate.add(aLeft);
        } else if (areNotEquals(aLeft.getPresenceStatus(), aRight.get().getPresenceStatus())) {
          attendeeToSave.add(aLeft);
        }
      }
    });
    right.getAttendees().forEach(aRight -> {
      Optional<Attendee> aLeft = left.getAttendees().get(aRight.getId());
      if (aLeft.isEmpty() &&
          attendeeToSave.stream().noneMatch(a -> a.getId().equals(aRight.getId()))) {
        attendeeToRemove.add(aRight);
      }
    });
    if (!attendeeToSave.isEmpty()) {
      diff.put(SAVE_ATTENDEE_ATTR, attendeeToSave);
    }
    if (!attendeeToRemove.isEmpty()) {
      diff.put(REMOVE_ATTENDEE_ATTR, attendeeToRemove);
    }
    if (!attendeeStatusToUpdate.isEmpty()) {
      diff.put(UPDATE_ATTENDEE_STATUS_ATTR, attendeeStatusToUpdate);
    }
  }

  private void analyseAttributes(final CalendarComponent left, final CalendarComponent right) {
    final Map<String, String> attributesToSave = new HashMap<>();
    final Set<String> attributesToRemove = new HashSet<>();
    final Map<String, String> leftAttributes = left.getAttributes().getData();
    final Map<String, String> rightAttributes = right.getAttributes().getData();
    Collection<String> allAttributeKeys =
        CollectionUtil.union(leftAttributes.keySet(), rightAttributes.keySet());
    for (String key : allAttributeKeys) {
      boolean isOnLeft = leftAttributes.containsKey(key);
      if (isOnLeft) {
        boolean isOnRight = rightAttributes.containsKey(key);
        final String leftValue = leftAttributes.get(key);
        if (!isOnRight || areNotEquals(leftValue, rightAttributes.get(key))) {
          attributesToSave.put(key, leftValue);
        }
      } else {
        attributesToRemove.add(key);
      }
    }
    if (!attributesToSave.isEmpty()) {
      diff.put(SAVE_ATTRIBUTE_ATTR, attributesToSave);
    }
    if (!attributesToRemove.isEmpty()) {
      diff.put(REMOVE_ATTRIBUTE_ATTR, attributesToRemove);
    }
  }

  private boolean areNotEquals(Object left, Object right) {
    return ! new EqualsBuilder().append(left, right).build();
  }
}
