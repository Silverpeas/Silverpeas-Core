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
package com.silverpeas.notification.delayed.delegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silverpeas.notification.delayed.DelayedNotificationFactory;
import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.notification.delayed.model.DelayedNotificationData;
import com.silverpeas.notification.delayed.model.DelayedNotificationUserSetting;
import com.silverpeas.notification.delayed.synthese.DelayedNotificationSyntheseData;
import com.silverpeas.notification.delayed.synthese.SyntheseResource;
import com.silverpeas.notification.delayed.synthese.SyntheseResourceNotification;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.MapUtil;
import com.silverpeas.util.comparator.AbstractComplexComparator;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;

import com.stratelia.silverpeas.notificationManager.AbstractNotification;
import com.stratelia.silverpeas.notificationManager.NotificationParameterNames;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServer;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationDelegate extends AbstractNotification {

  private final static String LOCATION_SEPARATOR = " &gt; ";

  /** Global settings */
  private final Map<String, ResourceLocator> multilang = new HashMap<String, ResourceLocator>();

  /** Notification server instance */
  private final NotificationServer notificationServer = new NotificationServer();

  /** User details cache */
  private static final int MAX_USER_DETAIL_ITEMS = 100;
  private final Map<Integer, UserDetail> userDetailCache = new LinkedHashMap<Integer, UserDetail>(
      MAX_USER_DETAIL_ITEMS);

  /** String templates */
  private SilverpeasTemplate template = null;

  /** Comparators */
  private Comparator<NotificationResourceData> resourceComparator;
  private Comparator<DelayedNotificationData> delayedNotificationComparator;

  /**
   * Default constructor
   */
  protected DelayedNotificationDelegate() {
    // Nothing to do
  }

  /*
   * User setting updates
   */

  /**
   * Deleting all delayed notification data of a user
   * @param userId
   * @throws Exception
   */
  public static void executeUserDeleting(final int userId) throws Exception {

    // Forcing sending of delayed notification on the aimed channel
    executeForceDelayedNotificationsSending(userId, DelayedNotificationFactory.getDelayedNotification()
        .getWiredChannels());

    // Deleting settings
    for (final NotifChannel channel : DelayedNotificationFactory.getDelayedNotification().getWiredChannels()) {
      final DelayedNotificationUserSetting settings =
          DelayedNotificationFactory.getDelayedNotification().getDelayedNotificationUserSettingByUserIdAndChannel(
              userId, channel);
      if (settings != null) {
        DelayedNotificationFactory.getDelayedNotification().deleteDelayedNotificationUserSetting(settings);
      }
    }
  }

  /**
   * When user settings change, if the new frequency is NONE then the delayed notifications saved have to be sent
   * @param userId
   * @param channel
   * @param frequency
   * @throws Exception
   */
  public static DelayedNotificationUserSetting executeUserSettingsUpdating(final int userId,
      final NotifChannel channel, final DelayedNotificationFrequency frequency) throws Exception {
    DelayedNotificationUserSetting result = null;
    // Getting old settings
    final DelayedNotificationUserSetting oldSettings =
        DelayedNotificationFactory.getDelayedNotification()
            .getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel);

    // Updating settings
    if (frequency != null) {
      result =
          DelayedNotificationFactory.getDelayedNotification().saveDelayedNotificationUserSetting(
              userId, channel, frequency);
    } else if (oldSettings != null) {
      // User settings are deleted from persistence system. Default Silverpeas's frequency will be
      // use for the given user.
      DelayedNotificationFactory.getDelayedNotification().deleteDelayedNotificationUserSetting(
          oldSettings);
    }

    // Forcing sending of delayed notification on the aimed channel if settings have been changed
    if ((oldSettings == null && frequency != null) ||
        (oldSettings != null && !oldSettings.getFrequency().equals(frequency))) {
      executeForceDelayedNotificationsSending(userId, Collections.singleton(channel));
    }

    return result;
  }

  /*
   * New notification
   */

  /**
   * Easy call of new notification process
   * @param delayedNotificationData
   * @throws Exception
   */
  public static void executeNewNotification(final DelayedNotificationData delayedNotificationData)
      throws Exception {
    new DelayedNotificationDelegate().performNewNotificationSending(delayedNotificationData);
  }

  /**
   * Handling a new notification
   * @param delayedNotificationData
   * @throws Exception
   */
  protected void performNewNotificationSending(final DelayedNotificationData delayedNotificationData)
      throws Exception {
    if (!isThatToBeDelayed(delayedNotificationData)) {
      sendNotification(delayedNotificationData.getNotificationData());
    } else {
      DelayedNotificationFactory.getDelayedNotification().saveDelayedNotification(
          delayedNotificationData);
    }
  }

  /**
   * Checks if the notification has to be delayed or not
   * @param delayedNotificationData
   * @return
   */
  private boolean isThatToBeDelayed(final DelayedNotificationData delayedNotificationData) {

    // The notification have to be sent immediately
    if (delayedNotificationData.isSendImmediately()) {
      return false;
    }

    // The notification action has to be defined
    if (delayedNotificationData.getAction() == null) {
      return false;
    }

    // The notification priority has to be different from URGENT or ERROR and the action type has to
    // be setted
    if (NotificationParameters.NORMAL != delayedNotificationData.getNotificationParameters().iMessagePriority) {
      return false;
    }

    // The user frequency has to be different from NONE
    if (DelayedNotificationFrequency.NONE.equals(DelayedNotificationFactory
        .getDelayedNotification().getUserFrequency(delayedNotificationData.getUserId(),
            delayedNotificationData.getChannel()))) {
      return false;
    }

    // The last conditions
    return delayedNotificationData.isValid();
  }

  /*
   * Delayed notifications
   */

  /**
   * Easy call of delayed notifications process
   * @param date
   * @throws Exception
   */
  public static void executeDelayedNotificationsSending(final Date date) throws Exception {
    new DelayedNotificationDelegate().performDelayedNotificationsSending(date,
        DelayedNotificationFactory.getDelayedNotification().getWiredChannels());
  }

  /**
   * Easy call of delayed notifications process. Forces the sending of all the delayed notifications saved for all users
   * @throws Exception
   */
  public static void executeForceDelayedNotificationsSending() throws Exception {
    new DelayedNotificationDelegate().forceDelayedNotificationsSending();
  }

  /**
   * Easy call of delayed notifications process. Forces the sending of all the delayed notifications saved for a given
   * user
   * @param userId
   * @param channels
   * @throws Exception
   */
  public static void executeForceDelayedNotificationsSending(final int userId,
      final Set<NotifChannel> channels) throws Exception {
    executeForceDelayedNotificationsSending(Collections.singletonList(userId), channels);
  }

  /**
   * Easy call of delayed notifications process. Forces the sending of all the delayed notifications saved for given
   * users and channels
   * @param userIds
   * @param channels
   * @throws Exception
   */
  public static void executeForceDelayedNotificationsSending(final List<Integer> userIds,
      final Set<NotifChannel> channels)
      throws Exception {
    new DelayedNotificationDelegate().forceDelayedNotificationsSending(userIds, channels);
  }

  /**
   * Forces the sending of all the delayed notifications saved for all users
   * @throws Exception
   */
  protected void forceDelayedNotificationsSending() throws Exception {

    // Searching all the users from delayed notifications
    final List<Integer> usersToBeNotified =
        DelayedNotificationFactory.getDelayedNotification()
            .findAllUsersToBeNotified(
                DelayedNotificationFactory.getDelayedNotification().getWiredChannels());

    // Performing all users to notify
    forceDelayedNotificationsSending(usersToBeNotified, DelayedNotificationFactory
        .getDelayedNotification().getWiredChannels());
  }

  /**
   * Forces the sending of all the delayed notifications saved for given users and channels
   * @param userIds
   * @param channels
   * @throws Exception
   */
  protected void forceDelayedNotificationsSending(final List<Integer> userIds,
      final Set<NotifChannel> channels) throws Exception {
    performUsersDelayedNotifications(userIds, channels);
  }

  /**
   * Handling the saved delayed notifications from a given date and given channels
   * @param date
   * @param channels
   * @throws Exception
   */
  protected void performDelayedNotificationsSending(final Date date,
      final Set<NotifChannel> channels) throws Exception {

    // Searching all the users that have to be notify from a given date and given channels
    final List<Integer> usersToBeNotified =
        DelayedNotificationFactory.getDelayedNotification().findUsersToBeNotified(
            date,
            channels,
            DelayedNotificationFactory.getDelayedNotification()
                .getDefaultDelayedNotificationFrequency());

    // Performing all users to notify
    performUsersDelayedNotifications(usersToBeNotified, channels);
  }

  /**
   * Performing delayed notifications for given users and channels
   * @param usersToBeNotified
   * @param channels
   * @throws Exception
   * @throws NotificationServerException
   */
  private void performUsersDelayedNotifications(final List<Integer> usersToBeNotified,
      final Set<NotifChannel> channels)
      throws Exception {

    // Stopping if no users to notify
    if (CollectionUtil.isEmpty(usersToBeNotified)) {
      return;
    }

    // Performing all users to notify
    final Collection<Long> delayedNotificationIdsToDelete = new ArrayList<Long>();
    try {
      Map<NotifChannel, List<DelayedNotificationData>> delayedNotifications;
      for (final Integer userIdToNotify : usersToBeNotified) {

        // Searching current user notifications, group by channels
        delayedNotifications =
            DelayedNotificationFactory.getDelayedNotification()
                .findDelayedNotificationByUserIdGroupByChannel(userIdToNotify, channels);

        // Browse channel notifications
        for (final Map.Entry<NotifChannel, List<DelayedNotificationData>> mapEntry : delayedNotifications
            .entrySet()) {
          delayedNotificationIdsToDelete.addAll(performUserDelayedNotificationsOnChannel(
              mapEntry.getKey(), mapEntry.getValue()));
        }
      }
    } finally {

      // Deleting massively the delayed notifications treated and the associated notification
      // resource data
      DelayedNotificationFactory.getDelayedNotification().deleteDelayedNotifications(
          delayedNotificationIdsToDelete);
    }
  }

  /**
   * Performing delayed notifications for a given user and a given channel
   * @param channel
   * @param delayedNotifications
   * @throws Exception
   * @throws NotificationServerException
   */
  private Collection<Long> performUserDelayedNotificationsOnChannel(final NotifChannel channel,
      final List<DelayedNotificationData> delayedNotifications) throws Exception {
    final DelayedNotificationSyntheseData synthese = buildSynthese(delayedNotifications);
    sendNotification(createNotificationData(channel, synthese));
    return synthese.getDelayedNotificationIdProceeded();
  }

  /**
   * Builds all the elements constituting the synthese of user notifications
   * @param delayedNotifications
   * @return
   * @throws Exception
   */
  private DelayedNotificationSyntheseData buildSynthese(
      final List<DelayedNotificationData> delayedNotifications) throws Exception {

    // Result
    final DelayedNotificationSyntheseData synthese = initializeSynthese(delayedNotifications);

    // Indexing
    final Map<NotificationResourceData, List<DelayedNotificationData>> resourcesAndNotifications =
        new HashMap<NotificationResourceData, List<DelayedNotificationData>>();
    for (final DelayedNotificationData delayedNotificationData : delayedNotifications) {
      MapUtil.putAddList(resourcesAndNotifications, delayedNotificationData.getResource(),
          delayedNotificationData);
    }

    // Sorting indexes
    final List<NotificationResourceData> orderedResources =
        new ArrayList<NotificationResourceData>(resourcesAndNotifications.keySet());
    Collections.sort(orderedResources, getResourceComparator());

    // Browsing all the delayed notifications to build the synthese
    for (final NotificationResourceData resource : orderedResources) {

      // Performing a resource and her associated notifications
      prepareSyntheseResourceAndNotifications(synthese, resource,
          resourcesAndNotifications.get(resource));
    }

    // Building the final message
    synthese.setMessage(buildMessage(synthese));

    // Returning the initialized synthese
    return synthese;
  }

  /**
   * @param synthese
   * @param resource
   * @param notifications
   * @throws Exception
   */
  private void prepareSyntheseResourceAndNotifications(
      final DelayedNotificationSyntheseData synthese,
      final NotificationResourceData resource, final List<DelayedNotificationData> notifications)
      throws Exception {

    // Sorting delayed notifications
    Collections.sort(notifications, getDelayedNotificationComparator());

    // Initializing the synthese resource
    final SyntheseResource syntheseResource = new SyntheseResource();
    synthese.addResource(syntheseResource);
    synthese.addNbNotifications(notifications.size());

    // Filling the synthese resource data
    syntheseResource.setName(resource.getResourceName());
    syntheseResource.setDescription(resource.getResourceDescription());
    if (syntheseResource.getDescription() != null) {
      syntheseResource.setDescription(EncodeHelper.convertWhiteSpacesForHTMLDisplay(syntheseResource
          .getDescription()));
    }
    syntheseResource.setLocation(resource.getResourceLocation().replaceAll(NotificationResourceData.LOCATION_SEPARATOR,
        LOCATION_SEPARATOR));
    syntheseResource.setUrl(resource.getResourceUrl());
    if (syntheseResource.getUrl() != null) {
      syntheseResource.setUrl(computeURL(synthese.getUserId(), syntheseResource.getUrl()));
    }

    // Browsing notifications
    SyntheseResourceNotification syntheseNotification;
    boolean isPreviousHasMessage = false;
    for (final DelayedNotificationData delayedNotificationData : notifications) {
      syntheseNotification = new SyntheseResourceNotification();
      syntheseResource.addNotification(syntheseNotification);

      // Action
      syntheseNotification.setAction(getStringTranslation("resourceAction" +
          delayedNotificationData.getAction().name(), synthese.getLanguage()));

      // User
      syntheseNotification.setFromUserName(getUserDetail(delayedNotificationData.getFromUserId())
          .getDisplayedName());

      // Date
      syntheseNotification.setDate(DateUtil.getOutputDate(
          delayedNotificationData.getCreationDate(), synthese.getLanguage()));

      // Time
      syntheseNotification.setTime(DateUtil.getOutputHour(
          delayedNotificationData.getCreationDate(), synthese.getLanguage()));

      // Message
      syntheseNotification.setMessage(nullIfBlank(delayedNotificationData.getMessage()));
      syntheseNotification.setPreviousHasMessage(isPreviousHasMessage);
      if (syntheseNotification.getMessage() != null) {
        isPreviousHasMessage = true;
        syntheseNotification.setMessage(EncodeHelper.convertWhiteSpacesForHTMLDisplay(
            syntheseNotification.getMessage()));
      } else {
        isPreviousHasMessage = false;
      }

      // Indicates that the notification has been treated
      synthese.getDelayedNotificationIdProceeded().add(delayedNotificationData.getId());
    }
  }

  /**
   * Just a little tool
   * @return
   */
  private static String nullIfBlank(final String string) {
    return StringUtils.isBlank(string) ? null : string;
  }

  /**
   * Builds the message
   * @param synthese
   * @return
   * @throws Exception
   */
  private String buildMessage(final DelayedNotificationSyntheseData synthese) throws Exception {
    clearTemplate();
    getTemplate().setAttribute("delay",
        getStringTranslation("delay" + synthese.getFrequency().name(), synthese.getLanguage()));
    getTemplate().setAttribute("userName", getUserDetail(synthese.getUserId()).getDisplayedName());
    getTemplate().setAttribute("nbResources", synthese.getNbResources());
    getTemplate().setAttribute("severalResources", (synthese.getNbResources() > 1));
    getTemplate().setAttribute("nbNotifications", synthese.getNbNotifications());
    getTemplate().setAttribute("severalNotifications", (synthese.getNbNotifications() > 1));
    getTemplate().setAttribute("resources", synthese.getResources());
    return getTemplate().applyFileTemplate("messageLayout_" + synthese.getLanguage());
  }

  /**
   * Builds the subject
   * @param synthese
   * @return
   */
  private String buildSubject(final DelayedNotificationSyntheseData synthese) {
    clearTemplate();
    getTemplate().setAttribute("frequency",
        getStringTranslation("frequency" + synthese.getFrequency().name(), synthese.getLanguage()));
    return getTemplate().applyFileTemplate("subject_" + synthese.getLanguage());
  }

  /**
   * Clears silverpeas template
   * @return
   */
  private void clearTemplate() {
    getTemplate().getAttributes().clear();
  }

  /**
   * Gets a Silverpeas template
   * @return
   */
  private SilverpeasTemplate getTemplate() {
    if (template == null) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("notification/delayed");
    }
    return template;
  }

  /**
   * Initializing common data of the notification synthese from the first delayed notification
   * @param delayedNotifications
   * @return
   */
  private DelayedNotificationSyntheseData initializeSynthese(
      final List<DelayedNotificationData> delayedNotifications) {
    final DelayedNotificationSyntheseData synthese = new DelayedNotificationSyntheseData();
    final DelayedNotificationData first = delayedNotifications.get(0);
    synthese.setUserId(first.getUserId());
    synthese.setFrequency(DelayedNotificationFactory.getDelayedNotification().getUserFrequency(
        first.getUserId(), first.getChannel()));
    synthese.setLanguage(first.getLanguage());
    synthese.setSubject(buildSubject(synthese));
    return synthese;
  }

  /**
   * Creating the notification data from a given channel, a given delayed notification and with the final message.
   * Currently, only the SMTP channel is managed
   * @param channel
   * @param synthese
   * @return
   * @throws Exception
   */
  private NotificationData createNotificationData(final NotifChannel channel,
      final DelayedNotificationSyntheseData synthese) throws Exception {
    final NotificationData notificationData = new NotificationData();

    // Receiver user
    final UserDetail receiver = getUserDetail(synthese.getUserId());

    // Sender (administrator)
    final UserDetail sender = getUserDetail(-1);

    // Log
    SilverTrace.info("delayedNotificationDelegate",
        "DelayedNotificationDelegate.createNotificationData()",
        "root.MSG_GEN_PARAM_VALUE", "userId =" + synthese.getUserId());

    // Set the channel
    notificationData.setTargetChannel(channel.name());

    // Set the destination address
    notificationData.setTargetReceipt(NotifChannel.SMTP.equals(channel) ? receiver.geteMail()
        : Integer.toString(synthese.getUserId()));

    // Set the sender name
    notificationData.setSenderName(sender.getDisplayedName());

    // Set target parameters
    notificationData.setTargetParam(new HashMap<String, Object>());

    // Set sender in parameters
    notificationData.getTargetParam().put(
        NotificationParameterNames.FROM,
        NotifChannel.SMTP.equals(channel) ? sender.geteMail() : sender.getId());

    // Set subject parameter
    notificationData.getTargetParam()
        .put(NotificationParameterNames.SUBJECT, synthese.getSubject());

    // Set date parameter
    notificationData.getTargetParam().put(NotificationParameterNames.DATE, new Date());

    // Set the language
    notificationData.getTargetParam().put(NotificationParameterNames.LANGUAGE,
        synthese.getLanguage());

    // Set the message
    notificationData.setMessage(synthese.getMessage());

    // Set that the answer is not allowed
    notificationData.setAnswerAllowed(false);

    // Returns the notification data
    return notificationData;
  }

  /*
   * Commons
   */

  /**
   * Gets the translation of an element
   * @param key
   * @param language
   * @return
   */
  private String getStringTranslation(final String key, final String language) {
    ResourceLocator rl = multilang.get(language);
    if (rl == null) {
      rl =
          new ResourceLocator(
              "com.stratelia.silverpeas.notificationManager.multilang.notificationManagerBundle",
              language);
      multilang.put(language, rl);
    }
    return rl.getString(key, null);
  }

  /**
   * Gets the comprator of resource data
   * @return
   */
  private Comparator<NotificationResourceData> getResourceComparator() {
    if (resourceComparator == null) {
      resourceComparator = new AbstractComplexComparator<NotificationResourceData>() {
        @Override
        protected ValueBuffer getValuesToCompare(final NotificationResourceData object) {
          return new ValueBuffer().append(object.getResourceLocation())
              .append(object.getResourceType()).append(object.getResourceName())
              .append(object.getResourceDescription()).append(object.getComponentInstanceId());
        }
      };
    }
    return resourceComparator;
  }

  /**
   * Gets the comparator of delayed notification data
   * @return
   */
  private Comparator<DelayedNotificationData> getDelayedNotificationComparator() {
    if (delayedNotificationComparator == null) {
      delayedNotificationComparator = new AbstractComplexComparator<DelayedNotificationData>() {
        @Override
        protected ValueBuffer getValuesToCompare(final DelayedNotificationData object) {
          return new ValueBuffer().append(object.getCreationDate()).append(
              object.getAction().getPriority());
        }
      };
    }
    return delayedNotificationComparator;
  }

  /**
   * Centralizes the searches of user details with cache feature
   * @param userId
   * @return
   * @throws Exception
   */
  protected UserDetail getUserDetail(final Integer userId) throws Exception {
    UserDetail userDetail = userDetailCache.get(userId);
    if (userDetail == null) {
      if ((userId >= 0)) {
        userDetail = AdminReference.getAdminService().getUserDetail(Integer.toString(userId));
      } else {
        userDetail = new UserDetail();
        userDetail.setId(Integer.toString(userId));
        userDetail.seteMail(AdminReference.getAdminService().getAdministratorEmail());
      }
      if (userDetailCache.size() >= MAX_USER_DETAIL_ITEMS) {
        userDetailCache.remove(userDetailCache.keySet().iterator().next());
      }
      userDetailCache.put(userId, userDetail);
    }
    return userDetail;
  }

  /**
   * Centralizes notification sending
   * @param notificationData
   * @throws NotificationServerException
   */
  protected void sendNotification(final NotificationData notificationData)
      throws NotificationServerException {

    // Removing Java Strings of the computed message
    notificationData.setMessage(notificationData.getMessage().replaceAll("[\r\n\t]", ""));

    // Adding the notification
    notificationServer.addNotification(notificationData);
  }
}
