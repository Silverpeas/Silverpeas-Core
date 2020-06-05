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
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.ExternalAttendee;
import org.silverpeas.core.calendar.InternalAttendee;
import org.silverpeas.core.calendar.notification.user.AbstractCalendarEventUserNotificationBuilder;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.RemoveSenderRecipientBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.calendar.CalendarEventUtil.getDateWithOffset;
import static org.silverpeas.core.util.DateUtil.getDateOutputFormat;
import static org.silverpeas.core.util.DateUtil.getHourOutputFormat;

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
class AttendeeNotificationBuilder extends AbstractCalendarEventUserNotificationBuilder<Contribution>
    implements RemoveSenderRecipientBehavior {

  private NotifAction notifCause;
  private CalendarOperation operation = CalendarOperation.NONE;
  private User sender;
  private List<Attendee> recipients;
  private List<Attendee> attendees;
  private boolean immediately = false;

  /**
   * Constructs a new builder of user notification against the attendee(s) of a calendar component.
   * @param calendarComponent the calendar component concerned by the notification.
   * @param action the action that was performed onto the event.
   */
  AttendeeNotificationBuilder(final Contribution calendarComponent, final NotifAction action) {
    super(calendarComponent, null);
    this.notifCause = action;
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
    this.operation = operation;
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

  /**
   * Sets the sender of the user notification to build.
   * @param sender the sender of the notification.
   * @return itself.
   */
  public AttendeeNotificationBuilder from(final User sender) {
    this.sender = sender;
    return this;
  }

  /**
   * The notification must be sent immediately whatever the wish of the user about it.
   * @return itself.
   */
  public AttendeeNotificationBuilder immediately() {
    this.immediately = true;
    return this;
  }

  @Override
  protected boolean isSendImmediately() {
    return this.immediately;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (this.operation == CalendarOperation.NONE) {
      return "subject.default";
    } else if (this.operation == CalendarOperation.EVENT_UPDATE ||
               this.operation == CalendarOperation.SINCE_EVENT_UPDATE) {
      return "subject.eventUpdate";
    } else {
      return "subject.attendance";
    }
  }

  @Override
  protected void performTemplateData(final Contribution contribution,
      final SilverpeasTemplate template) {
    String language = ((LocalizedContribution) contribution).getLanguage();
    template.setAttribute("contributionDate", dateOf(getResource(), language));
    template.setAttribute("several", this.operation.isSeveralImplied());
    template.setAttribute("attendees",
        this.attendees.stream().map(Attendee::getFullName).collect(Collectors.toList()));
    if (CalendarOperation.ATTENDEE_PARTICIPATION == this.operation ||
        CalendarOperation.SINCE_ATTENDEE_PARTICIPATION == this.operation) {
      Optional<Attendee> attendee =
          this.attendees.stream().filter(a -> a.getId().equals(sender.getId())).findFirst();
      attendee.ifPresent(a -> {
        final String bundleKey =
            "event.attendee.participation." + a.getParticipationStatus().name().toLowerCase();
        template.setAttribute("participation", getBundle(language).getString(bundleKey));
      });
    }
  }

  @Override
  protected String getTemplateFileName() {
    return this.operation.getTemplateName();
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }

  @Override
  protected NotifAction getAction() {
    return this.notifCause;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
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

  private String dateOf(final Object contribution, final String language) {
    final CalendarComponent calendarComponent;
    if (contribution instanceof CalendarEvent) {
      calendarComponent = ((CalendarEvent) contribution).asCalendarComponent();
    } else if (contribution instanceof CalendarEventOccurrence) {
      calendarComponent = ((CalendarEventOccurrence) contribution).asCalendarComponent();
    } else if (contribution instanceof CalendarComponent) {
      calendarComponent = (CalendarComponent) contribution;
    } else {
      return null;
    }

    final Temporal startDate =
        getDateWithOffset(calendarComponent, calendarComponent.getPeriod().getStartDate());
    if (startDate instanceof LocalDate) {
      return ((LocalDate) startDate)
          .format(DateTimeFormatter.ofPattern(getDateOutputFormat(language).getPattern()));
    } else {
      return ((OffsetDateTime) startDate).format(DateTimeFormatter.ofPattern(
          getDateOutputFormat(language).getPattern() + " " +
              getHourOutputFormat(language).getPattern())) + " (" +
          calendarComponent.getCalendar().getZoneId() + ")";
    }
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
