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
package org.silverpeas.web.sharing.control;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.sharing.model.DownloadDetail.QUERY_ORDER_BY;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.sharing.services.SharingTicketService;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.sharing.bean.SharingNotificationVO;
import org.silverpeas.core.web.sharing.notification.FileSharingUserNotification;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.sharing.model.DownloadDetail.QUERY_ORDER_BY.*;
import static org.silverpeas.core.sharing.model.Ticket.QUERY_ORDER_BY.*;

public class FileSharingSessionController extends AbstractComponentSessionController {

  private static final Map<Integer, Pair<Ticket.QUERY_ORDER_BY, Ticket.QUERY_ORDER_BY>>
      TICKET_ORDER_BIES = new HashMap<>();
  private static final int TICKET_CREATION_DATE_INDEX = 1;
  private static final int TICKET_END_DATE_INDEX = 4;
  private static final int TICKET_NBACCESS_DATE_INDEX = 5;
  private static final int DEFAULT_TICKET_PAGINATION_SIZE = 25;
  private PaginationPage ticketPagination;
  private Ticket.QUERY_ORDER_BY ticketOrderBy;

  private static final Map<Integer, Pair<QUERY_ORDER_BY, QUERY_ORDER_BY>> DOWNLOAD_ORDER_BIES =
      new HashMap<>();
  private static final int DOWNLOAD_DATE_INDEX = 1;
  private static final int DOWNLOAD_IP_INDEX = 2;
  private static final int DEFAULT_PAGINATION_SIZE = 10;
  private PaginationPage downloadPagination;
  private QUERY_ORDER_BY downloadOrderBy;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public FileSharingSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.sharing.multilang.fileSharingBundle",
        "org.silverpeas.sharing.settings.fileSharingIcons");
    ticketPagination = new PaginationPage(1, DEFAULT_TICKET_PAGINATION_SIZE);
    downloadPagination = new PaginationPage(1, DEFAULT_PAGINATION_SIZE);
  }

  public PaginationPage getTicketPagination() {
    return ticketPagination;
  }

  public void setTicketPagination(final PaginationPage ticketPagination) {
    this.ticketPagination = ticketPagination;
  }

  public Map<Integer, Pair<Ticket.QUERY_ORDER_BY, Ticket.QUERY_ORDER_BY>> getTicketOrderBies() {
    return TICKET_ORDER_BIES;
  }

  public void setTicketOrderBy(final Ticket.QUERY_ORDER_BY ticketOrderBy) {
    this.ticketOrderBy = ticketOrderBy;
  }

  public PaginationPage getDownloadPagination() {
    return downloadPagination;
  }

  public Map<Integer, Pair<QUERY_ORDER_BY, QUERY_ORDER_BY>> getDownloadOrderBies() {
    return DOWNLOAD_ORDER_BIES;
  }

  public void setDownloadPagination(final PaginationPage downloadPagination) {
    this.downloadPagination = downloadPagination;
  }

  public void setDownloadOrderBy(final QUERY_ORDER_BY downloadOrderBy) {
    if (downloadOrderBy != null) {
      this.downloadOrderBy = downloadOrderBy;
    }
  }

  public List<Ticket> getTicketsByUser() {
    return getFileSharingService().getTicketsByUser(getUserId(), ticketPagination, ticketOrderBy);
  }

  public void updateTicket(Ticket ticket) {
    UserDetail user = getUserDetail();
    ticket.setLastModifier(user);
    ticket.setUpdateDate(new Date());
    getFileSharingService().updateTicket(ticket);
  }

  public void deleteTicket(String key) {
    getFileSharingService().deleteTicket(key);
  }

  public Ticket getTicket(String key) {
    return getFileSharingService().getTicket(key);
  }

  public SilverpeasList getTicketDownloads(Ticket ticket) {
    return getFileSharingService().getTicketDownloads(ticket, downloadPagination, downloadOrderBy);
  }

  private SharingTicketService getFileSharingService() {
    return SharingServiceProvider.getSharingTicketService();
  }

  public void notifyUsers(Ticket ticket, SharingNotificationVO sharingParam) {
    String selectedUsersStr = sharingParam.getSelectedUsers();
    String externalEmails = sharingParam.getExternalEmails();

    if (StringUtil.isDefined(selectedUsersStr) || StringUtil.isDefined(externalEmails)) {
      // Notify users
      FileSharingUserNotification.notify(ticket, sharingParam);
    }
  }

  static {
    TICKET_ORDER_BIES.put(TICKET_CREATION_DATE_INDEX, Pair.of(CREATION_DATE_ASC, CREATION_DATE_DESC));
    TICKET_ORDER_BIES.put(TICKET_END_DATE_INDEX, Pair.of(END_DATE_ASC, END_DATE_DESC));
    TICKET_ORDER_BIES.put(TICKET_NBACCESS_DATE_INDEX, Pair.of(NB_ACCESS_DATE_ASC, NB_ACCESS_DATE_DESC));

    DOWNLOAD_ORDER_BIES.put(DOWNLOAD_DATE_INDEX, Pair.of(DOWNLOAD_DATE_ASC, DOWNLOAD_DATE_DESC));
    DOWNLOAD_ORDER_BIES.put(DOWNLOAD_IP_INDEX, Pair.of(DOWNLOAD_IP_ASC, DOWNLOAD_IP_DESC));
  }
}