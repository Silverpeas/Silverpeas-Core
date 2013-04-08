/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.sharing.services;

import com.silverpeas.sharing.model.SimpleFileTicket;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.security.ShareableAccessControl;
import com.silverpeas.sharing.security.ShareableResource;
import org.silverpeas.attachment.model.SimpleDocument;

/**
 * Access control to shared attachments
 */
public class SimpleFileAccessControl implements ShareableAccessControl<SimpleDocument> {

  @Override
  public boolean isReadable(ShareableResource<SimpleDocument> resource) {
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(resource.getToken());
    return (ticket != null && ticket instanceof SimpleFileTicket && ((SimpleFileTicket) ticket).
            getResource().getAccessedObject().equals(resource.getAccessedObject()));
  }
}
