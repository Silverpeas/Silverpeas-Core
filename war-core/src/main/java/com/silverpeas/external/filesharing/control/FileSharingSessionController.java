/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.external.filesharing.control;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import com.silverpeas.external.filesharing.model.FileSharingService;
import com.silverpeas.external.filesharing.model.FileSharingServiceFactory;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class FileSharingSessionController extends AbstractComponentSessionController {

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public FileSharingSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.external.filesharing.multilang.fileSharingBundle",
        "com.silverpeas.external.filesharing.settings.fileSharingIcons");
  }

  public List<TicketDetail> getTicketsByUser() throws RemoteException {
    return getFileSharingService().getTicketsByUser(getUserId());
  }

  public String createTicket(TicketDetail ticket) {
    TicketDetail newTicket = ticket;
    if (newTicket.getCreator() == null) {
      newTicket.setCreator(getUserDetail());
    }
    return getFileSharingService().createTicket(newTicket);
  }

  public void updateTicket(TicketDetail ticket) {
    TicketDetail newTicket = ticket;
    UserDetail user = getUserDetail();
    newTicket.setLastModifier(user);
    newTicket.setUpdateDate(new Date());
    getFileSharingService().updateTicket(newTicket);
  }

  public void deleteTicket(String key) {
    getFileSharingService().deleteTicket(key);
  }

  public TicketDetail getTicket(String key) throws RemoteException {
    TicketDetail ticket = getFileSharingService().getTicket(key);
    return ticket;
  }

  private FileSharingService getFileSharingService() {
    return FileSharingServiceFactory.getFactory().getFileSharingService();
  }
}