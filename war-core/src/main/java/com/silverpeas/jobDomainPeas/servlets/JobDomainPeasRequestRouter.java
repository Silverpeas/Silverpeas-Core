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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jobDomainPeas.servlets;

import com.silverpeas.jobDomainPeas.JobDomainPeasException;
import com.silverpeas.jobDomainPeas.JobDomainSettings;
import com.silverpeas.jobDomainPeas.control.JobDomainPeasSessionController;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.DomainDriver;
import com.stratelia.webactiv.beans.admin.DomainDriverManager;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasTrappedException;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Class declaration
 * @author
 */
public class JobDomainPeasRequestRouter extends
    ComponentRequestRouter<JobDomainPeasSessionController> {

  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
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
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobDomainSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, JobDomainPeasSessionController jobDomainSC,
      HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("jobDomainPeas", "JobDomainPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + jobDomainSC.getUserId() + " Function=" + function);

    try {
      if (!jobDomainSC.isAccessGranted()) {
        throw new JobDomainPeasException("JobDomainPeasRequestRouter.getDestination",
            SilverpeasException.ERROR, "root.EX_BAD_USER_RIGHT", "MODULE JOBDOMAIN : user "
            + jobDomainSC.getUserId());
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
      } // USER Actions --------------------------------------------
      else if (function.startsWith("user")) {
        if (function.startsWith("userContent")) {
          if ((request.getParameter("Iduser") != null)
              && (request.getParameter("Iduser").length() > 0)) {
            jobDomainSC.setTargetUser(request.getParameter("Iduser"));
          }
        } else if (function.equals("userGetP12")) {
          String userId = request.getParameter("Iduser");
          jobDomainSC.getP12(userId);
        } else if (function.startsWith("userCreate")) {
          boolean userPasswordValid = false;
          if ((request.getParameter("userPasswordValid") != null)
              && (request.getParameter("userPasswordValid").equals("true"))) {
            userPasswordValid = true;
          }

          // process extra properties
          HashMap<String, String> properties = getExtraPropertyValues(request);

          String sendEmailParam = request.getParameter("sendEmail");
          boolean sendEmail =
              (StringUtil.isDefined(sendEmailParam) && "true".equals(sendEmailParam));

          jobDomainSC.createUser(
              EncodeHelper.htmlStringToJavaString(request.getParameter("userLogin")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userLastName")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userFirstName")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userEMail")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userAccessLevel")),
              userPasswordValid,
              EncodeHelper.htmlStringToJavaString(request.getParameter("userPassword")),
              properties, request.getParameter("GroupId"), request, sendEmail);

        } else if (function.startsWith("usersCsvImport")) {
          List<FileItem> fileItems = FileUploadUtil.parseRequest(request);

          FileItem fileItem = FileUploadUtil.getFile(fileItems, "file_upload");
          String sendEmailParam = FileUploadUtil.getParameter(fileItems, "sendEmail");
          boolean sendEmail =
              (StringUtil.isDefined(sendEmailParam) && "true".equals(sendEmailParam));

          if (fileItem != null) {
            jobDomainSC.importCsvUsers(fileItem, sendEmail, request);
          }

          destination = "domainContent.jsp";
        } else if (function.startsWith("userModify")) {
          boolean userPasswordValid = false;
          if ((request.getParameter("userPasswordValid") != null)
              && (request.getParameter("userPasswordValid").equals("true"))) {
            userPasswordValid = true;
          }

          // process extra properties
          HashMap<String, String> properties = getExtraPropertyValues(request);

          String sendEmailParam = request.getParameter("sendEmail");
          boolean sendEmail =
              (StringUtil.isDefined(sendEmailParam) && "true".equals(sendEmailParam));

          jobDomainSC.modifyUser(request.getParameter("Iduser"),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userLastName")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userFirstName")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userEMail")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("userAccessLevel")),
              userPasswordValid,
              EncodeHelper.htmlStringToJavaString(request.getParameter("userPassword")),
              properties, request, sendEmail);
        } else if (function.startsWith("userDelete")) {
          jobDomainSC.deleteUser(request.getParameter("Iduser"));
        } else if (function.startsWith("userMS")) {
          String userId = request.getParameter("Iduser");
          String accessLevel = request.getParameter("userAccessLevel");

          // process extra properties
          HashMap<String, String> properties = getExtraPropertyValues(request);

          if (properties.isEmpty()) {
            jobDomainSC.modifySynchronizedUser(userId, accessLevel);
          } else {
            // extra properties have been set, modify user full
            jobDomainSC.modifyUserFull(userId, accessLevel, properties);
          }
        } else if (function.startsWith("userSearchToImport")) {
          Hashtable<String, String> query;
          List<UserDetail> users;
          jobDomainSC.clearListSelectedUsers();
          jobDomainSC.setIndexOfFirstItemToDisplay("0");

          String fromArray = request.getParameter("FromArray");
          if (StringUtil.isDefined(fromArray)) {
            query = jobDomainSC.getQueryToImport();
            users = jobDomainSC.getUsersToImport();
          } else {
            query = new Hashtable<String, String>();
            Enumeration<String> parameters = request.getParameterNames();
            String paramName;
            String paramValue;
            while (parameters.hasMoreElements()) {
              paramName = parameters.nextElement();
              if (!paramName.startsWith("Pagination")) {
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

          destination = getDestination("displayUserImport", jobDomainSC,
              request);
        } else if (function.equals("userImport")) {
          String[] specificIds = request.getParameterValues("specificIds");
          // Massive users import
          if (specificIds != null) {
            processSelection(request, jobDomainSC);
            specificIds = new String[jobDomainSC.getListSelectedUsers().size()];
            jobDomainSC.getListSelectedUsers().toArray(specificIds);
            jobDomainSC.importUsers(specificIds);
          } // Unitary user Import
          else {
            String specificId = request.getParameter("specificIds");

            if (StringUtil.isDefined(specificId)) {
              jobDomainSC.importUser(specificId);
            }
          }
        } else if (function.equals("userImportAll")) {
          Iterator<UserDetail> usersIt = jobDomainSC.getUsersToImport().iterator();
          ArrayList<String> listSelectedUsersIds = new ArrayList<String>();
          while (usersIt.hasNext()) {
            listSelectedUsersIds.add(usersIt.next().getSpecificId());
          }
          jobDomainSC.setListSelectedUsers(listSelectedUsersIds);
          String[] specificIds = new String[jobDomainSC.getListSelectedUsers().size()];
          jobDomainSC.getListSelectedUsers().toArray(specificIds);
          jobDomainSC.importUsers(specificIds);
        } else if (function.equals("userView")) {
          String specificId = request.getParameter("specificId");

          UserFull user = jobDomainSC.getUser(specificId);

          request.setAttribute("UserFull", user);

          destination = "userView.jsp";
        } else if (function.startsWith("userSynchro")) {
          jobDomainSC.synchroUser(request.getParameter("Iduser"));
        } else if (function.startsWith("userUnSynchro")) {
          jobDomainSC.unsynchroUser(request.getParameter("Iduser"));
        } else if (function.equals("userOpen")) {
          String userId = request.getParameter("userId");

          OrganizationController orgaController = jobDomainSC.getOrganizationController();
          UserDetail user = orgaController.getUserDetail(userId);
          String domainId = user.getDomainId();
          if (domainId == null) {
            domainId = "-1";
          }

          // not refresh the domain
          jobDomainSC.setRefreshDomain(false);

          // domaine
          jobDomainSC.setTargetDomain(domainId);

          // réinitialise les groupes
          jobDomainSC.returnIntoGroup(null);

          // groupe d'appartenance
          AdminController adminController = new AdminController(jobDomainSC.getUserId());
          String[] groupIds = adminController.getDirectGroupsIdsOfUser(userId);
          if (groupIds != null && groupIds.length > 0) {
            for (final String groupId : groupIds) {
              Group group = orgaController.getGroup(groupId);

              String groupDomainId = group.getDomainId();
              if (groupDomainId == null) {
                groupDomainId = "-1";
              }
              if (!groupDomainId.equals("-1")) {
                jobDomainSC.goIntoGroup(group.getId());
                break;
              }
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
      } // GROUP Actions --------------------------------------------
      else if (function.startsWith("group")) {
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
        } // Operation functions
        else if (function.startsWith("groupCreate")) {
          bHaveToRefreshDomain = jobDomainSC.createGroup(request.getParameter("Idparent"),
              EncodeHelper.htmlStringToJavaString(request.getParameter("groupName")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("groupDescription")),
              request.getParameter("groupRule"));
        } else if (function.startsWith("groupModify")) {
          bHaveToRefreshDomain = jobDomainSC.modifyGroup(request.getParameter("Idgroup"),
              EncodeHelper.htmlStringToJavaString(request.getParameter("groupName")),
              EncodeHelper.htmlStringToJavaString(request.getParameter("groupDescription")),
              request.getParameter("groupRule"));
        } else if (function.startsWith("groupAddRemoveUsers")) {
          bHaveToRefreshDomain = jobDomainSC.updateGroupSubUsers(
              jobDomainSC.getTargetGroup().getId(), jobDomainSC.getSelectedUsersIds());
        } else if (function.startsWith("groupDelete")) {
          bHaveToRefreshDomain = jobDomainSC.deleteGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupSynchro")) {
          bHaveToRefreshDomain = jobDomainSC.synchroGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupUnSynchro")) {
          bHaveToRefreshDomain = jobDomainSC.unsynchroGroup(request.getParameter("Idgroup"));
        } else if (function.startsWith("groupImport")) {
          bHaveToRefreshDomain =
              jobDomainSC.importGroup(EncodeHelper.htmlStringToJavaString(request.
              getParameter("groupName")));
        } else if (function.equals("groupManagersView")) {
          List<List> groupManagers = jobDomainSC.getGroupManagers();

          request.setAttribute("Users", groupManagers.get(0).iterator());
          request.setAttribute("Groups", groupManagers.get(1).iterator());

          destination = "groupManagers.jsp";
        } else if (function.equals("groupManagersChoose")) {
          try {
            jobDomainSC.initUserPanelForGroupManagers(
                (String) request.getAttribute("myComponentURL"));
          } catch (Exception e) {
            SilverTrace.warn("jobStartPagePeas",
                "JobStartPagePeasRequestRouter.getDestination()",
                "root.EX_USERPANEL_FAILED", "function = " + function, e);
          }
          destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
        } else if (function.equals("groupManagersDelete")) {
          jobDomainSC.deleteGroupProfile();
          destination = getDestination("groupManagersView", jobDomainSC,
              request);
        } else if (function.equals("groupManagersUpdate")) {
          jobDomainSC.updateGroupProfile();

          request.setAttribute("urlToReload", "groupManagersView");
          destination = "closeWindow.jsp";
        } else if (function.equals("groupManagersCancel")) {
          request.setAttribute("urlToReload", "groupManagersView");
          destination = "closeWindow.jsp";
        } else if (function.equals("groupOpen")) {
          String groupId = request.getParameter("groupId");

          if (jobDomainSC.isAccessGranted() || jobDomainSC.isGroupManagerOnGroup(groupId)) {
            OrganizationController orgaController = jobDomainSC.getOrganizationController();
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
        }

        if (destination.length() <= 0) {
          if (bHaveToRefreshDomain) {
            destination = getDestination("domainRefresh", jobDomainSC, request);
          } else if (jobDomainSC.getTargetGroup() != null) {
            destination = "groupContent.jsp";
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
        } else {
          if (function.startsWith("domainContent")) {
            jobDomainSC.returnIntoGroup(null);
          } // Operation functions
          else if (function.startsWith("domainCreate")) {
            String newDomainId =
                jobDomainSC.createDomain(EncodeHelper.htmlStringToJavaString(request
                .getParameter("domainName")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainDescription")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainDriver")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainProperties")),
                EncodeHelper.htmlStringToJavaString(request
                .getParameter("domainAuthentication")),
                EncodeHelper
                .htmlStringToJavaString(request.getParameter("silverpeasServerURL")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainTimeStamp")));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + newDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainSQLCreate")) {
            String newDomainId =
                jobDomainSC.createSQLDomain(EncodeHelper.htmlStringToJavaString(request
                    .getParameter("domainName")), EncodeHelper.htmlStringToJavaString(request
                    .getParameter("domainDescription")), EncodeHelper
                    .htmlStringToJavaString(request.getParameter("silverpeasServerURL")), request
                    .getParameter("userDomainQuotaMaxCount"));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + newDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainModify")) {
            String modifiedDomainId =
                jobDomainSC.modifyDomain(EncodeHelper.htmlStringToJavaString(request.
                getParameter("domainName")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainDescription")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainDriver")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainProperties")),
                EncodeHelper
                .htmlStringToJavaString(request.getParameter("domainAuthentication")),
                EncodeHelper
                .htmlStringToJavaString(request.getParameter("silverpeasServerURL")),
                EncodeHelper.htmlStringToJavaString(request.getParameter("domainTimeStamp")));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain=" + modifiedDomainId);
            destination = "goBack.jsp";
          } else if (function.startsWith("domainSQLModify")) {
            String modifiedDomainId =
                jobDomainSC.modifySQLDomain(EncodeHelper.htmlStringToJavaString(request
                    .getParameter("domainName")), EncodeHelper.htmlStringToJavaString(request
                    .getParameter("domainDescription")), EncodeHelper
                    .htmlStringToJavaString(request.getParameter("silverpeasServerURL")), request
                    .getParameter("userDomainQuotaMaxCount"));
            request.setAttribute("URLForContent", "domainNavigation?Iddomain="
                + modifiedDomainId);
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
              SilverTrace.info("jobDomainPeas", "JobDomainPeasRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "SynchroReport=" + strSynchroReport);
              request.setAttribute("SynchroReport", strSynchroReport);
              destination = "domainSynchroReport.jsp";
            }
          } else if (function.startsWith("domainSynchro")) {
            jobDomainSC.synchroDomain(Integer.parseInt(request.getParameter("IdTraceLevel")));
            destination = "domainSynchroPing.jsp";
          } else if (function.startsWith("domainSQLSynchro")) {
            jobDomainSC.synchroSQLDomain();
            destination = "domainSynchroPing.jsp";
          } else if (function.startsWith("domainRefresh")) {
            request.setAttribute("URLForContent", "domainNavigation?Iddomain="
                + jobDomainSC.getTargetDomain().getId());
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
          Group newGroup = new Group();

          newGroup.setSuperGroupId(request.getParameter("Idgroup"));
          request.setAttribute("groupObject", newGroup);
          request.setAttribute("action", "groupCreate");
          request.setAttribute("groupsPath", jobDomainSC.getPath(
              (String) request.getAttribute("myComponentURL"), jobDomainSC
              .getString("JDP.groupAdd")
              +
              "..."));
          destination = "groupCreate.jsp";
        } else if (function.startsWith("displayGroupModify")) {
          request.setAttribute("groupObject", jobDomainSC.getTargetGroup());
          request.setAttribute("action", "groupModify");
          request.setAttribute("groupsPath", jobDomainSC.getPath(
              (String) request.getAttribute("myComponentURL"), jobDomainSC.getString(
              "JDP.groupUpdate") + "..."));
          destination = "groupCreate.jsp";
        } else if (function.startsWith("displayGroupImport")) {
          request.setAttribute("groupsPath", jobDomainSC.getPath(
              (String) request.getAttribute("myComponentURL"), jobDomainSC.getString(
              "JDP.groupImport") + "..."));
          destination = "groupImport.jsp";
        } else if (function.startsWith("displaySelectUserOrGroup")) {
          destination =
              jobDomainSC.initSelectionPeasForOneGroupOrUser((String) request.getAttribute(
              "myComponentURL"));
        } else if (function.startsWith("displayAddRemoveUsers")) {
          destination = jobDomainSC.initSelectionPeasForGroups((String) request.getAttribute(
              "myComponentURL"));
        } else if (function.startsWith("displayUserCreate")) {
          long domainRight = jobDomainSC.getDomainActions();
          request.setAttribute("isUserRW", (domainRight & DomainDriver.ACTION_CREATE_USER) != 0);

          DomainDriverManager domainDriverManager = new DomainDriverManager();
          DomainDriver domainDriver = domainDriverManager.getDomainDriver(
              Integer.parseInt(jobDomainSC.getTargetDomain().getId()));
          UserFull newUser = new UserFull(domainDriver);
          newUser.setPasswordAvailable(true);

          request.setAttribute("userObject", newUser);
          request.setAttribute("action", "userCreate");
          request.setAttribute("groupsPath", jobDomainSC.getPath(
              (String) request.getAttribute("myComponentURL"),
              jobDomainSC.getString("JDP.userAdd") + "..."));
          request.setAttribute("minLengthLogin", jobDomainSC.getMinLengthLogin());
          request.setAttribute("CurrentUser", jobDomainSC.getUserDetail());
          // if community management is activated, add groups on this user is manager
          if (JobDomainSettings.m_UseCommunityManagement) {
            request.setAttribute("GroupsManagedByCurrentUser", jobDomainSC
                .getUserManageableGroups());
          }

          destination = "userCreate.jsp";
        } else if (function.startsWith("displayUsersCsvImport")) {
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
              "myComponentURL"), jobDomainSC.getString("JDP.csvImport") + "..."));
          destination = "usersCsvImport.jsp";
        } else if (function.startsWith("displayUserModify")) {
          long domainRight = jobDomainSC.getDomainActions();

          request.setAttribute("isUserRW", (domainRight & DomainDriver.ACTION_UPDATE_USER) != 0);

          request.setAttribute("userObject", jobDomainSC.getTargetUserFull());
          request.setAttribute("action", "userModify");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
              "myComponentURL"), jobDomainSC.getString("JDP.userUpdate") + "..."));
          request.setAttribute("minLengthLogin", jobDomainSC.getMinLengthLogin());
          request.setAttribute("CurrentUser", jobDomainSC.getUserDetail());
          destination = "userCreate.jsp";
        } else if (function.startsWith("displayUserMS")) {
          request.setAttribute("userObject", jobDomainSC.getTargetUserFull());
          request.setAttribute("action", "userMS");
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
              "myComponentURL"), jobDomainSC.getString("JDP.userUpdate") + "..."));
          request.setAttribute("minLengthLogin", jobDomainSC.getMinLengthLogin());
          request.setAttribute("CurrentUser", jobDomainSC.getUserDetail());

          destination = "userCreate.jsp";
        } else if (function.startsWith("displayUserImport")) {
          request.setAttribute("SelectedIds", jobDomainSC.getListSelectedUsers());
          request.setAttribute("FirstUserIndex", jobDomainSC.
              getIndexOfFirstItemToDisplay());
          request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
              "myComponentURL"), jobDomainSC.getString("JDP.userImport") + "..."));
          request.setAttribute("properties", jobDomainSC.getPropertiesToImport());
          destination = "userImport.jsp";
        } else if (function.startsWith("displayDomainCreate")) {
          Domain theNewDomain = new Domain();
          theNewDomain.setDriverClassName("com.stratelia.silverpeas.domains.ldapdriver.LDAPDriver");
          theNewDomain.setPropFileName("com.stratelia.silverpeas.domains.domain");
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
          SynchroReport.setTraceLevel(Integer.parseInt(request.getParameter("IdTraceLevel")));
          destination = "dynamicSynchroReport.jsp";
        }
      } else if (function.startsWith("welcome")) {
        jobDomainSC.returnIntoGroup(null);
        request.setAttribute("DisplayOperations", jobDomainSC.getUserDetail().isAccessAdmin());

        ResourceLocator rs = new ResourceLocator(
            "com.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings", "");
        Properties configuration = new Properties();
        configuration
            .setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString("templatePath"));
        configuration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
            "customersTemplatePath"));
        SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(
            configuration);

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
      } else if (function.equals("Pagination")) {
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
      } else {
        destination = function;
      }

      // 2) Prepare the pages
      // --------------------
      if (jobDomainSC.getTargetDomain() != null) {
        request.setAttribute("domainObject", jobDomainSC.getTargetDomain());
      }
      if (destination.equals("domainContent.jsp")) {
        long domainRight = jobDomainSC.getDomainActions();
        request.setAttribute("theUser", jobDomainSC.getUserDetail());
        request.setAttribute("subGroups", jobDomainSC.getSubGroups(false));
        request.setAttribute("subUsers", jobDomainSC.getSubUsers(false));
        request.setAttribute("isDomainRW", ((domainRight & DomainDriver.ACTION_CREATE_GROUP) != 0)
            || ((domainRight & DomainDriver.ACTION_CREATE_USER) != 0));
        request.setAttribute("isUserRW", (domainRight & DomainDriver.ACTION_CREATE_USER) != 0);
        request.setAttribute("isDomainSync",
            ((domainRight & DomainDriver.ACTION_SYNCHRO_USER) != 0)
            || ((domainRight & DomainDriver.ACTION_SYNCHRO_GROUP) != 0));

        request.setAttribute("isOnlyGroupManager", jobDomainSC.isOnlyGroupManager());
        request.setAttribute("isUserAddingAllowedForGroupManager", jobDomainSC.
            isUserAddingAllowedForGroupManager());
      } else if (destination.equals("groupContent.jsp")
          || destination.equals("exportgroup.jsp")) {
        long domainRight = jobDomainSC.getDomainActions();

        request.setAttribute("groupObject", jobDomainSC.getTargetGroup());
        request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
            "myComponentURL"), null));
        request.setAttribute("subGroups", jobDomainSC.getSubGroups(true));
        request.setAttribute("subUsers", jobDomainSC.getSubUsers(true));
        request.setAttribute("isDomainRW", ((domainRight & DomainDriver.ACTION_CREATE_GROUP) != 0)
            || ((domainRight & DomainDriver.ACTION_CREATE_USER) != 0));
        request.setAttribute("isUserRW", (domainRight & DomainDriver.ACTION_CREATE_USER) != 0);
        request.setAttribute("isDomainSync",
            ((domainRight & DomainDriver.ACTION_SYNCHRO_USER) != 0)
            || ((domainRight & DomainDriver.ACTION_SYNCHRO_GROUP) != 0));

        request
            .setAttribute("isGroupManagerOnThisGroup", jobDomainSC.isGroupManagerOnCurrentGroup());
        request.setAttribute("isGroupManagerDirectlyOnThisGroup", jobDomainSC.
            isGroupManagerDirectlyOnCurrentGroup());
        request.setAttribute("isOnlyGroupManager", jobDomainSC.isOnlyGroupManager());
      } else if (destination.equals("userContent.jsp")) {
        request.setAttribute("groupsPath", jobDomainSC.getPath((String) request.getAttribute(
            "myComponentURL"), null));

        if (jobDomainSC.getTargetDomain() != null) {
          long domainRight = jobDomainSC.getDomainActions();

          SilverTrace.info("jobDomainPeas", "JobDomainPeasRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "domainRight=" + domainRight
              + " & DomainDriver.ACTION_X509_USER = " + DomainDriver.ACTION_X509_USER);

          request.setAttribute("isDomainRW",
              ((domainRight & DomainDriver.ACTION_CREATE_GROUP) != 0)
              || ((domainRight & DomainDriver.ACTION_CREATE_USER) != 0));
          request.setAttribute("isUserRW", (domainRight & DomainDriver.ACTION_CREATE_USER) != 0);
          request.setAttribute("isDomainSync",
              ((domainRight & DomainDriver.ACTION_SYNCHRO_USER) != 0)
              || ((domainRight & DomainDriver.ACTION_SYNCHRO_GROUP) != 0));
          request.setAttribute("isX509Enabled", (domainRight & DomainDriver.ACTION_X509_USER) != 0);
          request.setAttribute("isOnlyGroupManager", jobDomainSC.isOnlyGroupManager());
          request.setAttribute("userManageableByGroupManager", jobDomainSC.
              isUserInAtLeastOneGroupManageableByCurrentUser());
        }
        request.setAttribute("userObject", jobDomainSC.getTargetUserFull());
      } else if (destination.equals("domainNavigation.jsp")) {
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
      } else if (destination.equals("groupManagers.jsp")) {
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

    SilverTrace.info("jobDomainPeas", "JobDomainPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
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
    HashMap<String, String> properties = new HashMap<String, String>();
    Enumeration<String> parameters = request.getParameterNames();
    while (parameters.hasMoreElements()) {
      String parameterName = parameters.nextElement();
      if (parameterName.startsWith("prop_")) {
        String property = parameterName.substring(5, parameterName.length()); // remove "prop_"
        properties.put(property, request.getParameter(parameterName));
      }
    }
    return properties;
  }
}
