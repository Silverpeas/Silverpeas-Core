/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.jobstartpage.servlets;

import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.ParameterList;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.web.jobstartpage.JobStartPagePeasSettings;
import org.silverpeas.web.jobstartpage.control.JobStartPagePeasSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.exception.QuotaRuntimeException;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.memory.MemoryUnit;
import org.silverpeas.core.template.SilverpeasTemplate;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobStartPagePeasRequestRouter extends ComponentRequestRouter<JobStartPagePeasSessionController> {

  private static final long serialVersionUID = 3751632991093466433L;
  private static final String WELCOME_SPACE_ADMIN_TEMPLATE_FILE = "/space/welcome_space_admin_";
  private static final String WELCOME_SPACE_MGR_TEMPLATE_FILE = "/space/welcome_space_manager_";

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
   * ********************* Gestion de la page d'accueil d'espace
   * ****************************************
   */
  /**
   * @param function
   * @param jobStartPageSC
   * @param request
   * @return
   * @throws RemoteException
   */
  public String getDestinationStartPage(String function,
      JobStartPagePeasSessionController jobStartPageSC, HttpRequest request) throws
      ClipboardException {
    if (!jobStartPageSC.getClipboardSelectedObjects().isEmpty()) {
      request.setAttribute("ObjectsSelectedInClipboard", "true");
    }
    StartPageFunction startPage;
    try {
      startPage = StartPageFunction.valueOf(function);
    } catch (IllegalArgumentException ex) {
      return null;
    }
    switch (startPage) {
      case ModifyJobStartPage:
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
        request.setAttribute("FirstPageType", Integer.valueOf(spaceint1.getFirstPageType()));
        request.setAttribute("FirstPageParam", spaceint1.getFirstPageExtraParam());
        request.setAttribute("Peas", jobStartPageSC.getManagedSpaceComponents());
        setSpacesNameInRequest(spaceint1, jobStartPageSC, request);
        return "/jobStartPagePeas/jsp/ModifyJobStartPage.jsp";
      case Choice:
        return getDestination(request.getParameter("choix"), jobStartPageSC, request);
      case DefaultStartPage:
        spaceint1 = jobStartPageSC.getSpaceInstById();
        spaceint1.setFirstPageType(SpaceInst.FP_TYPE_STANDARD);
        spaceint1.setFirstPageExtraParam("");
        jobStartPageSC.updateSpaceInst(spaceint1);
        request.setAttribute("urlToReload", "StartPageInfo");
        return "/jobStartPagePeas/jsp/closeWindow.jsp";
      case Portlet:
        spaceint1 = jobStartPageSC.getSpaceInstById();
        spaceint1.setFirstPageType(SpaceInst.FP_TYPE_PORTLET);
        jobStartPageSC.updateSpaceInst(spaceint1);
        request.setAttribute("fullURL", URLUtil.getApplicationURL()
            + "/dt?dt.SpaceId=" + jobStartPageSC.getManagedSpaceId() + "&dt.Role=Admin");
        return "/jobStartPagePeas/jsp/goBack.jsp";
      case SelectPeas:
        spaceint1 = jobStartPageSC.getSpaceInstById();
        spaceint1.setFirstPageType(SpaceInst.FP_TYPE_COMPONENT_INST);
        spaceint1.setFirstPageExtraParam(request.getParameter("peas"));
        jobStartPageSC.updateSpaceInst(spaceint1);
        request.setAttribute("urlToReload", "StartPageInfo");
        return "/jobStartPagePeas/jsp/closeWindow.jsp";
      case URL:
        String url = request.getParameter("URL");
        spaceint1 = jobStartPageSC.getSpaceInstById();
        spaceint1.setFirstPageType(SpaceInst.FP_TYPE_HTML_PAGE);
        spaceint1.setFirstPageExtraParam(url);
        jobStartPageSC.updateSpaceInst(spaceint1);
        request.setAttribute("urlToReload", "StartPageInfo");
        return "/jobStartPagePeas/jsp/closeWindow.jsp";
      case SetPortlet:
        spaceint1 = jobStartPageSC.getSpaceInstById();
        spaceint1.setFirstPageType(SpaceInst.FP_TYPE_PORTLET);
        spaceint1.setFirstPageExtraParam("");
        jobStartPageSC.updateSpaceInst(spaceint1);
        request.setAttribute("urlToReload", "StartPageInfo");
        return "/jobStartPagePeas/jsp/closeWindow.jsp";

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
      jobStartPageSC.init(); // Only ONCE
      destination = "/jobStartPagePeas/jsp/jobStartPage.jsp";
    } else if (function.startsWith("GoToSpace")) {
      if (StringUtil.isDefined(request.getParameter("Espace"))) {
        jobStartPageSC.setSpaceId(request.getParameter("Espace"));
      } else {
        jobStartPageSC.setSpaceId(null);
      }
      request.setAttribute("haveToRefreshMainPage", Boolean.TRUE);
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.jsp";
    } else if ("jobStartPageNav".equals(function)) {
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.jsp";
    } else if ("welcome".equals(function)) {
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
    } else if ("ViewBin".equals(function)) {
      request.setAttribute("Spaces", jobStartPageSC.getRemovedSpaces());
      request.setAttribute("Components", jobStartPageSC.getRemovedComponents());
      destination = "/jobStartPagePeas/jsp/bin.jsp";
    } else if ("RestoreFromBin".equals(function)) {
      String itemId = request.getParameter("ItemId");
      if (StringUtil.isDefined(itemId)) {
        if (itemId.startsWith("WA")) {
          jobStartPageSC.restoreSpaceFromBin(itemId);
        } else {
          jobStartPageSC.restoreComponentFromBin(itemId);
        }
      } else {
        String[] spaceIds = request.getParameterValues("SpaceIds");
        for (int i = 0; spaceIds != null && i < spaceIds.length; i++) {
          jobStartPageSC.restoreSpaceFromBin(spaceIds[i]);
        }
        String[] componentIds = request.getParameterValues("ComponentIds");
        for (int i = 0; componentIds != null && i < componentIds.length; i++) {
          jobStartPageSC.restoreComponentFromBin(componentIds[i]);
        }
      }
      request.setAttribute("haveToRefreshNavBar", Boolean.TRUE);
      destination = getDestination("ViewBin", jobStartPageSC, request);
    } else if ("RemoveDefinitely".equals(function)) {
      String itemId = request.getParameter("ItemId");
      if (StringUtil.isDefined(itemId)) {
        if (itemId.startsWith("WA")) {
          jobStartPageSC.deleteSpaceInBin(itemId);
        } else {
          jobStartPageSC.deleteComponentInBin(itemId);
        }
      } else {
        String[] spaceIds = request.getParameterValues("SpaceIds");
        for (int i = 0; spaceIds != null && i < spaceIds.length; i++) {
          jobStartPageSC.deleteSpaceInBin(spaceIds[i]);
        }
        String[] componentIds = request.getParameterValues("ComponentIds");
        for (int i = 0; componentIds != null && i < componentIds.length; i++) {
          jobStartPageSC.deleteComponentInBin(componentIds[i]);
        }
      }
      destination = getDestinationNavBar("ViewBin", jobStartPageSC, request);
    }

    return destination;
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

    if (function.equals("GoToComponent")) {
      String compoId = request.getParameter("ComponentId");
      jobStartPageSC.setManagedInstanceId(compoId);
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(compoId);
      String espaceId = compoint1.getDomainFatherId(); // WAid
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(espaceId);
      String idFather = spaceint1.getDomainFatherId();
      if ("0".equals(idFather) || idFather == null) {// l'instance appartient à un espace
        jobStartPageSC.setSpaceId(spaceint1.getId());
      }

      destination = "/jobStartPagePeas/jsp/componentInfo.jsp";
    } else if ("SetupComponent".equals(function)) {
      String compoId = request.getParameter("ComponentId");
      if (jobStartPageSC.isComponentManageable(compoId)) {
        jobStartPageSC.setManagedInstanceId(compoId,
            JobStartPagePeasSessionController.SCOPE_FRONTOFFICE);
        destination = getDestination("UpdateInstance", jobStartPageSC, request);
      } else {
        destination = "/admin/jsp/accessForbidden.jsp";
      }
    } else if ("GoToCurrentComponent".equals(function)) {
      destination = "/jobStartPagePeas/jsp/componentInfo.jsp";
    } else if ("ListComponent".equals(function)) {
      setSpacesNameInRequest(jobStartPageSC, request);
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
        destination = getDestination("GoToCurrentComponent", jobStartPageSC, request);
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas déportée vers les
        // appelants
        request.setAttribute("When", "ComponentCreation");
        request.setAttribute("ErrorMessage", sErrorMessage);

        setSpacesNameInRequest(jobStartPageSC, request);

        destination = "/jobStartPagePeas/jsp/error.jsp";
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
        destination = getDestination("GoToCurrentComponent", jobStartPageSC, request);
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "ComponentUpdate");
        setSpacesNameInRequest(jobStartPageSC, request);
        destination = "/jobStartPagePeas/jsp/error.jsp";
      }
    } else if (function.equals("DeleteInstance")) {
      // Delete the instance
      jobStartPageSC.deleteComponentInst(jobStartPageSC.getManagedInstanceId());

      refreshNavBar(jobStartPageSC, request);
      destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
    } else if (function.equals("RoleInstance")) {
      String profileId = request.getParameter("IdProfile");
      String profileName = request.getParameter("NameProfile");
      String profileLabel = request.getParameter("LabelProfile");

      ProfileInst profile = jobStartPageSC.getProfile(profileId, profileName, profileLabel);
      jobStartPageSC.setManagedProfile(profile);
      request.setAttribute("Profile", profile);

      destination = "/jobStartPagePeas/jsp/roleInstance.jsp";
    } else if (function.equals("CurrentRoleInstance")) {
      request.setAttribute("Profile", jobStartPageSC.getManagedProfile());

      destination = "/jobStartPagePeas/jsp/roleInstance.jsp";
    } else if (function.equals("SelectUsersGroupsProfileInstance")) {
      List<String> userIds = (List<String>) StringUtil
          .splitString(request.getParameter("UserPanelCurrentUserIds"), ',');
      List<String> groupIds = (List<String>) StringUtil
          .splitString(request.getParameter("UserPanelCurrentGroupIds"), ',');
      jobStartPageSC
          .initUserPanelInstanceForGroupsUsers((String) request.getAttribute("myComponentURL"),
              userIds, groupIds);
      destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    } else if (function.equals("EffectiveSetInstanceProfile")) {
      String[] userIds =
          StringUtil.split(request.getParameter("roleItems" + "UserPanelCurrentUserIds"), ',');
      String[] groupIds = StringUtil
          .split(request.getParameter("roleItems" + "UserPanelCurrentGroupIds"), ',');
      jobStartPageSC.updateInstanceProfile(userIds, groupIds);
      destination = getDestination("CurrentRoleInstance", jobStartPageSC, request);
    } else if (function.equals("PlaceComponentAfter")) {
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(
          jobStartPageSC.getManagedInstanceId());
      request.setAttribute("currentComponentName", compoint1.getLabel());
      request.setAttribute("brothers", jobStartPageSC.getBrotherComponents(false));
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      request.setAttribute("currentSpace", spaceint1);
      destination = "/jobStartPagePeas/jsp/placeComponentAfter.jsp";
    } else if (function.equals("EffectivePlaceComponent")) {
      jobStartPageSC.setComponentPlace(request.getParameter("ComponentBefore"));
      refreshNavBar(jobStartPageSC, request);
      request.setAttribute("urlToReload", "GoToCurrentComponent");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.startsWith("copy")) {
      String objectId = request.getParameter("Id");
      String objectType = request.getParameter("Type");
      try {
        if ("Space".equals(objectType)) {
          jobStartPageSC.copySpace(objectId);
        } else {
          jobStartPageSC.copyComponent(objectId);
        }
      } catch (Exception e) {
        throw new AdminException(
            "JobStartPagePeasRequestRouter.getDestination()",
            SilverpeasException.ERROR, "jobStartPagePeas.CANT_COPY_COMPONENT",
            e);
      }
      destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null)
          + "Idle.jsp?message=REFRESHCLIPBOARD";
    } else if ("Cut".equals(function)) {
      String objectId = request.getParameter("Id");
      String objectType = request.getParameter("Type");
      try {
        if ("Space".equals(objectType)) {
          jobStartPageSC.cutSpace(objectId);
        } else if ("Component".equals(objectType)) {
          jobStartPageSC.cutComponent(objectId);
        }
      } catch (Exception e) {
        throw new AdminException("JobStartPagePeasRequestRouter.getDestination()",
            SilverpeasException.ERROR, "jobStartPagePeas.CANT_CUT_ITEM", e);
      }
      destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null)
          + "Idle.jsp?message=REFRESHCLIPBOARD";
    } else if ("Paste".equals(function)) {
      Map<String, String> options = getPasteOptions(request);
      try {
        jobStartPageSC.paste(options);
      } catch (Exception e) {
        throw new AdminException("JobStartPagePeasRequestRouter.getDestination()",
            SilverpeasException.ERROR, "jobStartPagePeas.CANT_PAST_COMPONENT", e);
      }
      refreshNavBar(jobStartPageSC, request);
      if (StringUtil.isDefined(jobStartPageSC.getManagedSpaceId())) {
        destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
      } else {
        destination = getDestination("welcome", jobStartPageSC, request);
      }
    } else if (function.equals("OpenComponent")) {
      // check if user can update it
      String id = request.getParameter("ComponentId");
      if (!jobStartPageSC.isComponentManageable(id)) {
        destination = "/admin/jsp/accessForbidden.jsp";
      } else {
        jobStartPageSC.init();
        request.setAttribute("PopupMode", true);
        destination = getDestination("GoToComponent", jobStartPageSC, request);
      }
    }

    return destination;
  }

  private Map<String, String> getPasteOptions(HttpServletRequest request) {
    Map<String, String[]> parameters = request.getParameterMap();
    Map<String, String> pasteOptions = new HashMap<>();
    for (String parameterName : parameters.keySet()) {
      if (parameterName.startsWith(PasteDetail.OPTION_PREFIX)) {
        pasteOptions.put(parameterName, parameters.get(parameterName)[0]);
      }
    }
    return pasteOptions;
  }

  public String getDestinationSpace(String function,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpRequest request) throws Exception {
    String destination = null;

    if (function.equals("StartPageInfo")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(); // espace courant

      if (spaceint1 == null) {
        destination = getDestination("welcome", jobStartPageSC, request);
      } else {
        destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
      }
    } else if (function.equals("GoToSubSpace")) {
      String subSpaceId = request.getParameter("SubSpace");
      if (StringUtil.isDefined(subSpaceId)) {
        jobStartPageSC.setSubSpaceId(request.getParameter("SubSpace"));
        request.setAttribute("haveToRefreshMainPage", Boolean.TRUE);
      }
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.jsp";
    } else if (function.equals("DesactivateMaintenance")) {
      String allIntranet = request.getParameter("allIntranet");
      if ("1".equals(allIntranet)) {
        jobStartPageSC.setAppModeMaintenance(false);
        destination = getDestination("welcome", jobStartPageSC, request);
      } else {
        String spaceId = jobStartPageSC.getManagedSpaceId();
        jobStartPageSC.setSpaceMaintenance(spaceId, false);
        refreshNavBar(jobStartPageSC, request);
        destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
      }
    } else if (function.equals("ActivateMaintenance")) {
      String allIntranet = request.getParameter("allIntranet");
      if ("1".equals(allIntranet)) {
        jobStartPageSC.setAppModeMaintenance(true);
        destination = getDestination("welcome", jobStartPageSC, request);
      } else {
        String spaceId = jobStartPageSC.getManagedSpaceId();
        jobStartPageSC.setSpaceMaintenance(spaceId, true);
        refreshNavBar(jobStartPageSC, request);
        destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
      }
    } else if (function.equals("PlaceSpaceAfter")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      request.setAttribute("CurrentSpace", spaceint1);
      request.setAttribute("Brothers", jobStartPageSC.getBrotherSpaces(false));
      destination = "/jobStartPagePeas/jsp/placeSpaceAfter.jsp";
    } else if (function.equals("EffectivePlaceSpaceAfter")) {
      jobStartPageSC.setSpacePlace(request.getParameter("SpaceBefore"));
      refreshNavBar(jobStartPageSC, request);
      request.setAttribute("urlToReload", "StartPageInfo");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("CreateSpace")) {
      setSpacesNameInRequest(jobStartPageSC, request);

      request.setAttribute("SousEspace", request.getParameter("SousEspace"));
      request.setAttribute("spaceTemplates", jobStartPageSC.getAllSpaceTemplates());
      request.setAttribute("brothers", jobStartPageSC.getBrotherSpaces(true));
      request.setAttribute("isUserAdmin", Boolean.valueOf(jobStartPageSC.isUserAdmin()));
      destination = "/jobStartPagePeas/jsp/createSpace.jsp";
    } else if (function.equals("SetSpaceTemplateProfile")) {
      String spaceTemplate = request.getParameter("SpaceTemplate");

      if (request.getParameter("NameObject") != null
          && request.getParameter("NameObject").length() > 0) {
        jobStartPageSC.setCreateSpaceParameters(request.getParameter("NameObject"), request.
            getParameter("Description"),
            request.getParameter("SousEspace"), spaceTemplate, I18NHelper.getSelectedContentLanguage(
                request),
            request.getParameter("SelectedLook"),
            request.getParameter("ComponentSpaceQuota"),
            request.getParameter("DataStorageQuota"));
      }

      destination = getDestinationSpace("EffectiveCreateSpace", jobStartPageSC, request);

    } else if (function.equals("EffectiveCreateSpace")) { // Space CREATE action
      String spaceId = jobStartPageSC.createSpace();
      if (spaceId != null && spaceId.length() > 0) {
        jobStartPageSC.setSpacePlace(request.getParameter("SpaceBefore"));
        refreshNavBar(jobStartPageSC, request);
        destination = getDestinationSpace("StartPageInfo", jobStartPageSC, request);
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "SpaceCreation");
        setSpacesNameInRequest(jobStartPageSC, request);
        destination = "/jobStartPagePeas/jsp/error.jsp";
      }
    } else if (function.equals("UpdateSpace")) {
      String translation = request.getParameter("Translation");
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);
      request.setAttribute("Space", spaceint1);
      request.setAttribute("Translation", translation);
      request.setAttribute("IsInheritanceEnable", Boolean.valueOf(
          JobStartPagePeasSettings.isInheritanceEnable));
      request.setAttribute("isUserAdmin", Boolean.valueOf(jobStartPageSC.isUserAdmin()));

      destination = "/jobStartPagePeas/jsp/updateSpace.jsp";
    } else if (function.equals("EffectiveUpdateSpace")) {
      // Update the space
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      request2SpaceInst(spaceint1, jobStartPageSC, request);

      String spaceId = jobStartPageSC.updateSpaceInst(spaceint1);
      if (spaceId != null && spaceId.length() > 0) {
        refreshNavBar(jobStartPageSC, request);
        destination = getDestinationSpace("StartPageInfo", jobStartPageSC, request);
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "SpaceUpdate");

        setSpacesNameInRequest(jobStartPageSC, request);

        destination = "/jobStartPagePeas/jsp/error.jsp";
      }
    } else if (function.equals("DeleteSpace")) {
      // Delete the space
      String spaceId = request.getParameter("Id");
      jobStartPageSC.deleteSpace(spaceId);
      refreshNavBar(jobStartPageSC, request);
      if ((jobStartPageSC.getManagedSpaceId() != null)
          && (jobStartPageSC.getManagedSpaceId().length() > 0)) {
        destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
      } else {
        destination = getDestination("welcome", jobStartPageSC, request);
      }
    } else if (function.equals("RecoverSpaceRights")) {
      String spaceId = request.getParameter("Id");
      if (StringUtil.isDefined(spaceId)) {
        jobStartPageSC.recoverSpaceRights(spaceId);
      } else {
        if (jobStartPageSC.isUserAdmin()) {
          jobStartPageSC.recoverSpaceRights(null);
        }
      }
      destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
    } else if (function.equals("SpaceManager")) {
      String role = request.getParameter("Role");
      if (!StringUtil.isDefined(role)) {
        role = "Manager";
      }

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      SpaceProfileInst profile = spaceint1.getSpaceProfileInst(role);
      if (profile != null) {
        request.setAttribute("Profile", profile);
      }

      // get groups and users which manage current space
      List<Group> groupes = jobStartPageSC.getAllCurrentGroupSpace(role);
      List<UserDetail> users = jobStartPageSC.getAllCurrentUserSpace(role);
      request.setAttribute("listGroupSpace", groupes);
      request.setAttribute("listUserSpace", users);

      request.setAttribute("SpaceExtraInfos", jobStartPageSC.getManagedSpace());
      request.setAttribute("ProfileEditable", jobStartPageSC.isProfileEditable());
      request.setAttribute("Role", role);

      if ("Manager".equals(role)) {
        request.setAttribute("InheritedProfile", "Manager");
        // get groups and users which manage space parent
        request.setAttribute("listInheritedGroups", jobStartPageSC.getGroupsManagerOfParentSpace());
        request.setAttribute("listInheritedUsers", jobStartPageSC.getUsersManagerOfParentSpace());
        request.setAttribute("IsInheritanceEnable", true);
      } else {
        // get inherited profile
        SpaceProfileInst inheritedProfile = spaceint1.getInheritedSpaceProfileInst(role);
        if (inheritedProfile != null) {
          request.setAttribute("InheritedProfile", inheritedProfile);
          request.setAttribute("listInheritedGroups",
              jobStartPageSC.groupIds2groups(inheritedProfile.getAllGroups()));
          request.setAttribute("listInheritedUsers", jobStartPageSC.userIds2users(inheritedProfile.
              getAllUsers()));
        }
        request.setAttribute("IsInheritanceEnable", JobStartPagePeasSettings.isInheritanceEnable);
      }

      destination = "/jobStartPagePeas/jsp/spaceManager.jsp";
    } else if (function.equals("SelectUsersGroupsSpace")) {
      List<String> userIds = (List<String>) StringUtil
          .splitString(request.getParameter("UserPanelCurrentUserIds"), ',');
      List<String> groupIds = (List<String>) StringUtil
          .splitString(request.getParameter("UserPanelCurrentGroupIds"), ',');
      jobStartPageSC
          .initUserPanelSpaceForGroupsUsers((String) request.getAttribute("myComponentURL"),
              userIds, groupIds);
      destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    } else if (function.equals("EffectiveSetSpaceProfile")) {
      String role = request.getParameter("Role");
      List<String> userIds = (List<String>)
          StringUtil.splitString(request.getParameter("roleItems" + "UserPanelCurrentUserIds"), ',');
      List<String> groupIds = (List<String>) StringUtil
          .splitString(request.getParameter("roleItems" + "UserPanelCurrentGroupIds"), ',');
      jobStartPageSC.updateSpaceRole(role, userIds, groupIds);
      destination = getDestination("SpaceManager", jobStartPageSC, request);
    } else if (function.equals("SpaceLook")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      request.setAttribute("Space", spaceint1);
      request.setAttribute("SpaceLookHelper", jobStartPageSC.getSpaceLookHelper());
      request.setAttribute("SpaceExtraInfos", jobStartPageSC.getManagedSpace());
      request.setAttribute("IsInheritanceEnable", Boolean.valueOf(
          JobStartPagePeasSettings.isInheritanceEnable));

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

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
      destination = getDestination("SpaceLook", jobStartPageSC, request);
    } else if (function.equals("RemoveFileToLook")) {
      String fileName = request.getParameter("FileName");
      jobStartPageSC.removeExternalElementOfSpaceAppearance(fileName);
      destination = getDestination("SpaceLook", jobStartPageSC, request);
    } else if (function.equals("OpenSpace")) {
      jobStartPageSC.init();

      if (StringUtil.isDefined(request.getParameter("Espace"))) {
        jobStartPageSC.setSpaceId(request.getParameter("Espace"));
      } else {
        jobStartPageSC.setSpaceId(null);
      }

      destination = getDestination("StartPageInfo", jobStartPageSC, request);
    } else if (function.equals("OpenSubSpace")) {
      jobStartPageSC.init();

      if (StringUtil.isDefined(request.getParameter("Espace"))) {
        jobStartPageSC.setSpaceId(request.getParameter("Espace"));
        if (StringUtil.isDefined(request.getParameter("SousEspace"))) {
          jobStartPageSC.setSubSpaceId(request.getParameter("SousEspace"));
        }
      } else {
        jobStartPageSC.setSpaceId(null);
      }

      destination = getDestination("StartPageInfo", jobStartPageSC, request);
    }

    return destination;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobStartPageSC
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, JobStartPagePeasSessionController jobStartPageSC,
      HttpRequest request) {
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
        request.setAttribute("CurrentSpaceId", jobStartPageSC.getSpaceId());
        request.setAttribute("CurrentSubSpaceId", jobStartPageSC.getSubSpaceId());
      } else if ("/jobStartPagePeas/jsp/welcome.jsp".equals(destination)) {
        request.setAttribute("isUserAdmin", Boolean.valueOf(jobStartPageSC.isUserAdmin()));
        request.setAttribute("globalMode", Boolean.valueOf(jobStartPageSC.isAppInMaintenance()));
        request.setAttribute("IsBackupEnable", jobStartPageSC.isBackupEnable());
        request.setAttribute("IsBasketEnable", JobStartPagePeasSettings.isBasketEnable);
      } else if ("/jobStartPagePeas/jsp/startPageInfo.jsp".equals(destination)) {
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(); // espace
        // courant
        request.setAttribute("isUserAdmin", Boolean.valueOf(jobStartPageSC.isUserAdmin()));
        request.setAttribute("FirstPageType", Integer.valueOf(spaceint1.getFirstPageType()));
        request.setAttribute("Description", spaceint1.getDescription());
        String spaceId = jobStartPageSC.getManagedSpaceId();
        request.setAttribute("currentSpaceId", spaceId);
        request.setAttribute("MaintenanceState", jobStartPageSC.getCurrentSpaceMaintenanceState());

        setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

        request.setAttribute("SpaceExtraInfos", jobStartPageSC.getManagedSpace());
        request.setAttribute("NameProfile", jobStartPageSC.getSpaceProfileName(spaceint1));
        request.setAttribute("IsBackupEnable", jobStartPageSC.isBackupEnable());

        request.setAttribute("IsInheritanceEnable", JobStartPagePeasSettings.isInheritanceEnable);

        request.setAttribute("CopiedComponents", jobStartPageSC.getCopiedComponents());

        request.setAttribute("Space", spaceint1);
      } else if ("/jobStartPagePeas/jsp/componentInfo.jsp".equals(destination)) {
        prepareDisplayComponentInfo(jobStartPageSC, request);
      } else if ("/jobStartPagePeas/jsp/roleInstance.jsp".equals(destination)) {
        ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC.
            getManagedInstanceId());
        request.setAttribute("ComponentInst", compoint1);

        request.setAttribute("Profiles", jobStartPageSC.getAllProfiles(compoint1));

        // Profile, liste des groupes et user du role courant
        ProfileInst profile = jobStartPageSC.getManagedProfile();
        request.setAttribute("Profile", profile);
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
        request.setAttribute("IsInheritanceEnable", Boolean.valueOf(
            JobStartPagePeasSettings.isInheritanceEnable));

        String profileHelp = jobStartPageSC.getManagedProfileHelp(compoint1.getName());
        request.setAttribute("ProfileHelp", profileHelp);

        request.setAttribute("Scope", jobStartPageSC.getScope());
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
    request.setAttribute("haveToRefreshNavBar", Boolean.TRUE);
  }

  private void setSpacesNameInRequest(
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
    setSpacesNameInRequest(jobStartPageSC.getSpaceInstById(), jobStartPageSC,
        request);
  }

  private void setSpacesNameInRequest(SpaceInst spaceint1,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
    if (spaceint1 != null) {
      request.setAttribute("CurrentSpaceId", spaceint1.getId());
      if (!spaceint1.isRoot()) {// je suis sur un ss-espace
        request.setAttribute("nameSubSpace", spaceint1.getName(jobStartPageSC.getLanguage()));
      } else {
        request.setAttribute("nameSubSpace", null);
      }
    } else {
      request.setAttribute("nameSubSpace", null);
    }
  }

  private void request2SpaceInst(SpaceInst spaceInst,
      JobStartPagePeasSessionController jobStartPageSC, HttpServletRequest request) {
    String name = request.getParameter("NameObject");
    String desc = request.getParameter("Description");
    String componentSpaceQuotaMaxCount = request.getParameter("ComponentSpaceQuota");
    String dataStorageQuotaMaxCount = request.getParameter("DataStorageQuota");
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

    // Component space quota
    if (jobStartPageSC.isUserAdmin() && JobStartPagePeasSettings.componentsInSpaceQuotaActivated
        && StringUtil.isDefined(componentSpaceQuotaMaxCount)) {
      try {
        spaceInst.setComponentSpaceQuotaMaxCount(Integer.valueOf(componentSpaceQuotaMaxCount));
      } catch (QuotaException qe) {
        throw new QuotaRuntimeException("Space", SilverpeasRuntimeException.ERROR, qe.getMessage(),
            qe);
      }
    }

    // Data storage quota
    if (jobStartPageSC.isUserAdmin() && JobStartPagePeasSettings.dataStorageInSpaceQuotaActivated
        && StringUtil.isDefined(dataStorageQuotaMaxCount)) {
      try {
        spaceInst.setDataStorageQuotaMaxCount(UnitUtil.convertTo(
            Long.valueOf(dataStorageQuotaMaxCount), MemoryUnit.MB, MemoryUnit.B));
      } catch (QuotaException qe) {
        throw new QuotaRuntimeException("Space", SilverpeasRuntimeException.ERROR, qe.getMessage(),
            qe);
      }
    }
  }

  private void request2ComponentInst(ComponentInst componentInst,
      HttpServletRequest request,
      JobStartPagePeasSessionController jobStartPageSC) {
    String name = request.getParameter("NameObject");
    String desc = request.getParameter("Description");
    if (desc == null) {
      desc = "";
    }
    String pPublic = request.getParameter("PublicComponent");
    String pHidden = request.getParameter("HiddenComponent");
    String pInheritance = request.getParameter("InheritanceBlocked");
    componentInst.setLabel(name);
    componentInst.setDescription(desc);
    componentInst.setPublic(StringUtil.isDefined(pPublic));
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

    ParameterList parameters = component.getAllParameters().clone();
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
    request.setAttribute("Parameters", sessionController.getParameters(waComponent, false));
    request.setAttribute("JobPeas", waComponent.getLabel(language));
    request.setAttribute("Profiles", sessionController.getAllProfiles(componentInst));
    request.setAttribute("ComponentInst", componentInst);
    request.setAttribute("IsInheritanceEnable", JobStartPagePeasSettings.isInheritanceEnable);
    request.setAttribute("Scope", sessionController.getScope());
    return "/jobStartPagePeas/jsp/updateInstance.jsp";
  }

  private String prepareCreateInstance(JobStartPagePeasSessionController sessionController,
      HttpServletRequest request) {
    String destination;
    String componentName = request.getParameter("ComponentName");
    WAComponent component = sessionController.getComponentByName(componentName);
    if (component != null && component.isVisible()) {
      setSpacesNameInRequest(sessionController, request);
      request.setAttribute("Parameters", sessionController.getParameters(component, true));
      request.setAttribute("WAComponent", component);
      request.setAttribute("brothers", sessionController.getBrotherComponents(true));
      destination = "/jobStartPagePeas/jsp/createInstance.jsp";
    } else {
      request.setAttribute("When", "ComponentCreation");
      request.setAttribute("CurrentSpaceId", sessionController.getSpaceId());
      String msg = sessionController.getString("JSPP.ErrorUnknownComponent");
      request.setAttribute("ErrorMessage", MessageFormat.format(msg, componentName));
      destination = "/jobStartPagePeas/jsp/error.jsp";
    }
    return destination;
  }

  private void prepareDisplayComponentInfo(JobStartPagePeasSessionController sessionController,
      HttpServletRequest request) {
    ComponentInst componentInst = sessionController.getComponentInst(sessionController.
        getManagedInstanceId());
    // Search for component 'generic' label
    WAComponent waComponent = sessionController.getComponentByName(componentInst.getName());
    request.setAttribute("Parameters", sessionController.getParameters(waComponent, false));
    request.setAttribute("ComponentInst", componentInst);
    request.setAttribute("JobPeas", waComponent);
    request.setAttribute("Profiles", sessionController.getAllProfiles(componentInst));
    request.setAttribute("IsInheritanceEnable", JobStartPagePeasSettings.isInheritanceEnable);
    request.setAttribute("MaintenanceState", sessionController.getCurrentSpaceMaintenanceState());
    request.setAttribute("Scope", sessionController.getScope());
  }
}