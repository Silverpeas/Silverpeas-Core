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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package com.stratelia.silverpeas.pdcPeas.servlets;

import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdcPeas.control.PdcUtilizationSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;

public class PdcUtilizationRequestRouter extends ComponentRequestRouter {

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcUtilizationSessionController(mainSessionCtrl,
        componentContext,
        "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle",
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons");
    // return new PdcUtilizationSessionController();
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  public String getSessionControlBeanName() {
    return "pdcUtilization";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";

    PdcUtilizationSessionController pdcSC = (PdcUtilizationSessionController) componentSC; // get
    // the
    // session
    // controller
    // to
    // inform
    // the
    // request
    try {

      if (function.startsWith("Main")) {
        // the user is on the main page

        String componentId = request.getParameter("ComponentId");
        if (componentId != null)
          pdcSC.init(componentId);

        List list = pdcSC.getUsedAxisByInstanceId();

        // assign attributes into the request
        request.setAttribute("AxisList", list); // set a sorted list

        setBrowseContextInRequest(pdcSC, request);

        // create the new destination
        destination = "/pdcPeas/jsp/utilization.jsp";

      } else if (function.startsWith("ChangeViewType")) {
        // the user changes the view

        // get URL parameters
        String type = request.getParameter("ViewType");

        pdcSC.setCurrentView(type);

        setBrowseContextInRequest(pdcSC, request);

        // create the new destination
        destination = getDestination("UtilizationViewAxis", componentSC,
            request);

      } else if (function.startsWith("UtilizationViewAxis")) {
        // the user wants to choose an axis

        List list = pdcSC.getAxis();

        request.setAttribute("AxisList", list);
        request.setAttribute("ViewType", pdcSC.getCurrentView());

        setBrowseContextInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/utilizationChoose.jsp";

      } else if (function.startsWith("UtilizationChooseAxis")) {
        // the user wants to use an axis

        String axisId = request.getParameter("Id");
        Axis axis = pdcSC.getAxisDetail(axisId);

        // Recherche si cet axe est déjà utilisé
        List list = pdcSC.getUsedAxisByInstanceId();
        UsedAxis usedAxis = null;
        Integer isMandatory = null;
        Integer isVariant = null;
        for (int i = 0; i < list.size(); i++) {
          usedAxis = (UsedAxis) list.get(i);
          if (usedAxis.getAxisId() == new Integer(axisId).intValue()) {
            // L'axe est déjà utilisé
            // On récupère ses paramêtres
            isMandatory = new Integer(usedAxis.getMandatory());
            isVariant = new Integer(usedAxis.getVariant());
            break;
          }
        }

        request.setAttribute("AxisDetail", axis);
        request.setAttribute("IsMandatory", isMandatory);
        request.setAttribute("IsVariant", isVariant);

        setBrowseContextInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/utilizationAdd.jsp";

      } else if (function.startsWith("UtilizationAddAxis")) {
        // the user wants to add an axis

        String baseValue = request.getParameter("BaseValue");
        String mandatoryStr = request.getParameter("Mandatory");
        String variantStr = request.getParameter("Variant");

        int mandatory = 1;
        if (mandatoryStr != null && mandatoryStr.equals("0")) {
          mandatory = 0;
        }

        int variant = 1;
        if (variantStr != null && variantStr.equals("0")) {
          variant = 0;
        }

        UsedAxis usedAxis = new UsedAxis(-1, "unknown", -1, new Integer(
            baseValue).intValue(), mandatory, variant);

        int status = pdcSC.addUsedAxis(usedAxis);

        switch (status) {
          case 1:
            request.setAttribute("UsedAxis", usedAxis);
            request.setAttribute("AxisDetail", pdcSC.getCurrentAxis());

            String axisId = request.getParameter("Id");

            // Recherche si cet axe est déjà utilisé
            List list = pdcSC.getUsedAxisByInstanceId();
            Integer isMandatory = null;
            Integer isVariant = null;
            for (int i = 0; i < list.size(); i++) {
              usedAxis = (UsedAxis) list.get(i);
              if (usedAxis.getAxisId() == new Integer(axisId).intValue()) {
                // L'axe est déjà utilisé
                // On récupère ses paramêtres
                isMandatory = new Integer(usedAxis.getMandatory());
                isVariant = new Integer(usedAxis.getVariant());
                break;
              }
            }

            request.setAttribute("IsMandatory", isMandatory);
            request.setAttribute("IsVariant", isVariant);

            setBrowseContextInRequest(pdcSC, request);

            destination = "/pdcPeas/jsp/utilizationAdd.jsp";
            break;
          default:
            destination = getDestination("Main", componentSC, request);
        }

      } else if (function.startsWith("UtilizationEditAxis")) {
        // the user wants to edit an used axis

        String usedAxisId = request.getParameter("Id");
        UsedAxis usedAxis = pdcSC.getUsedAxis(usedAxisId);
        Axis axis = pdcSC.getAxisDetail(new Integer(usedAxis.getAxisId())
            .toString());

        request.setAttribute("AxisDetail", axis);
        request.setAttribute("UsedAxis", usedAxis);

        setBrowseContextInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/utilizationEdit.jsp";

      } else if (function.startsWith("UtilizationUpdateAxis")) {
        // the user wants to update an used axis

        String usedAxisId = request.getParameter("Id");
        String baseValue = request.getParameter("BaseValue");
        String mandatoryStr = request.getParameter("Mandatory");
        String variantStr = request.getParameter("Variant");

        int mandatory = 1;
        if (mandatoryStr != null && mandatoryStr.equals("0")) {
          mandatory = 0;
        }

        int variant = 1;
        if (variantStr != null && variantStr.equals("0")) {
          variant = 0;
        }

        UsedAxis usedAxis = new UsedAxis(usedAxisId, "unknown", -1,
            new Integer(baseValue).intValue(), mandatory, variant);

        int status = pdcSC.updateUsedAxis(usedAxis);

        switch (status) {
          case 1:
            request.setAttribute("UsedAxis", usedAxis);
            request.setAttribute("AxisDetail", pdcSC.getCurrentAxis());
            request.setAttribute("ErrorState", "1");
            setBrowseContextInRequest(pdcSC, request);
            destination = "/pdcPeas/jsp/utilizationEdit.jsp";
            break;
          case 2:
            request.setAttribute("UsedAxis", usedAxis);
            request.setAttribute("AxisDetail", pdcSC.getCurrentAxis());
            request.setAttribute("ModificationNotAllowed", "1");
            setBrowseContextInRequest(pdcSC, request);
            destination = "/pdcPeas/jsp/utilizationEdit.jsp";
            break;
          default:
            destination = getDestination("Main", componentSC, request);
        }

      } else if (function.startsWith("UtilizationDeleteAxis")) {
        // the user wants to delete some used axis

        String usedAxisIds = request.getParameter("Ids");

        StringTokenizer st = new StringTokenizer(usedAxisIds, ",");
        for (; st.hasMoreTokens();) {
          pdcSC.deleteUsedAxis(st.nextToken());
        }

        destination = getDestination("Main", componentSC, request);

      } else {
        destination = "/pdcPeas/jsp/" + function;
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private void setBrowseContextInRequest(PdcUtilizationSessionController pdcSC,
      HttpServletRequest request) {
    request.setAttribute("browseContext", new String[] { pdcSC.getSpaceLabel(),
        pdcSC.getComponentLabel() });
  }

}