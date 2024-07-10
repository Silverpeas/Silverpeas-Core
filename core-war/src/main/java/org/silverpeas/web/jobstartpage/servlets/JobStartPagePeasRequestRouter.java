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
package org.silverpeas.web.jobstartpage.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.ParameterList;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.AdminComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.web.jobstartpage.JobStartPagePeasSettings;
import org.silverpeas.web.jobstartpage.NavBarJsonEncoder;
import org.silverpeas.web.jobstartpage.control.JobStartPagePeasSessionController;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

public class JobStartPagePeasRequestRouter extends
    AdminComponentRequestRouter<JobStartPagePeasSessionController> {

  private static final long serialVersionUID = 3751632991093466433L;
  private static final String WELCOME_SPACE_ADMIN_TEMPLATE_FILE = "/space/welcome_space_admin_";
  private static final String WELCOME_SPACE_MGR_TEMPLATE_FILE = "/space/welcome_space_manager_";
  private static final String WELCOME_FCT = "welcome";
  private static final String VIEW_BIN_FCT = "ViewBin";
  private static final String GO_TO_COMPONENT_FCT = "GoToComponent";
  private static final String GO_TO_CURRENT_COMPONENT_FCT = "GoToCurrentComponent";
  private static final String GO_TO_CURRENT_COMPONENT_DEST = "/jobStartPagePeas/jsp" +
      "/goToCurrentComponent.jsp";
  private static final String SPACE_LOOK_FCT = "SpaceLook";
  private static final String ESPACE_PARAM = "Espace";
  private static final String SPACE_ID_PARAM = "SpaceId";
  private static final String COMPONENT_ID_PARAM = "ComponentId";
  private static final String USER_PANEL_CURRENT_USER_IDS_PARAM = "UserPanelCurrentUserIds";
  private static final String USER_PANEL_CURRENT_GROUP_IDS_PARAM = "UserPanelCurrentGroupIds";
  private static final String AS_JSON_PARAM = "AsJson";
  private static final String INHERITANCE_ATTR = "IsInheritanceEnable";
  private static final String URL_TO_RELOAD_ATTR = "urlToReload";
  private static final String PROFILE_ATTR = "Profile";
  private static final String BROTHERS_ATTR = "brothers";
  private static final String SOUS_ESPACE_ATTR = "SousEspace";
  private static final String IS_USER_ADMIN_ATTR = "isUserAdmin";
  private static final String SPACE_EXTRA_INFOS_ATTR = "SpaceExtraInfos";
  private static final String CURRENT_SPACE_ID_ATTR = "CurrentSpaceId";
  private static final String COMPONENT_INST_ATTR = "ComponentInst";
  private static final String PROFILES_ATTR = "Profiles";
  private static final String SCOPE_ATTR = "Scope";
  private static final String NAME_SPACE_ATTR = "spaceName";
  private static final String DESCRIPTION_SPACE_ATTR = "spaceDescription";
  private static final String PARAMETERS_ATTR = "Parameters";
  private static final String HAVE_TO_REFRESH_NAV_BAR_ATTR = "haveToRefreshNavBar";
  private static final String START_PAGE_INFO_DEST = "StartPageInfo";
  private static final String START_PAGE_INFO_FULL_DEST = "/jobStartPagePeas/jsp/startPageInfo.jsp";
  private static final String CLOSE_WINDOW_FULL_DEST = "/jobStartPagePeas/jsp/closeWindow.jsp";
  private static final String COMPONENT_INFO_FULL_DEST = "/jobStartPagePeas/jsp/componentInfo.jsp";
  private static final String ERROR_FULL_DEST = "/jobStartPagePeas/jsp/error.jsp";
  private static final String ROLE_INSTANCE_FULL_DEST = "/jobStartPagePeas/jsp/roleInstance.jsp";
  private static final String ROLE_ITEMS_PREFIX = "roleItems";
  private static final String SPACE_TYPE = "Space";
  private static final String WRITE_OPERATION_PARTS =
      "(?i)^.*(create|update|modify|delete|remove|effective).*$";

  @Override
  public JobStartPagePeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobStartPagePeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   *
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return "jobStartPagePeas";
  }

  /**
   * Handle the homepage of current space.
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobStartPageSC the controller
   * @param request the incoming request
   * @return the destination if any, null otherwise.
   * @throws ClipboardException on clipboard technical error.
   */
  public String getDestinationStartPage(String function,
      JobStartPagePeasSessionController jobStartPageSC, HttpRequest request) throws
      ClipboardException {
    if (!jobStartPageSC.getClipboardSelectedObjects().isEmpty()) {
      request.setAttribute("ObjectsSelectedInClipboard", "true");
    }
    SpaceInst spaceInst;
    if ("SaveHomepageChoice".equals(function)) {
      spaceInst = jobStartPageSC.getSpaceInstById();
      spaceInst.setFirstPageType(SpaceHomePageType.valueOf(request.getParameter("type")).ordinal());
      spaceInst.setFirstPageExtraParam(defaultStringIfNotDefined(request.getParameter("value")));
      jobStartPageSC.updateSpaceInst(spaceInst);
      return emptyJsonResponse();
    } else if ("SetPortlet".equals(function)) {
      spaceInst = jobStartPageSC.getSpaceInstById();
      spaceInst.setFirstPageType(SpaceInst.FP_TYPE_PORTLET);
      spaceInst.setFirstPageExtraParam("");
      jobStartPageSC.updateSpaceInst(spaceInst);
      request.setAttribute(URL_TO_RELOAD_ATTR, START_PAGE_INFO_DEST);
      return CLOSE_WINDOW_FULL_DEST;
    }
    return null;
  }

  /**
   * ********************* Gestion de la navigation à gauche
   * ****************************************
   */
  /**
   * @param function
   * @param jobStartPageSC
   * @param request
   * @return
   */
  public String getDestinationNavBar(String function,
      JobStartPagePeasSessionController jobStartPageSC, HttpRequest request) {
    String destination = null;

    if ("Main".equals(function)) {
      jobStartPageSC.init(true); // Only ONCE
      final String spaceId = request.getParameter(SPACE_ID_PARAM);
      setSpaceOrHomepage(jobStartPageSC, spaceId);
      destination = "/jobStartPagePeas/jsp/jobStartPage.jsp";
    } else if (function.startsWith("GoToSpace")) {
      jobStartPageSC.init(false); // Initialization if necessary
      final String spaceId = request.getParameter(ESPACE_PARAM);
      setSpaceOrHomepage(jobStartPageSC, spaceId);
      destination = emptyJsonResponse();
    } else if ("jobStartPageNav".equals(function)) {
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.jsp";
    } else if ("jobStartPageNavAsJson".equals(function)) {
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.json";
    } else if (WELCOME_FCT.equals(function)) {
      // Get Silverpeas template from JobStartSessionSettings
      SilverpeasTemplate template = jobStartPageSC.getSilverpeasTemplate();
      // Check user right
      if (jobStartPageSC.isUserAdmin()) {
        request.setAttribute("Content", template
            .applyFileTemplate(WELCOME_SPACE_ADMIN_TEMPLATE_FILE + jobStartPageSC.getLanguage()));
      } else {
        request.setAttribute("Content", template
            .applyFileTemplate(WELCOME_SPACE_MGR_TEMPLATE_FILE + jobStartPageSC.getLanguage()));
      }
      destination = "/jobStartPagePeas/jsp/welcome.jsp";
    } else if (VIEW_BIN_FCT.equals(function)) {
      jobStartPageSC.checkAdminAccessOnly();
      request.setAttribute("Spaces", jobStartPageSC.getRemovedSpaces());
      request.setAttribute("Components", jobStartPageSC.getRemovedComponents());
      destination = "/jobStartPagePeas/jsp/bin.jsp";
    } else if ("RestoreFromBin".equals(function) || "RemoveDefinitely".equals(function)) {
      jobStartPageSC.checkAdminAccessOnly();
      final List<String> spaceIds;
      final List<String> componentIds;
      final String itemId = request.getParameter("ItemId");
      if (StringUtil.isDefined(itemId)) {
        if (itemId.startsWith("WA")) {
          spaceIds = singletonList(itemId);
          componentIds = emptyList();
        } else {
          spaceIds = emptyList();
          componentIds = singletonList(itemId);
        }
      } else {
        spaceIds = request.getParameterAsList("SpaceIds");
        componentIds = request.getParameterAsList("ComponentIds");
      }
      if ("RestoreFromBin".equals(function)) {
        spaceIds.forEach(jobStartPageSC::restoreSpaceFromBin);
        componentIds.forEach(jobStartPageSC::restoreComponentFromBin);
      } else {
        spaceIds.forEach(jobStartPageSC::deleteSpaceInBin);
        componentIds.forEach(jobStartPageSC::deleteComponentInBin);
      }
      destination = sendJson(encodeObject(o -> o
          .putJSONArray("spaceIds", a -> a.addJSONArray(spaceIds))
          .putJSONArray("componentIds", a -> a.addJSONArray(componentIds))));
    }

    return destination;
  }

  private void setSpaceOrHomepage(final JobStartPagePeasSessionController jspSC,
      final String spaceId) {
    if (StringUtil.isDefined(spaceId)) {
      jspSC.setSpaceId(spaceId);
    } else {
      jspSC.setSpaceId(null);
    }
  }

  /**
   * ********************* Gestion des composants ****************************************
   */
  /**
   * @param function
   * @param jobStartPageSC
   * @param request
   * @return
   * @throws AdminException
   */
  public String getDestinationComponent(String function,
      JobStartPagePeasSessionController jobStartPageSC, HttpRequest request) throws
      AdminException {
    String destination = null;

    if (function.equals(GO_TO_COMPONENT_FCT)) {
      String compoId = request.getParameter(COMPONENT_ID_PARAM);
      ComponentInst componentInst = jobStartPageSC.getComponentInst(compoId);
      ofNullable(componentInst.getDomainFatherId())
          .map(jobStartPageSC::getSpaceInstById)
          .map(SpaceInst::getId)
          .ifPresent(jobStartPageSC::setSpaceId);
      jobStartPageSC.setManagedInstanceId(compoId);
      destination = request.getParameterAsBoolean(AS_JSON_PARAM) ?
          emptyJsonResponse() :
          COMPONENT_INFO_FULL_DEST;
    } else if ("SetupComponent".equals(function)) {
      String compoId = request.getParameter(COMPONENT_ID_PARAM);
      jobStartPageSC.setManagedInstanceId(compoId,
          JobStartPagePeasSessionController.SCOPE_FRONTOFFICE);
      destination = getDestination("UpdateInstance", jobStartPageSC, request);
    } else if (GO_TO_CURRENT_COMPONENT_FCT.equals(function)) {
      destination = COMPONENT_INFO_FULL_DEST;
    } else if ("ListComponent".equals(function)) {
      setSpaceNameInRequest(jobStartPageSC, request);
      request.setAttribute("ListComponents", jobStartPageSC.getAllLocalizedComponents());
      destination = "/jobStartPagePeas/jsp/componentsList.jsp";
    } else if (function.equals("CreateInstance")) {
      jobStartPageSC.setManagedInstanceId(null);
      destination = prepareCreateInstance(jobStartPageSC, request);
    } else if ("EffectiveCreateInstance".equals(function)) {
      // Create the component
      ComponentInst componentInst = new ComponentInst();
      request2ComponentInst(componentInst, request, jobStartPageSC);

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      componentInst.setDomainFatherId(spaceint1.getId());

      // Add the component
      String sComponentId = null;
      String sErrorMessage = null;
      try {
        sComponentId = jobStartPageSC.addComponentInst(componentInst);
      } catch (QuotaException e) {
        sErrorMessage = jobStartPageSC.getString("JSPP.componentSpaceQuotaFull");
      }
      if (StringUtil.isDefined(sComponentId)) {
        jobStartPageSC.setManagedInstanceId(sComponentId);
        jobStartPageSC.setComponentPlace(request.getParameter("ComponentBefore"));
        refreshNavBar(jobStartPageSC, request);
        request.setAttribute(COMPONENT_ID_PARAM, sComponentId);
        destination = GO_TO_CURRENT_COMPONENT_DEST;
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas déportée vers les
        // appelants
        request.setAttribute("When", "ComponentCreation");
        request.setAttribute("ErrorMessage", sErrorMessage);

        setSpaceNameInRequest(jobStartPageSC, request);

        destination = ERROR_FULL_DEST;
      }
    } else if (function.equals("UpdateInstance")) {
      destination = prepareUpdateInstance(jobStartPageSC, request);
    } else if (function.equals("EffectiveUpdateInstance")) {
      ComponentInst componentInst = jobStartPageSC.getComponentInst(jobStartPageSC.
          getManagedInstanceId());
      request2ComponentInst(componentInst, request, jobStartPageSC);
      // Update the instance
      String componentId = jobStartPageSC.updateComponentInst(componentInst);
      if (StringUtil.isDefined(componentId)) {
        if (jobStartPageSC.getScope() == JobStartPagePeasSessionController.SCOPE_BACKOFFICE) {
          refreshNavBar(jobStartPageSC, request);
        }
        request.setAttribute(COMPONENT_ID_PARAM, componentId);
        destination = GO_TO_CURRENT_COMPONENT_DEST;
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "ComponentUpdate");
        setSpaceNameInRequest(jobStartPageSC, request);
        destination = ERROR_FULL_DEST;
      }
    } else if (function.equals("DeleteInstance")) {
      // Delete the instance
      jobStartPageSC.deleteComponentInst(jobStartPageSC.getManagedInstanceId());

      refreshNavBar(jobStartPageSC, request);
      destination = START_PAGE_INFO_FULL_DEST;
    } else if (function.equals("RoleInstance")) {
      String profileId = request.getParameter("IdProfile");
      String profileName = request.getParameter("NameProfile");
      String profileLabel = request.getParameter("LabelProfile");

      ProfileInst profile = jobStartPageSC.getProfile(profileId, profileName, profileLabel);
      jobStartPageSC.setManagedProfile(profile);
      request.setAttribute(PROFILE_ATTR, profile);

      destination = ROLE_INSTANCE_FULL_DEST;
    } else if (function.equals("CurrentRoleInstance")) {
      request.setAttribute(PROFILE_ATTR, jobStartPageSC.getManagedProfile());

      destination = ROLE_INSTANCE_FULL_DEST;
    } else if (function.equals("SelectUsersGroupsProfileInstance")) {
      List<String> userIds = (List<String>) StringUtil
          .splitString(request.getParameter(USER_PANEL_CURRENT_USER_IDS_PARAM), ',');
      List<String> groupIds = (List<String>) StringUtil
          .splitString(request.getParameter(USER_PANEL_CURRENT_GROUP_IDS_PARAM), ',');
      jobStartPageSC
          .initUserPanelInstanceForGroupsUsers((String) request.getAttribute("myComponentURL"),
              userIds, groupIds);
      destination = Selection.getSelectionURL();
    } else if (function.equals("EffectiveSetInstanceProfile")) {
      String[] userIds =
          StringUtil.split(request.getParameter(
              ROLE_ITEMS_PREFIX + USER_PANEL_CURRENT_USER_IDS_PARAM), ',');
      String[] groupIds = StringUtil
          .split(request.getParameter(ROLE_ITEMS_PREFIX + USER_PANEL_CURRENT_GROUP_IDS_PARAM), ',');
      jobStartPageSC.updateInstanceProfile(userIds, groupIds);
      destination = getDestination("CurrentRoleInstance", jobStartPageSC, request);
    } else if (function.equals("PlaceComponentAfter")) {
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(
          jobStartPageSC.getManagedInstanceId());
      request.setAttribute("currentComponentName", compoint1.getLabel());
      request.setAttribute(BROTHERS_ATTR, jobStartPageSC.getBrotherComponents(false));
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      request.setAttribute("currentSpace", spaceint1);
      destination = "/jobStartPagePeas/jsp/placeComponentAfter.jsp";
    } else if (function.equals("EffectivePlaceComponent")) {
      jobStartPageSC.setComponentPlace(request.getParameter("ComponentBefore"));
      refreshNavBar(jobStartPageSC, request);
      request.setAttribute(URL_TO_RELOAD_ATTR,
          "goToCurrentComponent.jsp?" + HAVE_TO_REFRESH_NAV_BAR_ATTR + "=true&ComponentId=" +
          jobStartPageSC.getManagedInstanceId());
      destination = CLOSE_WINDOW_FULL_DEST;
    } else if (function.startsWith("copy")) {
      String objectId = request.getParameter("Id");
      String objectType = request.getParameter("Type");
      try {
        if (SPACE_TYPE.equals(objectType)) {
          jobStartPageSC.copySpace(objectId);
        } else {
          jobStartPageSC.copyComponent(objectId);
        }
      } catch (Exception e) {
        throw new AdminException("Fail to copy " + objectType + " " + objectId, e);
      }
      destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null)
          + "Idle.jsp?message=REFRESHCLIPBOARD";
    } else if ("Cut".equals(function)) {
      String objectId = request.getParameter("Id");
      String objectType = request.getParameter("Type");
      try {
        if (SPACE_TYPE.equals(objectType)) {
          jobStartPageSC.cutSpace(objectId);
        } else if ("Component".equals(objectType)) {
          jobStartPageSC.cutComponent(objectId);
        }
      } catch (Exception e) {
        throw new AdminException("Fail to cut " + objectType + " " + objectId, e);
      }
      destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null)
          + "Idle.jsp?message=REFRESHCLIPBOARD";
    } else if ("Paste".equals(function)) {
      Map<String, String> options = getPasteOptions(request);
      try {
        jobStartPageSC.paste(options);
      } catch (Exception e) {
        throw new AdminException("Pasting failure", e);
      }
      refreshNavBar(jobStartPageSC, request);
      if (StringUtil.isDefined(jobStartPageSC.getManagedSpaceId())) {
        destination = START_PAGE_INFO_FULL_DEST;
      } else {
        destination = getDestination(WELCOME_FCT, jobStartPageSC, request);
      }
    } else if (function.equals("OpenComponent")) {
      jobStartPageSC.init(true);
      request.setAttribute("PopupMode", true);
      destination = getDestination(GO_TO_COMPONENT_FCT, jobStartPageSC, request);
    }

    return destination;
  }

  private Map<String, String> getPasteOptions(HttpServletRequest request) {
    final Map<String, String[]> parameters = request.getParameterMap();
    final Map<String, String> pasteOptions = new HashMap<>();
    for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
      if (parameter.getKey().startsWith(PasteDetail.OPTION_PREFIX)) {
        pasteOptions.put(parameter.getKey(), parameter.getValue()[0]);
      }
    }
    return pasteOptions;
  }

  public String getDestinationSpace(String function,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpRequest request) throws Exception {
    String destination = null;

    if (START_PAGE_INFO_DEST.equals(function)) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(); // espace courant
      if (spaceint1 == null) {
        destination = getDestination(WELCOME_FCT, jobStartPageSC, request);
      } else {
        destination = START_PAGE_INFO_FULL_DEST;
      }
    } else if (function.equals("GoToSubSpace")) {
      jobStartPageSC.init(false); // Initialization if necessary
      final String subSpaceId = request.getParameter("SubSpace");
      if (StringUtil.isDefined(subSpaceId)) {
        jobStartPageSC.setSubSpaceId(subSpaceId);
      }
      destination = emptyJsonResponse();
    } else if (function.equals("DesactivateMaintenance")) {
      String allIntranet = request.getParameter("allIntranet");
      if ("1".equals(allIntranet)) {
        jobStartPageSC.setAppModeMaintenance(false);
        destination = getDestination(WELCOME_FCT, jobStartPageSC, request);
      } else {
        String spaceId = jobStartPageSC.getManagedSpaceId();
        jobStartPageSC.setSpaceMaintenance(spaceId, false);
        refreshNavBar(jobStartPageSC, request);
        destination = START_PAGE_INFO_FULL_DEST;
      }
    } else if (function.equals("ActivateMaintenance")) {
      String allIntranet = request.getParameter("allIntranet");
      if ("1".equals(allIntranet)) {
        jobStartPageSC.setAppModeMaintenance(true);
        destination = getDestination(WELCOME_FCT, jobStartPageSC, request);
      } else {
        String spaceId = jobStartPageSC.getManagedSpaceId();
        jobStartPageSC.setSpaceMaintenance(spaceId, true);
        refreshNavBar(jobStartPageSC, request);
        destination = START_PAGE_INFO_FULL_DEST;
      }
    } else if (function.equals("PlaceSpaceAfter")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      request.setAttribute("CurrentSpace", spaceint1);
      request.setAttribute("Brothers", jobStartPageSC.getBrotherSpaces(false));
      destination = "/jobStartPagePeas/jsp/placeSpaceAfter.jsp";
    } else if (function.equals("EffectivePlaceSpaceAfter")) {
      jobStartPageSC.setSpacePlace(request.getParameter("SpaceBefore"));
      refreshNavBar(jobStartPageSC, request);
      request.setAttribute(URL_TO_RELOAD_ATTR,
          START_PAGE_INFO_DEST + "?" + HAVE_TO_REFRESH_NAV_BAR_ATTR + "=true");
      destination = CLOSE_WINDOW_FULL_DEST;
    } else if (function.equals("CreateSpace")) {
      setSpaceNameInRequest(jobStartPageSC, request);

      request.setAttribute(SOUS_ESPACE_ATTR, request.getParameter(SOUS_ESPACE_ATTR));
      request.setAttribute(BROTHERS_ATTR, jobStartPageSC.getBrotherSpaces(true));
      request.setAttribute(IS_USER_ADMIN_ATTR, jobStartPageSC.isUserAdmin());
      destination = "/jobStartPagePeas/jsp/createSpace.jsp";
    } else if (function.equals("EffectiveCreateSpace")) {
      // Space CREATE action
      SpaceInst newSpace = new SpaceInst();
      request2SpaceInst(newSpace, request);
      String spaceId = jobStartPageSC.createSpace(newSpace);
      if (spaceId != null && spaceId.length() > 0) {
        initQuotaData(newSpace, request, jobStartPageSC);
        jobStartPageSC.setSpacePlace(request.getParameter("SpaceBefore"));
        refreshNavBar(jobStartPageSC, request);
        destination = getDestinationSpace(START_PAGE_INFO_DEST, jobStartPageSC, request);
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "SpaceCreation");
        setSpaceNameInRequest(jobStartPageSC, request);
        destination = ERROR_FULL_DEST;
      }
    } else if (function.equals("UpdateSpace")) {
      String translation = request.getParameter("Translation");
      SpaceInst spaceInst = jobStartPageSC.getSpaceInstById();
      setSpaceInfosInRequest(spaceInst, jobStartPageSC, request);
      request.setAttribute(SPACE_TYPE, spaceInst);
      request.setAttribute("Translation", translation);
      request.setAttribute(IS_USER_ADMIN_ATTR, jobStartPageSC.isUserAdmin());

      destination = "/jobStartPagePeas/jsp/updateSpace.jsp";
    } else if (function.equals("EffectiveUpdateSpace")) {
      // Update the space
      SpaceInst spaceInst = jobStartPageSC.getSpaceInstById();
      request2SpaceInst(spaceInst, request);
      String spaceId = jobStartPageSC.updateSpaceInst(spaceInst);
      if (spaceId != null && spaceId.length() > 0) {
        initQuotaData(spaceInst, request, jobStartPageSC);
        refreshNavBar(jobStartPageSC, request);
        destination = getDestinationSpace(START_PAGE_INFO_DEST, jobStartPageSC, request);
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "SpaceUpdate");

        setSpaceNameInRequest(jobStartPageSC, request);

        destination = ERROR_FULL_DEST;
      }
    } else if (function.equals("DeleteSpace")) {
      // Delete the space
      String spaceId = request.getParameter("Id");
      jobStartPageSC.deleteSpace(spaceId);
      refreshNavBar(jobStartPageSC, request);
      if ((jobStartPageSC.getManagedSpaceId() != null)
          && (jobStartPageSC.getManagedSpaceId().length() > 0)) {
        destination = START_PAGE_INFO_FULL_DEST;
      } else {
        destination = getDestination(WELCOME_FCT, jobStartPageSC, request);
      }
    } else if (function.equals("RecoverSpaceRights")) {
      String spaceId = request.getParameter("Id");
      String recoverDest = START_PAGE_INFO_FULL_DEST;
      if (StringUtil.isDefined(spaceId)) {
        jobStartPageSC.recoverSpaceRights(spaceId);
      } else {
        if (jobStartPageSC.isUserAdmin()) {
          jobStartPageSC.recoverSpaceRights(null);
          recoverDest = getDestination(START_PAGE_INFO_DEST, jobStartPageSC, request);
        }
      }
      destination = recoverDest;
    } else if (function.equals("SpaceManager")) {
      String role = request.getParameter("Role");
      if (!StringUtil.isDefined(role)) {
        role = "Manager";
      }

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      setSpaceInfosInRequest(spaceint1, jobStartPageSC, request);

      // get groups and users which manage current space
      SpaceProfile spaceProfile = jobStartPageSC.getCurrentSpaceProfile(role);
      request.setAttribute("SpaceProfile", spaceProfile);

      request.setAttribute(SPACE_EXTRA_INFOS_ATTR, jobStartPageSC.getManagedSpace());
      request.setAttribute("Role", role);

      if (SilverpeasRole.MANAGER == SilverpeasRole.fromString(role)) {
        request.setAttribute(INHERITANCE_ATTR, true);
      } else {
        request.setAttribute(INHERITANCE_ATTR, JobStartPagePeasSettings.isInheritanceEnable);
      }

      destination = "/jobStartPagePeas/jsp/spaceManager.jsp";
    } else if (function.equals("SelectUsersGroupsSpace")) {
      List<String> userIds = (List<String>) StringUtil
          .splitString(request.getParameter(USER_PANEL_CURRENT_USER_IDS_PARAM), ',');
      List<String> groupIds = (List<String>) StringUtil
          .splitString(request.getParameter(USER_PANEL_CURRENT_GROUP_IDS_PARAM), ',');
      jobStartPageSC
          .initUserPanelSpaceForGroupsUsers((String) request.getAttribute("myComponentURL"),
              userIds, groupIds);
      destination = Selection.getSelectionURL();
    } else if (function.equals("EffectiveSetSpaceProfile")) {
      String role = request.getParameter("Role");
      List<String> userIds = (List<String>)
          StringUtil.splitString(request.getParameter(ROLE_ITEMS_PREFIX +
              USER_PANEL_CURRENT_USER_IDS_PARAM), ',');
      List<String> groupIds = (List<String>) StringUtil
          .splitString(request.getParameter(ROLE_ITEMS_PREFIX + USER_PANEL_CURRENT_GROUP_IDS_PARAM), ',');
      jobStartPageSC.updateSpaceRole(role, userIds, groupIds);
      destination = getDestination("SpaceManager", jobStartPageSC, request);
    } else if (function.equals(SPACE_LOOK_FCT)) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      request.setAttribute(SPACE_TYPE, spaceint1);
      request.setAttribute("SpaceLookHelper", jobStartPageSC.getSpaceLookHelper());
      request.setAttribute(SPACE_EXTRA_INFOS_ATTR, jobStartPageSC.getManagedSpace());
      request.setAttribute(INHERITANCE_ATTR, JobStartPagePeasSettings.isInheritanceEnable);

      setSpaceInfosInRequest(spaceint1, jobStartPageSC, request);

      // Add spacePosition data
      String configSpacePosition = jobStartPageSC.getConfigSpacePosition();
      // We have to process all case because config can change
      if ("BEFORE".equalsIgnoreCase(configSpacePosition)
          || "AFTER".equalsIgnoreCase(configSpacePosition)) {
        request.setAttribute("displaySpaceOption", Boolean.FALSE);
      } else {
        request.setAttribute("displaySpaceOption", Boolean.TRUE);
      }
      destination = "/jobStartPagePeas/jsp/spaceLook.jsp";
    } else if (function.equals("UpdateSpaceLook")) {
      List<FileItem> items = request.getFileItems();
      jobStartPageSC.updateSpaceAppearance(items);
      destination = getDestination(SPACE_LOOK_FCT, jobStartPageSC, request);
    } else if (function.equals("RemoveFileToLook")) {
      String fileName = request.getParameter("FileName");
      jobStartPageSC.removeExternalElementOfSpaceAppearance(fileName);
      destination = getDestination(SPACE_LOOK_FCT, jobStartPageSC, request);
    } else if (function.equals("OpenSpace")) {
      jobStartPageSC.init(true);
      setSpaceOrHomepage(jobStartPageSC, request.getParameter(ESPACE_PARAM));
      destination = getDestination(START_PAGE_INFO_DEST, jobStartPageSC, request);
    } else if (function.equals("OpenSubSpace")) {
      jobStartPageSC.init(true);
      if (StringUtil.isDefined(request.getParameter(ESPACE_PARAM))) {
        jobStartPageSC.setSpaceId(request.getParameter(ESPACE_PARAM));
        if (StringUtil.isDefined(request.getParameter(SOUS_ESPACE_ATTR))) {
          jobStartPageSC.setSubSpaceId(request.getParameter(SOUS_ESPACE_ATTR));
        }
      } else {
        jobStartPageSC.setSpaceId(null);
      }
      destination = getDestination(START_PAGE_INFO_DEST, jobStartPageSC, request);
    }

    return destination;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobStartPageSC the controller
   * @param request the incoming request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getAdminDestination(String function, JobStartPagePeasSessionController jobStartPageSC,
      HttpRequest request) {
    if (function.matches(WRITE_OPERATION_PARTS)) {
      final String spaceId = jobStartPageSC.getManagedSpaceId();
      final String instanceId = jobStartPageSC.getManagedInstanceId();
      jobStartPageSC.checkAccessGranted(spaceId, instanceId, false);
    }
    if (request.getParameterAsBoolean(HAVE_TO_REFRESH_NAV_BAR_ATTR)) {
      request.setAttribute(HAVE_TO_REFRESH_NAV_BAR_ATTR, Boolean.TRUE);
    }
    String destination;
    try {
      destination = getDestinationStartPage(function, jobStartPageSC, request);
      if (destination == null) {
        destination = getDestinationNavBar(function, jobStartPageSC, request);
      }
      if (destination == null) {
        destination = getDestinationComponent(function, jobStartPageSC, request);
      }
      if (destination == null) {
        destination = getDestinationSpace(function, jobStartPageSC, request);
      }
      if (destination == null) {
        destination = "/jobStartPagePeas/jsp/" + function;
      }
      if ("/jobStartPagePeas/jsp/jobStartPageNav.jsp".equals(destination)) {
        request.setAttribute("Spaces", jobStartPageSC.getSpaces());
        request.setAttribute("SubSpaces", jobStartPageSC.getSubSpaces());
        request.setAttribute("SpaceComponents", jobStartPageSC.getSpaceComponents());
        request.setAttribute("SubSpaceComponents", jobStartPageSC.getSubSpaceComponents());
        request.setAttribute(CURRENT_SPACE_ID_ATTR, jobStartPageSC.getSpaceId());
        request.setAttribute("CurrentSubSpaceId", jobStartPageSC.getSubSpaceId());
      } else if ("/jobStartPagePeas/jsp/jobStartPageNav.json".equals(destination)) {
        destination = sendJson(NavBarJsonEncoder.with(jobStartPageSC).encode());
      } else if ("/jobStartPagePeas/jsp/welcome.jsp".equals(destination)) {
        request.setAttribute(IS_USER_ADMIN_ATTR, jobStartPageSC.isUserAdmin());
        request.setAttribute("globalMode", jobStartPageSC.isAppInMaintenance());
        request.setAttribute("IsBackupEnable", jobStartPageSC.isBackupEnable());
        request.setAttribute("IsBasketEnable", JobStartPagePeasSettings.isBasketEnable);
      } else if (START_PAGE_INFO_FULL_DEST.equals(destination)) {
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(); // espace
        // courant
        request.setAttribute(IS_USER_ADMIN_ATTR, jobStartPageSC.isUserAdmin());
        request.setAttribute("FirstPageType", spaceint1.getFirstPageType());
        String spaceId = jobStartPageSC.getManagedSpaceId();
        request.setAttribute("currentSpaceId", spaceId);
        request.setAttribute("MaintenanceState", jobStartPageSC.getCurrentSpaceMaintenanceState());

        setSpaceInfosInRequest(spaceint1, jobStartPageSC, request);

        request.setAttribute(SPACE_EXTRA_INFOS_ATTR, jobStartPageSC.getManagedSpace());
        request.setAttribute("IsBackupEnable", jobStartPageSC.isBackupEnable());

        request.setAttribute(INHERITANCE_ATTR, JobStartPagePeasSettings.isInheritanceEnable);

        request.setAttribute("CopiedComponents", jobStartPageSC.getCopiedComponents());

        request.setAttribute(SPACE_TYPE, spaceint1);
      } else if (COMPONENT_INFO_FULL_DEST.equals(destination)) {
        prepareDisplayComponentInfo(jobStartPageSC, request);
      } else if (ROLE_INSTANCE_FULL_DEST.equals(destination)) {
        ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC.
            getManagedInstanceId());
        request.setAttribute(COMPONENT_INST_ATTR, compoint1);

        request.setAttribute(PROFILES_ATTR, jobStartPageSC.getAllProfiles(compoint1));

        // Profile, liste des groupes et user du role courant
        ProfileInst profile = jobStartPageSC.getManagedProfile();
        request.setAttribute(PROFILE_ATTR, profile);
        request.setAttribute("listGroup", jobStartPageSC.getAllCurrentGroupInstance());
        request.setAttribute("listUser", jobStartPageSC.getAllCurrentUserInstance());

        // Profile hérité, liste des groupes et user du role hérité courant
        ProfileInst inheritedProfile = jobStartPageSC.getManagedInheritedProfile();
        if (inheritedProfile != null) {
          request.setAttribute("InheritedProfile", inheritedProfile);
          request.setAttribute("listInheritedGroups",
              jobStartPageSC.groupIds2groups(inheritedProfile.getAllGroups()));
          request.setAttribute("listInheritedUsers", jobStartPageSC.userIds2users(inheritedProfile.
              getAllUsers()));
        }
        request.setAttribute("ProfileEditable", jobStartPageSC.isProfileEditable());
        request.setAttribute(INHERITANCE_ATTR, JobStartPagePeasSettings.isInheritanceEnable);

        String profileHelp = jobStartPageSC.getManagedProfileHelp(compoint1.getName());
        request.setAttribute("ProfileHelp", profileHelp);

        request.setAttribute(SCOPE_ATTR, jobStartPageSC.getScope());
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  protected void refreshNavBar(JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
    jobStartPageSC.refreshCurrentSpaceCache();
    request.setAttribute(HAVE_TO_REFRESH_NAV_BAR_ATTR, Boolean.TRUE);
  }

  private void setSpaceNameInRequest(
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
    setSpaceInfosInRequest(jobStartPageSC.getSpaceInstById(), jobStartPageSC,
        request);
  }

  private void setSpaceInfosInRequest(SpaceInst spaceint1,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
      request.setAttribute(CURRENT_SPACE_ID_ATTR, spaceint1.getId());
      request.setAttribute(NAME_SPACE_ATTR, spaceint1.getName(jobStartPageSC.getLanguage()));
      request.setAttribute(DESCRIPTION_SPACE_ATTR, spaceint1.getDescription(jobStartPageSC.getLanguage()));
/*
      if (!spaceint1.isRoot()) {// je suis sur un ss-espace
        request.setAttribute(NAME_SUB_SPACE_ATTR, spaceint1.getName(jobStartPageSC.getLanguage()));
        request.setAttribute(DESCRIPTION_SUB_SPACE_ATTR, spaceint1.getDescription(jobStartPageSC.getLanguage()));
      } else {
        request.setAttribute(NAME_SUB_SPACE_ATTR, null);
        request.setAttribute(DESCRIPTION_SUB_SPACE_ATTR, null);
      }
    } else {
      request.setAttribute(NAME_SUB_SPACE_ATTR, null);
      request.setAttribute(DESCRIPTION_SUB_SPACE_ATTR, null);
    }
    */
  }

  private void request2SpaceInst(SpaceInst spaceInst, HttpServletRequest request) {
    String name = request.getParameter("NameObject");
    String desc = request.getParameter("Description");
    String pInheritance = request.getParameter("InheritanceBlocked");
    String look = request.getParameter("SelectedLook");
    if (desc == null) {
      desc = "";
    }
    // Update the space
    spaceInst.setName(name);
    spaceInst.setDescription(desc);
    if (StringUtil.isDefined(pInheritance)) {
      spaceInst.setInheritanceBlocked(StringUtil.getBooleanValue(pInheritance));
    }
    if (StringUtil.isDefined(look)) {
      spaceInst.setLook(look);
    }
    I18NHelper.setI18NInfo(spaceInst, request);
  }

  private void initQuotaData(SpaceInst spaceInst, HttpServletRequest request,
      JobStartPagePeasSessionController jobStartPageSC) {
    String componentSpaceQuotaMaxCount = request.getParameter("ComponentSpaceQuota");
    String dataStorageQuotaMaxCount = request.getParameter("DataStorageQuota");
    jobStartPageSC.saveSpaceQuota(spaceInst, componentSpaceQuotaMaxCount,
        dataStorageQuotaMaxCount);
  }

  private void request2ComponentInst(ComponentInst componentInst,
      HttpServletRequest request,
      JobStartPagePeasSessionController jobStartPageSC) {
    String name = request.getParameter("NameObject");
    String desc = request.getParameter("Description");
    if (desc == null) {
      desc = "";
    }
    if (JobStartPagePeasSettings.isPublicParameterEnable) {
      String pPublic = request.getParameter("PublicComponent");
      componentInst.setPublic(StringUtil.isDefined(pPublic));
    } else {
      WAComponent.getByInstanceId(componentInst.getId())
          .map(WAComponent::isPublicByDefault)
          .ifPresent(componentInst::setPublic);
    }
    String pHidden = request.getParameter("HiddenComponent");
    String pInheritance = request.getParameter("InheritanceBlocked");
    componentInst.setLabel(name);
    componentInst.setDescription(desc);
    componentInst.setHidden(StringUtil.isDefined(pHidden));
    if (StringUtil.isDefined(pInheritance)) {
      componentInst.setInheritanceBlocked(StringUtil.getBooleanValue(pInheritance));
    }
    I18NHelper.setI18NInfo(componentInst, request);
    // Add the parameters (if they exist)
    WAComponent component;
    if (StringUtil.isDefined(componentInst.getName())) {
      component = jobStartPageSC.getComponentByName(componentInst.getName());
    } else {
      String componentName = request.getParameter("ComponentName");
      component = jobStartPageSC.getComponentByName(componentName);
      componentInst.setName(componentName);
    }

    ParameterList parameters = component.getAllParameters().copy();
    for (Parameter parameter : parameters) {
      String value = request.getParameter(parameter.getName());
      if (parameter.isCheckbox() && !StringUtil.isDefined(value)) {
        value = "no";
      }
      parameter.setValue(value);
    }
    componentInst.setParameters(parameters);
  }

  private String prepareUpdateInstance(JobStartPagePeasSessionController sessionController,
      HttpServletRequest request) {
    String language = sessionController.getLanguage();
    ComponentInst componentInst = sessionController.getComponentInst(sessionController.
        getManagedInstanceId());
    // Search for component 'generic' label
    WAComponent waComponent = sessionController.getComponentByName(componentInst.getName());
    request.setAttribute(PARAMETERS_ATTR, sessionController.getParameters(waComponent, false));
    request.setAttribute("JobPeas", waComponent.getLabel(language));
    request.setAttribute(PROFILES_ATTR, sessionController.getAllProfiles(componentInst));
    request.setAttribute(COMPONENT_INST_ATTR, componentInst);
    request.setAttribute(SCOPE_ATTR, sessionController.getScope());
    return "/jobStartPagePeas/jsp/updateInstance.jsp";
  }

  private String prepareCreateInstance(JobStartPagePeasSessionController sessionController,
      HttpServletRequest request) {
    String destination;
    String componentName = request.getParameter("ComponentName");
    WAComponent component = sessionController.getComponentByName(componentName);
    if (component != null && component.isVisible()) {
      setSpaceNameInRequest(sessionController, request);
      request.setAttribute(PARAMETERS_ATTR, sessionController.getParameters(component, true));
      request.setAttribute("WAComponent", component);
      request.setAttribute(BROTHERS_ATTR, sessionController.getBrotherComponents(true));
      destination = "/jobStartPagePeas/jsp/createInstance.jsp";
    } else {
      request.setAttribute("When", "ComponentCreation");
      request.setAttribute(CURRENT_SPACE_ID_ATTR, sessionController.getSpaceId());
      String msg = sessionController.getString("JSPP.ErrorUnknownComponent");
      request.setAttribute("ErrorMessage", MessageFormat.format(msg, componentName));
      destination = ERROR_FULL_DEST;
    }
    return destination;
  }

  private void prepareDisplayComponentInfo(JobStartPagePeasSessionController sessionController,
      HttpServletRequest request) {
    ComponentInst componentInst = sessionController.getComponentInst(sessionController.
        getManagedInstanceId());
    // Search for component 'generic' label
    WAComponent waComponent = sessionController.getComponentByName(componentInst.getName());
    request.setAttribute(PARAMETERS_ATTR, sessionController.getParameters(waComponent, false));
    request.setAttribute(COMPONENT_INST_ATTR, componentInst);
    request.setAttribute("JobPeas", waComponent);
    request.setAttribute(PROFILES_ATTR, sessionController.getAllProfiles(componentInst));
    request.setAttribute(INHERITANCE_ATTR, JobStartPagePeasSettings.isInheritanceEnable);
    request.setAttribute("MaintenanceState", sessionController.getCurrentSpaceMaintenanceState());
    request.setAttribute(SCOPE_ATTR, sessionController.getScope());
  }
}
