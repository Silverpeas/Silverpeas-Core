/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.sharing.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.sharing.bean.SharingNotificationVO;

import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class FileSharingUserNotification extends AbstractTemplateUserNotificationBuilder<Ticket> {

  private static final String FILE_SHARING_BUNDLE_SUBJECT_KEY =
      "sharing.notification.message.subject.file";
  private static final String FOLDER_SHARING_BUNDLE_SUBJECT_KEY =
      "sharing.notification.message.subject.folder";

  private static final String FILE_SHARING_TEMPLATE_PATH = "fileSharing";
  private static final String FILE_SHARING_TEMPLATE_FILE_NAME = "fileSharing";
  private static final String NODE_SHARING_TEMPLATE_FILE_NAME = "nodeSharing";

  private static final String COMMA_CHARACTER = ",";

  private SharingNotificationVO fileSharingParam;

  /**
   * @param resource
   * @param fileSharingParam
   */
  public FileSharingUserNotification(Ticket resource, SharingNotificationVO fileSharingParam) {
    super(resource);
    this.fileSharingParam = fileSharingParam;
  }

  @Override
  protected String getBundleSubjectKey() {
    String sharedObjectType = getResource().getSharedObjectType();
    if (Ticket.NODE_TYPE.equals(sharedObjectType)) {
      return FOLDER_SHARING_BUNDLE_SUBJECT_KEY;
    }
    //else FILE_TYPE or VERSION_TYPE
    return FILE_SHARING_BUNDLE_SUBJECT_KEY;
  }

  @Override
  protected void performTemplateData(String language, Ticket resource, SilverpeasTemplate template) {
    Ticket ticket = getResource();
    String userId = getUserId();
    getNotificationMetaData().addLanguage(language,
        getBundle(language).getString(getBundleSubjectKey(), getTitle()), "");
    template.setAttribute("senderUser", OrganisationControllerFactory.getOrganisationController().
        getUserDetail(userId));
    template.setAttribute("attachmentUrl", fileSharingParam.getAttachmentUrl());
    if (StringUtil.isDefined(fileSharingParam.getAdditionalMessage())) {
      template.setAttribute("additionalMessage", fileSharingParam.getAdditionalMessage());
    }
    template.setAttribute("ticket", resource);
    if (ticket.getNbAccessMax() != 0) {
      template.setAttribute("limitedAccess", "true");
    }
  }

  private String getUserId() {
    String userId = getResource().getLastModifier();
    if (!StringUtil.isDefined(userId)) {
      userId = getResource().getCreatorId();
    }
    return userId;
  }

  @Override
  protected void performNotificationResource(String language, Ticket resource,
      NotificationResourceData notificationResourceData) {
  }

  @Override
  protected String getTemplatePath() {
    return FILE_SHARING_TEMPLATE_PATH;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.sharing.multilang.fileSharingBundle";
  }

  @Override
  protected String getFileName() {
    String sharedObjectType = getResource().getSharedObjectType();
    if (Ticket.NODE_TYPE.equals(sharedObjectType)) {
      return NODE_SHARING_TEMPLATE_FILE_NAME;
    }
    //else FILE_TYPE or VERSION_TYPE
    return FILE_SHARING_TEMPLATE_FILE_NAME;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.UPDATE;
  }

  /*
   * Core service, component instance identifier is useless
   * @see
   * com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getComponentInstanceId()
   */
  @Override
  protected String getComponentInstanceId() {
    return null;
  }

  @Override
  protected String getSender() {
    return getUserId();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    String selectedUsersStr = this.fileSharingParam.getSelectedUsers();
    List<String> listUsers = new ArrayList<String>();
    if (StringUtil.isDefined(selectedUsersStr)) {
      Collections.addAll(listUsers, selectedUsersStr.split(COMMA_CHARACTER));
    }
    return listUsers;
  }

  @Override
  protected Collection<String> getExternalAddressesToNotify() {
    List<String> externalAddresses = new ArrayList<String>();
    String externalAddressesStr = this.fileSharingParam.getExternalEmails();
    if (StringUtil.isDefined(externalAddressesStr)) {
      String[] externalAddressesArray = externalAddressesStr.split(COMMA_CHARACTER);
      for (String externalAddress : externalAddressesArray) {
        if (StringUtil.isValidEmailAddress(externalAddress)) {
          externalAddresses.add(externalAddress);
        }
      }
    }
    return externalAddresses;
  }

  /**
   * Builds and sends a file sharing notification. A warning message is logged when an exception is
   * catched.
   * @param resource the ticket file sharing resource
   */
  public static void notify(final Ticket resource, final SharingNotificationVO fileSharingParam) {
    try {
      UserNotificationHelper.buildAndSend(new FileSharingUserNotification(resource,
          fileSharingParam));
    } catch (final Exception e) {
      SilverTrace.warn("webPages", "FileSharingUserNotification.notify()",
          "fileSharing.EX_ALERT_USERS_ERROR", "tocken = " + resource.getToken(), e);
    }
  }

  @Override
  protected boolean isSendImmediatly() {
    return super.isSendImmediatly();
  }
}
