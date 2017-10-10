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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.ExternalAttendee;
import org.silverpeas.core.calendar.InternalAttendee;
import org.silverpeas.core.calendar.notification.user.AbstractCalendarEventUserNotificationBuilder;
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

  private NotifAction notifCause;
  private UpdateCause updateCause = UpdateCause.NONE;
  private User sender;
  private List<Attendee> recipients;
  private List<Attendee> attendees;

  /**
   * Constructs a new builder of user notification against the attendee(s) of a calendar component.
   * @param calendarComponent the calendar component concerned by the notification.
   * @param action the action that was performed onto the event.
   */
  @SuppressWarnings("unchecked")
  AttendeeNotificationBuilder(final Contribution calendarComponent,
      final NotifAction action) {
    super(calendarComponent, null);
    this.notifCause = action;
  }

  /**
   * Sets the cause of an update that implies the specified list of attendees. In such a case,
   * the cause is one of the following:
   * <ul>
   * <li>{@link UpdateCause#ATTENDEE_ADDING}</li>
   * <li>{@link UpdateCause#ATTENDEE_REMOVING}</li>
   * <li>{@link UpdateCause#ATTENDEE_PARTICIPATION}</li>
   * <li>{@link UpdateCause#ATTENDEE_PRESENCE}</li>
   * </ul>
   * If the list of attendees is empty, then the cause should be {@link UpdateCause#EVENT_UPDATE}.
   * @param cause the cause of an update action.
   * @param attendees the attendees concerned by the update.
   * @return itself.
   */
  public AttendeeNotificationBuilder about(UpdateCause cause,
      List<Attendee> attendees) {
    this.updateCause = cause;
    this.attendees = attendees;
    return this;
  }

  /**
   * Sets the cause of an update that implies the specified list of attendees. In such a case,
   * the cause is one of the following:
   * <ul>
   * <li>{@link UpdateCause#ATTENDEE_ADDING}</li>
   * <li>{@link UpdateCause#ATTENDEE_REMOVING}</li>
   * <li>{@link UpdateCause#ATTENDEE_PARTICIPATION}</li>
   * <li>{@link UpdateCause#ATTENDEE_PRESENCE}</li>
   * </ul>
   * If there is no attendees, then the cause should be {@link UpdateCause#EVENT_UPDATE}.
   * @param cause the cause of an update action.
   * @param attendees the attendees concerned by the update.
   * @return itself.
   */
  public AttendeeNotificationBuilder about(UpdateCause cause,
      final Attendee... attendees) {
    return about(cause, Arrays.asList(attendees));
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

  @Override
  protected String getBundleSubjectKey() {
    switch (this.updateCause) {
      case NONE:
        return "subject.default";
      case EVENT_UPDATE:
        return "subject.eventUpdate";
      default:
        return "subject.attendance";
    }
  }

  @Override
  protected void performTemplateData(final Contribution contribution,
      final SilverpeasTemplate template) {
    String language = ((LocalizedContribution) contribution).getLanguage();
    template.setAttribute("event", contribution);
    template.setAttribute("attendees",
        this.attendees.stream().map(Attendee::getFullName).collect(Collectors.toList()));
    if (UpdateCause.ATTENDEE_PARTICIPATION.equals(this.updateCause)) {
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
    if (this.getAction() == NotifAction.UPDATE) {
      return this.updateCause.getTemplateName();
    }
    return null;
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
}
