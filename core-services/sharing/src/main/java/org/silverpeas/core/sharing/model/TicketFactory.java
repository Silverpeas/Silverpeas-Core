/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;


/**
 *
 * @author ehugonnet
 */
public class TicketFactory {

  public static Ticket aTicket(TicketDetail detail) {
    if (!isUserAllowed((int) detail.getSharedObjectId(), detail.getComponentId(),
        detail.getCreatorId(), detail.getSharedObjectType())) {
      return null;
    }
    if (Ticket.FILE_TYPE.equalsIgnoreCase(detail.getSharedObjectType())) {
      return SimpleFileTicket.builder(detail).build();
    }
    if (Ticket.VERSION_TYPE.equalsIgnoreCase(detail.getSharedObjectType())) {
      return VersionFileTicket.builder(detail).build();
    }
    if (Ticket.NODE_TYPE.equalsIgnoreCase(detail.getSharedObjectType())) {
      return NodeTicket.builder(detail).build();
    }
    if (Ticket.PUBLICATION_TYPE.equalsIgnoreCase(detail.getSharedObjectType())) {
      return PublicationTicket.builder(detail).build();
    }
    return null;
  }

  private static boolean isUserAllowed(int sharedObjectId, String componentId, String creatorId,
      String type) {
    if (Ticket.FILE_TYPE.equalsIgnoreCase(type) || Ticket.VERSION_TYPE.equalsIgnoreCase(type)) {
      SimpleDocumentPK pk = new SimpleDocumentPK(null, componentId);
      pk.setOldSilverpeasId(sharedObjectId);
      SimpleDocument doc =
          AttachmentServiceProvider.getAttachmentService().searchDocumentById(pk, null);
      return doc.isSharingAllowedForRolesFrom(UserDetail.getById(creatorId));
    } else if (Ticket.NODE_TYPE.equalsIgnoreCase(type)) {
      return NodeAccessControl.get().isUserAuthorized(creatorId,
          new NodePK(String.valueOf(sharedObjectId), componentId),
              AccessControlContext.init().onOperationsOf(AccessControlOperation.SHARING));
    } else if (Ticket.PUBLICATION_TYPE.equalsIgnoreCase(type)) {
      return PublicationAccessControl.get().isUserAuthorized(creatorId,
          new PublicationPK(String.valueOf(sharedObjectId), componentId),
            AccessControlContext.init().onOperationsOf(AccessControlOperation.SHARING));
    }
    return false;
  }
}
