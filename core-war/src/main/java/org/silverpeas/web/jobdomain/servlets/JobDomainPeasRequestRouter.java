/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.web.jobdomain.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.DomainDriverManagerProvider;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.SilverpeasTrappedException;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.web.jobdomain.JobDomainPeasException;
import org.silverpeas.web.jobdomain.JobDomainSettings;
import org.silverpeas.web.jobdomain.UserRequestData;
import org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import static org.silverpeas.core.admin.domain.DomainDriver.ActionConstants.*;
import static org.silverpeas.core.web.token.SynchronizerTokenService.SESSION_TOKEN_KEY;

/**
 * Class declaration
 *
 * @author
 */
public class JobDomainPeasRequestRouter extends
    ComponentRequestRouter<JobDomainPeasSessionController> {

  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public JobDomainPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobDomainPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "jobDomainPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobDomainSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, JobDomainPeasSessionController jobDomainSC,
      HttpRequest request) {
    String destination = "";


    try {
      if (!jobDomainSC.isAccessGranted()) {
        throw new JobDomainPeasException("Bad right for user {0}", jobDomainSC.getUserId());
      }
      // 1) Performs the action
      // ----------------------
      if (function.startsWith("selectUserOrGroup")) {
        String id;

        function = "domainContent";
        id = jobDomainSC.getSelectedUserId();
        if (id != null) {
          jobDomainSC.setTargetUser(id);
          function = "userContent";
        } else {
          id = jobDomainSC.getSelectedGroupId();
          if (id != null) {
            jobDomainSC.goIntoGroup(id);
            function = "groupContent";
          }
        }
      }

      if (function.startsWith("Main")) {
        jobDomainSC.returnIntoGroup(null);
        jobDomainSC.setDefaultTargetDomain();
        destination = "jobDomain.jsp";
      } else if ("PreviousUser".equals(function)) {
        UserDetail user = jobDomainSC.getPrevious();
        jobDomainSC.setTargetUser(user.getId());
        destination = "userContent.jsp";
      } else if ("NextUser".equals(function)) {
        UserDetail user = jobDomainSC.getNext();
        jobDomainSC.setTargetUser(user.getId());
        destination = "userContent.jsp";
      } else if (function.startsWith("user")) {
        // USER Actions --------------------------------------------
        String userId = request.getParameter("Iduser");
        if (function.startsWith("userContent")) {
          if (StringUtil.isDefined(userId)) {
            jobDomainSC.setTargetUser(userId);
          }
        } else if ("userGetP12".equals(function)) {
          jobDomainSC.getP12(userId);
        } else if (function.startsWith("userCreate")) {
          UserRequestData userRequestData =
              RequestParameterDecoder.decode(request, UserRequestData.class);

          // process extra properties
          HashMap<String, String> properties = getExtraPropertyValues(request);

          jobDomainSC.createUser(userRequestData, properties, request);

        } else if (function.startsWith("usersCsvImport")) {
          List<FileItem> fileItems = request.getFileItems();
          UserRequestData userRequestData =
              RequestParameterDecoder.decode(request, UserRequestData.class);

          FileItem fileItem = FileUploadUtil.getFile(fileItems, "file_upload");

          if (fileItem != null) {
            jobDomainSC.importCsvUsers(fileItem, userRequestData, request);
          }

          destination = "domainContent.jsp";
        } else if (function.startsWith("userModify")) {
          UserRequestData userRequestData =
              RequestParameterDecoder.decode(request, UserRequestData.class);

          // process extra properties
          HashMap<String, String> properties = getExtraPropertyValues(request);

          jobDomainSC.modifyUser(userRequestData, properties, request);
        } else if (function.startsWith("userBlock")) {
          jobDomainSC.blockUser(userId);
        } else if (function.startsWith("userUnblock")) {
          jobDomainSC.unblockUser(userId);
        } else if (function.startsWith("userDeactivate")) {
          jobDomainSC.deactivateUser(userId);
        } else if (function.startsWith("userActivate")) {
          jobDomainSC.activateUser(userId);
        } else if (function.startsWith("userDelete")) {
          jobDomainSC.deleteUser(userId);
        } else if (function.startsWith("userAvatarDelete")) {
          jobDomainSC.deleteUserAvatar(userId);
        } else if ("userViewRights".equals(function)) {
          request.setAttribute("UserProfiles", jobDomainSC.getCurrentProfiles());
        } else if (function.startsWith("userMS")) {
          UserRequestData userRequestData =
              RequestParameterDecoder.decode(request, UserRequestData.class);

          // process extra properties
          HashMap<String, String> properties = getExtraPropertyValues(request);

          jobDomainSC.modifySynchronizedUser(userRequestData, properties, request);
        } else if (function.startsWith("userSearchToImport")) {
          Map<String, String> query;
          List<UserDetail> users;
          jobDomainSC.clearListSelectedUsers();
          jobDomainSC.setIndexOfFirstItemToDisplay("0");

          String fromArray = request.getParameter("FromArray");
          if (StringUtil.isDefined(fromArray)) {
            query = jobDomainSC.getQueryToImport();
            users = jobDomainSC.getUsersToImport();
          } else {
            query = new Hashtable<>();
            Enumeration<String> parameters = request.getParameterNames();
            String paramName;
            String paramValue;
            while (parameters.hasMoreElements()) {
              paramName = parameters.nextElement();
              if (!paramName.startsWith("Pagination") && !paramName.equals(SESSION_TOKEN_KEY)) {
                paramValue = request.getParameter(paramName);
                if (StringUtil.isDefined(paramValue)) {
                  query.put(paramName, paramValue);
                }
              }
            }

            users = jobDomainSC.searchUsers(query);
          }

          request.setAttribute("Query", query);
          request.setAttribute("Users", users);

          destination = getDestination("displayUserImport", jobDomainSC, request);
        } else if ("userImport".equals(function)) {
          String[] specificIds = request.getParameterValues("specificIds");
          // Massive users import
          if (specificIds != null) {
            processSelection(request, jobDomainSC);
            specificIds = new String[jobDomainSC.getListSelectedUsers().size()];
            jobDomainSC.getListSelectedUsers().toArray(specificIds);
            jobDomainSC.importUsers(specificIds);
          } else {
            // Unitary user Import
            String specificId = request.getParameter("specificIds");
            if (StringUtil.isDefined(specificId)) {
              jobDomainSC.importUser(specificId);
            }
          }
        } else if ("userImportAll".equals(function)) {
          Iterator<UserDetail> usersIt = jobDomainSC.getUsersToImport().iterator();
          ArrayList<String> listSelectedUsersIds = new ArrayList<>();
          while (usersIt.hasNext()) {
            listSelectedUsersIds.add(usersIt.next().getSpecificId());
          }
          jobDomainSC.setListSelectedUsers(listSelectedUsersIds);
          String[] specificIds = new String[jobDomainSC.getListSelectedUsers().size()];
          jobDomainSC.getListSelectedUsers().toArray(specificIds);
          jobDomainSC.importUsers(specificIds);
        } else if ("userView".equals(function)) {
          String specificId = request.getParameter("specificId");

          UserFull user = jobDomainSC.getUser(specificId);

          request.setAttribute("UserFull", user);

          destination = "userView.jsp";
        } else if (function.startsWith("userSynchro")) {
          jobDomainSC.synchroUser(userId);
        } else if (function.startsWith("userUnSynchro")) {
          jobDomainSC.unsynchroUser(userId);
        } else if ("userOpen".equals(function)) {
          userId = request.getParameter("userId");

          UserDetail user = UserDetail.getById(userId);
          String domainId = user.getDomainId();
          if (domainId == null) {
            domainId = Domain.MIXED_DOMAIN_ID;
          }

          // not refresh the domain
          jobDomainSC.setRefreshDomain(false);

          // domaine
          jobDomainSC.setTargetDomain(domainId);

          // réinitialise les groupes
          jobDomainSC.returnIntoGroup(null);

          // groupe d'appartenance
          AdminController adminController = ServiceProvider.getService(AdminController.class);
          List<GroupDetail> groups = adminController.getDirectGroupsOfUser(userId);
          for (final Group group : groups) {
            String groupDomainId = group.getDomainId();
            if (groupDomainId == null) {
              groupDomainId = "-1";
            }
            if (!"-1".equals(groupDomainId)) {
              jobDomainSC.goIntoGroup(group.getId());
              break;
            }
          }

          // user
          jobDomainSC.setTargetUser(userId);
        }
        if (destination.length() <= 0) {
          if (jobDomainSC.getTargetUserDetail() != null) {
            destination = "userContent.jsp";
          } else {
            destination = getDestination("groupContent", jobDomainSC, request);
          }
        }
      } else if (function.startsWith("group")) {
        // GROUP Actions --------------------------------------------
        boolean bHaveToRefreshDomain = false;

        jobDomainSC.setTargetUser(null);

        // Browse functions
        // ----------------
        if (function.startsWith("groupContent")) {
          String groupId = request.getParameter("Idgroup");
          if (StringUtil.isDefined(groupId)) {
            jobDomainSC.goIntoGroup(groupId);
          }
        } else if (function.startsWith("groupExport.txt")) {
          String groupId = request.getParameter("Idgroup");
          if (StringUtil.isDefined(groupId)) {
            jobDomainSC.goIntoGroup(request.getParameter("Idgroup"));
            destination = "exportgroup.jsp";
          }
        } else if (function.startsWith("groupReturn")) {
          jobDomainSC.returnIntoGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupSet")) {
          jobDomainSC.returnIntoGroup(null);
          jobDomainSC.goIntoGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupCreate")) {
          bHaveToRefreshDomain = jobDomainSC.createGroup(request.getParameter("Idparent"),
              WebEncodeHelper.htmlStringToJavaString(request.getParameter("groupName")),
              WebEncodeHelper.htmlStringToJavaString(request.getParameter("groupDescription")),
              request.getParameter("groupRule"));
        } else if (function.startsWith("groupModify")) {
          bHaveToRefreshDomain = jobDomainSC.modifyGroup(request.getParameter("Idgroup"),
              WebEncodeHelper.htmlStringToJavaString(request.getParameter("groupName")),
              WebEncodeHelper.htmlStringToJavaString(request.getParameter("groupDescription")),
              request.getParameter("groupRule"));
        } else if (function.startsWith("groupAddRemoveUsers")) {
          bHaveToRefreshDomain = jobDomainSC
              .updateGroupSubUsers(jobDomainSC.getTargetGroup().getId(), jobDomainSC.getSelectedUsersIds());
        } else if (function.startsWith("groupDelete")) {
          bHaveToRefreshDomain = jobDomainSC.deleteGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupSynchro")) {
          bHaveToRefreshDomain = jobDomainSC.synchroGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupUnSynchro")) {
          bHaveToRefreshDomain = jobDomainSC.unsynchroGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupImport")) {
          bHaveToRefreshDomain = jobDomainSC.importGroup(WebEncodeHelper.htmlStringToJavaString(request.getParameter("groupName")));
        } else if ("groupManagersView".equals(function)) {
          List<List> groupManagers = jobDomainSC.getGroupManagers();

          request.setAttribute("Users", groupManagers.get(0));
          request.setAttribute("Groups", groupManagers.get(1));

          destination = "groupManagers.jsp";
        } else if ("groupManagersChoose".equals(function)) {
          List<String> userIds = (List<String>) StringUtil
              .splitString(request.getParameter("UserPanelCurrentUserIds"), ',');
          List<String> groupIds = (List<String>) StringUtil
              .splitString(request.getParameter("UserPanelCurrentGroupIds"), ',');
          jobDomainSC.initUserPanelForGroupManagers(userIds, groupIds);
          destination = Selection.getSelectionURL();
        } else if ("groupManagersUpdate".equals(function)) {
          List<String> userIds = (List<String>) StringUtil
              .splitString(request.getParameter("roleItems" + "UserPanelCurrentUserIds"), ',');
          List<String> groupIds = (List<String>) StringUtil
              .splitString(request.getParameter("roleItems" + "UserPanelCurrentGroupIds"), ',');
          jobDomainSC.updateGroupProfile(userIds, groupIds);

          destination = getDestination("groupManagersView", jobDomainSC, request);
        } else if ("groupOpen".equals(function)) {
          String groupId = request.getParameter("groupId");

          if (jobDomainSC.isAccessGranted() || jobDomainSC.isGroupManagerOnGroup(groupId)) {
            OrganizationController orgaController = jobDomainSC.getOrganisationController();
            Group group = orgaController.getGroup(groupId);
            String domainId = group.getDomainId();
            if (domainId == null) {
              domainId = "-1";
            }

            // not refresh the domain
            jobDomainSC.setRefreshDomain(false);

            // domaine
            jobDomainSC.setTargetDomain(domainId);
            jobDomainSC.returnIntoGroup(null);

            // groupe(s) père(s)
            List<String> groupList = orgaController.getPathToGroup(groupId);
            for (String elementGroupId : groupList) {
              jobDomainSC.goIntoGroup(elementGroupId);
            }

            // groupe
            jobDomainSC.goIntoGroup(groupId);

            destination = "groupContent.jsp";
          } else {
            destination = "/admin/jsp/accessForbidden.jsp";
          }
        } else if ("groupViewRights".equals(function)) {
          request.setAttribute("GroupProfiles", jobDomainSC.getCurrentProfiles());
        }

        if (destination.length() <= 0) {
          if (jobDomainSC.getTargetGroup() != null) {
            if (bHaveToRefreshDomain) {
              reloadDomainNavigation(request);
            }
            destination = "groupContent.jsp";
          } else if (bHaveToRefreshDomain) {
            destination = getDestination("domainRefresh", jobDomainSC, request);
          } else {
            destination = getDestination("domainContent", jobDomainSC, request);
          }
        }
        // DOMAIN Actions --------------------------------------------
      } else if (function.startsWith("domain")) {
        jobDomainSC.setTargetUser(null);

        if (function.startsWith("domainNavigation")) {
          jobDomainSC.setTargetDomain(request.getParameter("Iddomain"));
          jobDomainSC.returnIntoGroup(null);
          jobDomainSC.setRefreshDomain(true);

          destination = "domainNavigation.jsp";
        } else if (function.startsWith("domainRefreshCurrentLevel")) {
          request.setAttribute("domainRefreshCurrentLevel", true);
          destination = "domainNavigation.jsp";
        } else {
          if (function.startsWith("domainContent")) {
            jobDomainSC.returnIntoGroup(null);
          } else if (function.startsWith("domainCreate")) {
            String newDomainId = jobDomainSC.createDomain(request2Domain(request));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + newDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainSQLCreate")) {
            String newDomainId = jobDomainSC.createSQLDomain(WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainName")),
                WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainDescription")),
                WebEncodeHelper.htmlStringToJavaString(request.getParameter("silverpeasServerURL")),
                request.getParameter("userDomainQuotaMaxCount"));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + newDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainModify")) {
            String modifiedDomainId = jobDomainSC.modifyDomain(request2Domain(request),
                    request.getParameter("userDomainQuotaMaxCount"));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + modifiedDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainSQLModify")) {
            String modifiedDomainId = jobDomainSC.modifySQLDomain(WebEncodeHelper.
                    htmlStringToJavaString(request.getParameter("domainName")), WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainDescription")),
                WebEncodeHelper.htmlStringToJavaString(request.getParameter("silverpeasServerURL")),
                request.getParameter("userDomainQuotaMaxCount"));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + modifiedDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainDelete")) {
            jobDomainSC.deleteDomain();
            request.setAttribute("URLForContent", "domainNavigation");
            destination = "goBack.jsp";
          } else if (function.startsWith("domainSQLDelete")) {
            jobDomainSC.deleteSQLDomain();
            request.setAttribute("URLForContent", "domainNavigation");
            destination = "goBack.jsp";
          } else if (function.startsWith("domainPingSynchro")) {
            if (jobDomainSC.isEnCours()) {
              destination = "domainSynchroPing.jsp";
            } else {
              String strSynchroReport = jobDomainSC.getSynchroReport();

              jobDomainSC.refresh();

              request.setAttribute("SynchroDomainReport", strSynchroReport);
              destination = "domainSynchroReport.jsp";
            }
          } else if (function.startsWith("domainSynchro")) {
            jobDomainSC.synchroDomain(Level.valueOf(request.getParameter("IdTraceLevel")));
            destination = "domainSynchroPing.jsp";
          } else if (function.startsWith("domainSQLSynchro")) {
            jobDomainSC.synchroSQLDomain();
            destination = "domainSynchroPing.jsp";
          } else if (function.startsWith("domainRefresh")) {
            request.setAttribute("URLForContent",
                "domainNavigation?Iddomain=" + jobDomainSC.getTargetDomain().getId());
            destination = "goBack.jsp";
          }

          if (destination.length() <= 0) {
            if (jobDomainSC.getTargetDomain() != null) {
              destination = "domainContent.jsp";
            } else {
              destination = getDestination("welcome", jobDomainSC, request);
            }
          }
        }
      } else if (function.startsWith("display")) {
        if (function.startsWith("displayGroupCreate")) {
          GroupDetail newGroup = new GroupDetail();

          newGroup.setSuperGroupId(request.getParameter("Idgroup"));
          request.setAttribute("groupObject", newGroup);
          request.setAttribute("action", "groupCreate");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.groupAdd") + "..."));
          destination = "groupCreate.jsp";
        } else if (function.startsWith("displayGroupModify")) {
          request.setAttribute("groupObject", jobDomainSC.getTargetGroup());
          request.setAttribute("action", "groupModify");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.groupUpdate") + "..."));
          destination = "groupCreate.jsp";
        } else if (function.startsWith("displayGroupImport")) {
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute
                  ("myComponentURL"),
              jobDomainSC.getString("JDP.groupImport") + "..."));
          destination = "groupImport.jsp";
        } else if (function.startsWith("displaySelectUserOrGroup")) {
          destination = jobDomainSC.initSelectionPeasForOneGroupOrUser((String) request.
              getAttribute("myComponentURL"));
        } else if (function.startsWith("displayAddRemoveUsers")) {
          destination = jobDomainSC.initSelectionPeasForGroups((String) request.getAttribute("myComponentURL"));
        } else if (function.startsWith("displayUserCreate")) {
          DomainDriverManager domainDriverManager =
              DomainDriverManagerProvider.getCurrentDomainDriverManager();
          DomainDriver domainDriver =
              domainDriverManager.getDomainDriver(jobDomainSC.getTargetDomain().getId());
          UserFull newUser = new UserFull(domainDriver);
          newUser.setPasswordAvailable(true);

          request.setAttribute("userObject", newUser);
          request.setAttribute("action", "userCreate");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.userAdd") + "..."));
          request.setAttribute("minLengthLogin", jobDomainSC.getMinLengthLogin());
          request.setAttribute("CurrentUser", jobDomainSC.getUserDetail());
          // if community management is activated, add groups on this user is manager
          if (JobDomainSettings.m_UseCommunityManagement) {
            request.setAttribute("GroupsManagedByCurrentUser", jobDomainSC.getUserManageableGroups());
          }

          destination = "userCreate.jsp";
        } else if (function.startsWith("displayUsersCsvImport")) {
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute
                  ("myComponentURL"),
              jobDomainSC.getString("JDP.csvImport") + "..."));
          destination = "usersCsvImport.jsp";
        } else if (function.startsWith("displayUserModify")) {
          request.setAttribute("userObject", jobDomainSC.getTargetUserFull());
          request.setAttribute("action", "userModify");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.userUpdate") + "..."));
          request.setAttribute("minLengthLogin", jobDomainSC.getMinLengthLogin());
          request.setAttribute("CurrentUser", jobDomainSC.getUserDetail());

          destination = "userCreate.jsp";
        } else if (function.startsWith("displayUserMS")) {
          request.setAttribute("userObject", jobDomainSC.getTargetUserFull());
          request.setAttribute("action", "userMS");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.userUpdate") + "..."));
          request.setAttribute("minLengthLogin", jobDomainSC.getMinLengthLogin());
          request.setAttribute("CurrentUser", jobDomainSC.getUserDetail());

          destination = "userCreate.jsp";
        } else if (function.startsWith("displayUserImport")) {
          request.setAttribute("SelectedIds", jobDomainSC.getListSelectedUsers());
          request.setAttribute("FirstUserIndex", jobDomainSC.
              getIndexOfFirstItemToDisplay());
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.userImport") + "..."));
          request.setAttribute("properties", jobDomainSC.getPropertiesToImport());
          destination = "userImport.jsp";
        } else if (function.startsWith("displayDomainCreate")) {
          Domain theNewDomain = new Domain();
          theNewDomain.setDriverClassName("org.silverpeas.core.admin.domain.driver.ldapdriver.LDAPDriver");
          theNewDomain.setPropFileName("org.silverpeas.domains.domain");
          theNewDomain.setAuthenticationServer("autDomain");
          request.setAttribute("domainObject", theNewDomain);
          request.setAttribute("action", "domainCreate");
          destination = "domainCreate.jsp";
        } else if (function.startsWith("displayDomainSQLCreate")) {
          Domain theNewDomain = new Domain();
          request.setAttribute("domainObject", theNewDomain);
          request.setAttribute("action", "domainSQLCreate");
          destination = "domainSQLCreate.jsp";
        } else if (function.startsWith("displayDomainModify")) {
          request.setAttribute("action", "domainModify");
          destination = "domainCreate.jsp";
        } else if (function.startsWith("displayDomainSQLModify")) {
          request.setAttribute("action", "domainSQLModify");
          destination = "domainSQLCreate.jsp";
        } else if (function.startsWith("displayDomainSynchro")) {
          destination = "domainSynchro.jsp";
        } else if (function.startsWith("displayDynamicSynchroReport")) {
          SynchroDomainReport.setReportLevel(Level.valueOf(request.getParameter("IdTraceLevel")));
          destination = "dynamicSynchroReport.jsp";
        }
      } else if (function.startsWith("welcome")) {
        jobDomainSC.returnIntoGroup(null);
        request.setAttribute("DisplayOperations", jobDomainSC.getUserDetail().isAccessAdmin());

        SettingBundle rs = ResourceLocator.getSettingBundle("org.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings");
        Properties configuration = new Properties();
        configuration
            .setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString("templatePath"));
        configuration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString("customersTemplatePath"));
        SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);

        // setting domains to welcome template
        List<Domain> allDomains = jobDomainSC.getAllDomains();
        // do not return mixed domain
        String[] domainsByList = new String[allDomains.size() - 1];
        for (int n = 1; n < allDomains.size(); n++) {
          domainsByList[n - 1] = allDomains.get(n).getName();
        }
        template.setAttribute("listDomains", domainsByList);
        request.setAttribute("Content",
            template.applyFileTemplate("register_" + jobDomainSC.getLanguage()));

        destination = "welcome.jsp";
      } else if ("Pagination".equals(function)) {
        processSelection(request, jobDomainSC);

        // traitement de la pagination : passage des parametres
        String index = request.getParameter("Pagination_Index");

        if (index != null && index.length() > 0) {
          jobDomainSC.setIndexOfFirstItemToDisplay(index);
        }
        // retour a l'album courant
        request.setAttribute("Query", jobDomainSC.getQueryToImport());
        request.setAttribute("Users", jobDomainSC.getUsersToImport());
        destination = getDestination("displayUserImport", jobDomainSC, request);
      } else if ("SelectRightsUserOrGroup".equals(function)) {
        destination = jobDomainSC.initSelectionRightsUserOrGroup();
      } else if ("AssignSameRights".equals(function)) {
        if (!jobDomainSC.isRightCopyReplaceEnabled()) {
          throwHttpForbiddenError();
        }
        //1 = replace rights | 2 = add rights
        String choiceAssignRights = request.getParameter("choiceAssignRights");
        String sourceRightsId = request.getParameter("sourceRightsId");
        //Set | Element
        String sourceRightsType = request.getParameter("sourceRightsType");
        //true | false
        boolean nodeAssignRights = request.getParameterAsBoolean("nodeAssignRights");

        jobDomainSC
              .assignRights(choiceAssignRights, sourceRightsId, sourceRightsType, nodeAssignRights);

        if (jobDomainSC.getTargetUserDetail() != null) {
          destination = "userContent.jsp";
        } else {
          destination = "groupContent.jsp";
        }
      } else {
        destination = function;
      }

      // 2) Prepare the pages
      // --------------------
      if (jobDomainSC.getTargetDomain() != null) {
        request.setAttribute("domainObject", jobDomainSC.getTargetDomain());
      }
      if ("domainContent.jsp".equals(destination)) {
        jobDomainSC.refresh();
        long domainRight = jobDomainSC.getDomainActions();
        request.setAttribute("theUser", jobDomainSC.getUserDetail());
        request.setAttribute("subGroups", jobDomainSC.getSubGroups(false));
        request.setAttribute("subUsers", jobDomainSC.getSubUsers(false));
        request.setAttribute("isDomainRW", ((domainRight & ACTION_CREATE_GROUP) != 0)
            || ((domainRight & ACTION_CREATE_USER) != 0));
        request.setAttribute("isUserRW", (domainRight & ACTION_CREATE_USER) != 0);
        request.setAttribute("isDomainSync",
            ((domainRight & ACTION_SYNCHRO_USER) != 0)
            || ((domainRight & ACTION_SYNCHRO_GROUP) != 0));

        request.setAttribute("isOnlyGroupManager", jobDomainSC.isOnlyGroupManager());
        request.setAttribute("isUserAddingAllowedForGroupManager", jobDomainSC.
            isUserAddingAllowedForGroupManager());
      } else if ("groupContent.jsp".equals(destination) || "exportgroup.jsp".equals(destination)) {
        long domainRight = jobDomainSC.getDomainActions();

        request.setAttribute("groupObject", jobDomainSC.getTargetGroup());
        request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
            "myComponentURL"), null));
        request.setAttribute("subGroups", jobDomainSC.getSubGroups(true));
        request.setAttribute("subUsers", jobDomainSC.getSubUsers(true));
        request.setAttribute("isDomainRW", ((domainRight & ACTION_CREATE_GROUP) != 0)
            || ((domainRight & ACTION_CREATE_USER) != 0));
        request.setAttribute("isUserRW", (domainRight & ACTION_CREATE_USER) != 0);
        request.setAttribute("isDomainSync",
            ((domainRight & ACTION_SYNCHRO_USER) != 0)
            || ((domainRight & ACTION_SYNCHRO_GROUP) != 0));

        request
            .setAttribute("isGroupManagerOnThisGroup", jobDomainSC.isGroupManagerOnCurrentGroup());
        request.setAttribute("isGroupManagerDirectlyOnThisGroup", jobDomainSC.
            isGroupManagerDirectlyOnCurrentGroup());
        request.setAttribute("isOnlyGroupManager", jobDomainSC.isOnlyGroupManager());

        request.setAttribute("ManageableSpaces", jobDomainSC.getManageablesSpaces());
        request.setAttribute("IsRightCopyReplaceEnabled",
            jobDomainSC.isRightCopyReplaceEnabled());
      } else if ("userContent.jsp".equals(destination)) {
        request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
            "myComponentURL"), null));

        if (jobDomainSC.getTargetDomain() != null) {
          long domainRight = jobDomainSC.getDomainActions();

          request.setAttribute("isDomainRW",
              ((domainRight & ACTION_CREATE_GROUP) != 0)
              || ((domainRight & ACTION_CREATE_USER) != 0));
          request.setAttribute("isUserRW", (domainRight & ACTION_CREATE_USER) != 0);
          request.setAttribute("isDomainSync",
              ((domainRight & ACTION_SYNCHRO_USER) != 0)
              || ((domainRight & ACTION_SYNCHRO_GROUP) != 0));
          request.setAttribute("isX509Enabled", (domainRight & ACTION_X509_USER) != 0);
          request.setAttribute("isOnlyGroupManager", jobDomainSC.isOnlyGroupManager());
          request.setAttribute("userManageableByGroupManager", jobDomainSC.
              isUserInAtLeastOneGroupManageableByCurrentUser());
        }
        request.setAttribute("userObject", jobDomainSC.getTargetUserFull());
        request.setAttribute("Index", jobDomainSC.getIndex());
        request.setAttribute("UserGroups", jobDomainSC.getCurrentUserGroups());
        request.setAttribute("UserManageableSpaces", jobDomainSC.getManageablesSpaces());
        request.setAttribute("UserManageableGroups", jobDomainSC.getManageablesGroups());
        request.setAttribute("IsRightCopyReplaceEnabled",
            jobDomainSC.isRightCopyReplaceEnabled());

      } else if ("domainNavigation.jsp".equals(destination)) {
        List<Domain> domains = jobDomainSC.getAllDomains();
        if (domains.size() == 1) {
          jobDomainSC.setTargetDomain(domains.get(0).getId());
        }
        request.setAttribute("allDomains", domains);
        request.setAttribute("allRootGroups", jobDomainSC.getAllRootGroups());
        request.setAttribute("CurrentDomain", jobDomainSC.getTargetDomain());
        if (jobDomainSC.getTargetDomain() != null) {
          request.setAttribute("URLForContent", "domainContent");
        } else {
          request.setAttribute("URLForContent", "welcome");
        }
      } else if ("groupManagers.jsp".equals(destination)) {
        request.setAttribute("groupObject", jobDomainSC.getTargetGroup());
        request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
            "myComponentURL"), null));
      }
      // 3) Concat the path
      // ------------------
      if (!destination.startsWith("/")) {
        destination = "/jobDomainPeas/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      if (e instanceof SilverpeasTrappedException) {
        destination = "/admin/jsp/errorpageTrapped.jsp";
      } else {
        destination = "/admin/jsp/errorpageMain.jsp";
      }
    }


    return destination;
  }

  /**
   * Marks into the request an attribute that indicates the domain navigation frame has to be
   * reloaded.
   * @param request the request to mark.
   */
  private void reloadDomainNavigation(final HttpRequest request) {
    request.setAttribute("reloadDomainNavigationFrame", true);
  }

  private void processSelection(HttpServletRequest request,
      JobDomainPeasSessionController jobDomainSC) {
    String selectedIds = request.getParameter("Pagination_SelectedIds");
    String notSelectedIds = request.getParameter("Pagination_NotSelectedIds");
    List<String> memSelected = jobDomainSC.getListSelectedUsers();
    StringTokenizer st = new StringTokenizer(selectedIds, ",");
    while (st.hasMoreTokens()) {
      String id = st.nextToken();
      memSelected.add(id);
    }

    st = new StringTokenizer(notSelectedIds, ",");
    while (st.hasMoreTokens()) {
      String id = st.nextToken();
      memSelected.remove(id);
    }
    jobDomainSC.setListSelectedUsers(memSelected);
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, String> getExtraPropertyValues(HttpServletRequest request) {
    // process extra properties
    HashMap<String, String> properties = new HashMap<>();
    Enumeration<String> parameters = request.getParameterNames();
    while (parameters.hasMoreElements()) {
      String parameterName = parameters.nextElement();
      if (parameterName.startsWith("prop_")) {
        // remove "prop_"
        String property = parameterName.substring(5, parameterName.length());
        properties.put(property, request.getParameter(parameterName));
      }
    }
    return properties;
  }

  private Domain request2Domain(HttpRequest request) {
    String name = WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainName"));
    String desc = WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainDescription"));
    String driver = WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainDriver"));
    String properties = WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainProperties"));
    String authent = WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainAuthentication"));
    String url = WebEncodeHelper.htmlStringToJavaString(request.getParameter("silverpeasServerURL"));
    String timeStamp = WebEncodeHelper.htmlStringToJavaString(request.getParameter("domainTimeStamp"));

    Domain domain = new Domain();
    domain.setName(name);
    domain.setDescription(desc);
    domain.setDriverClassName(driver);
    domain.setPropFileName(properties);
    domain.setAuthenticationServer(authent);
    domain.setSilverpeasServerURL(url);
    domain.setTheTimeStamp(timeStamp);

    return domain;
  }

}
