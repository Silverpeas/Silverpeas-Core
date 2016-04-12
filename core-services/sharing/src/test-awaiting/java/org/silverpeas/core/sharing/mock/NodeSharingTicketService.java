/*
 *  Copyright (C) 2000 - 2013 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.silverpeas.sharing.mock;

import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.NodeTicket;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.services.SharingTicketService;
import org.silverpeas.util.WAPrimaryKey;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
public class NodeSharingTicketService implements SharingTicketService {

  private String token;
  private WAPrimaryKey pk;

  public NodeSharingTicketService(String token, WAPrimaryKey pk) {
    this.token = token;
    this.pk = pk;
  }

  @Override
  public List<Ticket> getTicketsByUser(String userId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteTicketsForSharedObject(Long sharedObjectId, String type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Ticket getTicket(String key) {
    if (token.equals(key)) {
      return new NodeTicket(Integer.parseInt(pk.getId()), pk.getInstanceId(), "0", new Date(),
              null, -1);
    }
    return null;
  }

  @Override
  public String createTicket(Ticket ticket) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addDownload(DownloadDetail download) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateTicket(Ticket ticket) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteTicket(String key) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
