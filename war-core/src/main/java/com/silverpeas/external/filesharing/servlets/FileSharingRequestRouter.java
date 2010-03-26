/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.external.filesharing.servlets;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.external.filesharing.control.FileSharingSessionController;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public class FileSharingRequestRouter extends ComponentRequestRouter {
  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
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
  public ComponentSessionController createComponentSessionController(
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
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    String rootDest = "/fileSharing/jsp/";

    FileSharingSessionController fileSharingSC = (FileSharingSessionController) componentSC;
    SilverTrace.info("fileSharing", "FileSharingRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

    try {
      if (function.equals("Main")) {
        destination = getDestination("ViewTickets", fileSharingSC, request);
      } else if (function.equals("ViewTickets")) {
        // liste des tickets visibles pour l'utilisateur
        List<TicketDetail> tickets = (List<TicketDetail>) fileSharingSC.getTicketsByUser();
        request.setAttribute("Tickets", tickets);
        destination = rootDest + "viewTickets.jsp";
      } else if (function.equals("DeleteTicket")) {
        // Suppression d'un ticket
        String keyFile = request.getParameter("KeyFile");
        fileSharingSC.deleteTicket(keyFile);
        destination = getDestination("ViewTickets", fileSharingSC, request);
      }

      else if (function.equals("NewTicket")) {
        // récupération des données venant de attachment ou versioning
        String fileId = request.getParameter("FileId");
        String componentId = request.getParameter("ComponentId");
        String type = request.getParameter("Type"); // versioning or not

        String logicalName = null;
        if (StringUtil.isDefined(type) && "version".equalsIgnoreCase(type)) {
          VersioningUtil versioningUtil = new VersioningUtil();
          Document document =
              versioningUtil.getDocument(new DocumentPK(Integer.parseInt(fileId), componentId));
          logicalName = document.getName();
        } else {
          AttachmentDetail attachment =
              AttachmentController.searchAttachmentByPK(new AttachmentPK(fileId));
          logicalName = attachment.getLogicalName();
        }

        // passage des paramètres
        request.setAttribute("FileId", fileId);
        request.setAttribute("Versioning", new Boolean("version".equalsIgnoreCase(type)));
        request.setAttribute("ComponentId", componentId);
        request.setAttribute("FileName", logicalName);
        request.setAttribute("CreatorName", fileSharingSC.getUserDetail().getDisplayedName());
        destination = rootDest + "ticketManager.jsp";
      } else if (function.equals("CreateTicket")) {
        // récupération des paramètres venus de l'écran de saisie et création de l'objet
        // TicketDetail
        TicketDetail ticket = generateTicket(fileSharingSC, request);
        String keyFile = fileSharingSC.createTicket(ticket);
        // mettre à jour l'objet ticket
        ticket.setKeyFile(keyFile);
        request.setAttribute("Url", ticket.getUrl());

        destination = rootDest + "confirmTicket.jsp";
      } else if (function.equals("EditTicket")) {
        String keyFile = request.getParameter("KeyFile");
        TicketDetail ticket = fileSharingSC.getTicket(keyFile);
        request.setAttribute("Ticket", ticket);
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
      } else if (function.equals("DownloadFile")) {
        // contrôler
      }

      else {
        destination = rootDest + function;
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
    int fileId = Integer.parseInt(request.getParameter("FileId"));
    String componentId = request.getParameter("ComponentId");
    boolean versioning = false;
    if ("true".equals(request.getParameter("Versioning")))
      versioning = true;
    String date = request.getParameter("EndDate");
    Date endDate = DateUtil.stringToDate(date, fileSharingSC.getLanguage());
    int nbAccessMax = Integer.parseInt(request.getParameter("NbAccessMax"));
    return new TicketDetail(fileId, componentId, versioning, null, new Date(),
        endDate, nbAccessMax);
  }

  private TicketDetail updateTicket(String keyFile,
      FileSharingSessionController fileSharingSC, HttpServletRequest request)
      throws ParseException, RemoteException {
    TicketDetail ticket = fileSharingSC.getTicket(keyFile);
    String date = request.getParameter("EndDate");
    Date endDate = DateUtil.stringToDate(date, fileSharingSC.getLanguage());
    int nbAccessMax = Integer.parseInt(request.getParameter("NbAccessMax"));
    ticket.setEndDate(endDate);
    ticket.setNbAccessMax(nbAccessMax);
    return ticket;
  }
}
