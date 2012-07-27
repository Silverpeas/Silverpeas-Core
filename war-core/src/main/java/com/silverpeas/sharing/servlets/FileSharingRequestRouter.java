/**
* Copyright (C) 2000 - 2011 Silverpeas
*
* This program is free software: you can redistribute it and/or modify it under the terms of the
* GNU Affero General Public License as published by the Free Software Foundation, either version 3
* of the License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
* redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
* applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
* text describing the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with this program.
* If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.sharing.servlets;

import com.silverpeas.sharing.control.FileSharingSessionController;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.model.TicketFactory;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminReference;
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
*
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
*
* @param function The entering request function (ex : "Main.jsp")
* @param fileSharingSC The component Session Control, build and initialised.
* @return The complete destination URL for a forward (ex :
* "/almanach/jsp/almanach.jsp?flag=user")
*/
  @Override
  public String getDestination(String function, FileSharingSessionController fileSharingSC,
          HttpServletRequest request) {
    String destination = "";
    String rootDest = "/sharing/jsp/";
    SilverTrace.info("fileSharing", "FileSharingRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE",
            "User=" + fileSharingSC.getUserId() + " Function=" + function);

    try {
      if ("Main".equals(function)) {
        destination = getDestination("ViewTickets", fileSharingSC, request);
      } else if ("ViewTickets".equals(function)) {
        // liste des tickets visibles pour l'utilisateur
        List<Ticket> tickets = fileSharingSC.getTicketsByUser();
        request.setAttribute("Tickets", tickets);
        destination = rootDest + "viewTickets.jsp";
      } else if ("DeleteTicket".equals(function)) {
        // Suppression d'un ticket
        String token = request.getParameter("token");
        fileSharingSC.deleteTicket(token);
        destination = getDestination("ViewTickets", fileSharingSC, request);
      } else if ("NewTicket".equals(function)) {
        // récupération des données venant de attachment ou versioning
        String objectId = request.getParameter("objectId");
        String componentId = request.getParameter("componentId");
        String type = request.getParameter("type");
        UserDetail creator = fileSharingSC.getUserDetail();
        Ticket newTicket = TicketFactory.aTicket(Integer.parseInt(objectId), componentId, creator.
                getId(), new Date(), new Date(), 1, type);
        // passage des paramètres
        request.setAttribute("Ticket", newTicket);
        request.setAttribute("Creator", creator.getDisplayedName());
        request.setAttribute("Url", newTicket.getUrl(request));
        request.setAttribute("Action", "CreateTicket");
        destination = rootDest + "ticketManager.jsp";
      } else if ("CreateTicket".equals(function)) {
        // récupération des paramètres venus de l'écran de saisie et création de l'objet
        // Ticket
        Ticket ticket = generateTicket(fileSharingSC, request);
        String keyFile = fileSharingSC.createTicket(ticket);
        // mettre à jour l'objet ticket
        ticket.setToken(keyFile);
        request.setAttribute("Url", ticket.getUrl(request));

        destination = rootDest + "confirmTicket.jsp";
      } else if ("EditTicket".equals(function)) {
        String token = request.getParameter("token");
        Ticket ticket = fileSharingSC.getTicket(token);
        request.setAttribute("Creator", AdminReference.getAdminService().getUserDetail(ticket.
                getCreatorId()).getDisplayedName());
        if (StringUtil.isDefined(ticket.getLastModifier())) {
          UserDetail updater = AdminReference.getAdminService().getUserDetail(
                  ticket.getLastModifier());
          if (updater != null) {
            request.setAttribute("Updater", updater.getDisplayedName());
          }
        }
        request.setAttribute("Ticket", ticket);
        request.setAttribute("Url", ticket.getUrl(request));
        request.setAttribute("Action", "UpdateTicket");
        // appel jsp
        destination = rootDest + "ticketManager.jsp";
      } else if ("UpdateTicket".equals(function)) {
        // récupération des paramètres venus de l'écran de saisie
        String keyFile = request.getParameter("token");
        Ticket ticket = updateTicket(keyFile, fileSharingSC, request);
        ticket.setToken(keyFile);
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

  private Ticket generateTicket(FileSharingSessionController fileSharingSC,
          HttpServletRequest request)
          throws ParseException {
    Ticket ticket;
    UserDetail creator = fileSharingSC.getUserDetail();
    int fileId = Integer.parseInt(request.getParameter("objectId"));
    String componentId = request.getParameter("componentId");
    String type = request.getParameter("type");
    if (!StringUtil.isDefined(request.getParameter("continuous"))) {
      String date = request.getParameter("endDate");
      Date endDate = DateUtil.stringToDate(date, fileSharingSC.getLanguage());
      int maxAccessNb = Integer.parseInt(request.getParameter("nbAccessMax"));
      ticket = TicketFactory.aTicket(fileId, componentId, creator.getId(), new Date(), endDate,
              maxAccessNb, type);
    } else {
      ticket = TicketFactory.continuousTicket(fileId, componentId, creator.getId(), new Date(), type);
    }
    return ticket;
  }

  private Ticket updateTicket(String token, FileSharingSessionController fileSharingSC,
          HttpServletRequest request)
          throws ParseException, RemoteException {
    Ticket ticket = fileSharingSC.getTicket(token);
    if (!StringUtil.isDefined(request.getParameter("continuous"))) {
      String date = request.getParameter("endDate");
      Date endDate = DateUtil.stringToDate(date, fileSharingSC.getLanguage());
      int maxAccessNb = Integer.parseInt(request.getParameter("nbAccessMax"));
      ticket.setEndDate(endDate);
      ticket.setNbAccessMax(maxAccessNb);
    } else {
      ticket.setContinuous();
    }

    return ticket;
  }
}