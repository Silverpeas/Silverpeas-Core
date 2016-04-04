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

import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.web.pdc.control.PdcClassifySessionController;
import org.silverpeas.web.pdc.control.PdcFieldPositionsManager;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class PdcClassifyRequestRouter extends ComponentRequestRouter<PdcClassifySessionController> {

  private static final long serialVersionUID = -7647574714509474585L;

  @Override
  public PdcClassifySessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcClassifySessionController(mainSessionCtrl, componentContext,
        "org.silverpeas.pdcPeas.multilang.pdcBundle",
        "org.silverpeas.pdcPeas.settings.pdcPeasIcons");
  }

  @Override
  public String getSessionControlBeanName() {
    return "pdcClassify";
  }

  protected void setUpPdcSession(final PdcClassifySessionController pdcSC,
      final HttpServletRequest request) throws Exception {
    String silverObjectId = request.getParameter("SilverObjectId");
    String componentId = request.getParameter("ComponentId");
    if (isDefined(componentId)) {
      if (!isDefined(silverObjectId)) {
        String contentId = request.getParameter("ContentId");
        ContentManager contentManager = new ContentManager();
        silverObjectId = String.valueOf(contentManager.getSilverContentId(contentId, componentId));
      }

      if (isDefined(silverObjectId)) {
        pdcSC.clearCurrentSilverObjectIds();
        pdcSC.setCurrentSilverObjectId(silverObjectId);
        pdcSC.setCurrentComponentId(componentId);
        pdcSC.initializeJargon();
      }
    }
  }

  @Override
  public String getDestination(String function, PdcClassifySessionController pdcSC,
      HttpRequest request) {
    String destination = "";

    // get the session controller to inform the request
    PdcFieldPositionsManager pdcFPM = pdcSC.getPdcFieldPositionsManager();

    try {

      if (function.startsWith("Main")) {
        // the user is on the main page
        pdcFPM.reset();
        setUpPdcSession(pdcSC, request);

        List<ClassifyPosition> positions = pdcSC.getPositions();

        request.setAttribute("Positions", positions);

        setBrowseContextInRequest(pdcSC, request);

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", pdcSC.getActiveThesaurus());

        // create the new destination
        destination = "/pdcPeas/jsp/positions.jsp";

      } else if (function.startsWith("NewPosition")) {
        setUpPdcSession(pdcSC, request);
        // the user wants to add a position to the object
        request.setAttribute("UsedAxis", pdcSC.getUsedAxisToClassify());

        setBrowseContextInRequest(pdcSC, request);

        request.setAttribute("ListValues", new ArrayList());

        // !!! workaround to get the searchContext
        HttpSession session = request.getSession(true);
        PdcSearchSessionController pdcSearchSC =
            (PdcSearchSessionController) session.getAttribute("Silverpeas_pdcSearch_"
            + pdcSC.getCurrentComponentId());

        if (pdcSearchSC != null) {
          request.setAttribute("SearchContext", pdcSearchSC.getSearchContext());
        }

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", pdcSC.getActiveThesaurus());

        destination = "/pdcPeas/jsp/positionAdd.jsp";
      } else if (function.startsWith("EditPosition")) {
        setUpPdcSession(pdcSC, request);

        // the user wants to update a position to the object
        int positionId = new Integer(request.getParameter("Id")).intValue();

        request.setAttribute("UsedAxis", pdcSC.getUsedAxisToClassify());

        List<ClassifyPosition> positions = pdcSC.getPositions();

        // Extract position from list
        for (ClassifyPosition position : positions) {
          if (position.getPositionId() == positionId) {
            request.setAttribute("Position", position);
            break;
          }
        }

        setBrowseContextInRequest(pdcSC, request);

        request.setAttribute("ListValues", new ArrayList());
        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", pdcSC.getActiveThesaurus());

        destination = "/pdcPeas/jsp/positionEdit.jsp";

      } else if (function.startsWith("DeletePosition")) {
        // the user wants to delete some positions

        String positionIds = request.getParameter("Ids");

        StringTokenizer st = new StringTokenizer(positionIds, ",");
        for (; st.hasMoreTokens();) {
          pdcSC.deletePosition(st.nextToken());
        }
        if (pdcFPM.isEnabled()) {
          destination = getPdcFieldModeReturnDestination(
              request, pdcFPM, pdcSC.getString("pdcPeas.deletedPosition"));
        } else {
          String toURL = request.getParameter("ToURL");
          if (toURL != null && toURL.length() > 0) {
            request.setAttribute("ToURL", toURL);
            destination = "/pdcPeas/jsp/redirectToComponent.jsp";
          } else {
            destination = getDestination("Main", pdcSC, request);
          }
        }
      } else if (function.startsWith("CreatePosition")) {
        // the user wants to create a new position

        String selectedValues = request.getParameter("Values");
        ClassifyPosition position = buildPosition(null, selectedValues);
        int status = pdcSC.addPosition(position);

        switch (status) {
          case 1:
            request.setAttribute("Position", position);
            request.setAttribute("ErrorVariant", "1");
            request.setAttribute("UsedAxis", pdcSC.getUsedAxis());
            setBrowseContextInRequest(pdcSC, request);
            request.setAttribute("ListValues", new ArrayList());
            destination = "/pdcPeas/jsp/positionAdd.jsp";
            break;
          default:
            if (pdcFPM.isEnabled()) {
              destination = getPdcFieldModeReturnDestination(request, pdcFPM, null);
            } else {
              destination = "/pdcPeas/jsp/reload.jsp";
            }
        }
      } else if (function.startsWith("ReloadPosition")) {

        String selectedValues = request.getParameter("Values");
        request.setAttribute("ListValues", buildListPosition(selectedValues));
        request.setAttribute("UsedAxis", pdcSC.getUsedAxisToClassify());
        setBrowseContextInRequest(pdcSC, request);

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", pdcSC.getActiveThesaurus());

        destination = "/pdcPeas/jsp/positionAdd.jsp";

      } else if (function.startsWith("UpdatePosition")) {
        // the user wants to update a position

        String positionId = request.getParameter("Id");
        String selectedValues = request.getParameter("Values");

        ClassifyPosition position = buildPosition(positionId, selectedValues);

        // il faut tenir compte de la variance de la nouvelle position
        int status = pdcSC.updatePosition(position);

        switch (status) {
          case 1:
            request.setAttribute("Position", position);
            request.setAttribute("ErrorVariant", "1");
            request.setAttribute("UsedAxis", pdcSC.getUsedAxis());
            setBrowseContextInRequest(pdcSC, request);
            request.setAttribute("ListValues", new ArrayList());
            destination = "/pdcPeas/jsp/positionEdit.jsp";
            break;
          default:
            if (pdcFPM.isEnabled()) {
              destination = getPdcFieldModeReturnDestination(request, pdcFPM, null);
            } else {
              destination = "/pdcPeas/jsp/reload.jsp";
            }
        }

      } else if (function.startsWith("ReloadUpdatePosition")) {

        int positionId = new Integer(request.getParameter("Id")).intValue();

        request.setAttribute("UsedAxis", pdcSC.getUsedAxisToClassify());

        List<ClassifyPosition> positions = pdcSC.getPositions();

        // Extract position from list
        ClassifyPosition position = null;
        for (int i = 0; i < positions.size(); i++) {
          position = positions.get(i);
          if (position.getPositionId() == positionId) {
            break;
          }
        }

        request.setAttribute("LastPosition", position);

        setBrowseContextInRequest(pdcSC, request);

        String selectedValues = request.getParameter("Values");
        request.setAttribute("ListValues", buildListPosition(selectedValues));

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", pdcSC.getActiveThesaurus());

        destination = "/pdcPeas/jsp/positionEdit.jsp";

      } else if (function.equals("ToAddPositions")) {
        String objectIds = request.getParameter("ObjectIds"); // silverObjectIds
        List<String> lObjectIds = (List<String>) request.getAttribute("ObjectIds");

        String componentId = request.getParameter("ComponentId");
        if (!isDefined(componentId)) {
          componentId = (String) request.getAttribute("ComponentId");
        }

        pdcSC.initializeJargon();
        pdcSC.clearCurrentSilverObjectIds();
        pdcSC.setCurrentSilverObjectId(-1);

        if (isDefined(componentId)) {
          pdcSC.setCurrentComponentId(componentId);
        }

        if (isDefined(objectIds)) {
          StringTokenizer st = new StringTokenizer(objectIds, ",");
          while (st.hasMoreTokens()) {
            pdcSC.addCurrentSilverObjectId(st.nextToken());
          }
        } else if (lObjectIds != null) {
          Iterator<String> it = lObjectIds.iterator();
          while (it.hasNext()) {
            pdcSC.addCurrentSilverObjectId(it.next());
          }
        }

        destination = getDestination("NewPosition", pdcSC, request);

      } else if (function.equals("PdcFieldMode")) {
        String pdcFieldName = request.getParameter("pdcFieldName");
        String pdcFieldPositions = request.getParameter("pdcFieldPositions");
        String pdcAxis = request.getParameter("pdcAxis");
        String action = request.getParameter("action");
        pdcFPM.init(pdcFieldName, pdcFieldPositions, pdcAxis);
        destination = getDestination(action, pdcSC, request);
      } else {
        destination = "/pdcPeas/jsp/" + function;
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  /**
   * @param request The HTTP request.
   * @param pdcFPM The PDC field positions manager.
   * @param message If needed, a message to display on the reload page.
   * @return The reload page to display to update the positions of a PDC field.
   */
  private String getPdcFieldModeReturnDestination(HttpServletRequest request,
      PdcFieldPositionsManager pdcFPM, String message) {
    request.setAttribute("pdcFieldName", pdcFPM.getFieldName());
    request.setAttribute("pdcFieldPositions", pdcFPM.getPositionsToString());
    if (message != null) {
      request.setAttribute("infoMessage", message);
    }
    return "/pdcPeas/jsp/reload.jsp?pdcFieldMode=true";
  }

  private ClassifyPosition buildPosition(String positionId, String valuesFromJsp) {
    // valuesFromJsp looks like 12|/0/1/2/,14|/15/34/
    // [axisId|valuePath+valueId]*
    StringTokenizer st = new StringTokenizer(valuesFromJsp, ",");
    String valueInfo = "";
    String axisId = "";
    String valuePath = "";
    ClassifyValue value = null;
    List<ClassifyValue> values = new ArrayList<ClassifyValue>();
    for (; st.hasMoreTokens();) {
      valueInfo = st.nextToken();
      if (valueInfo.length() >= 3) {
        axisId = valueInfo.substring(0, valueInfo.indexOf("|"));
        valuePath = valueInfo.substring(valueInfo.indexOf("|") + 1, valueInfo.length());
        if (valuePath.startsWith("/")) {
          value = new ClassifyValue(Integer.parseInt(axisId), valuePath);
          values.add(value);
        }
      }
    }

    int id = -1;
    if (positionId != null) {
      id = Integer.parseInt(positionId);
    }
    ClassifyPosition position = new ClassifyPosition(values);
    position.setPositionId(id);
    return position;
  }

  private Collection<String> buildListPosition(String valuesFromJsp) {
    // valuesFromJsp looks like 12|/0/1/2/,14|/15/34/
    // [axisId|valuePath+valueId]*

    StringTokenizer st = new StringTokenizer(valuesFromJsp, ",");
    String valueInfo = "";
    List<String> values = new ArrayList<String>();

    for (; st.hasMoreTokens();) {
      valueInfo = st.nextToken();
      values.add(valueInfo);
    }

    return values;
  }

  private void setBrowseContextInRequest(PdcClassifySessionController pdcSC,
      HttpServletRequest request) {
    request.setAttribute("browseContext", new String[] { pdcSC.getSpaceLabel(),
        pdcSC.getComponentLabel(), pdcSC.getSpaceId(), pdcSC.getCurrentComponentId() });
  }
}