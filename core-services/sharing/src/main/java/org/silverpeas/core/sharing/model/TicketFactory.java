/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import java.util.Date;

/**
 *
 * @author ehugonnet
 */
public class TicketFactory {

  public static Ticket aTicket(int sharedObjectId, String componentId, String creatorId,
      Date creationDate, Date endDate, int nbAccessMax, String type) {
    if (isUserAllowed(sharedObjectId, componentId, creatorId, type)) {
      if(Ticket.FILE_TYPE.equalsIgnoreCase(type)) {
        return new SimpleFileTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
                nbAccessMax);
      }
      if(Ticket.VERSION_TYPE.equalsIgnoreCase(type)) {
        return new VersionFileTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
                nbAccessMax);
      }
      if(Ticket.NODE_TYPE.equalsIgnoreCase(type)) {
        return new NodeTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
                nbAccessMax);
      }
      if(Ticket.PUBLICATION_TYPE.equalsIgnoreCase(type)) {
        return new PublicationTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
            nbAccessMax);
      }
    }
    return null;
  }

  public static Ticket continuousTicket(int sharedObjectId, String componentId, String creatorId,
      Date creationDate, String type) {
    return aTicket(sharedObjectId, componentId, creatorId, creationDate, null, -1, type);
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
      AccessController<NodePK> nodeAccessController = AccessControllerProvider
          .getAccessController(NodeAccessControl.class);
      return nodeAccessController.isUserAuthorized(creatorId,
          new NodePK(String.valueOf(sharedObjectId), componentId),
              AccessControlContext.init().onOperationsOf(AccessControlOperation.sharing));
    } else if (Ticket.PUBLICATION_TYPE.equalsIgnoreCase(type)) {
      AccessController<PublicationPK> publicationAccessController = AccessControllerProvider
          .getAccessController(PublicationAccessControl.class);
      return publicationAccessController.isUserAuthorized(creatorId,
          new PublicationPK(String.valueOf(sharedObjectId), componentId),
            AccessControlContext.init().onOperationsOf(AccessControlOperation.sharing));
    }
    return false;
  }
}
