/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.Base;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import javax.inject.Singleton;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.WRITER;

/**
 * @author silveryocha
 */
@Base
@Singleton
public class DefaultInstancePublicationAccessControlExtension
    implements ComponentInstancePublicationAccessControlExtension {

  @Override
  public boolean canPublicationBePersistedOrDeletedBy(final PublicationDetail publication,
      final String instanceId, final String userId, final SilverpeasRole userRole,
      final AccessControlContext context) {
    final boolean authorized;
    if (userRole.isGreaterThan(SilverpeasRole.WRITER)) {
      authorized = publication == null
                   || !publication.isDraft()
                   || publication.isPublicationEditor(userId)
                   || isCoWritingEnabled(instanceId, context) && isDraftVisibleWithCoWriting();
    } else if (WRITER == userRole) {
      if (publication != null) {
        if (publication.isPublicationEditor(userId)) {
          authorized = true;
        } else if (publication.isDraft()) {
          authorized = isCoWritingEnabled(instanceId, context) && isDraftVisibleWithCoWriting();
        } else {
          authorized = isCoWritingEnabled(instanceId, context);
        }
      } else {
        authorized = isCoWritingEnabled(instanceId, context);
      }
    } else {
      authorized = false;
    }
    return authorized;
  }

  private boolean isCoWritingEnabled(final String instanceId, final AccessControlContext context) {
    final ComponentAccessController.DataManager componentDataManager =
        ComponentAccessController.getDataManager(context);
    return componentDataManager.isCoWritingEnabled(instanceId);
  }

  protected boolean isDraftVisibleWithCoWriting() {
    return false;
  }
}
