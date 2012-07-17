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

package com.silverpeas.directory.servlets;

import com.silverpeas.directory.control.DirectoryService;
import com.silverpeas.directory.model.Member;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.board.BoardSilverpeasV5;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Bensalem Nabil
 */
public class DirectoryServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static int ELEMENTS_PER_PAGE = 2;
  private String m_context;
  private ResourceLocator multilangG;
  private ResourceLocator multilang;
  private DirectoryService directorySC = new DirectoryService();
  private Pagination pagination;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String destination = "";
    m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    String userId = m_MainSessionCtrl.getUserId();
    directorySC.setUserId(userId);
    multilang = new ResourceLocator("com.silverpeas.directory.multilang.DirectoryBundle", "");
    multilangG = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", "");
    ResourceLocator settings =
        new ResourceLocator("com.silverpeas.directory.settings.DirectorySettings", "");
    try {
      ELEMENTS_PER_PAGE = Integer.parseInt(settings.getString("ELEMENTS_PER_PAGE"));

    } catch (NumberFormatException nfex) {
    }
    List<UserDetail> users = new ArrayList<UserDetail>();
    List<Member> membersToDisplay = new ArrayList<Member>();
    request.setAttribute("MyId", directorySC.getUserId());
    String function = request.getParameter("Action");
    if (function.equalsIgnoreCase("Main")) {
      String groupId = request.getParameter("GroupId");
      String spaceId = request.getParameter("SpaceId");
      String domainId = request.getParameter("DomainId");
      String contactId = request.getParameter("ContactId");
      // for display the common contact between me and user who have userId equal CommonContactId
      String commonContactId = request.getParameter("CommonContactId");
      if (StringUtil.isDefined(groupId)) {
        users = directorySC.getAllUsersByGroup(groupId);
      } else if (StringUtil.isDefined(spaceId)) {
        users = directorySC.getAllUsersBySpace(spaceId);
      } else if (StringUtil.isDefined(domainId)) {
        users = directorySC.getAllUsersByDomain(domainId);
      } else if (StringUtil.isInteger(contactId)) {
        users = directorySC.getAllContatcsOfUuser(contactId);
      } else if (StringUtil.isInteger(commonContactId)) {
        users = directorySC.getCommonContacts(directorySC.getUserId(), commonContactId);
      } else {
        users = directorySC.getAllUsers();
      }
      destination = doPagination(request, users, membersToDisplay);
    } else if (function.equalsIgnoreCase("searchByKey")) {
      users = directorySC.getUsersByLastName(request.getParameter("key").toUpperCase());
      destination = doPagination(request, users, membersToDisplay);
    } else if (function.equalsIgnoreCase("Pagination")) {
      users = directorySC.getLastListOfUsersCallded();
      destination = doPagination(request, users, membersToDisplay);
    }
    destination = getHtml(membersToDisplay);
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    out.println(destination);
  }

  /**
   *do pagination.
   *@param HttpServletRequest request.
   */
  String doPagination(HttpServletRequest request, List<UserDetail> users,
      List<Member> membersToDisplay) {
    GraphicElementFactory gef = (GraphicElementFactory) request.getSession().getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    int currentPage = 0;
    if (StringUtil.isInteger(request.getParameter("Index"))) {
      // request.getParameter("currentPage")=currentPage*ELEMENTS_PER_PAGE
      currentPage = Integer.parseInt(request.getParameter("Index"));
    }
    pagination = gef.getPagination(users.size(), ELEMENTS_PER_PAGE, currentPage);
    membersToDisplay.clear();
    membersToDisplay.addAll(toListMember(users.subList(pagination.getFirstItemIndex(), pagination.
        getLastItemIndex())));

    return "/directory/jsp/directory.jsp";
  }

  /**
   *tronsform list of UserDetail to list of Membre
   *@param List<UserDetail>
   */
  private List<Member> toListMember(List<UserDetail> uds) {
    List<Member> listMember = new ArrayList<Member>();

    for (UserDetail varUD : uds) {

      listMember.add(new Member(varUD));

    }
    return listMember;
  }

  private String getHtml(List<Member> members) {
    BoardSilverpeasV5 viewBoard = new BoardSilverpeasV5();

    // **************div=user**********************
    String html = "<div id=\"users\">";
    html += "<ol class=\"message_list\">";
    for (int i = 0; i < members.size(); i++) {
      Member member = members.get(i);
      html += "<li>" + "\n";
      html += viewBoard.printBefore() + "\n";
      // **************div=infoAndPhoto**********************
      html += "<div id=\"infoAndPhoto\">" + "\n";
      // **************div=profilPhoto**********************
      html += "<div id=\"profilPhoto\">" + "\n";
      html += "<a href=\"createPhoto\"><img" + "\n";
      html += "src=\"" + m_context + member.getUserDetail().getAvatar() + "\"" + "\n";
      html += "alt=\"viewUser\" class=\"avatar\" /> </a>" + "\n";
      html += "</div>" + "\n";
      // **************div=info**********************
      html += "<div id=\"info\">" + "\n";
      html += "<ul>" + "\n";
      html += "<li> <a class=\"userName\" href=\"" + m_context + "/Rprofil/jsp/Main?userId="
          + member.getId() + "\"" + "\n";
      html +=
          "class=\"link\">" + member.getLastName() + " " + member.getFirstName() + "</a>" + "\n";
      html += "</li>" + "\n";
      html += "<li>" + "\n";
      html +=
          "<a  class=\"userMail\"class=\"link\" href=\"#\" class=\"link\" onclick=\"OpenPopup(" +
          member.
          getId() + ",'" + member.getLastName() + " " + member.getFirstName() + "')\">" +
          member.
          getMail() + "" + "\n";
      html += "</a>" + "\n";
      html += "</li>" + "\n";
      html += "<li>" + "\n";
      html += multilangG.getString(multilang.getString(member.getAccessLevel()));
      html += "</li>" + "\n";
      html += "<li>" + "\n";
      if (member.isConnected()) {
        html +=
            "<img src=\"" + m_context +
            "/directory/jsp/icons/connected.jpg\" width=\"10\" height=\"10\" />"
            + multilang.getString("directory.connected") + " " + member.getDuration() + "\n";
      } else {
        html +=
            "<img src=\"" + m_context +
            "/directory/jsp/icons/deconnected.jpg\" width=\"10\" height=\"10\" />" + "\n";

      }
      html += "</li>" + "\n";
      html += "</ul>" + "\n";
      html += "</div>" + "\n";
      html += "</div>" + "\n";
      // **************div=info**********************
      html += "<div id=\"action\">" + "\n";
      if (!member.getId().equals(directorySC.getUserId())) {
        if (!member.isRelationOrInvitation(directorySC.getUserId())) {
          html += "<a href=\"#\"  onclick=\"OpenPopupInvitaion(" + member.getId() + ",'" + member.
              getLastName() + " " + member.getFirstName() + "')\">";
          html += "<span>Envoyer une invitation</span></a><br> <br>" + "\n";
        }
        html +=
            "<a href=\"#\"  onclick=\"OpenPopup(" + member.getId() + ",'" + member.getLastName() +
            " " + member.
            getFirstName() + "')\">";
        html += "<span>Envoyer un message</span></a>" + "\n";
      }
      html += "</div>" + "\n";
      html += viewBoard.printAfter() + "\n";
      html += "</li>" + "\n";
    }

    html += "<li>" + "\n";
    String pag = pagination.printIndex("doPaganation");
    html += pag;
    System.out.println(pag);
    html += "</li>" + "\n";

    html += "</ol>" + "\n";
    html += "</div>" + "\n";

    return html;
  }
}
