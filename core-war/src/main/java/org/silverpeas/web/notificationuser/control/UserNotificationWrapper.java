package org.silverpeas.web.notificationuser.control;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.ResourceLocator.getLocalizationBundle;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * A wrapper of a {@link org.silverpeas.core.notification.user.UserNotification} object with
 * additional methods to customize the user notification that was built and returned by a given
 * Silverpeas component instance. The customization is provided by the sender of such a
 * notification and they consist mainly by setting a title other than the provided one or by
 * setting an additional message.
 * @author mmoquillon
 */
public class UserNotificationWrapper implements UserNotification {

  private final UserNotification notification;
  private final String language;

  /**
   * Constructs a new wrapper of the specified user notification and with the given language.
   * @param notification the {@link UserNotification} object to wrap
   * @param senderLanguage an ISO-631-1 code of the language of the notification sender.
   */
  UserNotificationWrapper(final UserNotification notification, final String senderLanguage) {
    Objects.requireNonNull(notification);
    StringUtil.requireDefined(senderLanguage);
    this.notification = notification;
    this.language = senderLanguage;
  }

  /**
   * Sets a title to this notification.
   * @param title the title to set.
   * @return itself.
   */
  public UserNotificationWrapper setTitle(final String title) {
    if (StringUtil.isDefined(title) &&
        !title.equals(getNotificationMetaData().getTitle(this.language))) {
      final NotificationMetaData metaData = notification.getNotificationMetaData();
      final boolean isSimpleContents = metaData.getTemplateContents().isEmpty();
      final Map<String, String> simpleContents = metaData.getSimpleContents();
      for (String lang : DisplayI18NHelper.getLanguages()) {
        metaData.setTitle(Encode.forHtml(title), lang);
        if (isSimpleContents && isNotDefined(simpleContents.get(lang))) {
          final LocalizationBundle bundle = getLocalizationBundle(
              "org.silverpeas.alertUserPeas.multilang.alertUserPeasBundle", lang);
          metaData.addLanguage(lang, title, bundle.getString("AuthorMessage"));
        }
      }
    }
    return this;
  }

  /**
   * Sets a content to this notification.
   * @param content the content to set.
   * @return itself.
   */
  public UserNotificationWrapper setContent(final String content) {
    if (StringUtil.isDefined(content)) {
      final NotificationMetaData metaData = notification.getNotificationMetaData();
      final boolean isSimpleContents = metaData.getTemplateContents().isEmpty();
      final Map<String, String> simpleContents = metaData.getSimpleContents();
      for (String lang : DisplayI18NHelper.getLanguages()) {
        if (!isSimpleContents || isDefined(simpleContents.get(lang))) {
          metaData.addExtraMessage(content, lang);
        } else {
          metaData.setContent(content, lang);
        }
      }
    }
    return this;
  }

  /**
   * Sets a link for all the attachments of the specified contribution.
   * @param contributionId the unique identifier of the contribution in the component instance
   * from which this notification was built.
   * @return itself.
   */
  public UserNotificationWrapper setAttachmentLinksFor(final String contributionId) {
    if (StringUtil.isDefined(contributionId)) {
      notification.getNotificationMetaData().setAttachmentTargetId(contributionId);
    }
    return this;
  }

  /**
   * Sets the sender of this notification.
   * @param sender the sender to set.
   * @return itself.
   */
  public UserNotificationWrapper setSender(final User sender) {
    final NotificationMetaData metaData = notification.getNotificationMetaData();
    metaData.setSender(sender.getId());
    for (SilverpeasTemplate template : metaData.getTemplateContents().values()) {
      template.setAttribute("sender", sender);
      template.setAttribute("senderName", sender.getDisplayedName());
    }
    return this;
  }

  /**
   * Sets one or more recipients to this notification.
   * @param userIds a collection of user's unique identifiers.
   * @return itself.
   */
  public UserNotificationWrapper setRecipientUsers(final Collection<String> userIds) {
    final Collection<UserRecipient> userRecipients =
        userIds.stream().map(UserRecipient::new).collect(Collectors.toList());
    notification.getNotificationMetaData().setUserRecipients(userRecipients);
    return this;
  }

  /**
   * Sets one or more recipients to this notification.
   * @param groupIds a collection of group's unique identifiers.
   * @return itself.
   */
  public UserNotificationWrapper setRecipientGroups(final Collection<String> groupIds) {
    final Collection<GroupRecipient> groupRecipients =
        groupIds.stream().map(GroupRecipient::new).collect(Collectors.toList());
    notification.getNotificationMetaData().setGroupRecipients(groupRecipients);
    return this;
  }

  /**
   * Sets this notification as manual or not.
   * @param yesOrFalse either true for a manual notification or false otherwise.
   * @return itself.
   */
  public UserNotificationWrapper setAsManual(final boolean yesOrFalse) {
    if (yesOrFalse) {
      notification.getNotificationMetaData().manualUserNotification();
    }
    return this;
  }

  @Override
  public NotificationMetaData getNotificationMetaData() {
    return notification.getNotificationMetaData();
  }

  @Override
  public void send() {
    notification.send();
  }

  @Override
  public void send(final BuiltInNotifAddress notificationAddress) {
    notification.send(notificationAddress);
  }
}
  