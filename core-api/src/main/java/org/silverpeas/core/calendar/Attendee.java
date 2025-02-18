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
package org.silverpeas.core.calendar;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.persistence.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * An attendee is a user that participates in a calendar component that can be an event or anything
 * that can be planned in a calendar. It is uniquely defined by an identifier, for example, an
 * email address so that it can be notified about changes on the
 * {@link org.silverpeas.core.calendar.CalendarComponent} object or about its attendance. Its
 * participation in a {@link org.silverpeas.core.calendar.CalendarComponent} is qualified by a
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
  @JoinColumn(name = "componentId", referencedColumnName = "id", nullable = false)
  private CalendarComponent component;
  @OneToOne
  @JoinColumn(name = "delegate", referencedColumnName = "id")
  private Attendee delegate;
  @Column(name = "participation", nullable = false)
  @Enumerated(EnumType.STRING)
  private ParticipationStatus participationStatus = ParticipationStatus.AWAITING;
  @Column(name = "presence", nullable = false)
  @Enumerated(EnumType.STRING)
  private PresenceStatus presenceStatus = PresenceStatus.REQUIRED;
  @Transient
  private boolean presenceStatusChanged = false;
  @Transient
  private boolean participationStatusAnswered = false;

  /**
   * Constructs an empty attendee. This constructor is dedicated to the persistence engine.
   */
  protected Attendee() {
    // empty for JPA.
  }

  /**
   * Constructs a participant in the specified calendar component.
   * @param id the unique identifier of the attendee. It can be an address email, an identifier of a
   * user in Silverpeas, or anything from which we can notify him.
   * @param component a calendar component in which the attendee participates.
   */
  protected Attendee(String id, CalendarComponent component) {
    this.attendeeId = id;
    this.component = component;
  }

  /**
   * The unique identifier of this attendee. It can an email address, a unique identifier of a
   * user in Silverpeas, or whatever that can be used to notify the attendee.
   * @return the unique identifier of the attendee.
   */
  @Override
  public String getId() {
    return this.attendeeId;
  }

  /**
   * Gets the full name of this attendee. According to the type of the attendee, this can be
   * the actual full name of the user or its email address.
   * @return the attendee full name (either its first and last name or its email address)
   */
  public abstract String getFullName();

  /**
   * Gets the calendar component for which this attendee participates.
   * @return the calendar component in which this attendee participates.
   */
  public CalendarComponent getCalendarComponent() {
    return this.component;
  }

  /**
   * The status of the participation of this attendee.
   * @return the participation status.
   */
  public ParticipationStatus getParticipationStatus() {
    return this.participationStatus;
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
    participationStatusAnswered = this.participationStatus != ParticipationStatus.DELEGATED;
    this.participationStatus = ParticipationStatus.DELEGATED;
    this.delegate = InternalAttendee.fromUser(user).to(this.component)
        .withPresenceStatus(this.presenceStatus);
    this.delegate.delegate = this;
    this.component.getAttendees().add(this.delegate);
  }

  /**
   * Delegates the participation of this attendee to another participant. The delegated is added
   * among the calendar event's attendees.
   * @param email the email of another attendee. This attendee is expected to be a person external
   * to Silverpeas.
   */
  public void delegateTo(final String email) {
    participationStatusAnswered = this.participationStatus != ParticipationStatus.DELEGATED;
    this.participationStatus = ParticipationStatus.DELEGATED;
    this.delegate = ExternalAttendee.withEmail(email).to(this.component)
        .withPresenceStatus(this.presenceStatus);
    this.delegate.delegate = this;
    this.component.getAttendees().add(this.delegate);
  }

  void setParticipationStatus(final ParticipationStatus participationStatus) {
    participationStatusAnswered = this.participationStatus != participationStatus;
    this.participationStatus = participationStatus;
  }

  /**
   * Resets the attendance.
   */
  void resetParticipation() {
    participationStatusAnswered = false;
    this.participationStatus = ParticipationStatus.AWAITING;
  }

  /**
   * Accepts the attendance.<br>
   * Calling this method represents an answer action.<br>
   * Call {@link #setParticipationStatus(ParticipationStatus)} method to modify the status
   * without specifying that is in case of an answer.
   */
  public void accept() {
    participationStatusAnswered = this.participationStatus != ParticipationStatus.ACCEPTED;
    this.participationStatus = ParticipationStatus.ACCEPTED;
  }

  /**
   * Declines the attendance.<br>
   * Calling this method represents an answer action.<br>
   * Call {@link #setParticipationStatus(ParticipationStatus)} method to modify the status
   * without specifying that is in case of an answer.
   */
  public void decline() {
    participationStatusAnswered = this.participationStatus != ParticipationStatus.DECLINED;
    this.participationStatus = ParticipationStatus.DECLINED;
  }

  /**
   * Tentatively accepts the attendance.<br>
   * Calling this method represents an answer action.<br>
   * Call {@link #setParticipationStatus(ParticipationStatus)} method to modify the status
   * without specifying that is in case of an answer.
   */
  public void tentativelyAccept() {
    participationStatusAnswered = this.participationStatus != ParticipationStatus.TENTATIVE;
    this.participationStatus = ParticipationStatus.TENTATIVE;
  }

  /**
   * Indicates if it exists a change about the participation status from an answer action.
   * @return true if participation status has changed by an answer action.
   */
  boolean propertyChange() {
    return presenceStatusChanged || participationStatusAnswered;
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
    this.presenceStatusChanged = this.presenceStatus != presenceStatus;
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
   * Creates and returns a copy of this attendee. The returned attendee will be the same attendee
   * as this one and as such they will be related to the same calendar component.
   * @return an exact copy of this one.
   */
  Attendee copy() {
    Attendee copy = newEmptyAttendee();
    copy.attendeeId = attendeeId;
    copy.delegate = delegate;
    copy.participationStatus = participationStatus;
    copy.presenceStatus = presenceStatus;
    copy.participationStatusAnswered = participationStatusAnswered;
    copy.presenceStatusChanged = presenceStatusChanged;
    copy.component = component;
    return copy;
  }

  private Attendee newEmptyAttendee() {
    try {
      Constructor<? extends Attendee> constructor = getClass().getDeclaredConstructor();
      if (!constructor.canAccess(null)) {
        constructor.trySetAccessible();
      }
      return constructor.newInstance();
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Changes the calendar component in which this attendee participates.
   * @param calendarComponent a component in a calendar.
   */
  void setCalendarComponent(final CalendarComponent calendarComponent) {
    this.component = calendarComponent;
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
    DELEGATED;

    public boolean isAwaiting() {
      return this == AWAITING;
    }

    public boolean isAccepted() {
      return this == ACCEPTED;
    }

    public boolean isDeclined() {
      return this == DECLINED;
    }

    public boolean isTentative() {
      return this == TENTATIVE;
    }

    public boolean isDelegated() {
      return this == DELEGATED;
    }
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
    INFORMATIVE;

    public boolean isRequired() {
      return this == REQUIRED;
    }

    public boolean isOptional() {
      return this == OPTIONAL;
    }

    public boolean isInformative() {
      return this == INFORMATIVE;
    }
  }

  /**
   * A supplier of an instance of a concrete implementation of {@link Attendee}
   */
  @FunctionalInterface
  interface AttendeeSupplier {

    /**
     * Supplies an instance of an {@link Attendee} to the specified calendar component.
     * @param calendarComponent the calendar component to which the attendee has to participate.
     * @return an instance of {@link Attendee}.
     */
    Attendee to(final CalendarComponent calendarComponent);
  }

}
