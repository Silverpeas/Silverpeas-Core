/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.*;

import static org.silverpeas.core.notification.user.client.NotificationTemplateKey.NOTIFICATION_RECEIVER_GROUPS;
import static org.silverpeas.core.notification.user.client.NotificationTemplateKey.NOTIFICATION_RECEIVER_USERS;
import static org.silverpeas.core.ui.DisplayI18NHelper.verifyLanguage;
import static org.silverpeas.core.util.StringUtil.isDefined;

public class NotificationMetaData implements java.io.Serializable {
  private static final long serialVersionUID = 6004274748540324759L;

  public static final String BEFORE_MESSAGE_FOOTER_TAG = "<!--BEFORE_MESSAGE_FOOTER-->";
  public static final String AFTER_MESSAGE_FOOTER_TAG = "<!--AFTER_MESSAGE_FOOTER-->";
  private static final String SENDER_MESSAGE_ATTRIBUTE = "senderMessage";
  private static final String BREAK_LINE_REGEXP = "[\\n\\r]";

  private int messageType;
  private Date date;
  private String sender;
  private String source;
  private String sessionId;
  private final Collection<UserRecipient> userRecipients = new ArrayList<>();
  private final Collection<UserRecipient> userRecipientsToExclude = new ArrayList<>();
  private final Collection<GroupRecipient> groupRecipients = new ArrayList<>();
  private String externalLanguage = null;
  private final Collection<ExternalRecipient> externalRecipients = new ArrayList<>();
  private String componentId;
  private boolean isAnswerAllowed = false;
  private String fileName;
  private boolean sendImmediately = false;
  private NotifAction action;
  private final Map<String, NotificationResourceData> notificationResourceData =
      new HashMap<>();
  private final Map<String, String> titles = new HashMap<>();
  private final Map<String, String> contents = new HashMap<>();
  private final Map<String, Link> links = new HashMap<>();
  private transient Map<String, SilverpeasTemplate> templates;
  private transient Map<String, SilverpeasTemplate> templatesMessageFooter;

  private String originalExtraMessage = null;
  private boolean displayReceiversInFooter = false;

  private boolean isManualUserOne = false;

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
    this.templates = new HashMap<>();
    addLanguage(DisplayI18NHelper.getDefaultLanguage(), title, content);
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
    messageType = NotificationParameters.PRIORITY_NORMAL;
    date = new Date();
    sender = "";
    source = "";
    sessionId = "";
    componentId = "";
    isAnswerAllowed = false;
    fileName = null;
    this.templates = new HashMap<>();
    this.templatesMessageFooter = new HashMap<>();
    action = null;
    notificationResourceData.clear();
  }

  public final void addLanguage(String language, String title, String content) {
    titles.put(language, title);
    contents.put(language, content);
  }

  public Set<String> getLanguages() {
    Set<String> languages = new HashSet<>(titles.keySet());
    languages.addAll(templates.keySet());
    return languages;
  }

  public Map<String, String> getSimpleContents() {
    return Collections.unmodifiableMap(contents);
  }

  public Map<String, SilverpeasTemplate> getTemplateContents() {
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
    titles.put(DisplayI18NHelper.getDefaultLanguage(), title);
  }

  public void setTitle(String title, String language) {
    titles.put(language, title);
  }

  /**
   * Get message title
   * @return the message title
   */
  public String getTitle() {
    return getTitle(DisplayI18NHelper.getDefaultLanguage());
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
    contents.put(DisplayI18NHelper.getDefaultLanguage(), content);
  }

  public void setContent(String content, String language) {
    contents.put(language, content);
  }

  /**
   * Get message content
   * @return the message content
   */
  public String getContent() {
    return getContent(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getContent(String language) {
    StringBuilder result = new StringBuilder();
    appendMessageContent(result, language);
    appendExtraMessageHtmlFragment(result, language);

    // This below TAG permits to next treatments to decorate the message just before this footer
    result.append(BEFORE_MESSAGE_FOOTER_TAG);

    //add messageFooter containing receivers
    SilverpeasTemplate templateMessageFooter = getTemplateMessageFooter(language);
    if (templateMessageFooter != null && this.displayReceiversInFooter) {
      try {
        String receiverUsers = getUserReceiverFormattedList();
        if (StringUtil.isDefined(receiverUsers)) {
          templateMessageFooter
              .setAttribute(NOTIFICATION_RECEIVER_USERS.toString(), receiverUsers);
        }
        String receiverGroups = getGroupReceiverFormattedList();
        if (StringUtil.isDefined(receiverGroups)) {
          templateMessageFooter
              .setAttribute(NOTIFICATION_RECEIVER_GROUPS.toString(), receiverGroups);
        }
      } catch (NotificationException e) {
        SilverLogger.getLogger(this).error(e);
      }

      String messageFooter =
          templateMessageFooter.applyFileTemplate("messageFooter" + '_' + language)
              .replaceAll(BREAK_LINE_REGEXP, "");
      if (messageFooter.length() > 0) {
        result.append(messageFooter);
      }
    }

    // This below TAG permits to next treatments to decorate the message just after this footer
    result.append(AFTER_MESSAGE_FOOTER_TAG);


    return WebEncodeHelper.convertBlanksForHtml(result.toString());
  }

  private void appendMessageContent(final StringBuilder result, final String language) {
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
    }
  }

  /**
   * Appends to current message content the HTML fragment of an extra message if it does not yet
   * exists.
   * @param messageContent the message content.
   * @param language the current content language.
   */
  private void appendExtraMessageHtmlFragment(final StringBuilder messageContent,
      final String language) {
    final String extraMessage = getOriginalExtraMessage();
    if (isDefined(extraMessage) && !messageContent.toString()
        .replaceAll(BREAK_LINE_REGEXP, "")
        .contains(extraMessage.replaceAll(BREAK_LINE_REGEXP, ""))) {
      SilverpeasTemplate templateRepository =
          SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("notification");
      templateRepository.setAttribute(SENDER_MESSAGE_ATTRIBUTE, extraMessage);
      messageContent.append(
          templateRepository.applyFileTemplate("extraMessage" + '_' + verifyLanguage(language)));
    }
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
   * Gets message link in the default language of the platform.
   * @return the message link
   */
  public Link getLink() {
    return getLink(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Gets the message link in the given language.
   * @param language the ISO 631-1 code of a language supported by Silverpeas.
   * @return the message link.
   */
  public Link getLink(final String language) {
    final Link link = this.links.get(language);
    if (link == null) {
      return Link.EMPTY_LINK;
    }
    return link;
  }

  /**
   * Set message link
   * @param link the link to be set
   */
  public void setLink(String link) {
    this.links.put(DisplayI18NHelper.getDefaultLanguage(), new Link(link, ""));
  }

  /**
   * Set link
   * @param link the link to be set
   */
  public void setLink(Link link) {
    setLink(link, DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Set link
   * @param link the link to be set
   * @param language the language of the linkLabel
   */
  public void setLink(Link link, String language) {
    this.links.put(language, link);
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
   * Sets the unique identifier of a resource in Silverpeas that can have attachments. The target
   * is set for each {@link NotificationResourceData} instances embedded by this metadata.
   * @param attachmentTargetId the unique identifier of a resource. Generally, the one of a
   * contribution. If not defined, then nothing is done.
   */
  public void setAttachmentTargetId(final String attachmentTargetId) {
    if (StringUtil.isDefined(attachmentTargetId)) {
      this.notificationResourceData.forEach(
          (key, value) -> value.setAttachmentTargetId(attachmentTargetId));
    }
  }

  /**
   * Set message user recipients
   * @param users the user ids that must receive this message
   */
  public void setUserRecipients(Collection<UserRecipient> users) {
    this.userRecipients.clear();
    if (users != null) {
      addUserRecipients(users);
    }
  }

  /**
   * Get message user recipients
   * @return the message user recipients
   */
  public Collection<UserRecipient> getUserRecipients() {
    return Collections.unmodifiableCollection(userRecipients);
  }

  /**
   * Add a user recipient to user recipients. User that has not an activated state is not taken
   * into account.
   * @param user recipient that must be added
   */
  public void addUserRecipient(UserRecipient user) {
    if (User.isActivatedStateFor(user.getUserId())) {
      userRecipients.add(user);
    }
  }

  /**
   * Add a user recipient to user recipients. User that has not an activated state is not taken
   * into account.
   * @param users users to be added
   */
  public void addUserRecipients(UserRecipient... users) {
    if (users != null) {
      addUserRecipients(Arrays.asList(users));
    }
  }

  /**
   * Add a user recipient to user recipients. User that has not an activated state is not taken
   * into account.
   * @param users users to be added
   */
  public void addUserRecipients(Collection<UserRecipient> users) {
    if (users != null) {
      for (UserRecipient userRecipient : users) {
        addUserRecipient(userRecipient);
      }
    }
  }

  /**
   * Set message user recipients to exclude
   * @param users the user ids that must not receive this message
   */
  public void setUserRecipientsToExclude(Collection<UserRecipient> users) {
    this.userRecipientsToExclude.clear();
    if (users != null) {
      for(UserRecipient recipient: users) {
        addUserRecipient(recipient);
      }
    }
  }

  /**
   * Get message user recipients to exclude
   * @return the message user recipients
   */
  public Collection<UserRecipient> getUserRecipientsToExclude() {
    return Collections.unmodifiableCollection(userRecipientsToExclude);
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

  public String getExternalLanguage() {
    return Optional.ofNullable(externalLanguage).orElse(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets language to use for external receivers.
   * @param externalLanguage a lenguage as string.
   */
  public void setExternalLanguage(final String externalLanguage) {
    this.externalLanguage = externalLanguage;
  }

  /**
   * @return the externalAddress
   */
  public Collection<ExternalRecipient> getExternalRecipients() {
    return Collections.unmodifiableCollection(externalRecipients);
  }

  /**
   * @param externalRecipients the externalAddress to set
   */
  public void setExternalRecipients(Collection<ExternalRecipient> externalRecipients) {
    this.externalRecipients.clear();
    if (externalRecipients != null) {
      for(ExternalRecipient recipient: externalRecipients) {
        addExternalRecipient(recipient);
      }
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
    this.groupRecipients.clear();
    if (groups != null) {
      addGroupRecipients(groups);
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
      templates.get(language).setAttribute(SENDER_MESSAGE_ATTRIBUTE, message);
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
    setNotificationResourceData(DisplayI18NHelper.getDefaultLanguage(), notificationResourceData);
  }

  public void setNotificationResourceData(final String lang, final NotificationResourceData notificationResourceData) {
    this.notificationResourceData.put(lang, notificationResourceData);
  }

  public NotificationResourceData getNotificationResourceData() {
    return getNotificationResourceData(DisplayI18NHelper.getDefaultLanguage());
  }

  public NotificationResourceData getNotificationResourceData(final String lang) {
    return notificationResourceData.get(lang);
  }

  /**
   * Indicates if the notification is sent by a Silverpeas user or by a batch treatment.
   * @return true if the notification is sent by a Silverpeas user, false otherwise.
   */
  public boolean isSendByAUser() {
    return StringUtil.isInteger(getSender());
  }

  private SilverpeasTemplate createTemplateMessageFooter(String language) {
    SilverpeasTemplate templateFooter =
        SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("notification");
    this.templatesMessageFooter.put(language, templateFooter);
    return templateFooter;
  }

  public SilverpeasTemplate getTemplateMessageFooter(String language) {
    if(this.templatesMessageFooter == null) {
      this.templatesMessageFooter = new HashMap<>();
    }

    SilverpeasTemplate templateMessageFooter = this.templatesMessageFooter.get(language);
    if(templateMessageFooter == null) {
      templateMessageFooter = createTemplateMessageFooter(language);
    }

    return templateMessageFooter;
  }

  /**
   * Gets the complete list of users that will receive the notification, so it takes into account
   * users of groups.<br>
   * If the sender is identified, it is removed from the result.<br>
   * No internal data of the current {@link NotificationMetaData} is updated.
   * @return the complete list of users that will receive the notification.
   * @throws NotificationException if an error occurs
   */
  public Set<UserRecipient> getAllUserRecipients() throws NotificationException {
    return getAllUserRecipients(false);
  }

  /**
   * Gets the complete list of users that will receive the notification, so it takes into account
   * users of groups.<br>
   * If the sender is identified (as a user), it is removed from the result.<br>
   * @param updateInternalUserRecipientsToExclude if true, the internal container of user
   * recipients to exclude will be updated. This container is provided by {@link
   * #getUserRecipientsToExclude()}. If false, nothing is done.
   * @return the complete list of users that will receive the notification.
   * @throws NotificationException if an error occurs
   */
  public Set<UserRecipient> getAllUserRecipients(boolean updateInternalUserRecipientsToExclude)
      throws NotificationException {

    Set<UserRecipient> allUniqueUserRecipients = new HashSet<>();
    Collection<UserRecipient> users = getUserRecipients();
    Collection<GroupRecipient> groups = getGroupRecipients();
    Collection<UserRecipient> usersToExclude =
        updateInternalUserRecipientsToExclude ? getUserRecipientsToExclude() :
            new HashSet<>(getUserRecipientsToExclude());

    // First get direct users
    allUniqueUserRecipients.addAll(users);

    // Then get users included in groups
    final NotificationManager notificationManager = getNotificationManager();
    for (GroupRecipient group : groups) {
      allUniqueUserRecipients.addAll(notificationManager.getUsersFromGroup(group.getGroupId()));
    }

    // Then exclude users that don't have to be notified
    allUniqueUserRecipients.removeAll(usersToExclude);

    // Returning the completed list
    return allUniqueUserRecipients;
  }

  private Set<UserRecipient> getUsersForReceiverBlock()
      throws NotificationException {
    HashSet<UserRecipient> usersSet = new HashSet<>();
    usersSet.addAll(getUserRecipients());
    final NotificationManager notificationManager = getNotificationManager();
    for (GroupRecipient group : getGroupRecipients()) {
      if (!displayGroup(group.getGroupId())) {
        usersSet.addAll(notificationManager.getUsersFromGroup(group.getGroupId()));
      }
    }

    // Then exclude users that don't have to be notified
    usersSet.removeAll(getUserRecipientsToExclude());

    return usersSet;
  }

  protected boolean displayGroup(String groupId) {
    int threshold = NotificationManagerSettings.getReceiverThresholdAfterThatReplaceUserNameListByGroupName();
    Group group = Group.getById(groupId);
    int nbUsers = group.getNbUsers();
    boolean res1 = NotificationManagerSettings.isDisplayingUserNameListInsteadOfGroupEnabled();
    boolean res2 = threshold > 0 && nbUsers > threshold;
    return res1 || res2;
  }

  private String getUserReceiverFormattedList() throws NotificationException {
    StringBuilder users = new StringBuilder();
    if (NotificationManagerSettings.isDisplayingReceiversInNotificationMessageEnabled() && this.displayReceiversInFooter) {
      Set<UserRecipient> usersSet = getUsersForReceiverBlock();
      boolean first = true;
      for (UserRecipient anUsersSet : usersSet) {
        if (!first) {
          users.append(", ");
        }
        users.append(User.getById(anUsersSet.getUserId()).getDisplayedName());
        first = false;
      }
    }
    return users.toString();
  }

  private Set<GroupRecipient> getGroupsForReceiverBlock() {
    HashSet<GroupRecipient> groupsSet = new HashSet<>();
    for (GroupRecipient group : getGroupRecipients()) {
      if (displayGroup(group.getGroupId())) {
        // add groups names
        groupsSet.add(group);
      }
    }
    return groupsSet;
  }

  private String getGroupReceiverFormattedList() {
    StringBuilder groups = new StringBuilder();
    if (NotificationManagerSettings.isDisplayingReceiversInNotificationMessageEnabled() && this.displayReceiversInFooter) {
      Set<GroupRecipient> groupsSet = getGroupsForReceiverBlock();
      boolean first = true;
      for (GroupRecipient aGroupsSet : groupsSet) {
        if (!first) {
          groups.append(", ");
        }
        groups.append(Group.getById(aGroupsSet.getGroupId()).getName());
        first = false;
      }
    }
    return groups.toString();
  }

  /**
   * Calling this method to authorize the display of the users and groups that will
   * received the same notification in the footer of the notification message.
   * @return the current {@link NotificationMetaData} instance.
   */
  public NotificationMetaData displayReceiversInFooter() {
    this.displayReceiversInFooter = true;
    return this;
  }

  /**
   * Sets that the current {@link NotificationMetaData} instance concerns a manual notification
   * between a sender user and several user/group receivers.
   * @return the current {@link NotificationMetaData} instance.
   */
  public NotificationMetaData manualUserNotification() {
    this.isManualUserOne = true;
    return this;
  }

  /**
   * Indicates if the current {@link NotificationMetaData} concerns a manual notification between a
   * sender user and several user/group receivers. <br>
   * Warning : the sender information is not verified.
   * @return true if the current {@link NotificationMetaData} concerns a manual one, false
   * otherwise.
   */
  public boolean isManualUserOne() {
    return isManualUserOne;
  }

  /**
   * Gets the notification manager instance.
   * @return the notification manager instance.
   */
  private NotificationManager getNotificationManager() {
    return NotificationManager.get();
  }
}
