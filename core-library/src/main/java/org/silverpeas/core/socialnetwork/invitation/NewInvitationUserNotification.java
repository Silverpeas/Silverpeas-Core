package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

/**
 * Created by Nicolas on 03/02/2017.
 */
public class NewInvitationUserNotification extends AbstractInvitationUserNotification {

  public NewInvitationUserNotification(Invitation invitation) {
    super(invitation);
  }

  @Override
  protected void performTemplateData(final String language, final Invitation resource,
      final SilverpeasTemplate template) {

    UserFull sender = UserFull.getById(String.valueOf(resource.getSenderId()));
    template.setAttribute("senderUser", sender);
    template.setAttribute("userName", sender.getDisplayedName());
    template.setAttribute("senderMessage", resource.getMessage());
  }

  @Override
  protected void performNotificationResource(final String language, final Invitation resource,
      final NotificationResourceData notificationResourceData) {
    String url = URLUtil.getURL(URLUtil.CMP_MYPROFILE, null, null)
        + "MyInvitations";
    getNotificationMetaData().setLink(url);
    getNotificationMetaData().setOriginalExtraMessage(resource.getMessage());
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "myProfile.invitations.notification.notifLinkLabel";
  }

}