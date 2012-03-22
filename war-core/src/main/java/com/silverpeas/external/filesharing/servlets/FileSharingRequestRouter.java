/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.filesharing.servlets;

import com.silverpeas.external.filesharing.control.FileSharingSessionController;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class FileSharingRequestRouter extends ComponentRequestRouter<FileSharingSessionController> {

  private static final long serialVersionUID = -8855028133035807994L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "FileSharing";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public FileSharingSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new FileSharingSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, FileSharingSessionController fileSharingSC,
      HttpServletRequest request) {
    String destination = "";
    String rootDest = "/fileSharing/jsp/";
    SilverTrace.info("fileSharing", "FileSharingRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + fileSharingSC.getUserId() + " Function=" + function);

    try {
      if (function.equals("Main")) {
        destination = getDestination("ViewTickets", fileSharingSC, request);
      } else if (function.equals("ViewTickets")) {
        // liste des tickets visibles pour l'utilisateur
        List<TicketDetail> tickets = fileSharingSC.getTicketsByUser();
        request.setAttribute("Tickets", tickets);
        destination = rootDest + "viewTickets.jsp";
      } else if (function.equals("DeleteTicket")) {
        // Suppression d'un ticket
        String keyFile = request.getParameter("KeyFile");
        fileSharingSC.deleteTicket(keyFile);
        destination = getDestination("ViewTickets", fileSharingSC, request);
      } else if (function.equals("NewTicket")) {
        // récupération des données venant de attachment ou versioning
        String fileId = request.getParameter("FileId");
        String componentId = request.getParameter("ComponentId");
        String type = request.getParameter("Type"); // versioning or not
        boolean versioned = StringUtil.isDefined(type) && "version".equalsIgnoreCase(type);
        UserDetail creator = fileSharingSC.getUserDetail();
        TicketDetail newTicket = TicketDetail.aTicket(Integer.parseInt(fileId), componentId,
            versioned, creator, new Date(), new Date(), 1);

        // passage des paramètres
        request.setAttribute("Ticket", newTicket);
        request.setAttribute("Url", newTicket.getUrl(request));
        request.setAttribute("Action", "CreateTicket");
        destination = rootDest + "ticketManager.jsp";
      } else if (function.equals("CreateTicket")) {
        // récupération des paramètres venus de l'écran de saisie et création de l'objet
        // TicketDetail
        TicketDetail ticket = generateTicket(fileSharingSC, request);
        String keyFile = fileSharingSC.createTicket(ticket);
        // mettre à jour l'objet ticket
        ticket.setKeyFile(keyFile);
        request.setAttribute("Url", ticket.getUrl(request));

        destination = rootDest + "confirmTicket.jsp";
      } else if (function.equals("EditTicket")) {
        String keyFile = request.getParameter("KeyFile");
        TicketDetail ticket = fileSharingSC.getTicket(keyFile);
        request.setAttribute("Ticket", ticket);
        request.setAttribute("Url", ticket.getUrl(request));
        request.setAttribute("Action", "UpdateTicket");
        // appel jsp
        destination = rootDest + "ticketManager.jsp";
      } else if (function.equals("UpdateTicket")) {
        // récupération des paramètres venus de l'écran de saisie
        String keyFile = request.getParameter("KeyFile");
        TicketDetail ticket = updateTicket(keyFile, fileSharingSC, request);
        ticket.setKeyFile(keyFile);
        // modification du lien
        fileSharingSC.updateTicket(ticket);
        // retour sur le liste des tickets
        destination = getDestination("ViewTickets", fileSharingSC, request);
      } else {
        if ("!DownloadFile".equals(function)) {
          destination = rootDest + function;
        }
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("fileSharing", "FileSharingRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;

  }

  private TicketDetail generateTicket(
      FileSharingSessionController fileSharingSC, HttpServletRequest request)
      throws ParseException {
    TicketDetail ticket;
    UserDetail creator = fileSharingSC.getUserDetail();
    int fileId = Integer.parseInt(request.getParameter("FileId"));
    String componentId = request.getParameter("ComponentId");
    boolean versioning = false;
    if ("true".equals(request.getParameter("Versioning"))) {
      versioning = true;
    }
    if (!StringUtil.isDefined(request.getParameter("Continuous"))) {
      String date = request.getParameter("EndDate");
      Date endDate = DateUtil.stringToDate(date, fileSharingSC.getLanguage());
      int maxAccessNb = Integer.parseInt(request.getParameter("NbAccessMax"));
      ticket = TicketDetail.aTicket(fileId, componentId, versioning, creator, new Date(), endDate,
          maxAccessNb);
    } else {
      ticket = TicketDetail.continuousTicket(fileId, componentId, versioning, creator, new Date());
    }
    return ticket;
  }

  private TicketDetail updateTicket(String keyFile,
      FileSharingSessionController fileSharingSC, HttpServletRequest request)
      throws ParseException, RemoteException {
    TicketDetail ticket = fileSharingSC.getTicket(keyFile);
    if (!StringUtil.isDefined(request.getParameter("Continuous"))) {
      String date = request.getParameter("EndDate");
      Date endDate = DateUtil.stringToDate(date, fileSharingSC.getLanguage());
      int maxAccessNb = Integer.parseInt(request.getParameter("NbAccessMax"));
      ticket.setEndDate(endDate);
      ticket.setNbAccessMax(maxAccessNb);
    } else {
      ticket.setContinuous();
    }

    return ticket;
  }
}
