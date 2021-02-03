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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.ExternalAttendee;
import org.silverpeas.core.calendar.InternalAttendee;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.RemoveSenderRecipientBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A builder of notifications to attendees in a calendar component to inform them about some
 * changes in the component or in the attendance.
 * <p>
 * The message in the notification is built from a {@link SilverpeasTemplate}. The Silverpeas
 * Calendar API provides a default set of such templates but they can be overridden by the templates
 * of a service using the API. To override a template, then, in the directory whose name is the
 * service's one, just define for each template to override a template with the same name that the
 * default one.
 * @author mmoquillon
 */
class AttendeeNotificationBuilder extends AbstractCalendarEventUserNotificationBuilder
    implements RemoveSenderRecipientBehavior {

  private List<Attendee> recipients;
  private List<Attendee> attendees;

  /**
   * Constructs a new builder of user notification against the attendee(s) of a calendar component.
   * @param calendarComponent the calendar component concerned by the notification.
   * @param action the action that was performed onto the event.
   */
  AttendeeNotificationBuilder(final Contribution calendarComponent, final NotifAction action) {
    super(calendarComponent, action);
  }

  @Override
  public AttendeeNotificationBuilder about(final CalendarOperation operation) {
    return (AttendeeNotificationBuilder) super.about(operation);
  }

  /**
   * Sets the operation on a calendar component. If the operation implies on the attendance in
   * the calendar component, then attendees concerned by it have to be specified.
   * In such a case, the operation is one of the following:
   * <ul>
   * <li>{@link CalendarOperation#ATTENDEE_ADDING}</li>
   * <li>{@link CalendarOperation#ATTENDEE_REMOVING}</li>
   * <li>{@link CalendarOperation#ATTENDEE_PARTICIPATION}</li>
   * <li>{@link CalendarOperation#ATTENDEE_PRESENCE}</li>
   * </ul>
   * Otherwise the operation should be another value from the {@link CalendarOperation} enumeration.
   * @param operation the operation that was performed on the attendance of a calendar component.
   * @param attendees the attendees concerned by the operation.
   * @return itself.
   */
  public AttendeeNotificationBuilder about(CalendarOperation operation, List<Attendee> attendees) {
    about(operation);
    this.attendees = attendees;
    return this;
  }

  /**
   * Sets the operation on a calendar component. If the operation implies on the attendance in
   * the calendar component, then attendees concerned by it have to be specified.
   * In such a case, the operation is one of the following:
   * <ul>
   * <li>{@link CalendarOperation#ATTENDEE_ADDING}</li>
   * <li>{@link CalendarOperation#ATTENDEE_REMOVING}</li>
   * <li>{@link CalendarOperation#ATTENDEE_PARTICIPATION}</li>
   * <li>{@link CalendarOperation#ATTENDEE_PRESENCE}</li>
   * </ul>
   * Otherwise the operation should be another value from the {@link CalendarOperation} enumeration.
   * @param operation the operation that was performed on the attendance of a calendar component.
   * @param attendees the attendees concerned by the operation.
   * @return itself.
   */
  public AttendeeNotificationBuilder about(CalendarOperation operation,
      final Attendee... attendees) {
    return about(operation, Arrays.asList(attendees));
  }

  @Override
  public AttendeeNotificationBuilder from(final User sender) {
    return (AttendeeNotificationBuilder) super.from(sender);
  }

  @Override
  public AttendeeNotificationBuilder immediately() {
    return (AttendeeNotificationBuilder) super.immediately();
  }

  /**
   * Sets the recipients of the user notification to build.
   * @param attendees a list of recipients for the notification.
   * @return itself.
   */
  public AttendeeNotificationBuilder to(final List<Attendee> attendees) {
    this.recipients = attendees;
    return this;
  }

  /**
   * Sets the single recipient of the user notification to build.
   * @param attendee the recipient for the notification.
   * @return itself.
   */
  public AttendeeNotificationBuilder to(final Attendee attendee) {
    this.recipients = Collections.singletonList(attendee);
    return this;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (getOperation() == CalendarOperation.NONE) {
      return "subject.default";
    } else if (getOperation() == CalendarOperation.EVENT_UPDATE ||
               getOperation() == CalendarOperation.SINCE_EVENT_UPDATE) {
      return "subject.eventUpdate";
    } else {
      return "subject.attendance";
    }
  }

  @Override
  protected void performTemplateData(final Contribution contribution,
      final SilverpeasTemplate template) {
    super.performTemplateData(contribution, template);
    if (this.attendees != null) {
      final String language = ((LocalizedContribution) contribution).getLanguage();
      template.setAttribute("attendees",
          this.attendees.stream().map(Attendee::getFullName).collect(Collectors.toList()));
      if (CalendarOperation.ATTENDEE_PARTICIPATION == getOperation() ||
          CalendarOperation.SINCE_ATTENDEE_PARTICIPATION == getOperation()) {
        Optional<Attendee> attendee = this.attendees.stream().filter(a -> a.getId().equals(getSender())).findFirst();
        attendee.ifPresent(a -> {
          final String bundleKey =
              "event.attendee.participation." + a.getParticipationStatus().name().toLowerCase();
          template.setAttribute("participation", getBundle(language).getString(bundleKey));
        });
      }
    }
  }

  @Override
  public Collection<String> getUserIdsToNotify() {
    return this.recipients.stream()
        .filter(a -> a instanceof InternalAttendee)
        .map(Attendee::getId)
        .collect(Collectors.toSet());
  }

  @Override
  protected Collection<String> getExternalAddressesToNotify() {
    return this.recipients.stream()
        .filter(a -> a instanceof ExternalAttendee)
        .map(Attendee::getId)
        .collect(Collectors.toSet());
  }

  @Override
  protected boolean isUserCanBeNotified(final String userId) {
    if (PersonalComponentInstance.from(getComponentInstanceId()).isPresent()) {
      // It is the case of attendee of an event on a personal calendar
      return true;
    }
    return super.isUserCanBeNotified(userId);
  }
}
