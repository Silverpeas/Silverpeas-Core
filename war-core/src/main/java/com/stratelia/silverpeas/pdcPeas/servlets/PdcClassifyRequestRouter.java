package com.stratelia.silverpeas.pdcPeas.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.thesaurus.model.Jargon;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdcPeas.control.PdcClassifySessionController;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;

public class PdcClassifyRequestRouter extends ComponentRequestRouter {

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcClassifySessionController(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle",
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons");
  }

  public String getSessionControlBeanName() {
    return "pdcClassify";
  }

  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";

    PdcClassifySessionController pdcSC = (PdcClassifySessionController) componentSC; // get
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

        String silverObjectId = request.getParameter("SilverObjectId");
        String componentId = request.getParameter("ComponentId");

        pdcSC.clearCurrentSilverObjectIds();
        if (silverObjectId != null)
          pdcSC.setCurrentSilverObjectId(silverObjectId);
        if (componentId != null)
          pdcSC.setCurrentComponentId(componentId);

        List positions = pdcSC.getPositions();

        request.setAttribute("Positions", positions);

        setBrowseContextInRequest(pdcSC, request);

        pdcSC.initializeJargon();
        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", new Boolean(pdcSC
            .getActiveThesaurus()));

        // create the new destination
        destination = "/pdcPeas/jsp/positions.jsp";

      }

      else if (function.startsWith("NewPosition")) {
        // the user wants to add a position to the object

        List usedAxis = pdcSC.getUsedAxisToClassify();

        request.setAttribute("UsedAxis", usedAxis);

        setBrowseContextInRequest(pdcSC, request);

        request.setAttribute("ListValues", new ArrayList());

        // !!! workaround to get the searchContext
        HttpSession session = request.getSession(true);
        PdcSearchSessionController pdcSearchSC = (PdcSearchSessionController) session
            .getAttribute("Silverpeas_pdcSearch_"
                + pdcSC.getCurrentComponentId());

        if (pdcSearchSC != null)
          request.setAttribute("SearchContext", pdcSearchSC.getSearchContext());

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", new Boolean(pdcSC
            .getActiveThesaurus()));

        destination = "/pdcPeas/jsp/positionAdd.jsp";
      }

      else if (function.startsWith("EditPosition")) {
        // the user wants to update a position to the object

        int positionId = new Integer(request.getParameter("Id")).intValue();

        List usedAxis = pdcSC.getUsedAxisToClassify();
        request.setAttribute("UsedAxis", usedAxis);

        List positions = pdcSC.getPositions();

        // Extract position from list
        ClassifyPosition position = null;
        for (int i = 0; i < positions.size(); i++) {
          position = (ClassifyPosition) positions.get(i);
          if (position.getPositionId() == positionId)
            break;
        }

        request.setAttribute("Position", position);

        setBrowseContextInRequest(pdcSC, request);

        request.setAttribute("ListValues", new ArrayList());
        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", new Boolean(pdcSC
            .getActiveThesaurus()));

        destination = "/pdcPeas/jsp/positionEdit.jsp";

      }

      else if (function.startsWith("DeletePosition")) {
        // the user wants to delete some positions

        String positionIds = request.getParameter("Ids");

        StringTokenizer st = new StringTokenizer(positionIds, ",");
        for (; st.hasMoreTokens();) {
          pdcSC.deletePosition(st.nextToken());
        }
        String toURL = request.getParameter("ToURL");
        if (toURL != null && toURL.length() > 0) {
          request.setAttribute("ToURL", toURL);
          destination = "/pdcPeas/jsp/redirectToComponent.jsp";
        } else
          destination = getDestination("Main", componentSC, request);
      }

      else if (function.startsWith("CreatePosition")) {
        // the user wants to create a new position

        String selectedValues = request.getParameter("Values");
        ClassifyPosition position = buildPosition(null, selectedValues);
        int status = pdcSC.addPosition(position);

        switch (status) {
          case 1:
            request.setAttribute("Position", position);
            request.setAttribute("ErrorVariant", "1");
            List usedAxis = pdcSC.getUsedAxis();
            request.setAttribute("UsedAxis", usedAxis);
            setBrowseContextInRequest(pdcSC, request);
            request.setAttribute("ListValues", new ArrayList());
            destination = "/pdcPeas/jsp/positionAdd.jsp";
            break;
          default:
            destination = "/pdcPeas/jsp/reload.jsp";
        }
      }

      else if (function.startsWith("ReloadPosition")) {

        String selectedValues = request.getParameter("Values");
        Collection listPosition = buildListPosition(selectedValues);
        request.setAttribute("ListValues", listPosition);

        List usedAxis = pdcSC.getUsedAxisToClassify();
        request.setAttribute("UsedAxis", usedAxis);
        setBrowseContextInRequest(pdcSC, request);

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", new Boolean(pdcSC
            .getActiveThesaurus()));

        destination = "/pdcPeas/jsp/positionAdd.jsp";

      }

      else if (function.startsWith("UpdatePosition")) {
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
            List usedAxis = pdcSC.getUsedAxis();
            request.setAttribute("UsedAxis", usedAxis);
            setBrowseContextInRequest(pdcSC, request);
            request.setAttribute("ListValues", new ArrayList());
            destination = "/pdcPeas/jsp/positionEdit.jsp";
            break;
          default:
            destination = "/pdcPeas/jsp/reload.jsp";
        }

      }

      else if (function.startsWith("ReloadUpdatePosition")) {

        int positionId = new Integer(request.getParameter("Id")).intValue();

        List usedAxis = pdcSC.getUsedAxisToClassify();
        request.setAttribute("UsedAxis", usedAxis);

        List positions = pdcSC.getPositions();

        // Extract position from list
        ClassifyPosition position = null;
        for (int i = 0; i < positions.size(); i++) {
          position = (ClassifyPosition) positions.get(i);
          if (position.getPositionId() == positionId)
            break;
        }

        request.setAttribute("LastPosition", position);

        setBrowseContextInRequest(pdcSC, request);

        String selectedValues = request.getParameter("Values");
        Collection listPosition = buildListPosition(selectedValues);
        request.setAttribute("ListValues", listPosition);

        Jargon jargon = pdcSC.getJargon();
        request.setAttribute("Jargon", jargon);
        request.setAttribute("ActiveThesaurus", new Boolean(pdcSC
            .getActiveThesaurus()));

        destination = "/pdcPeas/jsp/positionEdit.jsp";

      }

      else if (function.equals("ToAddPositions")) {
        String objectIds = request.getParameter("ObjectIds"); // silverObjectIds
        List lObjectIds = (List) request.getAttribute("ObjectIds");

        String componentId = request.getParameter("ComponentId");
        if (!StringUtil.isDefined(componentId))
          componentId = (String) request.getAttribute("ComponentId");

        pdcSC.initializeJargon();
        pdcSC.clearCurrentSilverObjectIds();
        pdcSC.setCurrentSilverObjectId(-1);

        if (StringUtil.isDefined(componentId))
          pdcSC.setCurrentComponentId(componentId);

        if (StringUtil.isDefined(objectIds)) {
          StringTokenizer st = new StringTokenizer(objectIds, ",");
          while (st.hasMoreTokens())
            pdcSC.addCurrentSilverObjectId(st.nextToken());
        } else if (lObjectIds != null) {
          Iterator it = lObjectIds.iterator();
          while (it.hasNext())
            pdcSC.addCurrentSilverObjectId((String) it.next());
        }

        destination = getDestination("NewPosition", componentSC, request);

      }

      else {
        destination = "/pdcPeas/jsp/" + function;
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private ClassifyPosition buildPosition(String positionId, String valuesFromJsp) {
    // valuesFromJsp looks like 12|/0/1/2/,14|/15/34/
    // [axisId|valuePath+valueId]*
    StringTokenizer st = new StringTokenizer(valuesFromJsp, ",");
    String valueInfo = "";
    String axisId = "";
    String valuePath = "";
    ClassifyValue value = null;
    ArrayList values = new ArrayList();
    for (; st.hasMoreTokens();) {
      valueInfo = st.nextToken();
      if (valueInfo.length() >= 3) {
        axisId = valueInfo.substring(0, valueInfo.indexOf("|"));
        valuePath = valueInfo.substring(valueInfo.indexOf("|") + 1, valueInfo
            .length());
        value = new ClassifyValue(new Integer(axisId).intValue(), valuePath);
        values.add(value);
      }
    }

    int id = -1;
    if (positionId != null)
      id = new Integer(positionId).intValue();
    ClassifyPosition position = new ClassifyPosition(values);
    position.setPositionId(id);
    return position;
  }

  private Collection buildListPosition(String valuesFromJsp) {
    // valuesFromJsp looks like 12|/0/1/2/,14|/15/34/
    // [axisId|valuePath+valueId]*

    StringTokenizer st = new StringTokenizer(valuesFromJsp, ",");
    String valueInfo = "";
    ArrayList values = new ArrayList();

    for (; st.hasMoreTokens();) {
      valueInfo = st.nextToken();
      values.add(valueInfo);
    }

    return values;
  }

  private void setBrowseContextInRequest(PdcClassifySessionController pdcSC,
      HttpServletRequest request) {
    request.setAttribute("browseContext", new String[] { pdcSC.getSpaceLabel(),
        pdcSC.getComponentLabel() });
  }

}