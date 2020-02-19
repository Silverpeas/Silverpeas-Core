/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.web.directory.servlets;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.directory.DirectoryException;
import org.silverpeas.web.directory.control.DirectorySessionController;
import org.silverpeas.web.directory.model.DirectoryItem;
import org.silverpeas.web.directory.model.DirectoryItemList;
import org.silverpeas.web.directory.model.UserFragmentVO;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination
    .getPaginationPageFrom;

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
      List<String> lDomainIds = processDomains(request, directorySC);

      DirectoryItemList users;
      if ("Main".equalsIgnoreCase(function)) {

        String groupId = request.getParameter("GroupId");
        String groupIds = request.getParameter("GroupIds");
        String spaceId = request.getParameter("SpaceId");
        String userId = request.getParameter("UserId");
        // case of a direct access to directory of contacts of once component
        String componentId = request.getParameter("ComponentId");

        String sort = request.getParameter("Sort");
        if (StringUtil.isDefined(sort)) {
          directorySC.setCurrentSort(sort);
        } else {
          directorySC.setCurrentSort(DirectorySessionController.SORT_ALPHA);
        }

        String doNotUseContactsComponents = request.getParameter("DoNotUseContacts");
        directorySC.setDoNotUseContacts(StringUtil.getBooleanValue(doNotUseContactsComponents));

        if (StringUtil.isDefined(groupId)) {
          users = directorySC.getAllUsersByGroup(groupId);
        } else if (StringUtil.isDefined(groupIds)) {
          List<String> lGroupIds = Arrays.asList(StringUtil.split(groupIds, ','));
          users = directorySC.getAllUsersByGroups(lGroupIds);
        } else if (StringUtil.isDefined(spaceId)) {
          users = directorySC.getAllUsersBySpace(spaceId);
        } else if (!lDomainIds.isEmpty()) {
          directorySC.initSources(true);
          users = directorySC.getAllUsersByDomains();
        } else if (StringUtil.isDefined(userId)) {
          users = directorySC.getAllContactsOfUser(userId);
        } else if (StringUtil.isDefined(componentId)) {
          directorySC.setCurrentDirectory(DirectorySessionController.DIRECTORY_COMPONENT);
          users = directorySC.getContacts(componentId, true);
        } else {
          directorySC.initSources(false);
          users = directorySC.getAllUsers();
        }

        String view = request.getParameter("View");
        if (StringUtil.isDefined(view) && DirectorySessionController.VIEW_CONNECTED.equals(view)) {
            destination = getDestination(DirectorySessionController.VIEW_CONNECTED, directorySC, request);
        } else {
          destination = doPagination(request, users, directorySC);
        }
        request.setAttribute("ShowHelp", true);
      } else if ("RemoveUserFromLists".equals(function)) {
        String userId = request.getParameter("UserId");
        if (StringUtil.isDefined(userId)) {
          directorySC.removeUserFromLists(User.getById(userId));
        }
        users = directorySC.getLastListOfUsersCalled();
        destination = doPagination(request, users, directorySC);
      } else if ("CommonContacts".equals(function)) {
        String userId = request.getParameter("UserId");
        users = directorySC.getCommonContacts(userId);
        destination = doPagination(request, users, directorySC);
      } else if ("searchByKey".equalsIgnoreCase(function)) {
        boolean globalSearch = request.getParameterAsBoolean("Global");
        QueryDescription query;
        if (request.isContentInMultipart()) {
          query = directorySC.buildQuery(request.getFileItems(), globalSearch);
        } else {
          query = directorySC.buildSimpleQuery(request.getParameter("queryDirectory"), globalSearch);
        }
        if (query != null && !query.isEmpty()) {
          // case of direct search
          directorySC.initSources(!lDomainIds.isEmpty());
          users = directorySC.getUsersByQuery(query, globalSearch);
          destination = doPagination(request, users, directorySC);
        } else {
          destination = getDestination(DirectorySessionController.VIEW_ALL, directorySC, request);
        }
      } else if (function.equalsIgnoreCase(DirectorySessionController.VIEW_ALL)) {

        users = directorySC.getLastListOfAllUsers();
        if (users == null) {
          users = directorySC.getAllUsers();
        }
        destination = doPagination(request, users, directorySC);

      } else if (function.equalsIgnoreCase(DirectorySessionController.VIEW_CONNECTED)) {

        users = directorySC.getConnectedUsers();
        destination = doPagination(request, users, directorySC);

      } else if (isSearchByIndex(function)) {

        users = directorySC.getUsersByIndex(function);
        destination = doPagination(request, users, directorySC);

      } else if ("pagination".equalsIgnoreCase(function) ||
          "ChangeNumberItemsPerPage".equalsIgnoreCase(function)) {

        users = directorySC.getLastListOfUsersCalled();
        destination = doPagination(request, users, directorySC);

      } else if ("Sort".equals(function)) {
        String sort = request.getParameter("Type");
        directorySC.sort(sort);
        users = directorySC.getLastListOfUsersCalled();
        destination = doPagination(request, users, directorySC);
      } else if ("Clear".equals(function)) {
        directorySC.clear();
        destination = getDestination("Main", directorySC, request);
      } else if ("LimitTo".equals(function)) {
        // case of an access to directory but limited to one source
        String limitedToSourceId = request.getParameter("SourceId");
        directorySC.setSelectedSource(limitedToSourceId);
        if ("-1".equals(limitedToSourceId)) {
          users = directorySC.getAllUsers();
        } else if (limitedToSourceId.startsWith("yellow")){
          directorySC.setCurrentDirectory(DirectorySessionController.DIRECTORY_CONTACTS);
          users = directorySC.getContacts(limitedToSourceId, true);
        } else {
          directorySC.setCurrentDomains(Arrays.asList(limitedToSourceId));
          directorySC.setCurrentDirectory(DirectorySessionController.DIRECTORY_DOMAIN);
          users = directorySC.getAllUsersByDomains();
        }
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
  private boolean isSearchByIndex(String lettre) {
    if (lettre != null && lettre.length() == 1) {
      return Character.isLetter(lettre.charAt(0));
    } else {
      return false;
    }
  }

  /**
   * do pagination
   * @param request
   */
  private String doPagination(HttpRequest request, DirectoryItemList users,
      DirectorySessionController directorySC) {
    boolean doNotUseExtraForm = request.getParameterAsBoolean("DoNotUseExtraForm");
    final PaginationPage currentPagination = directorySC.getMemberPage();
    final PaginationPage newPagination = getPaginationPageFrom(request, currentPagination);
    directorySC.setMemberPage(newPagination);

    // setting one fragment per user displayed
    final SilverpeasList<DirectoryItem> membersToDisplay = newPagination.getPaginatedListFrom(users);
    final SilverpeasList<UserFragmentVO> fragments = directorySC.getFragments(membersToDisplay);
    request.setAttribute("UserFragments", fragments);

    request.setAttribute("userTotalNumber", membersToDisplay.originalListSize());
    request.setAttribute("memberPage", directorySC.getMemberPage());
    request.setAttribute("View", directorySC.getCurrentView());
    request.setAttribute("Scope", directorySC.getCurrentDirectory());
    request.setAttribute("Query", directorySC.getCurrentQuery());
    request.setAttribute("Sort", directorySC.getCurrentSort());
    request.setAttribute("ShowHelp", false);
    request.setAttribute("QuickUserSelectionEnabled", directorySC.isQuickUserSelectionEnabled());
    if (directorySC.getCurrentDirectory() == DirectorySessionController.DIRECTORY_DEFAULT ||
        directorySC.getCurrentDirectory() == DirectorySessionController.DIRECTORY_DOMAIN ||
        directorySC.getCurrentDirectory() == DirectorySessionController.DIRECTORY_CONTACTS) {
      request.setAttribute("DirectorySources", directorySC.getDirectorySources());
    }
    SilverpeasComponentInstance component = directorySC.getCurrentComponent();
    if (component != null) {
      doNotUseExtraForm = true;
    }
    if (!doNotUseExtraForm) {
      request.setAttribute("ExtraForm", directorySC.getExtraForm());
      request.setAttribute("ExtraFormContext", directorySC.getExtraFormContext());
    }
    processBreadCrumb(request, directorySC);
    return "/directory/jsp/directory.jsp";
  }

  private void processBreadCrumb(HttpServletRequest request, DirectorySessionController directorySC) {
    int directory = directorySC.getCurrentDirectory();
    StringBuilder breadCrumb =
        new StringBuilder(directorySC.getString("directory.breadcrumb." + directory));
    switch (directory) {
      case DirectorySessionController.DIRECTORY_DEFAULT:
      case DirectorySessionController.DIRECTORY_MINE:
        // do nothing
        break;

      case DirectorySessionController.DIRECTORY_COMMON:
        breadCrumb.append(" ").append(directorySC.getCommonUserDetail().getDisplayedName());
        break;

      case DirectorySessionController.DIRECTORY_OTHER:
        breadCrumb.append(" ").append(directorySC.getOtherUserDetail().getDisplayedName());
        break;

      case DirectorySessionController.DIRECTORY_GROUP:
        breadCrumb.append(" ");
        boolean firstGroup = true;
        for (Group group : directorySC.getCurrentGroups()) {
          if (!firstGroup) {
            breadCrumb.append(" & ");
          }
          breadCrumb.append(group.getName());
          firstGroup = false;
        }
        request.setAttribute("Groups", directorySC.getCurrentGroups());
        break;

      case DirectorySessionController.DIRECTORY_DOMAIN:
        breadCrumb.append(" ");
        boolean first = true;
        for (Domain domain : directorySC.getCurrentDomains()) {
          if (!first) {
            breadCrumb.append(" & ");
          }
          breadCrumb.append(domain.getName());
          first = false;
        }
        request.setAttribute("Domains", directorySC.getCurrentDomains());
        break;

      case DirectorySessionController.DIRECTORY_SPACE:
        breadCrumb.append(" ")
            .append(directorySC.getCurrentSpace().getName(directorySC.getLanguage()));
        break;

      case DirectorySessionController.DIRECTORY_CONTACTS:
      case DirectorySessionController.DIRECTORY_COMPONENT:
        breadCrumb.append(" ")
            .append(directorySC.getCurrentComponent().getLabel(directorySC.getLanguage()));
        break;

      default:
        break;
    }
    request.setAttribute("BreadCrumb", breadCrumb.toString());
  }

  private List<String> processDomains(HttpServletRequest request,
      DirectorySessionController directorySC) {
    String domainId = request.getParameter("DomainId");
    String domainIds = request.getParameter("DomainIds");
    List<String> lDomainIds = new ArrayList<>();
    if (StringUtil.isDefined(domainId)) {
      lDomainIds.add(domainId);
    } else if (StringUtil.isDefined(domainIds)) {
      lDomainIds = Arrays.asList(StringUtil.split(domainIds, ','));
    }
    if (!lDomainIds.isEmpty()) {
      directorySC.setCurrentDomains(lDomainIds);
    }
    return lDomainIds;
  }
}
