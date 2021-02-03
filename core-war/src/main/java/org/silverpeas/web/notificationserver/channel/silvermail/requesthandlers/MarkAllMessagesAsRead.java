/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.web.notificationserver.channel.silvermail.requesthandlers;

import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILPersistence;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import org.silverpeas.web.notificationserver.channel.silvermail.SILVERMAILRequestHandler;
import org.silverpeas.web.notificationserver.channel.silvermail.SILVERMAILSessionController;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Singleton
@Named("MarkAllMessagesAsRead")
public class MarkAllMessagesAsRead implements SILVERMAILRequestHandler {

  @Override
  public String handleRequest(ComponentSessionController componentSC, HttpServletRequest request) {
    SILVERMAILSessionController silvermailScc = (SILVERMAILSessionController) componentSC;

    try {
      SILVERMAILPersistence.markAllMessagesAsRead(componentSC.getUserId());
    } catch (NumberFormatException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }

    silvermailScc.getSelectedUserNotificationIds().clear();
    return "/SILVERMAIL/jsp/main.jsp";
  }

}
