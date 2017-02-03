package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

/**
 * Created by Nicolas on 03/02/2017.
 */
public class AcceptationUserNotification extends AbstractInvitationUserNotification {

  public AcceptationUserNotification(Invitation invitation) {
    super(invitation);
  }

  @Override
  protected void performTemplateData(final String language, final Invitation resource,
      final SilverpeasTemplate template) {
    UserFull user = UserFull.getById(getSender());

    template.setAttribute("senderUser", user);
    template.setAttribute("userName", user.getDisplayedName());
  }

  @Override
  protected void performNotificationResource(final String language, final Invitation resource,
      final NotificationResourceData notificationResourceData) {
    // this method is useless in case of this notification
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.RESPONSE;
  }
}