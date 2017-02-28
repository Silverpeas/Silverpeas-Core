package org.silverpeas.core.socialnetwork.invitation;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Collection;

/**
 * Created by Nicolas on 03/02/2017.
 */
public abstract class AbstractInvitationUserNotification
    extends AbstractTemplateUserNotificationBuilder<Invitation> {

  public AbstractInvitationUserNotification(Invitation invitation) {
    super(invitation);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.social.multilang.socialNetworkBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "socialNetwork";
  }

  @Override
  protected String getFileName() {
    if (isInvitation()) {
      return "sendInvitation";
    }
    return "acceptInvitation";
  }

  @Override
  protected String getSender() {
    if (isInvitation()) {
      return String.valueOf(getResource().getSenderId());
    }
    return String.valueOf(getResource().getReceiverId());
  }

  @Override
  protected String getBundleSubjectKey() {
    if (isInvitation()) {
      return "myProfile.invitations.notification.send.subject";
    }
    return "myProfile.invitations.notification.accept.subject";
  }

  @Override
  protected String getTitle() {
    return getBundle()
        .getStringWithParams(getBundleSubjectKey(), User.getById(getSender()).getFirstName());
  }

  private boolean isInvitation() {
    return NotifAction.PENDING_VALIDATION.equals(getAction());
  }

  @Override
  protected String getComponentInstanceId() {
    return null;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    if (isInvitation()) {
      return CollectionUtil.asList(String.valueOf(getResource().getReceiverId()));
    }
    return CollectionUtil.asList(String.valueOf(getResource().getSenderId()));
  }

}