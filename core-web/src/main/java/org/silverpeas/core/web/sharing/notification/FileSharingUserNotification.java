/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.web.sharing.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.web.sharing.bean.SharingNotificationVO;

import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.silvertrace.SilverTrace;

public class FileSharingUserNotification extends AbstractTemplateUserNotificationBuilder<Ticket> {

  private static final String FILE_SHARING_BUNDLE_SUBJECT_KEY =
      "sharing.notification.message.subject.file";
  private static final String FOLDER_SHARING_BUNDLE_SUBJECT_KEY =
      "sharing.notification.message.subject.folder";
  private static final String PUBLICATION_SHARING_BUNDLE_SUBJECT_KEY =
      "sharing.notification.message.subject.publication";

  private static final String FILE_SHARING_TEMPLATE_PATH = "fileSharing";
  private static final String FILE_SHARING_TEMPLATE_FILE_NAME = "fileSharing";
  private static final String NODE_SHARING_TEMPLATE_FILE_NAME = "nodeSharing";
  private static final String PUBLI_SHARING_TEMPLATE_FILE_NAME = "publiSharing";

  private static final String COMMA_CHARACTER = ",";

  private SharingNotificationVO fileSharingParam;

  /**
   * @param resource the sharing ticket entity
   * @param fileSharingParam file sharing parameters which contains selected users and external
   * emails ...
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
    } else if (Ticket.PUBLICATION_TYPE.equals(sharedObjectType)) {
      return PUBLICATION_SHARING_BUNDLE_SUBJECT_KEY;
    }
    //else FILE_TYPE or VERSION_TYPE
    return FILE_SHARING_BUNDLE_SUBJECT_KEY;
  }

  @Override
  protected void performTemplateData(String language, Ticket resource,
      SilverpeasTemplate template) {
    Ticket ticket = getResource();
    String userId = getUserId();
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    getNotificationMetaData()
        .addLanguage(language, title, "");
    template.setAttribute("senderUser", OrganizationControllerProvider.getOrganisationController().
        getUserDetail(userId));
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
    } else if (Ticket.PUBLICATION_TYPE.equals(sharedObjectType)) {
      return PUBLI_SHARING_TEMPLATE_FILE_NAME;
    }
    //else FILE_TYPE or VERSION_TYPE
    return FILE_SHARING_TEMPLATE_FILE_NAME;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.UPDATE;
  }

  /**
   * Core service, component instance identifier is useless
   * @see AbstractTemplateUserNotificationBuilder
   * #getComponentInstanceId()
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
    List<String> listUsers = new ArrayList<>();
    if (StringUtil.isDefined(selectedUsersStr)) {
      Collections.addAll(listUsers, selectedUsersStr.split(COMMA_CHARACTER));
    }
    return listUsers;
  }

  @Override
  protected Collection<String> getExternalAddressesToNotify() {
    List<String> externalAddresses = new ArrayList<>();
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
      UserNotificationHelper
          .buildAndSend(new FileSharingUserNotification(resource, fileSharingParam));
    } catch (final Exception e) {
      SilverTrace.warn("sharingTicket", "FileSharingUserNotification.notify()",
          "fileSharing.EX_ALERT_USERS_ERROR", "tocken = " + resource.getToken(), e);
    }
  }

  @Override
  protected boolean isSendImmediatly() {
    return super.isSendImmediatly();
  }

  @Override
  protected String getResourceURL(final Ticket resource) {
    return fileSharingParam.getAttachmentUrl();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    if (NODE_SHARING_TEMPLATE_FILE_NAME.equals(getFileName())) {
      return "sharing.notification.notifFolderLinkLabel";
    } else if (PUBLI_SHARING_TEMPLATE_FILE_NAME.equals(getFileName())) {
      return "sharing.notification.notifPublicationLinkLabel";
    }
    //else FILE_TYPE or VERSION_TYPE
    return "sharing.notification.notifFileLinkLabel";
  }
}
