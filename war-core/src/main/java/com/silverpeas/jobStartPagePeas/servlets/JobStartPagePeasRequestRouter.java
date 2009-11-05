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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.jobStartPagePeas.servlets;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings;
import com.silverpeas.jobStartPagePeas.SpaceLookHelper;
import com.silverpeas.jobStartPagePeas.control.JobStartPagePeasSessionController;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameters;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import java.rmi.RemoteException;

public class JobStartPagePeasRequestRouter extends ComponentRequestRouter {

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobStartPagePeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "jobStartPagePeas";
  }

  /*********************** Gestion de la page d'accueil d'espace *****************************************/
  public String getDestinationStartPage(String function,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) throws RemoteException {
    String destination = null;

    if (jobStartPageSC.getClipboard().getSelectedObjects().size() > 0) {
      request.setAttribute("ObjectsSelectedInClipboard", "true");
    }

    if (function.equals("UpdateJobStartPage")) // modif page d'accueil
    {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      request.setAttribute("FirstPageType", new Integer(spaceint1
          .getFirstPageType()));
      request
          .setAttribute("FirstPageParam", spaceint1.getFirstPageExtraParam());
      request.setAttribute("Peas", jobStartPageSC.getManagedSpaceComponents());

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      destination = "/jobStartPagePeas/jsp/UpdateJobStartPage.jsp";
    } else if (function.equals("Choice")) {
      destination = getDestination((String) request.getParameter("choix"),
          jobStartPageSC, request);
    } else if (function.equals("DefaultStartPage")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      spaceint1.setFirstPageType(SpaceInst.FP_TYPE_STANDARD);
      spaceint1.setFirstPageExtraParam("");
      jobStartPageSC.updateSpaceInst(spaceint1);

      request.setAttribute("urlToReload", "StartPageInfo");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("Portlet")) {
      // Temporary case until jsr168 will be officially released
      if (jobStartPageSC.isJSR168Used()) {
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
        spaceint1.setFirstPageType(SpaceInst.FP_TYPE_PORTLET);
        jobStartPageSC.updateSpaceInst(spaceint1);

        request.setAttribute("fullURL", GeneralPropertiesManager
            .getGeneralResourceLocator().getString("ApplicationURL")
            + "/dt?dt.SpaceId="
            + jobStartPageSC.getManagedSpaceId()
            + "&dt.Role=Admin");
      } else {
        request.setAttribute("fullURL", GeneralPropertiesManager
            .getGeneralResourceLocator().getString("ApplicationURL")
            + "/Rportlet/jsp/admin?spaceId="
            + jobStartPageSC.getManagedSpaceId());
      }
      destination = "/jobStartPagePeas/jsp/goBack.jsp";
    } else if (function.equals("SelectPeas")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      spaceint1.setFirstPageType(SpaceInst.FP_TYPE_COMPONENT_INST);
      spaceint1.setFirstPageExtraParam(request.getParameter("peas"));
      jobStartPageSC.updateSpaceInst(spaceint1);

      request.setAttribute("urlToReload", "StartPageInfo");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("URL")) {
      // C'est le quatrième type de page d'accueil d'espace
      String url = request.getParameter("URL");

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      spaceint1.setFirstPageType(SpaceInst.FP_TYPE_HTML_PAGE);
      spaceint1.setFirstPageExtraParam(url);
      jobStartPageSC.updateSpaceInst(spaceint1);

      request.setAttribute("urlToReload", "StartPageInfo");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("SetPortlet")) // on a validé des portlets
    {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      spaceint1.setFirstPageType(SpaceInst.FP_TYPE_PORTLET);
      spaceint1.setFirstPageExtraParam("");
      jobStartPageSC.updateSpaceInst(spaceint1);

      request.setAttribute("urlToReload", "StartPageInfo");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    }
    return destination;
  }

  /*********************** Gestion de la navigation à gauche *****************************************/
  public String getDestinationNavBar(String function,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
    String destination = null;

    if (function.equals("Main")) {
      jobStartPageSC.init(); // Only ONCE
      destination = "/jobStartPagePeas/jsp/jobStartPage.jsp";
    } else if (function.startsWith("GoToSpace")) {
      if ((request.getParameter("Espace") != null)
          && (request.getParameter("Espace").length() > 0)) {
        jobStartPageSC.setSpaceId(request.getParameter("Espace"));
      } else {
        jobStartPageSC.setSpaceId(null);
      }
      request.setAttribute("haveToRefreshMainPage", new Boolean(true));
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.jsp";
    } else if (function.equals("jobStartPageNav")) {
      destination = "/jobStartPagePeas/jsp/jobStartPageNav.jsp";
    } else if (function.equals("welcome")) {
      request.setAttribute("isUserAdmin", new Boolean(jobStartPageSC
          .isUserAdmin()));
      request.setAttribute("globalMode", new Boolean(jobStartPageSC
          .isAppInMaintenance()));
      request.setAttribute("IsBackupEnable", jobStartPageSC.isBackupEnable());
      request.setAttribute("IsBasketEnable", new Boolean(
          JobStartPagePeasSettings.isBasketEnable));

      destination = "/jobStartPagePeas/jsp/welcome.jsp";
    } else if (function.equals("ViewBin")) {
      request.setAttribute("Spaces", jobStartPageSC.getRemovedSpaces());
      request.setAttribute("Components", jobStartPageSC.getRemovedComponents());

      destination = "/jobStartPagePeas/jsp/bin.jsp";
    } else if (function.equals("RestoreFromBin")) {
      String itemId = request.getParameter("ItemId");
      if (isDefined(itemId)) {
        if (itemId.startsWith("WA")) {
          jobStartPageSC.restoreSpaceFromBin(itemId);
        } else {
          jobStartPageSC.restoreComponentFromBin(itemId);
        }
      } else {
        String[] itemIds = request.getParameterValues("ItemIds");
        for (int i = 0; itemIds != null && i < itemIds.length; i++) {
          itemId = itemIds[i];
          if (itemId.startsWith("WA")) {
            jobStartPageSC.restoreSpaceFromBin(itemId);
          } else {
            jobStartPageSC.restoreComponentFromBin(itemId);
          }
        }
      }

      request.setAttribute("haveToRefreshNavBar", new Boolean(true));

      destination = getDestination("ViewBin", jobStartPageSC, request);
    } else if (function.equals("RemoveDefinitely")) {
      String itemId = request.getParameter("ItemId");
      if (isDefined(itemId)) {
        if (itemId.startsWith("WA")) {
          jobStartPageSC.deleteSpaceInBin(itemId);
        } else {
          jobStartPageSC.deleteComponentInBin(itemId);
        }
      } else {
        String[] itemIds = request.getParameterValues("ItemIds");
        for (int i = 0; itemIds != null && i < itemIds.length; i++) {
          itemId = itemIds[i];
          if (itemId.startsWith("WA")) {
            jobStartPageSC.deleteSpaceInBin(itemId);
          } else {
            jobStartPageSC.deleteComponentInBin(itemId);
          }
        }
      }

      destination = getDestinationNavBar("ViewBin", jobStartPageSC, request);
    }

    return destination;
  }

  /*********************** Gestion des composants *****************************************/
  public String getDestinationComponent(String function,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) throws AdminException {
    String destination = null;

    ResourcesWrapper resource = (ResourcesWrapper) request
        .getAttribute("resources");
    ResourceLocator resourcePeasCore = new ResourceLocator("license.license",
        "");
    String licenseCoupercoller = resourcePeasCore.getString("coupercoller");

    if (function.equals("GoToComponent")) {
      String compoId = (String) request.getParameter("ComponentId");
      jobStartPageSC.setManagedInstanceId(compoId);
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(compoId);
      String espaceId = compoint1.getDomainFatherId(); // WAid
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(espaceId);
      String idFather = spaceint1.getDomainFatherId();
      if (idFather == null || (idFather != null && idFather.equals("0"))) {// l'instance
        // appartient
        // à
        // un
        // espace
        jobStartPageSC.setSpaceId(spaceint1.getId());
      }
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasRequestRouter.GoToComponent",
          "root.MSG_GEN_PARAM_VALUE", "compoId = " + compoId);
      destination = "/jobStartPagePeas/jsp/componentInfo.jsp";
    } else if (function.equals("GoToCurrentComponent")) {
      destination = "/jobStartPagePeas/jsp/componentInfo.jsp";
    } else if (function.equals("ListComponent")) {
      setSpacesNameInRequest(jobStartPageSC, request);

      request.setAttribute("ListComponents", jobStartPageSC.getAllComponents());

      destination = "/jobStartPagePeas/jsp/componentsList.jsp";
    } else if (function.equals("CreateInstance")) {
      String componentNum = (String) request.getParameter("ComponentNum");

      WAComponent componentInstSelected = jobStartPageSC
          .getComponentByNum(Integer.parseInt(componentNum));

      setSpacesNameInRequest(jobStartPageSC, request);

      List visibleParameters = getVisibleParameters(componentInstSelected
          .getSortedParameters());

      SPParameter hiddenParam = new SPParameter("HiddenComponent", resource
          .getString("JSPP.hiddenComponent"), "", false, "always", "checkbox",
          null, "useless", "no", null);
      visibleParameters.add(0, hiddenParam);

      if (JobStartPagePeasSettings.isPublicParameterEnable) {
        SPParameter publicParam = new SPParameter("PublicComponent", resource
            .getString("JSPP.publicComponent"), "", false, "always",
            "checkbox", null, "useless", "no", null);
        visibleParameters.add(0, publicParam);
      }

      request.setAttribute("Parameters", visibleParameters);
      request.setAttribute("HiddenParameters",
          getHiddenParameters(componentInstSelected.getSortedParameters()));

      request.setAttribute("ComponentNum", componentNum);
      request.setAttribute("WAComponent", componentInstSelected);
      request.setAttribute("brothers", jobStartPageSC
          .getBrotherComponents(true));

      destination = "/jobStartPagePeas/jsp/createInstance.jsp";
    } else if (function.equals("EffectiveCreateInstance")) {
      // Create the component
      ComponentInst componentInst = new ComponentInst();

      request2ComponentInst(componentInst, request, jobStartPageSC);

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      componentInst.setDomainFatherId(spaceint1.getId());

      // Add the component
      String sComponentId = jobStartPageSC.addComponentInst(componentInst);
      if (sComponentId != null && sComponentId.length() > 0) {
        jobStartPageSC.setManagedInstanceId(sComponentId);
        jobStartPageSC.setComponentPlace(request
            .getParameter("ComponentBefore"));
        refreshNavBar(jobStartPageSC, request);
        request.setAttribute("urlToReload", "GoToComponent?ComponentId="
            + sComponentId);
        destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
      } else {
        // TODO : Mauvaise gestion des exceptions
        // Si la création de l'espace se passe mal alors l'exception n'est pas
        // déportée vers les appelants
        request.setAttribute("When", "ComponentCreation");

        setSpacesNameInRequest(jobStartPageSC, request);

        destination = "/jobStartPagePeas/jsp/error.jsp";
      }
    } else if (function.equals("UpdateInstance")) {
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC
          .getManagedInstanceId());

      // Search for component 'generic' label
      String sCompoName = compoint1.getName();
      WAComponent componentInstBase = jobStartPageSC
          .getComponentByName(sCompoName);
      sCompoName = componentInstBase.getLabel();

      SPParameters xmlParameters = componentInstBase.getSPParameters();
      SPParameters dbParameters = compoint1.getSPParameters();

      SPParameters parameters = (SPParameters) xmlParameters.clone();
      parameters.mergeWith(dbParameters.getParameters());

      List visibleParameters = getVisibleParameters(parameters
          .getSortedParameters());

      String isHidden = "";
      if (compoint1.isHidden()) {
        isHidden = "yes";
      }

      SPParameter hiddenParam = new SPParameter("HiddenComponent", resource
          .getString("JSPP.hiddenComponent"), isHidden, false, "always",
          "checkbox", null, "useless", "no", null);
      visibleParameters.add(0, hiddenParam);

      if (JobStartPagePeasSettings.isPublicParameterEnable) {
        String isPublic = "";
        if (compoint1.isPublic()) {
          isPublic = "yes";
        }

        SPParameter publicParam = new SPParameter("PublicComponent", resource
            .getString("JSPP.publicComponent"), isPublic, false, "always",
            "checkbox", null, "useless", "no", null);
        visibleParameters.add(0, publicParam);
      }

      request.setAttribute("Parameters", visibleParameters);
      request.setAttribute("HiddenParameters", getHiddenParameters(parameters
          .getSortedParameters()));

      request.setAttribute("JobPeas", sCompoName);
      request.setAttribute("ComponentInst", compoint1);

      request.setAttribute("IsInheritanceEnable", new Boolean(
          JobStartPagePeasSettings.isInheritanceEnable));

      String espaceId = compoint1.getDomainFatherId(); // WAid
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(espaceId);

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      destination = "/jobStartPagePeas/jsp/updateInstance.jsp";
    } else if (function.equals("EffectiveUpdateInstance")) {
      ComponentInst componentInst = jobStartPageSC
          .getComponentInst(jobStartPageSC.getManagedInstanceId());

      request2ComponentInst(componentInst, request, jobStartPageSC);

      // Update the instance
      String componentId = jobStartPageSC.updateComponentInst(componentInst);
      if (componentId != null && componentId.length() > 0) {
        refreshNavBar(jobStartPageSC, request);
        request.setAttribute("urlToReload", "GoToComponent?ComponentId="
            + componentInst.getId());
        destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
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
      String profileId = (String) request.getParameter("IdProfile");
      String profileName = (String) request.getParameter("NameProfile");
      String profileLabel = (String) request.getParameter("LabelProfile");

      ProfileInst profile = jobStartPageSC.getProfile(profileId, profileName,
          profileLabel);
      jobStartPageSC.setManagedProfile(profile);
      request.setAttribute("Profile", profile);

      destination = "/jobStartPagePeas/jsp/roleInstance.jsp";
    } else if (function.equals("CurrentRoleInstance")) {
      request.setAttribute("Profile", jobStartPageSC.getManagedProfile());

      destination = "/jobStartPagePeas/jsp/roleInstance.jsp";
    } else if (function.equals("SelectUsersGroupsProfileInstance")) {
      try {
        jobStartPageSC.initUserPanelInstanceForGroupsUsers((String) request
            .getAttribute("myComponentURL"));
      } catch (Exception e) {
        SilverTrace.warn("jobStartPagePeas",
            "JobStartPagePeasRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }
      destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    } else if (function.equals("EffectiveCreateInstanceProfile")) {
      String profileId = jobStartPageSC.createInstanceProfile();
      request
          .setAttribute("urlToReload", "RoleInstance?IdProfile=" + profileId); // le
      // NameProfile
      // et
      // le
      // LabelProfile
      // n'est
      // pas
      // indispensable
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("EffectiveUpdateInstanceProfile")) {
      jobStartPageSC.updateInstanceProfile();
      request.setAttribute("urlToReload", "CurrentRoleInstance");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("CancelCreateOrUpdateInstanceProfile")) {
      request.setAttribute("urlToReload", "CurrentRoleInstance");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("PlaceComponentAfter")) {
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC
          .getManagedInstanceId());
      request.setAttribute("jobStartPageSC", jobStartPageSC);
      request.setAttribute("currentComponentName", compoint1.getLabel());
      request.setAttribute("brothers", jobStartPageSC
          .getBrotherComponents(false));
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      request.setAttribute("currentSpace", spaceint1);
      request.setAttribute("selectedSpace", spaceint1);
      request.setAttribute("spaces", jobStartPageSC
          .getUserManageableSpacesIds());
      Boolean validLicense = new Boolean(getLicense(licenseCoupercoller));
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasRequestRouter.PlaceComponentAfter()",
          "root.MSG_GEN_PARAM_VALUE", "validLicense= " + validLicense);
      request.setAttribute("validLicense", validLicense);
      destination = "/jobStartPagePeas/jsp/placeComponentAfter.jsp";
    } else if (function.equals("EffectivePlaceComponent")) {
      SpaceInst currentSpace = jobStartPageSC.getSpaceInstById();
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC
          .getManagedInstanceId());
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
          "root.MSG_GEN_PARAM_VALUE", "Managed spaceId= "
              + jobStartPageSC.getManagedSpaceId());
      SpaceInst destinationSpace = jobStartPageSC.getSpaceInstById(request
          .getParameter("DestinationSpace"));
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
          "root.MSG_GEN_PARAM_VALUE", "espace source: " + currentSpace.getId()
              + " espace dest=" + destinationSpace.getId());
      // Moving only if destination space and current space are different
      if (!destinationSpace.getId().equals(currentSpace.getId())) {
        SilverTrace.info("jobStartPagePeas",
            "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
            "root.MSG_GEN_PARAM_VALUE", "espace source: "
                + jobStartPageSC.isSpaceInMaintenance(currentSpace.getId()
                    .substring(2))
                + " espace dest="
                + jobStartPageSC.isSpaceInMaintenance(destinationSpace.getId()
                    .substring(2)));
        // Destination and source space in maintenance = OK
        if ((jobStartPageSC.isSpaceInMaintenance(destinationSpace.getId()
            .substring(2)) && jobStartPageSC.isSpaceInMaintenance(currentSpace
            .getId().substring(2)))
            || jobStartPageSC.isAppInMaintenance()) {
          // Moving component to space
          SilverTrace.info("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
              "root.MSG_GEN_PARAM_VALUE", "component = " + compoint1.getId()
                  + " espace dest:" + destinationSpace.getId().substring(2)
                  + " componentBefore="
                  + request.getParameter("ComponentBefore"));
          jobStartPageSC.setMoveComponentToSpace(compoint1, request
              .getParameter("DestinationSpace").substring(2), request
              .getParameter("ComponentBefore"));
          SilverTrace.info("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
              "root.MSG_GEN_PARAM_VALUE", "Retour OK");
          refreshNavBar(jobStartPageSC, request);
          request.setAttribute("urlToReload", "StartPageInfo");
          destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
        } else {
          // Destination and source space NOT in maintenance
          String spaceDest = request.getParameter("DestinationSpace");
          SilverTrace.info("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
              "root.MSG_GEN_PARAM_VALUE",
              "Spaces not in maintenance Espace destination= " + spaceDest);
          SpaceInst selectedSpace = jobStartPageSC.getSpaceInstById(spaceDest);
          request.setAttribute("error", resource
              .getString("JSPP.SpacesNotInMaintenance"));
          request.setAttribute("currentComponentName", compoint1.getLabel());
          request.setAttribute("currentSpace", currentSpace);
          request.setAttribute("selectedSpace", selectedSpace);
          request.setAttribute("spaces", jobStartPageSC
              .getUserManageableSpacesIds());
          request.setAttribute("jobStartPageSC", jobStartPageSC);
          Boolean validLicense = new Boolean(getLicense(licenseCoupercoller));
          SilverTrace.info("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
              "root.MSG_GEN_PARAM_VALUE", "validLicense= " + validLicense);
          request.setAttribute("validLicense", validLicense);
          // Get all components of the selected space
          request.setAttribute("brothers", jobStartPageSC
              .getComponentsOfSpace(selectedSpace.getId()));
          destination = "/jobStartPagePeas/jsp/placeComponentAfter.jsp";
        }
      } else {
        jobStartPageSC.setComponentPlace(request
            .getParameter("ComponentBefore"));
        refreshNavBar(jobStartPageSC, request);
        request.setAttribute("urlToReload", "GoToCurrentComponent");
        destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
      }
    } else if (function.equals("ChangeDestinationSpace")) {
      // Get current component
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC
          .getManagedInstanceId());
      // Get selected space
      String spaceDest = request.getParameter("DestinationSpace");
      SpaceInst selectedSpace = jobStartPageSC.getSpaceInstById(spaceDest);
      SpaceInst currentSpace = jobStartPageSC.getSpaceInstById();

      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasRequestRouter.ChangeDestinationSpace()",
          "root.MSG_GEN_PARAM_VALUE", "currentSpaceId=" + currentSpace.getId()
              + "selectedSpaceId=" + selectedSpace.getId());
      request.setAttribute("jobStartPageSC", jobStartPageSC);
      request.setAttribute("currentComponentName", compoint1.getLabel());
      request.setAttribute("currentSpace", currentSpace);
      request.setAttribute("selectedSpace", selectedSpace);
      request.setAttribute("spaces", jobStartPageSC
          .getUserManageableSpacesIds());
      Boolean validLicense = new Boolean(getLicense(licenseCoupercoller));
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasRequestRouter.EffectivePlaceComponent()",
          "root.MSG_GEN_PARAM_VALUE", "validLicense= " + validLicense);
      request.setAttribute("validLicense", validLicense);
      if (selectedSpace.getId().equals(currentSpace.getId())) {
        // Get only brothers components of the current component
        request.setAttribute("brothers", jobStartPageSC
            .getBrotherComponents(false));
      } else {
        // Get all components of the selected space
        request.setAttribute("brothers", jobStartPageSC
            .getComponentsOfSpace(selectedSpace.getId()));
      }
      destination = "/jobStartPagePeas/jsp/placeComponentAfter.jsp";
    } else if (function.equals("DeleteUsersGroupsProfileInstance")) {
      jobStartPageSC.deleteInstanceProfile();
      destination = getDestination("CurrentRoleInstance", jobStartPageSC,
          request);
    } else if (function.equals("ProfileInstanceDescription")) {
      ComponentInst compoint1 = jobStartPageSC.getComponentInst(jobStartPageSC
          .getManagedInstanceId());
      request.setAttribute("compoName", compoint1.getLabel());

      String espaceId = compoint1.getDomainFatherId(); // WAid
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(espaceId);
      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      request.setAttribute("Profile", jobStartPageSC.getManagedProfile());

      destination = "/jobStartPagePeas/jsp/descriptionProfileInstance.jsp";
    } else if (function.equals("EffectiveUpdateProfileInstanceDescription")) {
      String name = request.getParameter("NameObject");
      String desc = request.getParameter("Description");
      if (desc == null) {
        desc = "";
      }

      jobStartPageSC.updateProfileInstanceDescription(name, desc);

      request.setAttribute("urlToReload", "CurrentRoleInstance");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.startsWith("copy")) {
      String objectId = request.getParameter("Id");
      try {
        jobStartPageSC.copyComponent(objectId);
      } catch (Exception e) {
        throw new AdminException(
            "JobStartPagePeasRequestRouter.getDestination()",
            SilverpeasException.ERROR, "jobStartPagePeas.CANT_COPY_COMPONENT",
            e);
      }
      destination = URLManager.getURL(URLManager.CMP_CLIPBOARD)
          + "Idle.jsp?message=REFRESHCLIPBOARD";
    } else if (function.startsWith("paste")) {
      try {
        jobStartPageSC.pasteComponent();
      } catch (Exception e) {
        throw new AdminException(
            "JobStartPagePeasRequestRouter.getDestination()",
            SilverpeasException.ERROR, "jobStartPagePeas.CANT_PAST_COMPONENT",
            e);
      }
      refreshNavBar(jobStartPageSC, request);
      destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
    }

    return destination;
  }

  /*********************** Gestion des espaces *****************************************/
  public String getDestinationSpace(String function,
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) throws AdminException {
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
        request.setAttribute("haveToRefreshMainPage", new Boolean(true));
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
      request.setAttribute("currentSpaceName", spaceint1.getName());
      request.setAttribute("brothers", jobStartPageSC.getBrotherSpaces(false));
      destination = "/jobStartPagePeas/jsp/placeSpaceAfter.jsp";
    } else if (function.equals("EffectivePlaceSpaceAfter")) {
      jobStartPageSC.setSpacePlace(request.getParameter("SpaceBefore"));
      refreshNavBar(jobStartPageSC, request);
      request.setAttribute("urlToReload", "StartPageInfo");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("CreateSpace")) {
      setSpacesNameInRequest(jobStartPageSC, request);

      request.setAttribute("SousEspace", request.getParameter("SousEspace"));
      request.setAttribute("spaceTemplates", jobStartPageSC
          .getAllSpaceTemplates());
      request.setAttribute("brothers", jobStartPageSC.getBrotherSpaces(true));
      destination = "/jobStartPagePeas/jsp/createSpace.jsp";
    } else if (function.equals("SetSpaceTemplateProfile")) {
      String spaceTemplate = request.getParameter("SpaceTemplate");

      if (request.getParameter("NameObject") != null
          && request.getParameter("NameObject").length() > 0) {
        jobStartPageSC.setCreateSpaceParameters(request
            .getParameter("NameObject"), request.getParameter("Description"),
            request.getParameter("SousEspace"), spaceTemplate, I18NHelper
                .getSelectedLanguage(request), request
                .getParameter("SelectedLook"));
      }

      if (spaceTemplate != null && spaceTemplate.length() > 0
          && jobStartPageSC.getCurrentSpaceTemplateProfiles() != null
          && jobStartPageSC.getCurrentSpaceTemplateProfiles().length > 0) {
        setSpacesNameInRequest(jobStartPageSC, request);

        request
            .setAttribute("SpaceBefore", request.getParameter("SpaceBefore"));
        request.setAttribute("SpaceTemplateProfiles", jobStartPageSC
            .getCurrentSpaceTemplateProfiles());
        request.setAttribute("SpaceTemplateProfilesGroups", jobStartPageSC
            .getCurrentSpaceTemplateProfilesGroups());
        request.setAttribute("SpaceTemplateProfilesUsers", jobStartPageSC
            .getCurrentSpaceTemplateProfilesUsers());
        destination = "/jobStartPagePeas/jsp/setSpaceTemplateProfiles.jsp";
      } else {
        destination = getDestinationSpace("EffectiveCreateSpace",
            jobStartPageSC, request);
      }
    } else if (function.equals("InvokeUserPanelForTemplateProfile")) {
      destination = jobStartPageSC.initUserPanelForTemplateProfile(
          (String) request.getAttribute("myComponentURL"), request
              .getParameter("profileIndex"));
    } else if (function.equals("ReturnUserPanelForTemplateProfile")) {
      jobStartPageSC.returnUserPanelForTemplateProfile(request
          .getParameter("profileIndex"));
      destination = getDestinationSpace("SetSpaceTemplateProfile",
          jobStartPageSC, request);
    } else if (function.equals("EffectiveCreateSpace")) {
      String spaceId = jobStartPageSC.createSpace();
      if (spaceId != null && spaceId.length() > 0) {
        jobStartPageSC.setSpacePlace(request.getParameter("SpaceBefore"));
        refreshNavBar(jobStartPageSC, request);
        request.setAttribute("urlToReload", "StartPageInfo");
        destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
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
      request.setAttribute("IsInheritanceEnable", new Boolean(
          JobStartPagePeasSettings.isInheritanceEnable));

      destination = "/jobStartPagePeas/jsp/updateSpace.jsp";
    } else if (function.equals("EffectiveUpdateSpace")) {
      // Update the space
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      request2SpaceInst(spaceint1, request, jobStartPageSC);

      String spaceId = jobStartPageSC.updateSpaceInst(spaceint1);
      if (spaceId != null && spaceId.length() > 0) {
        refreshNavBar(jobStartPageSC, request);
        request.setAttribute("urlToReload", "StartPageInfo");
        destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
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
      jobStartPageSC.deleteCurrentSpace();
      refreshNavBar(jobStartPageSC, request);
      if ((jobStartPageSC.getManagedSpaceId() != null)
          && (jobStartPageSC.getManagedSpaceId().length() > 0)) {
        destination = "/jobStartPagePeas/jsp/startPageInfo.jsp";
      } else {
        destination = getDestination("welcome", jobStartPageSC, request);
      }
    } else if (function.equals("SpaceManager")) {
      String role = request.getParameter("Role");

      if (!StringUtil.isDefined(role)) {
        role = "Manager";
      }

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      SpaceProfileInst profile = spaceint1.getSpaceProfileInst(role);
      if (profile != null) {
        SilverTrace.info("jobStartPagePeas",
            "JobStartPagePeasRequestRouter.SpaceManager()",
            "root.MSG_GEN_PARAM_VALUE", "profileName=" + profile.getName()
                + " profileName=" + profile.getLabel());
        request.setAttribute("Profile", profile);
      }

      // liste des groupes et user qui sont manager de l'espace courant
      List groupes = jobStartPageSC.getAllCurrentGroupSpace(role);
      List users = jobStartPageSC.getAllCurrentUserSpace(role);
      request.setAttribute("listGroupSpace", groupes);
      request.setAttribute("listUserSpace", users);

      request.setAttribute("SpaceExtraInfos", jobStartPageSC.getManagedSpace());
      request.setAttribute("ProfileEditable", jobStartPageSC
          .isProfileEditable());
      request.setAttribute("Role", role);

      // Profile hérité, liste des groupes et user du role hérité courant
      SpaceProfileInst inheritedProfile = spaceint1
          .getInheritedSpaceProfileInst(role);
      if (inheritedProfile != null) {
        request.setAttribute("InheritedProfile", inheritedProfile);
        request.setAttribute("listInheritedGroups", jobStartPageSC
            .groupIds2groups(inheritedProfile.getAllGroups()));
        request.setAttribute("listInheritedUsers", jobStartPageSC
            .userIds2users(inheritedProfile.getAllUsers()));
      }

      request.setAttribute("IsInheritanceEnable", new Boolean(
          JobStartPagePeasSettings.isInheritanceEnable));

      destination = "/jobStartPagePeas/jsp/spaceManager.jsp";
    } else if (function.equals("SelectUsersGroupsSpace")) {
      String role = request.getParameter("Role");
      try {
        jobStartPageSC.initUserPanelSpaceForGroupsUsers((String) request
            .getAttribute("myComponentURL"), role);
      } catch (Exception e) {
        SilverTrace.warn("jobStartPagePeas",
            "JobStartPagePeasRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }
      destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    } else if (function.equals("EffectiveCreateSpaceProfile")) {
      String role = request.getParameter("Role");
      jobStartPageSC.createSpaceRole(role);
      request.setAttribute("urlToReload", "SpaceManager?Role=" + role);
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("EffectiveUpdateSpaceProfile")) {
      String role = request.getParameter("Role");
      jobStartPageSC.updateSpaceRole(role);
      request.setAttribute("urlToReload", "SpaceManager?Role=" + role);
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("CancelCreateOrUpdateSpaceProfile")) {
      String role = request.getParameter("Role");

      request.setAttribute("urlToReload", "SpaceManager?Role=" + role);
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("DeleteSpaceManager")) {
      String role = request.getParameter("Role");
      jobStartPageSC.deleteSpaceRole(role);
      destination = getDestination("SpaceManager", jobStartPageSC, request);
    } else if (function.equals("SpaceManagerDescription")) {
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      ArrayList m_ListSpaceProfileInst = spaceint1.getAllSpaceProfilesInst();
      int i = 0;
      SpaceProfileInst m_SpaceProfileInst = null;
      String name = "";
      String desc = "";
      if (i < m_ListSpaceProfileInst.size()) {// seulement le premier profil
        // (manager)
        m_SpaceProfileInst = (SpaceProfileInst) m_ListSpaceProfileInst.get(i);
        name = m_SpaceProfileInst.getLabel();
        if (name.equals("")) {
          name = jobStartPageSC.getMultilang().getString("Manager");
        }
        desc = m_SpaceProfileInst.getDescription();
      }

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      request.setAttribute("Name", name);
      request.setAttribute("Description", desc);
      destination = "/jobStartPagePeas/jsp/descriptionSpaceManager.jsp";
    } else if (function.equals("EffectiveUpdateSpaceManagerDescription")) {
      String name = request.getParameter("NameObject");
      String desc = request.getParameter("Description");
      if (desc == null) {
        desc = "";
      }

      // Update the space
      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();
      ArrayList m_ListSpaceProfileInst = spaceint1.getAllSpaceProfilesInst();
      int i = 0;
      SpaceProfileInst m_SpaceProfileInst = null;
      if (i < m_ListSpaceProfileInst.size()) {// seulement le premier profil
        // (manager)
        m_SpaceProfileInst = (SpaceProfileInst) m_ListSpaceProfileInst.get(i);
        m_SpaceProfileInst.setLabel(name);
        m_SpaceProfileInst.setDescription(desc);

        jobStartPageSC.updateSpaceManagersDescription(m_SpaceProfileInst);
      }

      request.setAttribute("urlToReload", "SpaceManager");
      destination = "/jobStartPagePeas/jsp/closeWindow.jsp";
    } else if (function.equals("SpaceLook")) {
      String path = getSpaceLookRepository(jobStartPageSC);

      List files = null;
      try {
        files = (List) FileFolderManager.getAllFile(path);
      } catch (UtilException e) {
        files = new ArrayList();
      }

      SpaceLookHelper slh = new SpaceLookHelper("Space"
          + jobStartPageSC.getManagedSpaceId());
      slh.setFiles(files);

      SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById();

      request.setAttribute("Space", spaceint1);
      request.setAttribute("SpaceLookHelper", slh);
      request.setAttribute("SpaceExtraInfos", jobStartPageSC.getManagedSpace());
      request.setAttribute("IsInheritanceEnable", new Boolean(
          JobStartPagePeasSettings.isInheritanceEnable));

      setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

      destination = "/jobStartPagePeas/jsp/spaceLook.jsp";
    } else if (function.equals("UpdateSpaceLook")) {
      try {
        List items = getRequestItems(request);

        FileItem file = getUploadedFile(items, "wallPaper");
        if (file != null && StringUtil.isDefined(file.getName())) {
          String extension = FileRepositoryManager.getFileExtension(file
              .getName());

          if (extension != null && extension.equalsIgnoreCase("jpeg")) {
            extension = "jpg";
          }

          FileRepositoryManager.createAbsolutePath("Space"
              + jobStartPageSC.getManagedSpaceId(), "look");

          String[] dir = new String[1];
          dir[0] = "look";

          String path = FileRepositoryManager.getAbsolutePath("Space"
              + jobStartPageSC.getManagedSpaceId(), dir);

          // Remove all wallpapers to ensure it is unique
          File wallPaper = new File(path + File.separator + "wallPaper.gif");
          if (wallPaper != null && wallPaper.exists()) {
            wallPaper.delete();
          }

          wallPaper = new File(path + File.separator + "wallPaper.jpg");
          if (wallPaper != null && wallPaper.exists()) {
            wallPaper.delete();
          }

          file.write(new File(path + File.separator + "wallPaper."
              + extension.toLowerCase()));
        }

        String selectedLook = getParameterValue(items, "SelectedLook");
        if (!StringUtil.isDefined(selectedLook)) {
          selectedLook = null;
        }

        SpaceInst space = jobStartPageSC.getSpaceInstById();
        space.setLook(selectedLook);

        jobStartPageSC.updateSpaceInst(space);
      } catch (Exception e) {
        throw new AdminException("JobStartPagePeasRequestRouter.AddFileToLook",
            SilverpeasException.ERROR, "jobStartPagePeas.CANT_UPLOAD_FILE", e);
      }

      destination = getDestination("SpaceLook", jobStartPageSC, request);
    } else if (function.equals("RemoveFileToLook")) {
      String fileName = request.getParameter("FileName");

      String path = getSpaceLookRepository(jobStartPageSC);

      File file = new File(path + File.separator + fileName);

      file.delete();

      destination = getDestination("SpaceLook", jobStartPageSC, request);
    }

    return destination;
  }

  private String getSpaceLookRepository(JobStartPagePeasSessionController sc) {
    return FileRepositoryManager.getAbsolutePath("Space"
        + sc.getManagedSpaceId(), new String[] { "look" });
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = null;
    JobStartPagePeasSessionController jobStartPageSC = (JobStartPagePeasSessionController) componentSC;
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + jobStartPageSC.getUserId()
            + " Function=" + function);

    try {
      if (destination == null) {
        destination = getDestinationStartPage(function, jobStartPageSC, request);
      }
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

      if (destination.equals("/jobStartPagePeas/jsp/jobStartPageNav.jsp")) {
        request.setAttribute("Spaces", jobStartPageSC.getSpaces());
        request.setAttribute("SubSpaces", jobStartPageSC.getSubSpaces());
        request.setAttribute("SpaceComponents", jobStartPageSC
            .getSpaceComponents());
        request.setAttribute("SubSpaceComponents", jobStartPageSC
            .getSubSpaceComponents());
        request.setAttribute("CurrentSpaceId", jobStartPageSC.getSpaceId());
        request.setAttribute("CurrentSubSpaceId", jobStartPageSC
            .getSubSpaceId());
      } else if (destination.equals("/jobStartPagePeas/jsp/startPageInfo.jsp")) {
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(); // espace
        // courant

        request.setAttribute("isUserAdmin", new Boolean(jobStartPageSC
            .isUserAdmin()));
        request.setAttribute("FirstPageType", new Integer(spaceint1
            .getFirstPageType()));
        request.setAttribute("Description", spaceint1.getDescription());
        String spaceId = jobStartPageSC.getManagedSpaceId();
        request.setAttribute("currentSpaceId", spaceId);
        Boolean mode = new Boolean(jobStartPageSC.isSpaceInMaintenance(spaceId));
        request.setAttribute("mode", mode.toString());

        setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

        request.setAttribute("SpaceExtraInfos", jobStartPageSC
            .getManagedSpace());
        request.setAttribute("NameProfile", jobStartPageSC
            .getSpaceProfileName(spaceint1));
        request.setAttribute("IsBackupEnable", jobStartPageSC.isBackupEnable());

        request.setAttribute("IsInheritanceEnable", new Boolean(
            JobStartPagePeasSettings.isInheritanceEnable));

        request.setAttribute("Space", spaceint1);
      } else if (destination.equals("/jobStartPagePeas/jsp/componentInfo.jsp")) {
        ComponentInst compoint1 = jobStartPageSC
            .getComponentInst(jobStartPageSC.getManagedInstanceId());
        String sCompoName = compoint1.getName();

        // Search for component 'generic' label
        WAComponent componentInstBase = jobStartPageSC
            .getComponentByName(sCompoName);
        sCompoName = componentInstBase.getLabel();

        SPParameters xmlParameters = componentInstBase.getSPParameters();
        SPParameters dbParameters = compoint1.getSPParameters();

        SPParameters parameters = (SPParameters) xmlParameters.clone();
        parameters.mergeWith(dbParameters.getParameters());

        List visibleParameters = getVisibleParameters(parameters
            .getSortedParameters());

        String isHidden = "";
        if (compoint1.isHidden()) {
          isHidden = "yes";
        }

        SPParameter hiddenParam = new SPParameter("HiddenComponent",
            jobStartPageSC.getString("JSPP.hiddenComponent"), isHidden, false,
            "always", "checkbox", null, "useless", "no", null);
        visibleParameters.add(0, hiddenParam);

        if (JobStartPagePeasSettings.isPublicParameterEnable) {
          String isPublic = "";
          if (compoint1.isPublic()) {
            isPublic = "yes";
          }

          SPParameter publicParam = new SPParameter("PublicComponent",
              jobStartPageSC.getString("JSPP.publicComponent"), isPublic,
              false, "always", "checkbox", null, "useless", "no", null);
          visibleParameters.add(0, publicParam);
        }

        request.setAttribute("Parameters", visibleParameters);

        request.setAttribute("ComponentInst", compoint1);
        request.setAttribute("JobPeas", sCompoName);

        String espaceId = compoint1.getDomainFatherId(); // WAid
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(espaceId);

        setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

        request.setAttribute("Profiles", jobStartPageSC
            .getAllProfiles(compoint1));
        request.setAttribute("IsInheritanceEnable", new Boolean(
            JobStartPagePeasSettings.isInheritanceEnable));
      } else if (destination.equals("/jobStartPagePeas/jsp/roleInstance.jsp")) {
        ComponentInst compoint1 = jobStartPageSC
            .getComponentInst(jobStartPageSC.getManagedInstanceId());
        request.setAttribute("compoName", compoint1.getLabel());

        String espaceId = compoint1.getDomainFatherId(); // WAid
        SpaceInst spaceint1 = jobStartPageSC.getSpaceInstById(espaceId);

        setSpacesNameInRequest(spaceint1, jobStartPageSC, request);

        request.setAttribute("Profiles", jobStartPageSC
            .getAllProfiles(compoint1));

        // Profile, liste des groupes et user du role courant
        request.setAttribute("Profile", jobStartPageSC.getManagedProfile());
        request.setAttribute("listGroup", jobStartPageSC
            .getAllCurrentGroupInstance());
        request.setAttribute("listUser", jobStartPageSC
            .getAllCurrentUserInstance());

        // Profile hérité, liste des groupes et user du role hérité courant
        ProfileInst inheritedProfile = jobStartPageSC
            .getManagedInheritedProfile();
        if (inheritedProfile != null) {
          request.setAttribute("InheritedProfile", inheritedProfile);
          request.setAttribute("listInheritedGroups", jobStartPageSC
              .groupIds2groups(inheritedProfile.getAllGroups()));
          request.setAttribute("listInheritedUsers", jobStartPageSC
              .userIds2users(inheritedProfile.getAllUsers()));
        }

        request.setAttribute("ProfileEditable", jobStartPageSC
            .isProfileEditable());
        request.setAttribute("IsInheritanceEnable", new Boolean(
            JobStartPagePeasSettings.isInheritanceEnable));
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  protected void refreshNavBar(
      JobStartPagePeasSessionController jobStartPageSC,
      HttpServletRequest request) {
    jobStartPageSC.refreshCurrentSpaceCache();
    request.setAttribute("haveToRefreshNavBar", new Boolean(true));
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
      if (!spaceint1.isRoot()) {// je suis sur un ss-espace
        String idFather = spaceint1.getDomainFatherId();
        SpaceInst spaceFather = jobStartPageSC.getSpaceInstById(idFather);
        request.setAttribute("currentSpaceName", spaceFather
            .getName(jobStartPageSC.getLanguage()));
        request.setAttribute("nameSubSpace", spaceint1.getName(jobStartPageSC
            .getLanguage()));
      } else {
        request.setAttribute("currentSpaceName", spaceint1
            .getName(jobStartPageSC.getLanguage()));
        request.setAttribute("nameSubSpace", null);
      }
    } else {
      request.setAttribute("currentSpaceName", "");
      request.setAttribute("nameSubSpace", null);
    }
  }

  private List getVisibleParameters(List parameters) {
    ArrayList visibleParameters = new ArrayList();
    SPParameter parameter = null;
    for (int p = 0; p < parameters.size(); p++) {
      parameter = (SPParameter) parameters.get(p);
      if (parameter.getUpdatable().toLowerCase().equals("never")
          || parameter.getUpdatable().toLowerCase().equals("hidden")) {
        // Le parametre n'est pas visible
      } else {
        visibleParameters.add(parameter);
      }
    }
    return visibleParameters;
  }

  private List getHiddenParameters(List parameters) {
    ArrayList hiddenParameters = new ArrayList();
    SPParameter parameter = null;
    for (int p = 0; p < parameters.size(); p++) {
      parameter = (SPParameter) parameters.get(p);
      if (parameter.getUpdatable().toLowerCase().equals("hidden")) {
        hiddenParameters.add(parameter);
      }
    }
    return hiddenParameters;
  }

  // NEWD DLE
  private boolean getLicense(String code) {
    boolean validSequence = true;
    String serial = "100406181111";
    try {
      for (int i = 0; i < 6 && validSequence; i++) {
        String groupe = code.substring(i * 6, i * 6 + 6);
        int total = 0;
        for (int j = 0; j < groupe.length(); j++) {
          String valeur = groupe.substring(j, j + 1);
          total += new Integer(valeur).intValue();
        }
        if (total != new Integer(serial.substring(i * 2, i * 2 + 2)).intValue()) {
          validSequence = false;
        }
      }
    } catch (Exception e) {
      validSequence = false;
    }
    return validSequence;
  }

  // NEWF DLE

  private boolean isDefined(String param) {
    return param != null && param.length() > 0
        && !"null".equalsIgnoreCase(param);
  }

  private void request2SpaceInst(SpaceInst spaceInst,
      HttpServletRequest request,
      JobStartPagePeasSessionController jobStartPageSC) {
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
      spaceInst.setInheritanceBlocked(new Boolean(pInheritance).booleanValue());
    }

    if (StringUtil.isDefined(look)) {
      spaceInst.setLook(look);
    }

    I18NHelper.setI18NInfo(spaceInst, request);
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
      componentInst.setInheritanceBlocked(new Boolean(pInheritance)
          .booleanValue());
    }

    I18NHelper.setI18NInfo(componentInst, request);

    // Add the parameters (if they exist)
    WAComponent componentInstSelected = null;
    if (StringUtil.isDefined(componentInst.getName())) {
      componentInstSelected = jobStartPageSC.getComponentByName(componentInst
          .getName());
    } else {
      String componentNum = (String) request.getParameter("ComponentNum");
      componentInstSelected = jobStartPageSC.getComponentByNum(Integer
          .parseInt(componentNum));
      String jobPeas = componentInstSelected.getName();
      componentInst.setName(jobPeas);
    }

    boolean isCheckbox = false;
    final String PARAM_TYPE_CHECKBOX = "checkbox";
    SPParameters xmlParameters = (SPParameters) componentInstSelected
        .getSPParameters().clone();
    List parameters = xmlParameters.getParameters();
    SPParameter parameter = null;
    String value = null;
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasRequestRouter.EffectiveCreateInstance",
        "root.MSG_GEN_PARAM_VALUE", "nb parameters = " + parameters.size());
    for (int nI = 0; nI < parameters.size(); nI++) {
      parameter = (SPParameter) parameters.get(nI);
      value = (String) request.getParameter(parameter.getName());
      isCheckbox = PARAM_TYPE_CHECKBOX.equals(parameter.getType());
      if (isCheckbox) {
        if (value == null) {
          value = "no";
        }
      }
      parameter.setValue(value);
    }
    componentInst.setParameters(parameters);
  }

  private List getRequestItems(HttpServletRequest request)
      throws FileUploadException {
    DiskFileUpload dfu = new DiskFileUpload();
    List items = dfu.parseRequest(request);
    return items;
  }

  private String getParameterValue(List items, String parameterName) {
    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }

  private FileItem getUploadedFile(List items, String parameterName) {
    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }
}
