/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.notification.user.delayed.delegate;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.AttachmentLink;
import org.silverpeas.core.notification.user.client.AbstractNotification;
import org.silverpeas.core.notification.user.client.NotificationParameterNames;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.delayed.DelayedNotificationProvider;
import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationUserSetting;
import org.silverpeas.core.notification.user.delayed.synthese.DelayedNotificationSyntheseData;
import org.silverpeas.core.notification.user.delayed.synthese.SyntheseResource;
import org.silverpeas.core.notification.user.delayed.synthese.SyntheseResourceNotification;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServer;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;

import javax.annotation.Nonnull;
import java.util.*;

import static org.silverpeas.core.notification.user.client.NotificationTemplateKey.NOTIFICATION_SERVER_URL;
import static org.silverpeas.core.notification.user.delayed.DelayedNotificationProvider.getDelayedNotification;
import static org.silverpeas.core.util.MapUtil.putAddList;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationDelegate extends AbstractNotification {

  private static final String LOCATION_SEPARATOR = " &gt; ";

  /**
   * User details cache
   */
  private static final int MAX_USER_DETAIL_ITEMS = 100;
  private final Map<Integer, UserDetail> userDetailCache =
      new LinkedHashMap<>(MAX_USER_DETAIL_ITEMS);

  /**
   * String templates
   */
  private SilverpeasTemplate template = null;

  /**
   * Comparators
   */
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
  public static void executeUserDeleting(final int userId)
      throws NotificationServerException, AdminException {

    // Forcing sending of delayed notification on the aimed channel
    executeForceDelayedNotificationsSending(userId,
        getDelayedNotification().getWiredChannels());

    // Deleting settings
    for (final NotifChannel channel : getDelayedNotification()
        .getWiredChannels()) {
      final DelayedNotificationUserSetting settings =
          getDelayedNotification()
              .getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel);
      if (settings != null) {
        getDelayedNotification()
            .deleteDelayedNotificationUserSetting(settings);
      }
    }
  }

  /**
   * When user settings change, if the new frequency is NONE then the delayed notifications saved
   * have to be sent
   * @param userId
   * @param channel
   * @param frequency
   * @throws Exception
   */
  public static DelayedNotificationUserSetting executeUserSettingsUpdating(final int userId,
      final NotifChannel channel, final DelayedNotificationFrequency frequency)
      throws NotificationServerException, AdminException {
    DelayedNotificationUserSetting result = null;
    // Getting old settings
    final DelayedNotificationUserSetting oldSettings =
        getDelayedNotification()
            .getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel);

    // Updating settings
    if (frequency != null) {
      result = getDelayedNotification()
          .saveDelayedNotificationUserSetting(userId, channel, frequency);
    } else if (oldSettings != null) {
      // User settings are deleted from persistence system. Default Silverpeas's frequency will be
      // use for the given user.
      getDelayedNotification()
          .deleteDelayedNotificationUserSetting(oldSettings);
    }

    // Forcing sending of delayed notification on the aimed channel if settings have been changed
    if ((oldSettings == null && frequency != null) ||
        (oldSettings != null && !oldSettings.getFrequency().equals(frequency))) {
      executeForceDelayedNotificationsSending(userId, Collections.singleton(channel));
    }

    return result;
  }

  /**
   * Easy call of new notification process
   * @param delayedNotificationData
   * @throws NotificationServerException
   */
  public static void executeNewNotification(final DelayedNotificationData delayedNotificationData)
      throws NotificationServerException {
    new DelayedNotificationDelegate().performNewNotificationSending(delayedNotificationData);
  }

  /**
   * Handling a new notification
   * @param delayedNotificationData
   * @throws Exception
   */
  protected void performNewNotificationSending(
      final DelayedNotificationData delayedNotificationData) throws NotificationServerException {
    if (!isThatToBeDelayed(delayedNotificationData)) {
      sendNotification(delayedNotificationData.getNotificationData());
    } else {
      DelayedNotificationProvider.getDelayedNotification()
          .saveDelayedNotification(delayedNotificationData);
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
    // be set
    if (NotificationParameters.PRIORITY_NORMAL !=
        delayedNotificationData.getNotificationParameters().getMessagePriority()) {
      return false;
    }

    // The user frequency has to be different from NONE
    if (DelayedNotificationFrequency.NONE.equals(
        getDelayedNotification()
            .getUserFrequency(delayedNotificationData.getUserId(),
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
  public static void executeDelayedNotificationsSending(final Date date)
      throws NotificationServerException, AdminException {
    new DelayedNotificationDelegate().performDelayedNotificationsSending(date,
        getDelayedNotification().getWiredChannels());
  }

  /**
   * Easy call of delayed notifications process. Forces the sending of all the delayed notifications
   * saved for all users
   * @throws Exception
   */
  public static void executeForceDelayedNotificationsSending()
      throws NotificationServerException, AdminException {
    new DelayedNotificationDelegate().forceDelayedNotificationsSending();
  }

  /**
   * Easy call of delayed notifications process. Forces the sending of all the delayed notifications
   * saved for a given
   * user
   * @param userId
   * @param channels
   * @throws Exception
   */
  public static void executeForceDelayedNotificationsSending(final int userId,
      final Set<NotifChannel> channels) throws NotificationServerException, AdminException {
    executeForceDelayedNotificationsSending(Collections.singletonList(userId), channels);
  }

  /**
   * Easy call of delayed notifications process. Forces the sending of all the delayed notifications
   * saved for given
   * users and channels
   * @param userIds
   * @param channels
   * @throws Exception
   */
  public static void executeForceDelayedNotificationsSending(final List<Integer> userIds,
      final Set<NotifChannel> channels) throws NotificationServerException, AdminException {
    new DelayedNotificationDelegate().forceDelayedNotificationsSending(userIds, channels);
  }

  /**
   * Forces the sending of all the delayed notifications saved for all users
   * @throws Exception
   */
  protected void forceDelayedNotificationsSending()
      throws NotificationServerException, AdminException {

    // Searching all the users from delayed notifications
    final List<Integer> usersToBeNotified = getDelayedNotification()
        .findAllUsersToBeNotified(
            getDelayedNotification().getWiredChannels());

    // Performing all users to notify
    forceDelayedNotificationsSending(usersToBeNotified,
        getDelayedNotification().getWiredChannels());
  }

  /**
   * Forces the sending of all the delayed notifications saved for given users and channels
   * @param userIds
   * @param channels
   * @throws Exception
   */
  void forceDelayedNotificationsSending(final List<Integer> userIds,
      final Set<NotifChannel> channels) throws NotificationServerException, AdminException {
    performUsersDelayedNotifications(userIds, channels);
  }

  void performDelayedNotificationsSending(final Date date,
      final Set<NotifChannel> channels) throws NotificationServerException, AdminException {

    // Searching all the users that have to be notify from a given date and given channels
    final List<Integer> usersToBeNotified = getDelayedNotification()
        .findUsersToBeNotified(date, channels, getDelayedNotification()
                .getDefaultDelayedNotificationFrequency());

    // Performing all users to notify
    performUsersDelayedNotifications(usersToBeNotified, channels);
  }

  private void performUsersDelayedNotifications(final List<Integer> usersToBeNotified,
      final Set<NotifChannel> channels) throws NotificationServerException, AdminException {

    // Stopping if no users to notify
    if (CollectionUtil.isEmpty(usersToBeNotified)) {
      return;
    }

    // Performing all users to notify
    final Collection<Long> delayedNotificationIdsToDelete = new ArrayList<>();
    try {
      Map<NotifChannel, List<DelayedNotificationData>> delayedNotifications;
      for (final Integer userIdToNotify : usersToBeNotified) {

        // Searching current user notifications, group by channels
        delayedNotifications = getDelayedNotification()
            .findDelayedNotificationByUserIdGroupByChannel(userIdToNotify, channels);

        // Browse channel notifications
        for (final Map.Entry<NotifChannel, List<DelayedNotificationData>> mapEntry :
            delayedNotifications
            .entrySet()) {
          delayedNotificationIdsToDelete.addAll(
              performUserDelayedNotificationsOnChannel(mapEntry.getKey(), mapEntry.getValue()));
        }
      }
    } finally {

      // Deleting massively the delayed notifications treated and the associated notification
      // resource data
      getDelayedNotification()
          .deleteDelayedNotifications(delayedNotificationIdsToDelete);
    }
  }

  private Collection<Long> performUserDelayedNotificationsOnChannel(final NotifChannel channel,
      final List<DelayedNotificationData> delayedNotifications)
      throws NotificationServerException, AdminException {
    final DelayedNotificationSyntheseData synthese = buildSynthese(delayedNotifications);
    sendNotification(createNotificationData(channel, synthese));
    return synthese.getDelayedNotificationIdProceeded();
  }

  private DelayedNotificationSyntheseData buildSynthese(
      final List<DelayedNotificationData> delayedNotifications) throws AdminException {

    // Result
    final DelayedNotificationSyntheseData synthese = initializeSynthese(delayedNotifications);

    // Indexing
    final Map<NotificationResourceData, List<DelayedNotificationData>> resourcesAndNotifications =
        new HashMap<>();
    for (final DelayedNotificationData delayedNotificationData : delayedNotifications) {
      putAddList(resourcesAndNotifications, delayedNotificationData.getResource(),
          delayedNotificationData);
    }

    // Sorting indexes
    final List<NotificationResourceData> orderedResources =
        new ArrayList<>(resourcesAndNotifications.keySet());
    orderedResources.sort(getResourceComparator());

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

  private void prepareSyntheseResourceAndNotifications(
      final DelayedNotificationSyntheseData synthese, final NotificationResourceData resource,
      final List<DelayedNotificationData> notifications) throws AdminException {
    final String language = synthese.getLanguage();
    resource.setCurrentLanguage(language);

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
      syntheseResource.setDescription(
          WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(syntheseResource.getDescription()));
    }
    syntheseResource.setLocation(resource.getResourceLocation()
        .replaceAll(NotificationResourceData.LOCATION_SEPARATOR, LOCATION_SEPARATOR));
    syntheseResource.setUrl(resource.getResourceUrl());
    if (syntheseResource.getUrl() != null) {
      syntheseResource.setUrl(computeURL(synthese.getUserId(), syntheseResource.getUrl()));
    }
    syntheseResource.setLinkLabel(getResourceLinkLabel(resource));

    if (isDefined(resource.getAttachmentTargetId()) &&
        isDefined(resource.getComponentInstanceId())) {
      ResourceReference attachmentTarget = new ResourceReference(resource.getAttachmentTargetId(),
          resource.getComponentInstanceId());
      final List<AttachmentLink> attachmentLinks = AttachmentLink
          .getForContribution(attachmentTarget, language);
      syntheseResource.setAttachmentLinks(attachmentLinks);
    }

    // Browsing notifications
    SyntheseResourceNotification syntheseNotification;
    boolean isPreviousHasMessage = false;
    for (final DelayedNotificationData delayedNotificationData : notifications) {
      syntheseNotification = new SyntheseResourceNotification();
      syntheseResource.addNotification(syntheseNotification);

      // Action
      syntheseNotification.setAction(getResourceActionLabel(delayedNotificationData));

      // User
      syntheseNotification.setFromUserName(
          getUserDetail(delayedNotificationData.getFromUserId()).getDisplayedName());

      // Date
      syntheseNotification.setDate(DateUtil
          .getOutputDate(delayedNotificationData.getCreationDate(), language));

      // Time
      syntheseNotification.setTime(DateUtil
          .getOutputHour(delayedNotificationData.getCreationDate(), language));

      // Message
      syntheseNotification.setMessage(nullIfBlank(delayedNotificationData.getMessage()));
      syntheseNotification.setPreviousHasMessage(isPreviousHasMessage);
      if (syntheseNotification.getMessage() != null) {
        isPreviousHasMessage = true;
        syntheseNotification.setMessage(
            WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(syntheseNotification.getMessage()));
      } else {
        isPreviousHasMessage = false;
      }

      // Indicates that the notification has been treated
      synthese.getDelayedNotificationIdProceeded()
          .add(Long.parseLong(delayedNotificationData.getId()));
    }
  }

  @Nonnull
  private String getResourceActionLabel(final DelayedNotificationData delayedNotificationData) {
    String label = getStringTranslation(
        "resourceAction" + delayedNotificationData.getAction().name(),
        delayedNotificationData.getResource().getCurrentLanguage());
    if (!delayedNotificationData.getResource().isFeminineGender()) {
      label = label.replace("ée", "é");
    }
    return label;
  }

  @Nonnull
  private String getResourceLinkLabel(final NotificationResourceData resource) {
    final String defaultLinkLabel = getStringTranslation("contribution.notifEventLinkLabel",
        resource.getCurrentLanguage());
    return defaultStringIfNotDefined(resource.getLinkLabel(), defaultLinkLabel);
  }

  /**
   * Just a little tool
   * @return
   */
  private static String nullIfBlank(final String string) {
    return StringUtils.isBlank(string) ? null : string;
  }

  private String buildMessage(final DelayedNotificationSyntheseData synthese)
      throws AdminException {
    clearTemplate();
    final User user = getUserDetail(synthese.getUserId());
    getTemplate().setAttribute(NOTIFICATION_SERVER_URL.toString(),
        getUserAutoRedirectSilverpeasServerURL(user.getId()));
    getTemplate().setAttribute("delay",
        getStringTranslation("delay" + synthese.getFrequency().name(), synthese.getLanguage()));
    getTemplate().setAttribute("userName", user.getDisplayedName());
    getTemplate().setAttribute("nbResources", synthese.getNbResources());
    getTemplate().setAttribute("severalResources", (synthese.getNbResources() > 1));
    getTemplate().setAttribute("nbNotifications", synthese.getNbNotifications());
    getTemplate().setAttribute("severalNotifications", (synthese.getNbNotifications() > 1));
    getTemplate().setAttribute("resources", synthese.getResources());
    return getTemplate().applyFileTemplate("messageLayout_" + synthese.getLanguage());
  }

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
      template = SilverpeasTemplateFactory
          .createSilverpeasTemplateOnCore("notification/delayed")
          .mergeRootWithCustom();
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
    synthese.setFrequency(getDelayedNotification()
        .getUserFrequency(first.getUserId(), first.getChannel()));
    synthese.setLanguage(first.getLanguage());
    synthese.setSubject(buildSubject(synthese));
    return synthese;
  }

  /**
   * Creating the notification data from a given channel, a given delayed notification and with the
   * final message.
   * Currently, only the SMTP channel is managed
   * @param channel
   * @param synthese
   * @return
   * @throws Exception
   */
  private NotificationData createNotificationData(final NotifChannel channel,
      final DelayedNotificationSyntheseData synthese) throws AdminException {
    final NotificationData notificationData = new NotificationData();

    // Receiver user
    final UserDetail receiver = getUserDetail(synthese.getUserId());

    // Sender (administrator)
    final UserDetail sender = getUserDetail(-1);

    // Set the channel
    notificationData.setTargetChannel(channel.name());

    // Set the destination address
    notificationData.setTargetReceipt(NotifChannel.SMTP.equals(channel) ? receiver.geteMail() :
        Integer.toString(synthese.getUserId()));

    // Set the sender name
    notificationData.setSenderName(sender.getDisplayedName());

    // Set target parameters
    notificationData.setTargetParam(new HashMap<>());

    // Set sender in parameters
    notificationData.getTargetParam().put(NotificationParameterNames.FROM,
        NotifChannel.SMTP.equals(channel) ? sender.geteMail() : sender.getId());

    // Set subject parameter
    notificationData.getTargetParam()
        .put(NotificationParameterNames.SUBJECT, synthese.getSubject());

    // Set date parameter
    notificationData.getTargetParam().put(NotificationParameterNames.DATE, new Date());

    // Set the language
    notificationData.getTargetParam()
        .put(NotificationParameterNames.LANGUAGE, synthese.getLanguage());

    // Hide Header and Footer in SMTP message
    notificationData.getTargetParam().put(NotificationParameterNames.HIDESMTPHEADERFOOTER,
        true);


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
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationManager.multilang.notificationManagerBundle",
          language);
    return messages.getString(key);
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
          return new ValueBuffer().append(object.getCreationDate())
              .append(object.getAction().getPriority());
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
  protected UserDetail getUserDetail(final Integer userId) throws AdminException {
    UserDetail userDetail = userDetailCache.get(userId);
    if (userDetail == null) {
      if ((userId >= 0)) {
        userDetail = AdministrationServiceProvider.getAdminService().getUserDetail(Integer.toString(userId));
      } else {
        userDetail = new UserDetail();
        userDetail.setId(Integer.toString(userId));
        userDetail.setLastName(AdministrationServiceProvider.getAdminService().getSilverpeasName());
        userDetail.seteMail(AdministrationServiceProvider.getAdminService().getSilverpeasEmail());
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
    NotificationServer notificationServer = NotificationServer.get();
    notificationServer.addNotification(notificationData);
  }
}
