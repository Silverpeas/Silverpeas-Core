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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.directory.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.directory.control.DirectorySessionController;
import com.silverpeas.directory.model.Member;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;

/**
 * @author azzedine
 */
public class DirectoryRequestRouter extends ComponentRequestRouter {

  private static int ELEMENTS_PER_PAGE = 2;
  public static final String AVATAR_FOLDER = "avatar";
  
  private static final long serialVersionUID = -1683812983096083815L;

  @Override
  public String getSessionControlBeanName() {
    return "directory";
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new DirectorySessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {

    String destination = "";

    try {
      ELEMENTS_PER_PAGE =
          Integer.parseInt(componentSC.getSettings().getString("ELEMENTS_PER_PAGE"));
    } catch (NumberFormatException nfex) {
    }

    DirectorySessionController directorySC = (DirectorySessionController) componentSC;

    SilverTrace.info("mytests", "DirectoryRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE",
        "User=" + componentSC.getUserId() + " Function=" + function);
    List<UserDetail> users = new ArrayList<UserDetail>();
    request.setAttribute("MyId", componentSC.getUserId());
    if (function.equalsIgnoreCase("Main")) {

      String groupId = request.getParameter("GroupId");
      String spaceId = request.getParameter("SpaceId");
      String domainId = request.getParameter("DomainId");

      if (StringUtil.isDefined(groupId)) {
        users = directorySC.getAllUsersByGroup(groupId);
      } else if (StringUtil.isDefined(spaceId)) {
        users = directorySC.getAllUsersBySpace(spaceId);
      } else if (StringUtil.isDefined(domainId)) {
        users = directorySC.getAllUsersByDomain(domainId);
      } else {
        users = directorySC.getAllUsers();
      }

      destination = doPagination(request, users);
    } else if (function.equalsIgnoreCase("searchByKey")) {

      users = directorySC.getUsersByLastName(request.getParameter("key").toUpperCase());
      destination = doPagination(request, users);

    } else if (function.equalsIgnoreCase("tous")) {

      request.setAttribute("Index", function);
      users = directorySC.getLastListOfAllUsers();
      destination = doPagination(request, users);

    } else if (isSearchByIndex(function)) {

      request.setAttribute("Index", function);
      users = directorySC.getUsersByIndex(function);
      destination = doPagination(request, users);

    } else if (function.equalsIgnoreCase("pagination")) {

      users = directorySC.getLastListOfUsersCallded();
      destination = doPagination(request, users);

    } else if (function.equalsIgnoreCase("viewUser")) {
      destination = "/Rprofil/jsp/Main?userId=" + request.getParameter("UserId");

    } else if (function.equalsIgnoreCase("NotificationView")) {
      String userId = request.getParameter("Recipient");
      request.setAttribute("User", new Member(directorySC.getUserDetail(userId)));
      destination = "/directory/jsp/notificationUser.jsp";

    } else if ("ProfilPublic".equalsIgnoreCase(function)) {

      destination = "/directory/jsp/profilPublic.jsp";
    } else if ("createPhoto".equalsIgnoreCase(function)) {
      String path = request.getParameter("Photo");
      request.setAttribute("path", path);
      destination = "/directory/jsp/createPhoto.jsp";
    } else if ("validation".equalsIgnoreCase(function)) {
      try {
        UserDetail user = directorySC.getUserDetail();
        request.setAttribute("user", user);
        destination = "/directory/jsp/" + this.saveAvatar(request, directorySC);
      } catch (UtilException e) {
        SilverTrace.error("directory", "DirectoryRequestRouter.validation", "ERROR", e);
      } catch (IOException e) {
        SilverTrace.error("directory", "DirectoryRequestRouter.validation", "ERROR", e);
      }
    }
    return destination;

  }

  /**
   *return true if this searche by index
   */
  boolean isSearchByIndex(String lettre) {
    if (lettre != null && lettre.length() == 1) {
      return Character.isLetter(lettre.charAt(0));// return true if "lettre is Letrre
    } else {
      return false;
    }
  }

  /**
   *tronsform list of UserDetail to list of Membre
   *@param List<UserDetail>
   */
  List<Member> toListMember(List<UserDetail> uds) {
    List<Member> listMember = new ArrayList<Member>();

    for (UserDetail varUD : uds) {

      listMember.add(new Member(varUD));

    }
    return listMember;
  }

  /**
   *do pagination
   *@param HttpServletRequest request
   */
  String doPagination(HttpServletRequest request, List<UserDetail> users) {
    int index = 0;
    if (StringUtil.isInteger(request.getParameter("Index"))) {
      index = Integer.parseInt(request.getParameter("Index"));
    }
    HttpSession session = request.getSession();
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
    Pagination pagination = gef.getPagination(users.size(), ELEMENTS_PER_PAGE, index);
    List<Member> membersToDisplay = new ArrayList<Member>();
    membersToDisplay = toListMember(users.subList(pagination.getFirstItemIndex(), pagination.
        getLastItemIndex()));
    request.setAttribute("Members", membersToDisplay);
    request.setAttribute("pagination", pagination);
    return "/directory/jsp/directory.jsp";
  }

  protected String saveAvatar(HttpServletRequest request, DirectorySessionController directorySC)
      throws IOException, UtilException {
    List<FileItem> parameters = FileUploadUtil.parseRequest(request);
    FileItem file = FileUploadUtil.getFile(parameters, "WAIMGVAR0");
    String avatar = directorySC.getPhoto(file.getName());
    ImageProfil img = new ImageProfil(avatar, AVATAR_FOLDER);
    img.saveImage(file.getInputStream());
    String vignette_url = "/display/avatar/" + avatar;
    request.setAttribute("vignette_url", vignette_url);

    return "test.jsp";
  }
}
