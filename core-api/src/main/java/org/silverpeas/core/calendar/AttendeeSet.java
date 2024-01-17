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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.CollectionUtil;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A set of attendees in a given calendar component.
 */
@Embeddable
public class AttendeeSet implements Iterable<Attendee>, Serializable {

  @Transient
  private CalendarComponent component;

  @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true, fetch =
      FetchType.EAGER)
  private Set<Attendee> attendees = new HashSet<>();

  /**
   * Constructs a new set of attendees for the specified calendar component.
   * @param component a calendar component (an event, a journal, a to-do, ...)
   */
  AttendeeSet(final CalendarComponent component) {
    this.component = component;
  }

  /**
   * Constructs an empty set of attendees for the persistence engine.
   */
  protected AttendeeSet() {
    // it is dedicated to JPA.
  }

  @Override
  public Iterator<Attendee> iterator() {
    return attendees.iterator();
  }

  /**
   * Performs the given action for each attendee in this calendar component until all of them
   * have been processed or the action throws an exception. Exceptions thrown by the action are
   * relayed to the caller.
   * @param action the action to be performed for each attendee.
   */
  public void forEach(final Consumer<? super Attendee> action) {
    attendees.forEach(action);
  }

  @Override
  public Spliterator<Attendee> spliterator() {
    return attendees.spliterator();
  }

  /**
   * Streams the attendees in this calendar component.
   * @return a {@link Stream} with the attendees in this calendar component.
   */
  public Stream<Attendee> stream() {
    return attendees.stream();
  }

  /**
   * Gets the attendee with the specified identifier in this calendar component. The identifier can
   * be either the identifier of an external attendee (like an email address) or the unique
   * identifier of a user in Silverpeas.
   * @param id the identifier of the attendee to get: either the identifier of a user in Silverpeas
   * or an identifier referring an external attendee (like an email address).
   * @return optionally the attendee matching the given identifier. If no such attendee participates
   * in this calendar component, then the optional value is empty, otherwise it contains the
   * expected attendee.
   */
  public Optional<Attendee> get(String id) {
    return this.attendees.stream().filter(a -> a.getId().equals(id)).findFirst();
  }

  /**
   * Adds an external attendee in this calendar component. The attendee is identified by the
   * specified email address.
   * @param email the email address of the external attendee.
   * @return the added attendee.
   */
  public Attendee add(final String email) {
    Attendee attendee = ExternalAttendee.withEmail(email).to(component);
    attendees.add(attendee);
    return attendee;
  }

  /**
   * Adds an internal attendee in this calendar component. The attendee represents the
   * specified Silverpeas user.
   * @param user a user in Silverpeas.
   * @return the added attendee.
   */
  public Attendee add(final User user) {
    Attendee attendee = InternalAttendee.fromUser(user).to(component);
    attendees.add(attendee);
    return attendee;
  }

  /**
   * Removes from the attendees in this calendar component the specified one.
   * @param attendee the attendee to remove.
   * @return the updated attendees in this calendar component.
   */
  AttendeeSet remove(final Attendee attendee) {
    attendees.remove(attendee);
    return this;
  }

  /**
   * Removes all of the attendees that match the specified filter.
   * @param filter the predicate against which each attendee is filtered.
   * @return the updated attendees in this calendar component.
   */
  public AttendeeSet removeIf(final Predicate<Attendee> filter) {
    attendees.removeIf(filter);
    return this;
  }

  /**
   * Clears this calendar component from all its attendees.
   * @return an empty collection of attendees in this calendar component.
   */
  public AttendeeSet clear() {
    attendees.clear();
    return this;
  }

  /**
   * Is there any attendees in this calendar component?
   * @return true if there is no attendees in this calendar component, false otherwise.
   */
  public boolean isEmpty() {
    return attendees.isEmpty();
  }

  /**
   * Is the specified attendee participates in this calendar component?
   * @param attendee an attendee.
   * @return true if the specified attendee is in the attendees in this calendar component, false
   * otherwise.
   */
  public boolean contains(final Attendee attendee) {
    return attendees.contains(attendee);
  }

  /**
   * Gets the size in attendees in this calendar component.
   * @return the number of attendees in this calendar component.
   */
  public int size() {
    return attendees.size();
  }

  @Override
  public int hashCode() {
    return attendees.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj != null && obj instanceof AttendeeSet &&
        attendees.equals(((AttendeeSet) obj).attendees);
  }

  /**
   * Is this set of attendees is same as the specified one? Two set of attendees are the same if
   * they have the same attendees without any change in their properties (presence, participation,
   * ...).
   * <p>
   * This method differs from equality as they don't compare the same thing: the {@code equals}
   * method in Java is a comparator by identity, meaning two objects are compared by their unique
   * identifier (either by their OID for non-persistent object or by their persistence identifier
   * for persistent object). The {@code isSameAs} method is a comparator by value, meaning two
   * objects are compared by their state; so two equal objects (that is referring to a same
   * object) can be different by their state: one representing a given state of the referred object
   * whereas the other represents another state of the referred object.
   * </p>
   * @param attendees the attendees to compare with.
   * @return true if the two set of attendees are equal and have the same state.
   */
  public boolean isSameAs(final AttendeeSet attendees) {
    if (!this.equals(attendees)) {
      return false;
    }
    for (Attendee attendee : this.attendees) {
      Optional<Attendee> other = attendees.get(attendee.getId());
      if (!other.isPresent()) {
        return false;
      }
      Attendee otherAttendee = other.get();
      if (attendee.getParticipationStatus() != otherAttendee.getParticipationStatus() ||
          attendee.getPresenceStatus() != otherAttendee.getPresenceStatus()) {
        return false;
      }
      if (!attendee.getFullName().equals(otherAttendee.getFullName()) ||
          !attendee.getDelegate().equals(otherAttendee.getDelegate()) ||
          !attendee.getCalendarComponent().equals(otherAttendee.getCalendarComponent())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Is this set of attendees containing a change from an action of participation status answer or
   * from an action of presence change?
   * Yes, but only if is does not exist an add or a deletion of attendee.
   * <p>
   * Indeed, a participation answer or a presence status change must not imply modification of last
   * update date of entities.
   * </p>
   * @param attendees the attendees to compare with.
   * @return true attendee set contains at least one attendee which indicates an answer action.
   */
  boolean onlyAttendeePropertyChange(final AttendeeSet attendees) {
    final long nbParticipationAnswersOrPresenceStatusChanges =
        stream().filter(Attendee::propertyChange).count();
    if (nbParticipationAnswersOrPresenceStatusChanges > 0 && size() == attendees.size()) {
      final Collection<Attendee> sameAttendeesBetweenInternalAndGiven =
          CollectionUtil.intersection(this.attendees, attendees.attendees);
      return size() == sameAttendeesBetweenInternalAndGiven.size();
    }
    return false;
  }

  /**
   * Add the specified attendee in the attendees in this calendar component. If the attendee
   * participates in another calendar component, then its participation changes as now he will
   * participate in the underlying calendar component.
   * @param attendee the attendee to add.
   * @return the updated attendees in this calendar component.
   */
  AttendeeSet add(final Attendee attendee) {
    attendee.setCalendarComponent(this.component);
    attendees.add(attendee);
    return this;
  }

  AttendeeSet withCalendarComponent(final CalendarComponent component) {
    this.component = component;
    return this;
  }
}
  