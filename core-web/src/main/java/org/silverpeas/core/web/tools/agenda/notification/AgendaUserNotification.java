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
package org.silverpeas.core.web.tools.agenda.notification;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * User notification from "My Diary" application.
 *
 * @author Yohann Chastagnier
 */
public class AgendaUserNotification extends AbstractTemplateUserNotificationBuilder<JournalHeader> {

  private SilverpeasCalendar calendarBm;
  private final NotifAction action;
  private final UserDetail sender;
  private final String attend;

  /**
   * Notification from delegator
   *
   * @param action
   * @param sender
   * @param resource
   */
  public AgendaUserNotification(final NotifAction action, final UserDetail sender,
      final JournalHeader resource) {
    this(action, sender, resource, null);
  }

  /**
   * Notification from attendee
   *
   * @param sender
   * @param resource
   * @param attend
   */
  public AgendaUserNotification(final UserDetail sender, final JournalHeader resource,
      final String attend) {
    this(NotifAction.RESPONSE, sender, resource, attend);
  }

  /**
   * Default hidden constructor
   *
   * @param action
   * @param sender
   * @param resource
   * @param attend
   */
  private AgendaUserNotification(final NotifAction action, final UserDetail sender,
      final JournalHeader resource, final String attend) {
    super(resource);
    this.action = action;
    this.sender = sender;
    this.attend = attend;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractResourceUserNotificationBuilder#initialize()
   */
  @Override
  protected void initialize() {
    super.initialize();
    getNotificationMetaData().setSource(getBundle(sender.getUserPreferences().getLanguage()).
        getString("agenda"));
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#getBundleSubjectKey
   * ()
   */
  @Override
  protected String getBundleSubjectKey() {
    final String subjectKey;
    switch (action) {
      case UPDATE:
        subjectKey = "titleUpdate";
        break;

      case DELETE:
        subjectKey = "titleDelete";
        break;

      case RESPONSE:
        subjectKey = "titleAttend";
        break;

      default:
        subjectKey = "titleCreate";
        break;
    }
    return subjectKey;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#getFileName()
   */
  @Override
  protected String getTemplateFileName() {
    final String fileName;
    switch (action) {
      case UPDATE:
        fileName = "updateEvent";
        break;

      case DELETE:
        fileName = "deleteEvent";
        break;

      case RESPONSE:
        fileName = "eventAttendeeResponse";
        break;

      default:
        fileName = "addEvent";
        break;
    }
    return fileName;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getUserIdsToNotify()
   */
  @Override
  protected Collection<String> getUserIdsToNotify() {
    final Collection<String> userIds;
    if (action == NotifAction.RESPONSE) {
      userIds = Collections.singleton(getResource().getDelegatorId());
    } else {
      userIds = new HashSet<>();
      for (final Attendee attendee : getSilverpeasCalendar().getJournalAttendees(getResource().getId())) {
        userIds.add(attendee.getUserId());
      }
    }
    return userIds;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#performTemplateData
   * (java.lang.String, java.lang.Object, SilverpeasTemplate)
   */
  @Override
  protected void performTemplateData(final String language, final JournalHeader resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("sender", sender.getDisplayedName());
    if (isDefined(attend)) {
      template.setAttribute(attend, attend);
    }
    template.setAttribute("name", getResource().getName());
    template.setAttribute("startDate",
        DateUtil.getOutputDate(getResource().getStartDate(), language));
    if (isDefined(getResource().getStartHour())) {
      template.setAttribute("startHour", getResource().getStartHour());
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#
   * performNotificationResource(java.lang.String, java.lang.Object,
   * com.silverpeas.notification.model.NotificationResourceData)
   */
  @Override
  protected void performNotificationResource(final String language, final JournalHeader resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getName());
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractResourceUserNotificationBuilder#getResourceURL(
   * java.lang.Object)
   */
  @Override
  protected String getResourceURL(final JournalHeader resource) {
    final StringBuilder sb =
        new StringBuilder(URLUtil.getURL(URLUtil.CMP_AGENDA, null, null));
    sb.append("journal.jsp?JournalId=");
    sb.append(resource.getId());
    sb.append("&Action=");
    if (action == NotifAction.DELETE) {
      return null;
    } else {
      sb.append("Update");
    }
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#getTemplatePath()
   */
  @Override
  protected String getTemplatePath() {
    return "agenda";
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getAction()
   */
  @Override
  protected NotifAction getAction() {
    return action;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getComponentInstanceId()
   */
  @Override
  protected String getComponentInstanceId() {
    return "";
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getSender()
   */
  @Override
  protected String getSender() {
    return sender.getId();
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getLocalizationBundlePath()
   */
  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.agenda.multilang.agenda";
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractResourceUserNotificationBuilder#isSendImmediatly()
   */
  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  /**
   * @return the calendarBm
   */
  protected SilverpeasCalendar getSilverpeasCalendar() {
    if (calendarBm == null) {
      calendarBm = ServiceProvider.getService(SilverpeasCalendar.class);
    }
    return calendarBm;
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "agenda.notifEventLinkLabel";
  }
}