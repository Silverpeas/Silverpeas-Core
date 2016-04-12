/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import org.silverpeas.core.sharing.security.AbstractShareableAccessControl;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

/**
 * Access control to shared publications and their content
 */
public class PublicationAccessControl<R>
    extends AbstractShareableAccessControl<PublicationTicket, R> {

  PublicationAccessControl() {
    super();
  }

  @Override
  protected boolean isReadable(PublicationTicket ticket, R accessedObject) throws Exception {
    if (accessedObject instanceof SimpleDocument) {
      SimpleDocument attachment = (SimpleDocument) accessedObject;
      return attachment.getForeignId().equals(String.valueOf(ticket.getSharedObjectId()));
    }
    if (accessedObject instanceof PublicationDetail) {
      PublicationDetail publication = (PublicationDetail) accessedObject;
      return ticket.getResource().getAccessedObject().equals(publication);
    }
    return false;
  }
}
