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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.PlannedOnCalendar;
import org.silverpeas.core.calendar.notification.user.AbstractCalendarUserNotificationBuilder;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.kernel.annotation.NonNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import static org.silverpeas.core.calendar.CalendarEventUtil.getDateWithOffset;
import static org.silverpeas.core.util.DateUtil.getDateOutputFormat;
import static org.silverpeas.core.util.DateUtil.getHourOutputFormat;

/**
 * An abstraction of all builder of notifications in a calendar component to inform the recipients
 * about some changes in the component.
 * <p>
 * The message in the notification is built from a {@link SilverpeasTemplate}. The Silverpeas
 * Calendar API provides a default set of such templates but they can be overridden by the templates
 * of a service using the API. To override a template, then, in the directory whose name is the
 * service's one, just define for each template to override a template with the same name that the
 * default one.
 *
 * @author silveryocha
 */
abstract class AbstractCalendarEventUserNotificationBuilder
    extends AbstractCalendarUserNotificationBuilder<PlannedOnCalendar> {

  private final NotifAction notificationCause;
  private CalendarOperation operation = CalendarOperation.NONE;
  private User sender;
  private boolean immediately = false;

  /**
   * Constructs a new builder of user notification of a calendar component.
   *
   * @param calendarComponent the calendar component concerned by the notification.
   * @param action the action that was performed onto the event.
   */
  AbstractCalendarEventUserNotificationBuilder(final PlannedOnCalendar calendarComponent,
      final NotifAction action) {
    super(calendarComponent, null);
    this.notificationCause = action;
  }

  /**
   * Sets the operation on a calendar component. Otherwise the operation should be another value
   * from the {@link CalendarOperation} enumeration.
   *
   * @param operation the operation that was performed on the attendance of a calendar component.
   * @return itself.
   */
  public AbstractCalendarEventUserNotificationBuilder about(CalendarOperation operation) {
    this.operation = operation;
    return this;
  }

  /**
   * Sets the sender of the user notification to build.
   *
   * @param sender the sender of the notification.
   * @return itself.
   */
  public AbstractCalendarEventUserNotificationBuilder from(final User sender) {
    this.sender = sender;
    return this;
  }

  /**
   * The notification must be sent immediately whatever the wish of the user about it.
   *
   * @return itself.
   */
  public AbstractCalendarEventUserNotificationBuilder immediately() {
    this.immediately = true;
    return this;
  }

  @Override
  protected boolean isSendImmediately() {
    return this.immediately;
  }

  @Override
  protected void performTemplateData(final LocalizedContribution contribution,
      final SilverpeasTemplate template) {
    final String language = contribution.getLanguage();
    CalendarComponent component = getResource().asCalendarComponent();
    if (component != null) {
      template.setAttribute("contributionStartDate", startDateOf(component, language));
      if (component.getPeriod().spanOverSeveralDays()) {
        template.setAttribute("contributionEndDate", endDateOf(component, language));
      }
    }
    template.setAttribute("several", this.operation.isSeveralImplied());
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
    return this.notificationCause;
  }

  private String startDateOf(@NonNull final CalendarComponent calendarComponent,
      @NonNull String language) {
    final Temporal startDate =
        getDateWithOffset(calendarComponent, calendarComponent.getPeriod().getStartDate());
    if (startDate instanceof LocalDate) {
      return ((LocalDate) startDate).format(
          DateTimeFormatter.ofPattern(getDateOutputFormat(language).getPattern()));
    } else {
      return ((OffsetDateTime) startDate).format(
          DateTimeFormatter.ofPattern(getDateOutputFormat(language).getPattern() + " " +
              getHourOutputFormat(language).getPattern()))
          + " (" + calendarComponent.getCalendar().getZoneId() + ")";
    }
  }

  private String endDateOf(@NonNull CalendarComponent calendarComponent,
      @NonNull String language) {
    if (calendarComponent.getPeriod().isInDays()) {
      LocalDate endDate = ((LocalDate) getDateWithOffset(calendarComponent,
          calendarComponent.getPeriod().getEndDate())).minusDays(1);
      return endDate.format(
          DateTimeFormatter.ofPattern(getDateOutputFormat(language).getPattern()));
    } else {
      OffsetDateTime endDateTime = (OffsetDateTime) getDateWithOffset(calendarComponent,
          calendarComponent.getPeriod().getEndDate());
      return endDateTime.format(
          DateTimeFormatter.ofPattern(getDateOutputFormat(language).getPattern() + " " +
              getHourOutputFormat(language).getPattern()))
          + " (" + calendarComponent.getCalendar().getZoneId() + ")";
    }
  }

  protected CalendarOperation getOperation() {
    return operation;
  }
}
