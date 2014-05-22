/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.sharing.servlets;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.sharing.bean.SharingNotificationVO;

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
   * @param fileSharingSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, FileSharingSessionController fileSharingSC,
      HttpRequest request) {
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
        if (newTicket == null) {
          throwHttpForbiddenError();
        }
        // passage des paramètres
        request.setAttribute("Ticket", newTicket);
        request.setAttribute("Creator", creator.getDisplayedName());
        request.setAttribute("Url", newTicket.getUrl(request));
        request.setAttribute("Action", "CreateTicket");
        destination = rootDest + "ticketManager.jsp";
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

        SharingNotificationVO sharingParam =
            new SharingNotificationVO(request.getParameter("users"),
                request.getParameter("externalEmails"), request.getParameter("additionalMessage"),
                ticket.getUrl(request));
        fileSharingSC.notifyUsers(ticket, sharingParam);
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

  private Ticket updateTicket(String token, FileSharingSessionController fileSharingSC,
      HttpServletRequest request)
      throws ParseException, RemoteException {
    Ticket ticket = fileSharingSC.getTicket(token);
    if ("1".equalsIgnoreCase(request.getParameter("validity"))) {
      String date = request.getParameter("endDate");
      Date endDate = DateUtil.getEndOfDay(DateUtil.stringToDate(date, fileSharingSC.getLanguage()));
      int maxAccessNb = Integer.parseInt(request.getParameter("nbAccessMax"));
      ticket.setEndDate(endDate);
      ticket.setNbAccessMax(maxAccessNb);
    } else {
      ticket.setContinuous();
    }

    return ticket;
  }
}