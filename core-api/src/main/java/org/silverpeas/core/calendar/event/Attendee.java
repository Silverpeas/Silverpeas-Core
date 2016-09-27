/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.calendar.event;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.event.notification.AttendeeLifeCycleEventNotifier;
import org.silverpeas.core.calendar.event.view.AttendeeParticipationOn;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * An attendee is a user that participates in an event. It is uniquely defined by an identifier, for
 * example, an email address
 * so that it can be notified about changes on the
 * {@link org.silverpeas.core.calendar.event.CalendarEvent} object or about its attendance. Its
 * participation in a {@link org.silverpeas.core.calendar.event.CalendarEvent} is qualified by a
 * status and by its presence requirement.
 * @author mmoquillon
 */
@Entity
@Table(name = "sb_cal_attendees")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Attendee extends SilverpeasJpaEntity<Attendee, UuidIdentifier> {

  @Column(nullable = false)
  private String attendeeId;
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "eventId", referencedColumnName = "id", nullable = false)
  private CalendarEvent event;
  @OneToOne
  @JoinColumn(name = "delegate", referencedColumnName = "id")
  private Attendee delegate;
  @Column(name = "participation", nullable = false)
  @Enumerated(EnumType.STRING)
  private ParticipationStatus participationStatus = ParticipationStatus.AWAITING;
  @Embedded
  private AttendeeParticipationOn participationOn = new AttendeeParticipationOn();
  @Column(name = "presence", nullable = false)
  @Enumerated(EnumType.STRING)
  private PresenceStatus presenceStatus = PresenceStatus.REQUIRED;
  @Transient
  private Attendee beforeUpdate = null;

  protected Attendee() {
  }

  /**
   * Constructs a participant in the specified calendar event.
   * @param id the unique identifier of the attendee. It can be an address email, an identifier of a
   * user in Silverpeas, or anything from which we can notify him.
   * @param event the event in which the attendee participates.
   */
  public Attendee(String id, CalendarEvent event) {
    this.attendeeId = id;
    this.event = event;
  }

  /**
   * The unique identifier of this attendee. It can an email address, a unique identifier of a
   * user in Silverpeas, or whatever that can be used to notify the attendee.
   * @return the unique identifier of the attendee.
   */
  public String getId() {
    return this.attendeeId;
  }

  /**
   * Gets the full name of this attendee. According to the type of the attendee, this can be
   * the actual full name of the user or its email address.
   * @return the attendee full name (either its first and last name or its email address)
   */
  abstract public String getFullName();

  /**
   * Gets the event in which this attendee participates.
   * @return the event in which this attendee participates.
   */
  public CalendarEvent getEvent() {
    return this.event;
  }

  /**
   * The status of the participation of this attendee.
   * @return the participation status.
   */
  public ParticipationStatus getParticipationStatus() {
    return this.participationStatus;
  }

  /**
   * Gets the participation on specified dates.
   * @return participation on specific dates.
   */
  public AttendeeParticipationOn getParticipationOn() {
    return participationOn;
  }

  /**
   * The delegate to whom or from whom the attendance as been delegated.
   * If the participation status of this attendee is {@link ParticipationStatus#DELEGATED} then
   * this method returns the attendee to whom the delegation has been done. Otherwise, it returns
   * the attendee that has delegated its attendance to this attendee.
   * @return optionally the attendee to whom or from whom a delegation has been done. If this
   * attendee isn't concerned by any delegation, then nothing is returned (the optional attendee is
   * empty).
   */
  public Optional<Attendee> getDelegate() {
    return Optional.ofNullable(this.delegate);
  }

  /**
   * Delegates the participation of this attendee to another participant. The delegated is added
   * among the calendar event's attendees.
   * @param user a user in Silverpeas.
   */
  public void delegateTo(final User user) {
    this.participationStatus = ParticipationStatus.DELEGATED;
    this.delegate = InternalAttendee.fromUser(user)
        .to(this.event)
        .withPresenceStatus(this.presenceStatus);
    this.delegate.delegate = this;
    this.event.getAttendees().add(this.delegate);
  }

  /**
   * Delegates the participation of this attendee to another participant. The delegated is added
   * among the calendar event's attendees.
   * @param email the email of another attendee. This attendee is expected to be a person external
   * to Silverpeas.
   */
  public void delegateTo(final String email) {
    this.participationStatus = ParticipationStatus.DELEGATED;
    this.delegate = ExternalAttendee.withEmail(email)
        .to(this.event)
        .withPresenceStatus(this.presenceStatus);
    this.delegate.delegate = this;
    this.event.getAttendees().add(this.delegate);
  }

  /**
   * Resets the attendance.
   */
  void resetParticipation() {
    this.participationStatus = ParticipationStatus.AWAITING;
    this.participationOn.clear();
  }

  /**
   * Accepts the attendance.
   */
  public void accept() {
    this.participationStatus = ParticipationStatus.ACCEPTED;
    this.participationOn.clear();
  }

  /**
   * Declines the attendance.
   */
  public void decline() {
    this.participationStatus = ParticipationStatus.DECLINED;
    this.participationOn.clear();
  }

  /**
   * Tentatively accepts the attendance.
   */
  public void tentativelyAccept() {
    this.participationStatus = ParticipationStatus.TENTATIVE;
    this.participationOn.clear();
  }

  /**
   * Resets the attendance on the specified date.
   */
  void resetParticipationOn(OffsetDateTime dateTime) {
    this.participationOn.clearOn(dateTime);
  }

  /**
   * Resets the attendance from the specified date.
   */
  void resetParticipationFrom(OffsetDateTime dateTime) {
    this.participationOn.clearFrom(dateTime);
  }

  /**
   * Accepts the attendance on specified date only.
   */
  public void acceptOn(OffsetDateTime dateTime) {
    this.participationOn.set(dateTime, ParticipationStatus.ACCEPTED);
  }

  /**
   * Declines the attendance on specified date only.
   */
  public void declineOn(OffsetDateTime dateTime) {
    this.participationOn.set(dateTime, ParticipationStatus.DECLINED);
  }

  /**
   * Tentatively accepts the attendance on specified date only.
   */
  public void tentativelyAcceptOn(OffsetDateTime dateTime) {
    this.participationOn.set(dateTime, ParticipationStatus.TENTATIVE);
  }

  /**
   * The status of presence in his participation as it was asked by the calendar event author.
   * If not set, by default, his presence status is {@link PresenceStatus#REQUIRED}.
   * @return the presence status of this attendee.
   */
  public PresenceStatus getPresenceStatus() {
    return this.presenceStatus;
  }

  /**
   * Sets a new presence status to this attendee.
   * @param presenceStatus the status of presence in his participation.
   */
  public void setPresenceStatus(final PresenceStatus presenceStatus) {
    this.presenceStatus = presenceStatus;
  }

  /**
   * Sets a new presence status to this attendee.
   * @param presenceStatus the status of presence in his participation.
   * @return himself.
   */
  public Attendee withPresenceStatus(final PresenceStatus presenceStatus) {
    setPresenceStatus(presenceStatus);
    return this;
  }

  /**
   * Compares this attendee with the another one. The two attendees are equal if they are the same
   * user and they attend the same event.
   * @param o another object, must be an attendee otherwise false is returned.
   * @return true if the two attendees are equal in term of user and of attended event.
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attendee)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final Attendee attendee = (Attendee) o;

    if (!attendeeId.equals(attendee.attendeeId)) {
      return false;
    }
    if (!event.equals(attendee.event)) {
      return false;
    }
    return true;
  }

  /**
   * Computes the hash code of this attendee. It is computed from the attendee identifier and from
   * the event in which he attends.
   * @return the hash code of this attendee.
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(attendeeId).append(event).toHashCode();
  }

  /**
   * Clones this attendee for the specified event. The cloned attendee is added in the given
   * event before returning it.
   * @param event the event for which this attendee is cloned.
   * @return the clone of this attendee but for the specified event.
   */
  public Attendee cloneFor(CalendarEvent event) {
    Attendee clone = clone();
    clone.event = event;
    clone.participationOn = participationOn.clone();
    event.getAttendees().add(clone);
    return clone;
  }

  protected abstract Attendee getFromPersistenceContext();

  @Override
  protected void performBeforeUpdate() {
    super.performBeforeUpdate();
    this.beforeUpdate = getFromPersistenceContext();
  }

  @PostPersist
  private void notifyAttendeeAdded() {
    notify(ResourceEvent.Type.CREATION, this);
  }

  @PostRemove
  private void notifyAttendeeRemoved() {
    notify(ResourceEvent.Type.DELETION, this);
  }

  @PostUpdate
  private void notifyAttendeeUpdated() {
    notify(ResourceEvent.Type.UPDATE, this.beforeUpdate, this);
  }

  private void notify(final ResourceEvent.Type type, final Attendee... states) {
    Transaction.performInNew(() -> {
      AttendeeLifeCycleEventNotifier notifier = AttendeeLifeCycleEventNotifier.get();
      notifier.notifyEventOn(type, states);
      return null;
    });
  }

  /**
   * Predefined participation status of an attendee.
   */
  public enum ParticipationStatus {
    /**
     * In awaiting of the answer of the attendee for the calendar event.
     */
    AWAITING,

    /**
     * The attendee has accepted to attend the calendar event.
     */
    ACCEPTED,

    /**
     * The attendee has declined to attend the calendar event.
     */
    DECLINED,

    /**
     * The attendee is tentatively accepted to attend the calendar event.
     */
    TENTATIVE,

    /**
     * The attendee has delegated its attendance to another attendee.
     */
    DELEGATED
  }

  /**
   * Predefined presence status of an attendee in his participation in a calendar event.
   */
  public enum PresenceStatus {
    /**
     * The participation of the attendee is required.
     */
    REQUIRED,

    /**
     * The participation of the attendee is optional.
     */
    OPTIONAL,

    /**
     * The attendee is just referred for information purpose only. He has not to be available.
     */
    INFORMATIVE
  }

  /**
   * A supplier of an instance of a concrete implementation of {@link Attendee}
   */
  @FunctionalInterface
  public interface AttendeeSupplier {

    /**
     * Supplies an instance of an {@link Attendee} to the specified event.
     * @param event the event to which the attendee has to participate.
     * @return an instance of {@link Attendee}.
     */
    Attendee to(final CalendarEvent event);
  }
}
