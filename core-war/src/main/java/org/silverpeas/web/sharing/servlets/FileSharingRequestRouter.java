/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.sharing.servlets;

import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.sharing.bean.SharingNotificationVO;
import org.silverpeas.web.sharing.control.FileSharingSessionController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane.getOrderByFrom;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination
    .getPaginationPageFrom;


public class FileSharingRequestRouter extends ComponentRequestRouter<FileSharingSessionController> {

  private static final long serialVersionUID = -8855028133035807994L;

  @Override
  public String getSessionControlBeanName() {
    return "FileSharing";
  }

  @Override
  public FileSharingSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new FileSharingSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(String function, FileSharingSessionController fileSharingSC,
      HttpRequest request) {
    String rootDest = "/sharing/jsp/";
    String destination;
    try {
      switch (function) {
        case "Main":
          destination = getDestination("ViewTickets", fileSharingSC, request);
          break;
        case "ViewTickets":
          // Download pagination and order by
          fileSharingSC.setTicketPagination(
              getPaginationPageFrom(request, fileSharingSC.getTicketPagination()));
          fileSharingSC.setTicketOrderBy(getOrderByFrom(request, fileSharingSC.getTicketOrderBies()));
          // liste des tickets visibles pour l'utilisateur
          List<Ticket> tickets = fileSharingSC.getTicketsByUser();
          request.setAttribute("Tickets", tickets);
          request.setAttribute("TicketsPagination", fileSharingSC.getTicketPagination());
          destination = rootDest + "viewTickets.jsp";
          break;
        case "DeleteTicket": {
          // Suppression d'un ticket
          String token = request.getParameter("token");
          fileSharingSC.deleteTicket(token);
          destination = getDestination("ViewTickets", fileSharingSC, request);
          break;
        }
        case "EditTicket": {
          String token = request.getParameter("token");
          Ticket ticket = fileSharingSC.getTicket(token);
          request.setAttribute("Creator", AdministrationServiceProvider.getAdminService().getUserDetail(ticket.
              getCreatorId()).getDisplayedName());
          if (StringUtil.isDefined(ticket.getLastModifier())) {
            UserDetail updater = AdministrationServiceProvider.getAdminService().getUserDetail(
                ticket.getLastModifier());
            if (updater != null) {
              request.setAttribute("Updater", updater.getDisplayedName());
            }
          }
          // Download pagination and order by
          fileSharingSC.setDownloadPagination(
              getPaginationPageFrom(request, fileSharingSC.getDownloadPagination()));
          fileSharingSC
              .setDownloadOrderBy(getOrderByFrom(request, fileSharingSC.getDownloadOrderBies()));
          // JSP attributes
          request.setAttribute("Ticket", ticket);
          request.setAttribute("TicketDownloads", fileSharingSC.getTicketDownloads(ticket));
          request.setAttribute("TicketDownloadPagination", fileSharingSC.getDownloadPagination());
          request.setAttribute("Url", ticket.getUrl(request));
          request.setAttribute("Action", "UpdateTicket");
          // appel jsp
          destination = rootDest + "ticketManager.jsp";
          break;
        }
        case "UpdateTicket": {
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
          break;
        }
        default:
          destination = "!DownloadFile".equals(function) ? rootDest + function : "";
          break;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;

  }

  private Ticket updateTicket(String token, FileSharingSessionController fileSharingSC,
      HttpServletRequest request)
      throws ParseException {
    Ticket ticket = fileSharingSC.getTicket(token);
    if ("1".equalsIgnoreCase(request.getParameter("validity"))) {
      String date = request.getParameter("endDate");
      Date endDate = DateUtil.getEndOfDay(DateUtil.stringToDate(date, fileSharingSC.getLanguage()));
      int maxAccessNb = Integer.parseInt(request.getParameter("nbAccessMax"));
      ticket.setEndDate(endDate);
      ticket.setNbAccessMax(maxAccessNb);
    } else {
      ticket.setAsUnlimited();
    }

    return ticket;
  }
}