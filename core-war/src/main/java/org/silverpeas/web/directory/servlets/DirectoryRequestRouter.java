/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.directory.servlets;

import org.silverpeas.web.directory.DirectoryException;
import org.silverpeas.web.directory.control.DirectorySessionController;
import org.silverpeas.web.directory.model.DirectoryItemList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * @author azzedine
 */
public class DirectoryRequestRouter extends ComponentRequestRouter<DirectorySessionController> {

  private static final long serialVersionUID = -1683812983096083815L;

  @Override
  public String getSessionControlBeanName() {
    return "directory";
  }

  @Override
  public DirectorySessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new DirectorySessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(String function, DirectorySessionController directorySC,
      HttpRequest request) {
    String destination = "";


    try {
      DirectoryItemList users = new DirectoryItemList();
      if (function.equalsIgnoreCase("Main")) {

        String groupId = request.getParameter("GroupId");
        String groupIds = request.getParameter("GroupIds");
        String spaceId = request.getParameter("SpaceId");
        String domainId = request.getParameter("DomainId");
        String domainIds = request.getParameter("DomainIds");
        String userId = request.getParameter("UserId");

        String sort = request.getParameter("Sort");
        if (StringUtil.isDefined(sort)) {
          directorySC.setCurrentSort(sort);
        } else {
          directorySC.setCurrentSort(DirectorySessionController.SORT_ALPHA);
        }

        if (StringUtil.isDefined(groupId)) {
          users = directorySC.getAllUsersByGroup(groupId);
        } else if (StringUtil.isDefined(groupIds)) {
          List<String> lGroupIds = Arrays.asList(StringUtil.split(groupIds, ','));
          users = directorySC.getAllUsersByGroups(lGroupIds);
        } else if (StringUtil.isDefined(spaceId)) {
          users = directorySC.getAllUsersBySpace(spaceId);
        } else if (StringUtil.isDefined(domainId)) {
          users = directorySC.getAllUsersByDomain(domainId);
        } else if (StringUtil.isDefined(domainIds)) {
          List<String> lDomainIds = Arrays.asList(StringUtil.split(domainIds, ','));
          users = directorySC.getAllUsersByDomains(lDomainIds);
        } else if (StringUtil.isDefined(userId)) {
          users = directorySC.getAllContactsOfUser(userId);
        } else {
          users = directorySC.getAllUsers();
        }

        String view = request.getParameter("View");
        if (StringUtil.isDefined(view) && DirectorySessionController.VIEW_CONNECTED.equals(view)) {
            destination = getDestination(DirectorySessionController.VIEW_CONNECTED, directorySC, request);
        } else {
          destination = doPagination(request, users, directorySC);
        }
        request.setAttribute("ShowHelp", true);
      } else if ("CommonContacts".equals(function)) {
        String userId = request.getParameter("UserId");
        users = directorySC.getCommonContacts(userId);
        destination = doPagination(request, users, directorySC);
      } else if (function.equalsIgnoreCase("searchByKey")) {
        String query = request.getParameter("key");
        boolean globalSearch = request.getParameterAsBoolean("Global");
        if (StringUtil.isDefined(query)) {
          users = directorySC.getUsersByQuery(query, globalSearch);
          destination = doPagination(request, users, directorySC);
        } else {
          destination = getDestination(DirectorySessionController.VIEW_ALL, directorySC, request);
        }
      } else if (function.equalsIgnoreCase(DirectorySessionController.VIEW_ALL)) {

        users = directorySC.getLastListOfAllUsers();
        destination = doPagination(request, users, directorySC);

      } else if (function.equalsIgnoreCase(DirectorySessionController.VIEW_CONNECTED)) {

        users = directorySC.getConnectedUsers();
        destination = doPagination(request, users, directorySC);

      } else if (isSearchByIndex(function)) {

        users = directorySC.getUsersByIndex(function);
        destination = doPagination(request, users, directorySC);

      } else if (function.equalsIgnoreCase("pagination")) {

        users = directorySC.getLastListOfUsersCalled();
        destination = doPagination(request, users, directorySC);

      } else if ("Sort".equals(function)) {
        String sort = request.getParameter("Type");
        directorySC.sort(sort);
        users = directorySC.getLastListOfUsersCalled();
        destination = doPagination(request, users, directorySC);
      }
    } catch (DirectoryException e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    return destination;

  }

  /**
   * return true if this searche by index
   */
  boolean isSearchByIndex(String lettre) {
    if (lettre != null && lettre.length() == 1) {
      return Character.isLetter(lettre.charAt(0));// return true if "lettre is Letrre
    } else {
      return false;
    }
  }

  /**
   * do pagination
   * @param request
   */
  String doPagination(HttpServletRequest request, DirectoryItemList users,
      DirectorySessionController directorySC) {
    int index = 0;
    if (StringUtil.isInteger(request.getParameter("Index"))) {
      index = Integer.parseInt(request.getParameter("Index"));
    }

    HttpSession session = request.getSession();
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        "SessionGraphicElementFactory");
    Pagination pagination = gef.getPagination(users.size(), directorySC.getElementsByPage(), index);
    DirectoryItemList membersToDisplay =
        users.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());

    // setting one fragment per user displayed
    request.setAttribute("UserFragments", directorySC.getFragments(membersToDisplay));

    request.setAttribute("userTotalNumber", users.size());
    request.setAttribute("pagination", pagination);
    request.setAttribute("paginationCounter", pagination.printCounter());
    request.setAttribute("View", directorySC.getCurrentView());
    request.setAttribute("Scope", directorySC.getCurrentDirectory());
    request.setAttribute("Query", directorySC.getCurrentQuery());
    request.setAttribute("Sort", directorySC.getCurrentSort());
    request.setAttribute("ShowHelp", false);
    processBreadCrumb(request, directorySC);
    return "/directory/jsp/directory.jsp";
  }

  private void processBreadCrumb(HttpServletRequest request, DirectorySessionController directorySC) {
    int directory = directorySC.getCurrentDirectory();
    String breadCrumb = directorySC.getString("directory.breadcrumb." + directory);
    switch (directory) {
      case DirectorySessionController.DIRECTORY_DEFAULT:
      case DirectorySessionController.DIRECTORY_MINE:
        // do nothing
        break;

      case DirectorySessionController.DIRECTORY_COMMON:
        breadCrumb += " " + directorySC.getCommonUserDetail().getDisplayedName();
        break;

      case DirectorySessionController.DIRECTORY_OTHER:
        breadCrumb += " " + directorySC.getOtherUserDetail().getDisplayedName();
        break;

      case DirectorySessionController.DIRECTORY_GROUP:
        breadCrumb += " ";
        boolean firstGroup = true;
        for (Group group : directorySC.getCurrentGroups()) {
          if (!firstGroup) {
            breadCrumb += " & ";
          }
          breadCrumb += group.getName();
          firstGroup = false;
        }
        break;

      case DirectorySessionController.DIRECTORY_DOMAIN:
        breadCrumb += " ";
        boolean first = true;
        for (Domain domain : directorySC.getCurrentDomains()) {
          if (!first) {
            breadCrumb += " & ";
          }
          breadCrumb += domain.getName();
          first = false;
        }
        break;

      case DirectorySessionController.DIRECTORY_SPACE:
        breadCrumb += " " + directorySC.getCurrentSpace().getName(directorySC.getLanguage());
        break;

      default:
        break;
    }
    request.setAttribute("BreadCrumb", breadCrumb);
  }
}
