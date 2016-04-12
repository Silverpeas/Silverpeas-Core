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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/
package org.silverpeas.web.notificationserver.channel.silvermail.requesthandlers;

import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.model.SentNotificationDetail;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILException;
import org.silverpeas.web.notificationserver.channel.silvermail.SILVERMAILRequestHandler;
import org.silverpeas.web.notificationserver.channel.silvermail.SILVERMAILSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Class declaration
 *
 * @author
 * @version %I%, %G%
 */
public class SentUserNotifications implements SILVERMAILRequestHandler {

  /**
   * Method declaration
   *
   * @param componentSC
   * @param request
   * @return
   * @throws SILVERMAILException
   * @see
   */
  @Override
  public String handleRequest(ComponentSessionController componentSC,
      HttpServletRequest request) throws SILVERMAILException {

    // passer en paramètre la liste dans la request
    SILVERMAILSessionController silvermailScc = (SILVERMAILSessionController) componentSC;
    List<SentNotificationDetail> sentNotifs = new ArrayList<SentNotificationDetail>();
    try {
      sentNotifs = silvermailScc.getUserMessageList();
    } catch (NotificationManagerException e) {

    }
    request.setAttribute("SentNotifs", sentNotifs);
    return "/SILVERMAIL/jsp/sentUserNotifications.jsp";
  }

}
