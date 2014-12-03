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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.notificationManager;

import static com.stratelia.silverpeas.notificationManager.NotificationTemplateKey.notification_receiver_groups;
import static com.stratelia.silverpeas.notificationManager.NotificationTemplateKey.notification_receiver_users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.util.Link;

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.util.ResourceLocator;

public class NotificationMetaData implements java.io.Serializable {

  private static final long serialVersionUID = 6004274748540324759L;
  private int messageType;
  private Date date;
  private String sender;
  private String source;
  private String link;
  private String sessionId;
  private Collection<UserRecipient> userRecipients;
  private Collection<UserRecipient> userRecipientsToExclude;
  private Collection<GroupRecipient> groupRecipients;
  private Collection<ExternalRecipient> externalRecipients;
  private String componentId;
  private boolean isAnswerAllowed = false;
  private String fileName;
  private boolean sendImmediately = false;
  private NotifAction action;
  private Map<String, NotificationResourceData> notificationResourceData =
      new HashMap<String, NotificationResourceData>();

  private Map<String, String> titles = new HashMap<String, String>();
  private Map<String, String> contents = new HashMap<String, String>();
  private Map<String, String> linkLabels = new HashMap<String, String>();

  private Map<String, SilverpeasTemplate> templates;
  private Map<String, SilverpeasTemplate> templatesMessageFooter;

  private String originalExtraMessage = null;
  private boolean displayReceiversInFooter = false;
  
  private ResourceLocator settings = new ResourceLocator(
      "org.silverpeas.notificationManager.settings.notificationManagerSettings", "");


  /**
   * Default Constructor
   */
  public NotificationMetaData() {
    reset();
  }

  /**
   * Most common used constructor
   * @param messageType message type (NORMAL, URGENT, ...)
   * @param title message title (=subject)
   * @param content message content (=body)
   */
  public NotificationMetaData(int messageType, String title, String content) {
    reset();
    this.messageType = messageType;
    this.templates = new HashMap<String, SilverpeasTemplate>();
    addLanguage(I18NHelper.defaultLanguage, title, content);
  }

  public NotificationMetaData(int messageType, String title,
      Map<String, SilverpeasTemplate> templates, String fileName) {
    this(messageType, title, "");
    reset();
    this.templates = templates;
    this.fileName = fileName;
  }

  /**
   * reset all attributes
   */
  private void reset() {
    messageType = NotificationParameters.NORMAL;
    date = new Date();
    sender = "";
    source = "";
    link = "";
    sessionId = "";
    userRecipients = new ArrayList<UserRecipient>();
    userRecipientsToExclude = new ArrayList<UserRecipient>();
    groupRecipients = new ArrayList<GroupRecipient>();
    externalRecipients = new ArrayList<ExternalRecipient>();
    componentId = "";
    isAnswerAllowed = false;
    fileName = null;
    this.templates = new HashMap<String, SilverpeasTemplate>();
    this.templatesMessageFooter = new HashMap<String, SilverpeasTemplate>();
    action = null;
    notificationResourceData.clear();
  }

  public final void addLanguage(String language, String title, String content) {
    titles.put(language, title);
    contents.put(language, content);
  }

  public Set<String> getLanguages() {
    return titles.keySet();
  }

  public Map<String, SilverpeasTemplate> getTemplates() {
    return Collections.unmodifiableMap(templates);
  }

  /**
   * Set message type
   * @param messageType the message type to be set
   */
  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

  /**
   * Get message type
   * @return the message type
   */
  public int getMessageType() {
    return messageType;
  }

  /**
   * Set message date
   * @param date the message date to be set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Get message date
   * @return the message date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Set message title
   * @param title the title to be set
   */
  public void setTitle(String title) {
    titles.put(I18NHelper.defaultLanguage, title);
  }

  public void setTitle(String title, String language) {
    titles.put(language, title);
  }

  /**
   * Get message title
   * @return the message title
   */
  public String getTitle() {
    // return title;
    return getTitle(I18NHelper.defaultLanguage);
  }

  public String getTitle(String language) {
    String result = "";
    if (templates != null && !templates.isEmpty()) {
      SilverpeasTemplate template = templates.get(language);
      if (template != null) {
        result = template.applyStringTemplate(titles.get(language));
      }
    } else {
      result = titles.get(language);
    }
    return result;
  }

  /**
   * Set message content
   * @param content the content to be set
   */
  public void setContent(String content) {
    // this.content = content;
    contents.put(I18NHelper.defaultLanguage, content);
  }

  public void setContent(String content, String language) {
    SilverTrace.info("notificationManager",
        "NotificationMetaData.setContent()", "root.MSG_GEN_ENTER_METHOD",
        "language = " + language + ", content = " + content);
    contents.put(language, content);
  }

  /**
   * Get message content
   * @return the message content
   */
  public String getContent() {
    return getContent(I18NHelper.defaultLanguage);
  }

  public String getContent(String language) {
    SilverTrace.info("notificationManager",
        "NotificationMetaData.getContent()", "root.MSG_GEN_ENTER_METHOD",
        "language = " + language);
    StringBuilder result = new StringBuilder();
    if (templates != null && !templates.isEmpty()) {
      SilverpeasTemplate template = templates.get(language);
      if (template != null) {
        result.append(template.applyFileTemplate(fileName + '_' + language));
      }
    } else {
      String content = contents.get(language);
      if(content != null) {
        result.append(content);
      }

      if(getOriginalExtraMessage() != null) {
        ResourceLocator alertUserPeasMessage = new ResourceLocator("org.silverpeas.alertUserPeas.multilang.alertUserPeasBundle",
            language);
        result.append("\n\n").append(alertUserPeasMessage.getString("AuthorMessage"))
            .append(" : \n")
            .append("<div style=\"background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; " +
                    "width:390px;\">")
            .append(getOriginalExtraMessage())
            .append("</div>");
      }
    }
    
    //add messageFooter containing receivers
    SilverpeasTemplate templateMessageFooter = getTemplateMessageFooter(language);
    if (templateMessageFooter != null && this.displayReceiversInFooter) {
      try {
        String receiver_users = addReceiverUsers();
        if (StringUtil.isDefined(receiver_users)) {
          templateMessageFooter.setAttribute(notification_receiver_users.toString(), receiver_users);
        }
        String receiver_groups = addReceiverGroups();
        if (StringUtil.isDefined(receiver_groups)) {
          templateMessageFooter.setAttribute(notification_receiver_groups.toString(), receiver_groups);
        }
      } catch (NotificationManagerException e) {
        SilverTrace.warn("notificationManager",
            "NotificationMetaData.getContent()",
            "root.EX_ADD_USERS_FAILED", e);
      }
      
      String messageFooter = templateMessageFooter.applyFileTemplate("messageFooter" + '_' + language);
      result.append("\n\n"+messageFooter);
    }
    SilverTrace.info("notificationManager", "NotificationMetaData.getContent()",
        "root.MSG_GEN_EXIT_METHOD", "result = " + result);
    return EncodeHelper.convertWhiteSpacesForHTMLDisplay(result.toString());
  }

  /**
   * Set message source
   * @param source the source to be set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * Get message source
   * @return the message source
   */
  public String getSource() {
    return source;
  }

  /**
   * Set message sender
   * @param sender the sender to be set
   */
  public void setSender(String sender) {
    this.sender = sender;
  }

  /**
   * Set answer allowed
   * @param answerAllowed if answer allowed
   */
  public void setAnswerAllowed(boolean answerAllowed) {
    this.isAnswerAllowed = answerAllowed;
  }

  /**
   * Get message sender
   * @return the message sender
   */
  public String getSender() {
    return sender;
  }

  /**
   * Get answer allowed
   * @return if answer is allowed
   */
  public boolean isAnswerAllowed() {
    return isAnswerAllowed;
  }

  /**
   * Set message link
   * @param link the link to be set
   */
  public void setLink(String link) {
    this.link = link;
  }

  /**
   * Get message link
   * @return the message link
   */
  public String getLink() {
    return link;
  }
  
  /**
   * Set link
   * @param link the link to be set
   */
  public void setLink(Link link) {
    setLink(link, I18NHelper.defaultLanguage);
  }
  
  /**
   * Set link
   * @param link the link to be set
   * @param language the language of the linkLabel
   */
  public void setLink(Link link, String language) {
    this.link = link.getLinkUrl();
    linkLabels.put(language, link.getLinkLabel());
  }

  /**
   * Get message linkLabel
   * @return the message linkLabel
   */
  public String getLinkLabel() {
    return getLinkLabel(I18NHelper.defaultLanguage);
  }

  public String getLinkLabel(String language) {
    return linkLabels.get(language);
  }

  /**
   * Set message session Id
   * @param sessionId the session Id to be set
   */
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Get message session Id
   * @return the message session Id
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Set message user recipients
   * @param users the user ids that must receive this message
   */
  public void setUserRecipients(Collection<UserRecipient> users) {
    if (users != null) {
      this.userRecipients = new ArrayList<UserRecipient>(users);
    } else {
      this.userRecipients = new ArrayList<UserRecipient>();
    }
  }

  /**
   * Get message user recipients
   * @return the message user recipients
   */
  public Collection<UserRecipient> getUserRecipients() {
    return userRecipients;
  }

  /**
   * Add a user recipient to user recipients
   * @param user recpient that must be added
   */
  public void addUserRecipient(UserRecipient user) {
    userRecipients.add(user);
  }

  /**
   * Add a user recipient to user recipients
   * @param users users to be added
   */
  public void addUserRecipients(UserRecipient[] users) {
    if (users != null) {
      this.userRecipients.addAll(Arrays.asList(users));
    }
  }

  /**
   * Add a user recipient to user recipients
   * @param users users to be added
   */
  public void addUserRecipients(Collection<UserRecipient> users) {
    if (users != null) {
      this.userRecipients.addAll(users);
    }
  }

  /**
   * Set message user recipients to exclude
   * @param users the user ids that must not receive this message
   */
  public void setUserRecipientsToExclude(Collection<UserRecipient> users) {
    if (users != null) {
      this.userRecipientsToExclude = new ArrayList<UserRecipient>(users);
    } else {
      this.userRecipientsToExclude = new ArrayList<UserRecipient>();
    }
  }

  /**
   * Get message user recipients to exclude
   * @return the message user recipients
   */
  public Collection<UserRecipient> getUserRecipientsToExclude() {
    return userRecipientsToExclude;
  }

  /**
   * Add a user recipient to user recipients to exclude
   * @param user recipient that must not be notified
   */
  public void addUserRecipientToExclude(UserRecipient user) {
    userRecipientsToExclude.add(user);
  }

  /**
   * Add a user recipient to user recipients to exclude
   * @param users recipient that must not be notified
   */
  public void addUserRecipientsToExclude(UserRecipient[] users) {
    if (users != null) {
      this.userRecipientsToExclude.addAll(Arrays.asList(users));
    }
  }

  /**
   * Add a user recipient to user recipients to exclude
   * @param users recipient that must not be notified
   */
  public void addUserRecipientsToExclude(Collection<UserRecipient> users) {
    if (users != null) {
      this.userRecipientsToExclude.addAll(users);
    }
  }

  /**
   * @return the externalAddress
   */
  public Collection<ExternalRecipient> getExternalRecipients() {
    return externalRecipients;
  }

  /**
   * @param externalRecipients the externalAddress to set
   */
  public void setExternalRecipients(Collection<ExternalRecipient> externalRecipients) {
    if (externalRecipients != null) {
      this.externalRecipients = externalRecipients;
    }
  }

  /**
   * @param externalRecipient the externalRecipient to add
   */
  public void addExternalRecipient(ExternalRecipient externalRecipient) {
    externalRecipients.add(externalRecipient);
  }

  /**
   * Set message group recipients
   * @param groups the groups that must receive this message
   */
  public void setGroupRecipients(Collection<GroupRecipient> groups) {
    if (groups != null) {
      this.groupRecipients = new ArrayList<GroupRecipient>(groups);
    } else {
      this.groupRecipients = new ArrayList<GroupRecipient>();
    }
  }

  /**
   * Get message group recipients
   * @return the message group recipients
   */
  public Collection<GroupRecipient> getGroupRecipients() {
    return Collections.unmodifiableCollection(groupRecipients);
  }

  /**
   * Add a group recipient to group recipients
   * @param group group that must be added
   */
  public void addGroupRecipient(GroupRecipient group) {
    groupRecipients.add(group);
  }

  /**
   * Add group recipients to group recipients
   * @param groups
   */
  public void addGroupRecipients(Collection<GroupRecipient> groups) {
    if (groups != null) {
      groupRecipients.addAll(groups);
    }
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public void addExtraMessage(String message, String language) {
    setOriginalExtraMessage(message);
    if (templates != null && !templates.isEmpty()) {
      templates.get(language).setAttribute("senderMessage", message);
    }
  }

  public String getOriginalExtraMessage() {
    return originalExtraMessage;
  }

  public void setOriginalExtraMessage(String originalExtraMessage) {
    this.originalExtraMessage = originalExtraMessage;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isSendImmediately() {
    return sendImmediately;
  }

  public void setSendImmediately(boolean sendImmediately) {
    this.sendImmediately = sendImmediately;
  }

  public NotifAction getAction() {
    return action;
  }

  public void setAction(NotifAction action) {
    this.action = action;
  }

  public void setNotificationResourceData(final NotificationResourceData notificationResourceData) {
    setNotificationResourceData(I18NHelper.defaultLanguage, notificationResourceData);
  }

  public void setNotificationResourceData(final String lang, final NotificationResourceData notificationResourceData) {
    this.notificationResourceData.put(lang, notificationResourceData);
  }

  public NotificationResourceData getNotificationResourceData() {
    return getNotificationResourceData(I18NHelper.defaultLanguage);
  }

  public NotificationResourceData getNotificationResourceData(final String lang) {
    return notificationResourceData.get(lang);
  }
  
  /**
   * Indicates if the notification is manual (sent by a Silverpeas user) or automatic.
   *
   * @return true if the notification is sent by a Silverpeas user - false otherwise.
   */
  public boolean isManual() {
    return StringUtil.isInteger(getSender());
  }
  
  private SilverpeasTemplate createTemplateMessageFooter(String language) {
    SilverpeasTemplate templateFooter = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("notification");
    this.templatesMessageFooter.put(language, templateFooter);  
    return templateFooter;
  }
  
  public SilverpeasTemplate getTemplateMessageFooter(String language) {
    if(this.templatesMessageFooter == null || this.templatesMessageFooter.isEmpty()) {
      this.templatesMessageFooter = new HashMap<String, SilverpeasTemplate>();
      return createTemplateMessageFooter(language);
    }
    
    SilverpeasTemplate templateMessageFooter = this.templatesMessageFooter.get(language);
    if(templateMessageFooter == null) {
      return createTemplateMessageFooter(language);
    }
   
    return templateMessageFooter;
  }
  
  public Set<UserRecipient> getUserSet()
      throws NotificationManagerException {
    HashSet<UserRecipient> usersSet = new HashSet<UserRecipient>();
    usersSet.addAll(getUserRecipients());
    for (GroupRecipient group : getGroupRecipients()) {
      if (!displayGroup(group.getGroupId())) {
        usersSet.addAll(new NotificationManager(null).getUsersFromGroup(group.getGroupId()));
      }
    }

    // Then exclude users that don't have to be notified
    usersSet.removeAll(getUserRecipientsToExclude());

    return usersSet;
  }
  
  private boolean displayGroup(String groupId) {
    String threshold = settings.getString("notif.receiver.displayUser.threshold");
    OrganisationController orgaController = OrganisationControllerFactory.
        getOrganisationController();
    Group group = orgaController.getGroup(groupId);
    int nbUsers = group.getNbUsers();
    boolean res1 = settings.getBoolean("notif.receiver.displayGroup", false);
    boolean res2 = StringUtil.isDefined(threshold);
    boolean res3 = StringUtil.isInteger(threshold);
    boolean res4 = nbUsers > Integer.parseInt(threshold);
    boolean result = res1 || (res2 && res3 && res4);
    return result;
  }
  
  public void displayReceiversInFooter() {
    this.displayReceiversInFooter = true;
  }
  
  public String addReceiverUsers() throws NotificationManagerException {
    StringBuilder users = new StringBuilder();
    if (settings.getBoolean("addReceiversInBody", false) && this.displayReceiversInFooter) {
      Set<UserRecipient> usersSet = getUserSet();
      OrganisationController orgaController = OrganisationControllerFactory.
        getOrganisationController();
      boolean first = true;
      for (UserRecipient anUsersSet : usersSet) {
        if (!first) {
          users.append(", ");
        }
        users.append(orgaController.getUserDetail(anUsersSet.getUserId()).getDisplayedName());
        first = false;
      }
    }
    return users.toString();
  }
  
  public Set<GroupRecipient> getGroupSet() {
    HashSet<GroupRecipient> groupsSet = new HashSet<GroupRecipient>();
    for (GroupRecipient group : getGroupRecipients()) {
      if (displayGroup(group.getGroupId())) {
        // add groups names
        groupsSet.add(group);
      }
    }
    return groupsSet;
  }

  public String addReceiverGroups() {
    StringBuilder groups = new StringBuilder();
    if (settings.getBoolean("addReceiversInBody", false) && this.displayReceiversInFooter) {
      Set<GroupRecipient> groupsSet = getGroupSet();
      OrganisationController orgaController =
        OrganisationControllerFactory.getOrganisationController();
      boolean first = true;
      for (GroupRecipient aGroupsSet : groupsSet) {
        if (!first) {
          groups.append(", ");
        }
        groups.append(orgaController.getGroup(aGroupsSet.getGroupId()).getName());
        first = false;
      }
    }
    return groups.toString();
  }
}