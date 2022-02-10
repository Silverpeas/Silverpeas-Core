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

package org.silverpeas.core.notification.user;

import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;

import java.util.Map;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
public abstract class AbstractComponentInstanceManualUserNotification
    implements ComponentInstanceManualUserNotification {

  protected boolean check(final NotificationContext context) {
    final String componentId = context.getComponentId();
    if (isDefined(componentId)) {
      final String publicationId = context.getPublicationId();
      if (isDefined(publicationId)) {
        return PublicationAccessControl.get().isUserAuthorized(context.getSender().getId(),
            new PublicationPK(publicationId, componentId));
      } else {
        final String nodeId = context.getNodeId();
        if (isDefined(nodeId)) {
          return NodeAccessControl.get()
              .isUserAuthorized(context.getSender().getId(), new NodePK(nodeId, componentId));
        }
      }
    }
    return true;
  }

  @Override
  public final UserNotification initializesWith(final NotificationContext context) {
    if (!check(context)) {
      throw new ForbiddenRuntimeException("Cannot access resource with " + context);
    }
    return createUserNotification(context);
  }

  /**
   * Creates the user notification after some verifications have been performed.
   * @param context a {@link Map} of key-values describing the context within which the user
   * notification has to be built.
   * @return a {@link UserNotification} object.
   */
  protected abstract UserNotification createUserNotification(final NotificationContext context);
}
