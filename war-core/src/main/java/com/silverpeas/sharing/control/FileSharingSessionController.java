/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.control;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.silverpeas.sharing.bean.SharingNotificationVO;
import org.silverpeas.sharing.notification.FileSharingUserNotification;

import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.silverpeas.sharing.services.SharingTicketService;
import com.silverpeas.util.StringUtil;
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
        "org.silverpeas.sharing.multilang.fileSharingBundle",
        "org.silverpeas.sharing.settings.fileSharingIcons");
  }

  public List<Ticket> getTicketsByUser() throws RemoteException {
    return getFileSharingService().getTicketsByUser(getUserId());
  }

  public void updateTicket(Ticket ticket) {
    Ticket newTicket = ticket;
    UserDetail user = getUserDetail();
    newTicket.setLastModifier(user);
    newTicket.setUpdateDate(new Date());
    getFileSharingService().updateTicket(newTicket);
  }

  public void deleteTicket(String key) {
    getFileSharingService().deleteTicket(key);
  }

  public Ticket getTicket(String key) throws RemoteException {
    return getFileSharingService().getTicket(key);
  }

  private SharingTicketService getFileSharingService() {
    return SharingServiceFactory.getSharingTicketService();
  }

  public void notifyUsers(Ticket ticket, SharingNotificationVO sharingParam) {
    String selectedUsersStr = sharingParam.getSelectedUsers();
    String externalEmails = sharingParam.getExternalEmails();

    if (StringUtil.isDefined(selectedUsersStr) || StringUtil.isDefined(externalEmails)) {
      // Notify users
      FileSharingUserNotification.notify(ticket, sharingParam);
    }
  }

}