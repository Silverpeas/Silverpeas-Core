/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.agenda.notification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;

import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.agenda.control.AgendaRuntimeException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.control.SilverpeasCalendar;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import static com.silverpeas.util.StringUtil.isDefined;

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
  protected String getFileName() {
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
    switch (action) {
      case RESPONSE:
        userIds = Collections.singleton(getResource().getDelegatorId());
        break;
      default:
        userIds = new HashSet<String>();
        for (final Attendee attendee : getCalendarBm().getJournalAttendees(getResource().getId())) {
          userIds.add(attendee.getUserId());
        }
        break;
    }
    return userIds;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#performTemplateData
   * (java.lang.String, java.lang.Object, com.silverpeas.util.template.SilverpeasTemplate)
   */
  @Override
  protected void performTemplateData(final String language, final JournalHeader resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getBundle(language).getString(
        getBundleSubjectKey(), getTitle()), "");
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
        new StringBuilder(URLManager.getURL(URLManager.CMP_AGENDA, null, null));
    sb.append("journal.jsp?JournalId=");
    sb.append(resource.getId());
    sb.append("&Action=");
    switch (action) {
      case DELETE:
        return null;

      default:
        sb.append("Update");
        break;
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
   * com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getMultilangPropertyFile()
   */
  @Override
  protected String getMultilangPropertyFile() {
    return "com.stratelia.webactiv.agenda.multilang.agenda";
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractResourceUserNotificationBuilder#isSendImmediatly()
   */
  @Override
  protected boolean isSendImmediatly() {
    return true;
  }

  /**
   * @return the calendarBm
   */
  protected SilverpeasCalendar getCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = EJBUtilitaire.getEJBObjectRef(JNDINames.CALENDARBM_EJBHOME,
            SilverpeasCalendar.class);
      } catch (final Exception e) {
        throw new AgendaRuntimeException("AgendaUserNotification.getCalendarBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }
}