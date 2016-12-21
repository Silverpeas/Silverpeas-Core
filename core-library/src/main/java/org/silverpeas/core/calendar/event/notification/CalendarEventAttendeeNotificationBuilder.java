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
package org.silverpeas.core.calendar.event.notification;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.ExternalAttendee;
import org.silverpeas.core.calendar.event.InternalAttendee;
import org.silverpeas.core.notification.user.RemoveSenderRecipientBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasStringTemplateUtil;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A builder of notifications to attendees in a calendar event to inform them about some
 * changes in the event or in the attendance.
 * <p>
 * The message in the notification is built from a {@link SilverpeasTemplate}. The Silverpeas
 * Calendar API provides a default set of such templates but they can be overridden by the templates
 * of a service using the API. To override a template, then, in the directory whose name is the
 * service's one, just define for each template to override a template with the same name that the
 * default one.
 * @author mmoquillon
 */
class CalendarEventAttendeeNotificationBuilder
    extends AbstractTemplateUserNotificationBuilder<CalendarEvent>
    implements RemoveSenderRecipientBehavior {

  private NotifAction notifCause;
  private UpdateCause updateCause = UpdateCause.NONE;
  private User sender;
  private List<Attendee> recipients;
  private List<Attendee> attendees;
  private Pair<Boolean, String> rootTemplatePath;

  /**
   * Constructs a new builder of user notification against the attendee(s) of a calendar event.
   * @param event the calendar event concerned by the notification.
   * @param action the action that was performed onto the event.
   */
  public CalendarEventAttendeeNotificationBuilder(final CalendarEvent event,
      final NotifAction action) {
    super(event);
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
  public CalendarEventAttendeeNotificationBuilder about(UpdateCause cause,
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
  public CalendarEventAttendeeNotificationBuilder about(UpdateCause cause,
      final Attendee... attendees) {
    return about(cause, Arrays.asList(attendees));
  }

  /**
   * Sets the recipients of the user notification to build.
   * @param attendees a list of recipients for the notification.
   * @return itself.
   */
  public CalendarEventAttendeeNotificationBuilder to(final List<Attendee> attendees) {
    this.recipients = attendees;
    return this;
  }

  /**
   * Sets the single recipient of the user notification to build.
   * @param attendee the recipient for the notification.
   * @return itself.
   */
  public CalendarEventAttendeeNotificationBuilder to(final Attendee attendee) {
    this.recipients = Collections.singletonList(attendee);
    return this;
  }

  /**
   * Sets the sender of the user notification to build.
   * @param sender the sender of the notification.
   * @return itself.
   */
  public CalendarEventAttendeeNotificationBuilder from(final User sender) {
    this.sender = sender;
    return this;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.calendar.multilang.usernotification";
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
  protected void performTemplateData(final String language, final CalendarEvent resource,
      final SilverpeasTemplate template) {
    template.setAttribute("sender", this.sender.getDisplayedName());
    template.setAttribute("event", resource);
    template.setAttribute("attendees",
        this.attendees.stream().map(Attendee::getFullName).collect(Collectors.toList()));
  }

  @Override
  protected void performNotificationResource(final String language, final CalendarEvent resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setComponentInstanceId(
        resource.getCalendar().getComponentInstanceId());
    notificationResourceData.setResourceName(resource.getTitle());
    notificationResourceData.setResourceDescription(resource.getDescription());
  }

  @Override
  protected String getFileName() {
    if (this.getAction() == NotifAction.UPDATE) {
      return this.updateCause.getTemplateName();
    }
    return null;
  }

  /**
   * @return a {@link Pair} instance which indicates first the StringTemplate repository as a
   * boolean, true if it is the components one, false if it is the core one. It indicates secondly
   * the template path into the root as a String.
   */
  private Pair<Boolean, String> getRootTemplatePath() {
    if (rootTemplatePath == null) {
      boolean componentRoot = false;
      Optional<SilverpeasComponentInstance> componentInst =
          SilverpeasComponentInstance.getById(getComponentInstanceId());
      String templatePath = "calendar";
      if (componentInst.isPresent() && SilverpeasStringTemplateUtil
          .isComponentTemplateExist(componentInst.get().getName(), getFileName())) {
        componentRoot = true;
        templatePath = componentInst.get().getName();
      }
      rootTemplatePath = Pair.of(componentRoot, templatePath);
    }
    return rootTemplatePath;
  }

  @Override
  protected String getTemplatePath() {
    return getRootTemplatePath().getSecond();
  }

  @Override
  protected SilverpeasTemplate createTemplate() {
    final SilverpeasTemplate template;
    if (getRootTemplatePath().getFirst()) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents(getTemplatePath());
    } else {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(getTemplatePath());
    }
    return template;
  }

  @Override
  protected NotifAction getAction() {
    return this.notifCause;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getCalendar().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return this.sender.getId();
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
