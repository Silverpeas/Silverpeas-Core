package org.silverpeas.core.contribution.template.publication;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

/**
 * Created by Nicolas on 14/09/2017.
 */
public class PublicationTemplateUserEventListener extends CDIResourceEventListener<UserEvent> {

  @Override
  public void onDeletion(final UserEvent event) throws Exception {
    UserDetail detail = event.getTransition().getBefore();
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    templateManager.deleteDirectoryData(detail.getId());
  }
}
