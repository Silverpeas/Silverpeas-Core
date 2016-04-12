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

package org.silverpeas.web.pdc.servlets;

import org.silverpeas.core.web.mvc.util.AccessForbiddenException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.AxisPK;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.web.pdc.control.PdcSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class PdcRequestRouter extends ComponentRequestRouter<PdcSessionController> {

  private static final long serialVersionUID = -1233766141114104308L;

  @Override
  public PdcSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcSessionController(mainSessionCtrl, componentContext,
        "org.silverpeas.pdcPeas.multilang.pdcBundle",
        "org.silverpeas.pdcPeas.settings.pdcPeasIcons");
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return "pdcPeas";
  }

  /**
   * This is the separator string between the name of axis or values and their order in the option
   * html tag
   */
  private static final String sepOptionValueTag = "#_$#";
  private static final int lenOfSeparator = PdcRequestRouter.sepOptionValueTag.length();

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param pdcSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, PdcSessionController pdcSC,
      HttpRequest request) {
    String destination = "";
    try {
      // récupération de la langue et passage en paramètre à la jsp
      setLanguageAsAttribute(pdcSC, request);

      if (function.startsWith("Main")) {
        // the user is on the main page

        // get values to set into the request
        String creationAllowed = "1"; // by default the creation is allowed
        if (!pdcSC.isCreationAllowed()) {
          creationAllowed = "0";
        }


        List<AxisHeader> list = pdcSC.getAxis();
        // assign attributes into the request
        request.setAttribute("AxisList", list); // set a sorted list
        request.setAttribute("CreationAllowed", creationAllowed);
        request.setAttribute("ViewType", pdcSC.getCurrentView()); // the type of the axis
        request.setAttribute("IsAdmin", Boolean.valueOf(pdcSC.isPDCAdmin()));
        if (!pdcSC.isPDCAdmin()) {
          request.setAttribute("ManageableAxis", pdcSC.getAxisManageables());
        }


        // create the new destination
        destination = "/pdcPeas/jsp/pdc.jsp";
      } else if (function.equals("ChangeLanguage")) {
        // récupération de la langue choisie
        String currentLanguage = request.getParameter("SwitchLanguage");
        // mise à jour de la langue courante
        pdcSC.setCurrentLanguage(currentLanguage);
        // rechargement de la page avec la nouvelle langue
        destination = getDestination("Main", pdcSC, request);

      } else if (function.equals("ChangeLanguageView")) {
        // récupération de la langue choisie
        String currentLanguage = request.getParameter("SwitchLanguage");

        String axeId = request.getParameter("Id");
        request.setAttribute("Axis", pdcSC.getAxisDetail(axeId));
        String valueId = request.getParameter("ValueId");
        request.setAttribute("ValueId", valueId);

        // mise à jour de la langue courante
        pdcSC.setCurrentLanguage(currentLanguage);
        // rechargement de la page avec la nouvelle langue
        destination = getDestination("ViewAxis", pdcSC, request);

      } else if (function.equals("ChangeLanguageValue")) {
        // récupération de la langue choisie
        String currentLanguage = request.getParameter("SwitchLanguage");

        String valueId = request.getParameter("Id");
        String axisId = request.getParameter("AxisId");

        Value currentValue = pdcSC.getValue(axisId, valueId);
        if (currentValue == null) {
          currentValue = pdcSC.getAxisValue(valueId);
          currentValue.setNbObjects(0);
        }
        request.setAttribute("Value", currentValue);
        request.setAttribute("Path", pdcSC.getFullPath(currentValue.getPK().getId()));

        // mise à jour de la langue courante
        pdcSC.setCurrentLanguage(currentLanguage);
        // rechargement de la page avec la nouvelle langue
        destination = getDestination("ViewValue", pdcSC, request);

      } else if (function.startsWith("ChangeViewType")) {
        // the user changes the view

        // get URL parameters
        String type = request.getParameter("ViewType"); // get the id of the
        // selected axe

        pdcSC.setCurrentView(type);

        // create the new destination
        destination = getDestination("Main", pdcSC, request);

      } else if (function.startsWith("NewAxis")) {
        // the user wants to add a new axis into the PDC.

        // get values from session controller to set into the request
        request.setAttribute("SecondaryAxis", pdcSC.getSecondaryAxis()); // set
        // a
        // sorted
        // list
        request.setAttribute("PrimaryAxis", pdcSC.getPrimaryAxis()); // set a
        // sorted
        // list
        request.setAttribute("ViewType", pdcSC.getCurrentView()); // the type of
        // the axe
        request.setAttribute("Translation", pdcSC.getCurrentLanguage());

        // create the new destination
        destination = "/pdcPeas/jsp/axisManager.jsp";

      } else if (function.startsWith("EditAxis")) {
        // the user want to modify an axis

        // get URL parameters
        String axeId = request.getParameter("Id"); // get the id of the selected
        // axe

        if (!StringUtil.isDefined(axeId)) {
          axeId = pdcSC.getCurrentAxis().getAxisHeader().getPK().getId();
        }

        // check user rights
        if (!pdcSC.isPDCAdmin() && !pdcSC.isAxisManager(axeId)) {
          throw new AccessForbiddenException("PdcRequestRouter.EditAxit",
              SilverpeasException.WARNING, null);
        }

        String viewType = pdcSC.getCurrentView(); // get the type of the current
        // axe
        List<AxisHeader> primaryAxis = pdcSC.getPrimaryAxis();
        List<AxisHeader> secondaryAxis = pdcSC.getSecondaryAxis();
        // search the right AxisHeader
        AxisHeader axisHeader = null;
        if (viewType.equals("P")) {
          // primary axe
          axisHeader = extractAxisHeader(axeId, primaryAxis);
        } else {
          axisHeader = extractAxisHeader(axeId, secondaryAxis);
        }

        // get values from session controller to set into the request
        request.setAttribute("SecondaryAxis", secondaryAxis); // set a sorted
        // list
        request.setAttribute("PrimaryAxis", primaryAxis); // set a sorted axis
        // header list
        request.setAttribute("AxisHeader", axisHeader);
        request.setAttribute("ViewType", pdcSC.getCurrentView());
        request.setAttribute("Modif", "1");

        // set currentvalue for ViewManager
        pdcSC.getAxisDetail(axeId);
        pdcSC.resetCurrentValue();
        // pdcSC.getAxisValue("0");

        String translation = request.getParameter("Translation");
        if (translation == null || translation.equals("null")
            || translation.length() != 0) {
          translation = pdcSC.getCurrentLanguage();
        }

        request.setAttribute("Translation", translation);

        // create the new destination
        destination = "/pdcPeas/jsp/axisManager.jsp";

      } else if (function.startsWith("DeleteAxis")) {
        // the user deletes an axe from the PDC.

        if (!pdcSC.isPDCAdmin()) {
          throw new AccessForbiddenException("PdcRequestRouter." + function,
              SilverpeasException.WARNING, null);
        }

        // get URL parameters
        String axesId = request.getParameter("Ids"); // get ids of selected axes

        // get all ids and remove corresponding axes
        StringTokenizer st = new StringTokenizer(axesId, ",");
        for (; st.hasMoreTokens();) {
          pdcSC.deleteAxis(st.nextToken());
        }

        destination = getDestination("Main", pdcSC, request);

      } else if (function.startsWith("ViewAxis")) {
        // the user wants to see the axis

        // get URL parameters
        String axeId = request.getParameter("Id"); // get the id of the selected
        // axe
        String valueId = request.getParameter("ValueId"); // get the id of the
        // selected value

        // get values from session controller to set into the request
        request.setAttribute("Axis", pdcSC.getAxisDetail(axeId));
        request.setAttribute("ValueId", valueId);

        // get rights for this axis and this user
        boolean isAxisManager = pdcSC.isAxisManager(axeId);
        request.setAttribute("IsAdmin", Boolean.valueOf(isAxisManager));
        if (!isAxisManager) {
          request.setAttribute("UserRights", pdcSC.getRights());
        }

        // create the new destination
        destination = "/pdcPeas/jsp/tree.jsp";

      } else if (function.startsWith("CreateAxis")) {
        // create an axe

        // get URL parameters
        String axeName = request.getParameter("Name").trim(); // get the name of
        // the axe
        String axeType = request.getParameter("Type");
        String axeOrder = extractOrder(request.getParameter("Order"));
        String axeDescription = request.getParameter("Description").trim(); // get
        // the
        // description
        // of
        // the
        // axe

        // create the axe
        AxisHeader axisHeader = new AxisHeader(new AxisPK("unknown"), axeName,
            axeType, (Integer.parseInt(axeOrder)), null, null, -1,
            axeDescription);


        // ajout de la langue
        axisHeader.setLanguage(I18NHelper.getSelectedContentLanguage(request));

        // récupération des traductions
        I18NHelper.setI18NInfo(axisHeader, request);

        int status = pdcSC.createAxis(axisHeader);



        switch (status) {
          case 1:
            request.setAttribute("MaxAxis", "1"); // max
            request.setAttribute("ViewType", pdcSC.getCurrentView());
            request.setAttribute("SecondaryAxis", pdcSC.getSecondaryAxis());
            request.setAttribute("PrimaryAxis", pdcSC.getPrimaryAxis());
            request.setAttribute("Translation", pdcSC.getCurrentLanguage());
            destination = "/pdcPeas/jsp/axisManager.jsp";
            break;
          case 2:
            request.setAttribute("AxisHeader", axisHeader);
            request.setAttribute("AlreadyExist", "1");
            request.setAttribute("ViewType", pdcSC.getCurrentView());
            request.setAttribute("SecondaryAxis", pdcSC.getSecondaryAxis());
            request.setAttribute("PrimaryAxis", pdcSC.getPrimaryAxis());
            destination = getDestination("AxisAlreadyExist", pdcSC,
                request);
            break;
          default:
            destination = "/pdcPeas/jsp/reloadPdc.jsp";
        }

      } else if (function.startsWith("AxisAlreadyExist")) {
        // The axe already exist :-)
        destination = "/pdcPeas/jsp/axisManager.jsp";

      } else if (function.startsWith("UpdateAxis")) {
        // the user updates an axis

        // get URL parameters
        String axeId = request.getParameter("Id"); // get the id of the axe
        String axeName = request.getParameter("Name").trim(); // get the name of
        // the axe
        String axeType = request.getParameter("Type");
        String axeOrder = extractOrder(request.getParameter("Order"));
        String axeDescription = request.getParameter("Description").trim(); // get
        // the
        // description
        // of
        // the
        // axe

        AxisPK currentAxisPK = new AxisPK(axeId);

        // axeOrder is not null then the user has no change the order
        int order = -1;
        if (axeOrder != null) {
          order = Integer.parseInt(axeOrder);
        }

        // update axis
        AxisHeader axisHeader = new AxisHeader(currentAxisPK, axeName, axeType,
            order, null, null, -1, axeDescription);

        // récupération des traductions
        I18NHelper.setI18NInfo(axisHeader, request);

        int status = pdcSC.updateAxis(axisHeader);

        switch (status) {
          case 2:
            request.setAttribute("AxisHeader", axisHeader); // already exist
            request.setAttribute("AlreadyExist", "1");
            request.setAttribute("ViewType", pdcSC.getCurrentView());
            request.setAttribute("SecondaryAxis", pdcSC.getSecondaryAxis());
            request.setAttribute("PrimaryAxis", pdcSC.getPrimaryAxis());
            destination = getDestination("AxisAlreadyExist", pdcSC, request);
            break;
          default:
            destination = "/pdcPeas/jsp/reloadPdc.jsp";
        }
      } else if (function.startsWith("ViewValue")) {
        // the user want to see a value of an axe

        // get URL parameters
        String valueId = request.getParameter("Id"); // get the id of the
        // selected value
        String axisId = request.getParameter("AxisId"); // get the id of the axe

        Value currentValue = null;
        if (!StringUtil.isDefined(valueId)) {
          currentValue = pdcSC.getCurrentValue();
        } else {
          currentValue = pdcSC.getValue(axisId, valueId);
        }

        if (currentValue == null) {// il n'y a pas de documents classés dans
          // cette valeur
          currentValue = pdcSC.getAxisValue(valueId);
          currentValue.setNbObjects(0);
        }
        request.setAttribute("AxisId", axisId);
        request.setAttribute("Value", currentValue);
        request.setAttribute("Path", pdcSC.getFullPath(currentValue.getPK().getId()));

        if (currentValue.getLevelNumber() == 0) {
          request.setAttribute("Root", "1");
        } else {
          request.setAttribute("Root", "0");
        }

        boolean isAdmin = pdcSC.isPDCAdmin() || pdcSC.isAxisManager()
            || pdcSC.isInheritedManager();
        request.setAttribute("IsAdmin", Boolean.valueOf(isAdmin));

        // create the new destination
        destination = "/pdcPeas/jsp/value.jsp";

      } else if (function.startsWith("EditValue")) {
        // the user want to modify a value of an axe

        // set into the request
        Axis currentAxis = pdcSC.getCurrentAxis(); // get the selected axe
        Value currentValue = pdcSC.getCurrentValue();
        Value oldValue = pdcSC.getAxisValue(currentValue.getPK().getId());

        request.setAttribute("Value", currentValue);
        request.setAttribute("Sisters", getSisterValues(currentAxis, oldValue));
        request.setAttribute("Path", pdcSC.getFullPath(oldValue.getPK().getId()));

        boolean isAdmin = pdcSC.isPDCAdmin() || pdcSC.isAxisManager()
            || pdcSC.isInheritedManager();
        request.setAttribute("IsAdmin", Boolean.valueOf(isAdmin));

        String translation = request.getParameter("Translation");
        if (!StringUtil.isDefined(translation)) {
          translation = pdcSC.getCurrentLanguage();
        }

        request.setAttribute("Translation", translation);

        // create the new destination
        destination = "/pdcPeas/jsp/editValue.jsp";
      } else if (function.startsWith("ToMoveValueChooseMother")) {
        // the user want to move a value of an axis

        // set into the request
        Axis currentAxis = pdcSC.getCurrentAxis(); // get the selected axe
        Value currentValue = pdcSC.getCurrentValue();

        request.setAttribute("Axis", currentAxis);
        request.setAttribute("Value", currentValue);
        request.setAttribute("Path", pdcSC.getFullPath(currentValue.getPK().getId()));

        String translation = request.getParameter("Translation");
        if (!StringUtil.isDefined(translation)) {
          translation = pdcSC.getCurrentLanguage();
        }

        request.setAttribute("Translation", translation);

        // get rights for this axis and this user and if user is admin or
        // kmAdmin
        Boolean KMadmin = Boolean.valueOf((pdcSC.getUserDetail().isAccessPdcManager() || pdcSC.
            getUserDetail().isAccessAdmin()));
        request.setAttribute("KMAdmin", KMadmin);
        request.setAttribute("UserRights", pdcSC.getRights());

        // create the new destination
        destination = "/pdcPeas/jsp/moveValue.jsp";
      } else if (function.startsWith("ToMoveValueGetSisters")) {
        // we need to move the subtree

        // set into the request
        Value currentValue = pdcSC.getCurrentValue();
        String newFatherId = (String) request.getParameter("newFatherId");

        if (!newFatherId.equals(currentValue.getFatherId())) {
          // get values to newFatherId
          List<Value> newSisters = getDaughterValues(pdcSC.getCurrentAxis(), pdcSC.getAxisValue(
              newFatherId, false));

          request.setAttribute("Sisters", newSisters);
          request.setAttribute("newFatherId", newFatherId);
        } else {
          request.setAttribute("AlreadyExist", "1"); // already exist
        }
        destination = getDestination("ToMoveValueChooseMother", pdcSC, request);
      } else if (function.startsWith("MoveValue")) {
        // we need to move the subtree

        // set into the request
        Value currentValue = pdcSC.getCurrentValue();

        String newFatherId = request.getParameter("newFatherId");
        String valueOrder = extractOrder(request.getParameter("Order"));

        if (!StringUtil.isDefined(newFatherId)) {
          newFatherId = (String) request.getAttribute("newFatherId");
        }

        int status = 1;
        if (!newFatherId.equals(currentValue.getFatherId())) {
          status = pdcSC.moveCurrentValueToNewFatherId(newFatherId, Integer.parseInt(valueOrder));
        }

        switch (status) {
          case 1:
            request.setAttribute("AlreadyExist", "1"); // already exist
            destination = getDestination("ToMoveValueChooseMother", pdcSC, request);
            break;
          default:
            destination = "/pdcPeas/jsp/reloadAxis.jsp";
        }
      } else if (function.startsWith("DeleteValue")) {
        // the user removes a value of an axe

        boolean isAdmin = pdcSC.isPDCAdmin() || pdcSC.isAxisManager()
            || pdcSC.isInheritedManager();
        if (!isAdmin) {
          throw new AccessForbiddenException("PdcRequestRouter.DeleteValue",
              SilverpeasException.WARNING, null);
        }

        // remove the value
        String valueId = pdcSC.getCurrentValue().getValuePK().getId();
        String daughterName = pdcSC.deleteValue(valueId);

        if (daughterName == null) {
          // create the new destination
          destination = "/pdcPeas/jsp/reloadAxis.jsp";
        } else {
          Value currentValue = pdcSC.getAxisValue(valueId);
          request.setAttribute("Value", currentValue);
          request.setAttribute("Path", pdcSC.getFullPath(currentValue.getPK().getId()));
          if (currentValue.getLevelNumber() == 0) {
            request.setAttribute("Root", "1");
          } else {
            request.setAttribute("Root", "0");
          }

          request.setAttribute("Id", valueId);
          request.setAttribute("DaughterNameWhichAlreadyExist", daughterName);
          destination = "/pdcPeas/jsp/value.jsp";
        }

      } else if (function.startsWith("DeleteArbo")) {
        // the user removes a value of an axe and its subvalue

        boolean isAdmin = pdcSC.isPDCAdmin() || pdcSC.isAxisManager()
            || pdcSC.isInheritedManager();
        if (!isAdmin) {
          throw new AccessForbiddenException("PdcRequestRouter.DeleteValue",
              SilverpeasException.WARNING, null);
        }

        // remove the value and subtree
        pdcSC.deleteValueAndSubtree(pdcSC.getCurrentValue().getValuePK().getId());

        // create the new destination
        destination = "/pdcPeas/jsp/reloadAxis.jsp";

      } else if (function.startsWith("NewMotherValue")) {
        // the user creates a mother value

        Axis currentAxis = pdcSC.getCurrentAxis();
        Value currentValue = pdcSC.getCurrentValue();

        // set into the request
        request.setAttribute("Value", currentValue);
        request.setAttribute("Sisters", getSisterValues(currentAxis,
            currentValue));

        if (currentValue.getLevelNumber() == 0) {
          request.setAttribute("Root", "1");
        } else {
          request.setAttribute("Root", "0");
        }

        request.setAttribute("Translation", pdcSC.getCurrentLanguage());

        // create the new destination
        destination = "/pdcPeas/jsp/newMotherValue.jsp";

      } else if (function.startsWith("NewDaughterValue")) {
        // the user creates a daughter value

        // set into the request
        request.setAttribute("AddType", "M");
        request.setAttribute("Sisters", getDaughterValues(pdcSC.getCurrentAxis(), pdcSC.
            getCurrentValue()));
        request.setAttribute("Value", pdcSC.getCurrentValue());

        request.setAttribute("Translation", pdcSC.getCurrentLanguage());

        // create the new destination
        destination = "/pdcPeas/jsp/newDaughterValue.jsp";

      } else if (function.startsWith("UpdateValue")) {
        // the user modifies a value of an axe

        // get URL parameters
        String valueId = request.getParameter("Id");
        String valueName = request.getParameter("Name").trim();
        String valueDescription = request.getParameter("Description").trim();
        String valueOrder = extractOrder(request.getParameter("Order"));

        Value currentValue = pdcSC.getAxisValue(valueId);
        // update the value object
        Value updatedValue = new Value(valueId, "unknown", valueName,
            valueDescription, null, null, null, -1, (Integer.parseInt(valueOrder)), null);

        // récupération des traductions
        I18NHelper.setI18NInfo(updatedValue, request);

        int status = pdcSC.updateValue(updatedValue);
        Axis currentAxis = pdcSC.getCurrentAxis();
        switch (status) {
          case 1:
            request.setAttribute("Value", currentValue);
            request.setAttribute("AlreadyExist", "1"); // already exist
            request.setAttribute("Sisters", getSisterValues(currentAxis,
                currentValue));
            request.setAttribute("Path", pdcSC.getFullPath(currentValue.getPK().getId()));
            if (currentValue.getLevelNumber() == 0) {
              request.setAttribute("Root", "1");
            } else {
              request.setAttribute("Root", "0");
            }

            destination = "/pdcPeas/jsp/editValue.jsp";// destination =
            // getDestination("EditValue",
            // componentSC, request);
            break;
          default:
            destination = "/pdcPeas/jsp/reloadAxis.jsp";
        }

      } else if (function.startsWith("CreateMotherValue")) {
        // create a new value

        // get URL parameters
        String valueName = request.getParameter("Name").trim(); // get the name
        // of the axe
        String valueDescription = request.getParameter("Description").trim(); // get
        // the
        // description
        // of
        // the
        // axe
        String valueOrder = extractOrder(request.getParameter("Order"));

        // create the axe
        Value value = new Value("UNKNOWN", "unknown", valueName,
            valueDescription, null, null, null, -1, (Integer.parseInt(valueOrder)), null);
        int status = pdcSC.insertMotherValue(value);

        switch (status) {
          case 1:
            request.setAttribute("ValueToCreate", value);
            Value currentValue = pdcSC.getCurrentValue();
            if (currentValue.getLevelNumber() == 0) {
              request.setAttribute("Root", "1");
            } else {
              request.setAttribute("Root", "0");
            }
            destination = getDestination("NewMotherValue", pdcSC, request);
            break;
          default:
            destination = "/pdcPeas/jsp/reloadAxis.jsp";
        }

      } else if (function.startsWith("CreateDaughterValue")) {
        // create a new value

        // get URL parameters
        String valueName = request.getParameter("Name").trim(); // get the name
        // of the axe
        String valueDescription = request.getParameter("Description").trim(); // get
        // the description of the axe
        String valueOrder = extractOrder(request.getParameter("Order"));

        // create the axe
        Value value = new Value("UNKNOWN", "unknown", valueName,
            valueDescription, null, null, null, -1, (Integer.parseInt(valueOrder)), null);
        int status = pdcSC.createDaughterValue(value);

        I18NHelper.setI18NInfo(value, request);

        switch (status) {
          case 1:
            request.setAttribute("ValueToCreate", value);
            destination = getDestination("NewDaughterValue", pdcSC, request);
            break;
          default:
            request.setAttribute("ValueCreated", "1");
            destination = getDestination("NewDaughterValue", pdcSC, request);
        }

      } else if (function.startsWith("ToRefresh")) {
        destination = "/pdcPeas/jsp/refreshView.jsp";

      } else if (function.startsWith("CloseCreationDaughterValue")) {
        destination = "/pdcPeas/jsp/reloadAxis.jsp";

      } else if (function.startsWith("ViewManager")) {
        // to view the permissions on the current value

        Value currentValue = pdcSC.getCurrentValue();

        request.setAttribute("Value", currentValue);
        request.setAttribute("Axis", pdcSC.getCurrentAxis().getAxisHeader());

        // on passe en paramètre les droits directs
        List<UserDetail> users = (List<UserDetail>) pdcSC.getManagers().get(0);
        List<Group> groups = (List<Group>) pdcSC.getManagers().get(1);
        request.setAttribute("Users", users);
        request.setAttribute("Groups", groups);

        if (currentValue != null) {
          // on passe en paramètre les droits hérités
          List inheritedManagers = pdcSC.getInheritedManagers(currentValue);
          List<UserDetail> usersInherited = (List<UserDetail>) inheritedManagers.get(0);
          List<Group> groupsInherited = (List<Group>) inheritedManagers.get(1);
          request.setAttribute("UsersInherited", usersInherited);
          request.setAttribute("GroupsInherited", groupsInherited);
        } else {
          List emptyList = new ArrayList();
          request.setAttribute("UsersInherited", emptyList);
          request.setAttribute("GroupsInherited", emptyList);
        }

        destination = "/pdcPeas/jsp/viewValueManager.jsp";

      } else if (function.startsWith("EditManager")) {
        // to choose the permissions on the current value
        destination = pdcSC.initUserPanelForPdcManager();
      } else if (function.startsWith("UpdateManager")) {
        // to update the permissions on the current value
        try {
          pdcSC.updateManager();
        } catch (Exception e) {
          SilverTrace.warn("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        destination = getDestination("ViewManager", pdcSC, request);
      } else if (function.startsWith("EraseManager")) {
        // to delete the permissions on the current value
        try {
          pdcSC.eraseManagers();
        } catch (Exception e) {
          SilverTrace.warn("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        destination = getDestination("ViewManager", pdcSC, request);

      } else if (function.startsWith("vsicAddPdc")) {

        destination = "/pdcPeas/jsp/addPdc_test.jsp";

      } else if (function.startsWith("vsicAddList")) {
        // assign attributes into the request
        String values = (String) request.getParameter("Values");
        if (values != null) {
          pdcSC.setValuesToPdcAddAPosteriori(values);
        }

        request.setAttribute("AxisList", pdcSC.getAxis()); // set a sorted list
        request.setAttribute("ViewType", pdcSC.getCurrentView()); // the type of
        // the axe

        destination = "/pdcPeas/jsp/directValuesTree.jsp";

      } else if (function.startsWith("vsicChangeType")) {
        String type = request.getParameter("ViewType"); // get the id of the
        // selected axe

        pdcSC.setCurrentView(type);
        destination = getDestination("vsicAddList", pdcSC, request);

      } else if (function.startsWith("vsicAddTree")) {// get URL parameters
        String axeId = request.getParameter("Id"); // get the id of the selected axis
        request.setAttribute("Axis", pdcSC.getAxisDetail(axeId));
        destination = "/pdcPeas/jsp/directValuesAxis.jsp";

      } else if (function.startsWith("vsicAddAxis")) {
        String valueId = request.getParameter("valueId"); // get the id of the selected axis
        String res = pdcSC.getValuesToPdcAddAPosteriori();
        String val = "";
        // just to set the current value
        pdcSC.getAxisValue(valueId);
        if (StringUtil.isDefined(res)) {
          StringTokenizer st = new StringTokenizer(res, ";");
          while (st.hasMoreTokens()) {
            val = st.nextToken();
            Value newValue = new Value("UNKNOWN", "unknown", val, val, null, null, null, -1, -1,
                null);
            pdcSC.createDaughterValue(newValue);
          }
        }

        destination = "/pdcPeas/jsp/addPdc_test.jsp";
      }
    } catch (AccessForbiddenException afe) {
      destination = "/admin/jsp/accessForbidden.jsp";
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  /**
   * Search the axisHeader whished from the sorted list of axis header.
   * @param axeId - the id of the whished axisHeader
   * @param axis - A primary or secondary list of axes
   * @return the whished AxisHeader or null
   */
  private AxisHeader extractAxisHeader(String axeId, List<AxisHeader> axis) {
    AxisHeader axisHeader = null;
    Iterator<AxisHeader> it = axis.iterator();

    while (it.hasNext()) {
      axisHeader = it.next();
      if (axisHeader.getPK().getId().equals(axeId)) {
        return axisHeader;
      }
    }

    return axisHeader;
  }

  /**
   * Search sisters values of a Value from an axe.
   * @param axis - A primary or secondary list of axes
   * @param value - a value of the axe
   * @return the list of sisters
   */
  private List<Value> getSisterValues(Axis axis, Value value) {
    List<Value> values = axis.getValues();
    String motherId = value.getMotherId();
    String valueId = value.getPK().getId();
    List<Value> sisterValues = new ArrayList<Value>();
    Value daughterValue = null; // values which are under the current axe
    // read the list of daughter values to determine the sister value
    Iterator<Value> it = values.iterator();
    while (it.hasNext()) {
      daughterValue = it.next();
      if (daughterValue.getMotherId().equals(motherId)
          && !(daughterValue.getPK().getId()).equals(valueId)) {
        sisterValues.add(daughterValue);
      }
    }
    return sisterValues;
  }

  /**
   * Search daughters values of a Value from an axe.
   * @param axis - A primary or secondary list of axes
   * @param mother - a value of the axe
   * @return the list of daughters
   */
  private List<Value> getDaughterValues(Axis axis, Value mother) {
    List<Value> values = axis.getValues();
    String motherId = mother.getPK().getId();

    List<Value> daughterValues = new ArrayList<Value>();
    Value value = null; // values which are under the current axe
    // read the list of daughter values to determine the sister value
    Iterator<Value> it = values.iterator();
    while (it.hasNext()) {
      value = it.next();
      if (value.getMotherId().equals(motherId)) {
        daughterValues.add(value);
      }
    }
    return daughterValues;
  }

  /**
   * Extract the order of axis or values values.
   * @param text - The value of the axis or the value (String+separator+order)
   * @return the order
   */
  private String extractOrder(String text) {
    if (StringUtil.isDefined(text)) {
      int separatorIdx = text.lastIndexOf(PdcRequestRouter.sepOptionValueTag);
      // the separator don't exist. Maybe, it's the last item of the selection
      if (separatorIdx != -1) {
        return text.substring(separatorIdx + PdcRequestRouter.lenOfSeparator);
      } else {
        return text;
      }
    }
    return "0";
  }

  private void setLanguageAsAttribute(PdcSessionController pdcSC,
      HttpServletRequest request) {
    // récupération de la langue et passage en paramètre à la jsp
    String currentLanguage = pdcSC.getCurrentLanguage();
    if (!StringUtil.isDefined(currentLanguage)) {
      currentLanguage = I18NHelper.defaultLanguage;
    }
    request.setAttribute("DisplayLanguage", currentLanguage);
  }
}