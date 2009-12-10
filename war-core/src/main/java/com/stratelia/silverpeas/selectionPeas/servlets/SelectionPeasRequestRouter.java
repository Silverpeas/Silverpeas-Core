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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selectionPeas.servlets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionExtraParams;
import com.stratelia.silverpeas.selectionPeas.CacheManager;
import com.stratelia.silverpeas.selectionPeas.control.SelectionPeasSessionController;
import com.stratelia.silverpeas.selectionPeas.jdbc.JdbcConnectorSetting;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasTrappedException;

/**
 * Class declaration
 * @author
 */
public class SelectionPeasRequestRouter extends ComponentRequestRouter {

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SelectionPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "selectionPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    SelectionPeasSessionController selectionPeasSC = (SelectionPeasSessionController) componentSC;
    SilverTrace.info("selectionPeas", "getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Function=" + function);

    try {
      if (function.equals("Main")) {
        selectionPeasSC.initSC(request.getParameter("SelectionType"));

        if (Selection.TYPE_JDBC_CONNECTOR.equals(selectionPeasSC
            .getSelectionType())) {
          initJdbcConnectorSetting(request, selectionPeasSC);
        }

        function = selectionPeasSC.getStartingFunction();
      } else if (function.equals("ReturnSearchElement")) {
        function = selectionPeasSC.returnSearchElement();
      } else if (function.equals("ReturnSearchSet")) {
        function = selectionPeasSC.returnSearchSet();
      }

      if (function.equals("DisplaySearchElement")) {
        destination = selectionPeasSC.getSearchElement();
      } else if (function.equals("DisplaySearchSet")) {
        destination = selectionPeasSC.getSearchSet();
      } else if (function.equals("DisplayBrowse")) {
        selectionPeasSC.initBrowse();
        destination = "selectionPeas.jsp";
      } else if (function.equals("DisplayCart")) {
        selectionPeasSC.initCart();
        destination = "selectionCart.jsp";
      } else if (function.equals("Validate")) {
        selectionPeasSC.validate();
        request.setAttribute("HostUrl", selectionPeasSC.getGoBackURL());
        destination = "goBack.jsp";
      } else if (function.equals("ValidateAndSetOpener")) {
        selectionPeasSC.validate();

        if (Selection.TYPE_USERS_GROUPS.equals(selectionPeasSC
            .getSelectionType())) {
          request.setAttribute("formName", selectionPeasSC.getSelection()
              .getHtmlFormName());
          request.setAttribute("elementId", selectionPeasSC.getSelection()
              .getHtmlFormElementId());
          request.setAttribute("elementName", selectionPeasSC.getSelection()
              .getHtmlFormElementName());

          if (!selectionPeasSC.isMultiSelect()) {
            UserDetail user = selectionPeasSC.getUserDetail(selectionPeasSC
                .getSelection().getFirstSelectedElement());
            if (user != null) {
              request.setAttribute("user", user);
            }
          } else {
            UserDetail[] users = selectionPeasSC.getOrganizationController()
                .getUserDetails(
                selectionPeasSC.getSelection().getSelectedElements());
            if (users != null && users.length > 0) {
              request.setAttribute("users", users);
            }
          }
        } else if (Selection.TYPE_JDBC_CONNECTOR.equals(selectionPeasSC
            .getSelectionType())) {

        }

        destination = "closeWrapper.jsp";
      } else if (function.equals("Cancel")) {
        request.setAttribute("HostUrl", selectionPeasSC.getCancelURL());
        destination = "goBack.jsp";
      } else if (function.equals("ZoomToElementInfos")) {
        String elementId = request.getParameter("elementId");
        request.setAttribute("infos", selectionPeasSC.getInfos(
            CacheManager.CM_ELEMENT, elementId));
        request.setAttribute("contentText", selectionPeasSC
            .getContentText(CacheManager.CM_ELEMENT));
        request.setAttribute("contentColumns", selectionPeasSC
            .getContentColumns(CacheManager.CM_ELEMENT));
        request.setAttribute("content", selectionPeasSC.getContent(
            CacheManager.CM_ELEMENT, elementId));
        request.setAttribute("action", "ZoomToElementInfos?elementId="
            + elementId);
        destination = "elementView.jsp";
      } else if (function.equals("ZoomToSetInfos")) {
        String setId = request.getParameter("elementId");
        request.setAttribute("infos", selectionPeasSC.getInfos(
            CacheManager.CM_SET, setId));
        request.setAttribute("contentText", selectionPeasSC
            .getContentText(CacheManager.CM_SET));
        request.setAttribute("contentColumns", selectionPeasSC
            .getContentColumns(CacheManager.CM_SET));
        request.setAttribute("content", selectionPeasSC.getContent(
            CacheManager.CM_SET, setId));
        request.setAttribute("action", "ZoomToSetInfos?elementId=" + setId);
        destination = "elementView.jsp";
      } else if (function.equals("BrowseOperation")) {
        destination = doBrowseOperation(request
            .getParameter("SpecificOperation"), selectionPeasSC, request);
      } else if (function.equals("CartDoOperation")) {
        destination = doCartOperation(
            request.getParameter("SpecificOperation"), selectionPeasSC, request);
      } else {
        destination = function;
      }

      if (!destination.startsWith("/")) {
        request.setAttribute("isSetSelectable", new Boolean(selectionPeasSC
            .isSetSelectable()));
        request.setAttribute("isElementSelectable", new Boolean(selectionPeasSC
            .isElementSelectable()));
        request.setAttribute("isMultiSelect", new Boolean(selectionPeasSC
            .isMultiSelect()));
        request.setAttribute("HostSpaceName", selectionPeasSC
            .getHostSpaceName());
        request.setAttribute("HostComponentName", selectionPeasSC
            .getHostComponentName());
        request.setAttribute("HostPath", selectionPeasSC.getHostPath());
        request.setAttribute("ToPopup", new Boolean(selectionPeasSC.isPopup()));

        // Prepare the parameters
        if (destination.equals("selectionPeas.jsp")) {
          request.setAttribute("setsColumnsHeader", selectionPeasSC
              .getColumnsHeader(CacheManager.CM_SET));
          request.setAttribute("elementsColumnsHeader", selectionPeasSC
              .getColumnsHeader(CacheManager.CM_ELEMENT));

          if (Selection.TYPE_USERS_GROUPS.equals(selectionPeasSC
              .getSelectionType())) {
            request.setAttribute("operationsToDisplay", selectionPeasSC
                .getOperations("DisplayBrowse"));
          }

          if (Selection.TYPE_JDBC_CONNECTOR.equals(selectionPeasSC
              .getSelectionType())) {
            request.setAttribute("isDisplaySets", new Boolean(false));
            request.setAttribute("isZoomToSetValid", new Boolean(false));
            request.setAttribute("isZoomToElementValid", new Boolean(true));
            request.setAttribute("externalFunctionName", "updateField");
            SelectionExtraParams selectionParams = selectionPeasSC
                .getSelection().getExtraParams();
            request.setAttribute("referenceColumnsNames", selectionParams
                .getParameter("columnsNames"));
            request.setAttribute("originFormIndex", new Integer(selectionParams
                .getParameter("formIndex")));
            request.setAttribute("originFieldsNames", selectionParams
                .getParameter("fieldsNames"));
          } else {
            request.setAttribute("isDisplaySets", new Boolean(true));
            request.setAttribute("isZoomToSetValid", new Boolean(true));
            request.setAttribute("isZoomToElementValid", new Boolean(false));
            request.setAttribute("originFormIndex", new Integer(0));
            request.setAttribute("originFieldName", "");
          }

          request.setAttribute("setMiniFilterSelect", selectionPeasSC
              .getMiniFilterString(CacheManager.CM_SET));
          request.setAttribute("elementMiniFilterSelect", selectionPeasSC
              .getMiniFilterString(CacheManager.CM_ELEMENT));
          request.setAttribute("setText", selectionPeasSC
              .getText(CacheManager.CM_SET));
          request.setAttribute("elementText", selectionPeasSC
              .getText(CacheManager.CM_ELEMENT));
          request.setAttribute("pageSetNavigation", selectionPeasSC
              .getNavigation(CacheManager.CM_SET));
          request.setAttribute("pageElementNavigation", selectionPeasSC
              .getNavigation(CacheManager.CM_ELEMENT));
          request.setAttribute("setsToDisplay", selectionPeasSC
              .getPage(CacheManager.CM_SET));
          request.setAttribute("elementsToDisplay", selectionPeasSC
              .getPage(CacheManager.CM_ELEMENT));

          request.setAttribute("selectedNumber", selectionPeasSC
              .getSelectedNumber());
          request.setAttribute("SetPath", selectionPeasSC.getSetPath());
        } else if (destination.equals("selectionCart.jsp")) {
          request.setAttribute("setsColumnsHeader", selectionPeasSC
              .getCartColumnsHeader(CacheManager.CM_SET));
          request.setAttribute("elementsColumnsHeader", selectionPeasSC
              .getCartColumnsHeader(CacheManager.CM_ELEMENT));

          request.setAttribute("operationsToDisplay", selectionPeasSC
              .getOperations("DisplayCart"));
          request.setAttribute("setMiniFilterSelect", selectionPeasSC
              .getCartMiniFilterString(CacheManager.CM_SET));
          request.setAttribute("elementMiniFilterSelect", selectionPeasSC
              .getCartMiniFilterString(CacheManager.CM_ELEMENT));
          request.setAttribute("setText", selectionPeasSC
              .getCartText(CacheManager.CM_SET));
          request.setAttribute("elementText", selectionPeasSC
              .getCartText(CacheManager.CM_ELEMENT));
          request.setAttribute("pageSetNavigation", selectionPeasSC
              .getCartNavigation(CacheManager.CM_SET));
          request.setAttribute("pageElementNavigation", selectionPeasSC
              .getCartNavigation(CacheManager.CM_ELEMENT));
          request.setAttribute("setsToDisplay", selectionPeasSC
              .getCartPage(CacheManager.CM_SET));
          request.setAttribute("elementsToDisplay", selectionPeasSC
              .getCartPage(CacheManager.CM_ELEMENT));
          request.setAttribute("isZoomToSetValid", new Boolean(false));
          request.setAttribute("isZoomToElementValid", new Boolean(false));
        }

        destination = "/selectionPeas/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      if (e instanceof SilverpeasTrappedException) {
        destination = "/admin/jsp/errorpageTrapped.jsp";
      } else {
        destination = "/admin/jsp/errorpageMain.jsp";
      }
    }

    SilverTrace.info("selectionPeas", "getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  protected String doCartOperation(String op,
      SelectionPeasSessionController selectionPeasSC, HttpServletRequest request) {
    String destination = "";

    selectionPeasSC.setCartSelected(CacheManager.CM_SET,
        getSetSelected(request));
    selectionPeasSC.setCartSelected(CacheManager.CM_ELEMENT,
        getElementSelected(request));

    SilverTrace.info("selectionPeas", "doCartOperation()",
        "root.MSG_GEN_PARAM_VALUE", "Operation=" + op);
    if ("GENERICPANELPREVIOUSSET".equals(op)) {
      selectionPeasSC.previousCartPage(CacheManager.CM_SET);
      destination = "selectionCart.jsp";
    } else if ("GENERICPANELNEXTSET".equals(op)) {
      selectionPeasSC.nextCartPage(CacheManager.CM_SET);
      destination = "selectionCart.jsp";
    } else if ("GENERICPANELPREVIOUSELEMENT".equals(op)) {
      selectionPeasSC.previousCartPage(CacheManager.CM_ELEMENT);
      destination = "selectionCart.jsp";
    } else if ("GENERICPANELNEXTELEMENT".equals(op)) {
      selectionPeasSC.nextCartPage(CacheManager.CM_ELEMENT);
      destination = "selectionCart.jsp";
    } else if ((op != null) && (op.startsWith("GENERICPANELMINIFILTER"))) {
      selectionPeasSC.setCartMiniFilter(request.getParameter("miniFilter"
          + op.substring("GENERICPANELMINIFILTER".length())), op
          .substring("GENERICPANELMINIFILTER".length()));
      destination = "selectionCart.jsp";
    } else if ("RemoveSelectedFromCart".equals(op)) {
      selectionPeasSC.removeSelectedFromCart();
      destination = getDestination("DisplayCart", selectionPeasSC, request);
    } else if ("RemoveAllFromCart".equals(op)) {
      selectionPeasSC.removeAllFromCart();
      destination = getDestination("DisplayCart", selectionPeasSC, request);
    } else if ((op != null) && (op.length() > 0)) { // Operation
      destination = getDestination(op, selectionPeasSC, request);
    } else // Go...
    {
      destination = getDestination("Validate", selectionPeasSC, request);
    }
    return destination;
  }

  protected String doBrowseOperation(String op,
      SelectionPeasSessionController selectionPeasSC, HttpServletRequest request) {
    String destination = "";

    if (selectionPeasSC.isMultiSelect()) {
      selectionPeasSC.setSelected(CacheManager.CM_SET, getSetSelected(request));
      selectionPeasSC.setSelected(CacheManager.CM_ELEMENT,
          getElementSelected(request));
    }

    SilverTrace.info("selectionPeas", "doBrowseOperation()",
        "root.MSG_GEN_PARAM_VALUE", "Operation=" + op);
    if ("GENERICPANELPREVIOUSSET".equals(op)) {
      selectionPeasSC.previousPage(CacheManager.CM_SET);
      destination = "selectionPeas.jsp";
    } else if ("GENERICPANELNEXTSET".equals(op)) {
      selectionPeasSC.nextPage(CacheManager.CM_SET);
      destination = "selectionPeas.jsp";
    } else if ("GENERICPANELPREVIOUSELEMENT".equals(op)) {
      selectionPeasSC.previousPage(CacheManager.CM_ELEMENT);
      destination = "selectionPeas.jsp";
    } else if ("GENERICPANELNEXTELEMENT".equals(op)) {
      selectionPeasSC.nextPage(CacheManager.CM_ELEMENT);
      destination = "selectionPeas.jsp";
    } else if ("GENERICPANELZOOMTOSET".equals(op)) {
      selectionPeasSC.setParentSet(request.getParameter("setId"));
      selectionPeasSC.setMiniFilter("", "_0_0"); // reset filter
      destination = "selectionPeas.jsp";
    } else if ((op != null) && (op.startsWith("GENERICPANELMINIFILTER"))) {
      selectionPeasSC.setMiniFilter(request.getParameter("miniFilter"
          + op.substring("GENERICPANELMINIFILTER".length())), op
          .substring("GENERICPANELMINIFILTER".length()));
      destination = "selectionPeas.jsp";
    } else if ((op != null) && (op.length() > 0)) { // Operation
      destination = getDestination(op, selectionPeasSC, request);
    } else // Go...
    {
      String setId = request.getParameter("setId");
      String elementId = request.getParameter("elementId");

      if (StringUtil.isDefined(setId) && !"undefined".equals(setId)) {
        selectionPeasSC.setOneSelected(CacheManager.CM_SET, setId);
      }
      if (StringUtil.isDefined(elementId) && !"undefined".equals(elementId)) {
        selectionPeasSC.setOneSelected(CacheManager.CM_ELEMENT, elementId);
      }

      SilverTrace.info("selectionPeas", "doBrowseOperation()",
          "root.MSG_GEN_PARAM_VALUE", "htmlFormName = "
          + selectionPeasSC.getSelection().getHtmlFormName());
      if (selectionPeasSC.getSelection().getHtmlFormName() != null) {
        destination = getDestination("ValidateAndSetOpener", selectionPeasSC,
            request);
      } else
        destination = getDestination("Validate", selectionPeasSC, request);
    }
    return destination;
  }

  protected String[] getFilters(HttpServletRequest request) {
    ArrayList filters = new ArrayList();
    int i = 0;
    String theValue = null;

    theValue = request.getParameter("filter" + Integer.toString(i));
    while (theValue != null) {
      filters.add(theValue);
      i++;
      theValue = request.getParameter("filter" + Integer.toString(i));
    }
    return (String[]) filters.toArray(new String[0]);
  }

  protected Set getElementSelected(HttpServletRequest request) {
    HashSet selected = new HashSet();
    int i = 0;
    String theValue = null;
    int nbMaxDisplayed = Integer.parseInt(request.getParameter("elementNB"));

    for (i = 0; i < nbMaxDisplayed; i++) {
      theValue = request.getParameter("element" + Integer.toString(i));
      if ((theValue != null) && (theValue.length() > 0)) {
        selected.add(theValue);
      }
    }
    return selected;
  }

  protected Set getSetSelected(HttpServletRequest request) {
    HashSet selected = new HashSet();
    int i = 0;
    String theValue = null;
    int nbMaxDisplayed = Integer.parseInt(request.getParameter("setNB"));

    for (i = 0; i < nbMaxDisplayed; i++) {
      theValue = request.getParameter("set" + Integer.toString(i));
      if ((theValue != null) && (theValue.length() > 0)) {
        selected.add(theValue);
      }
    }
    return selected;
  }

  private void initJdbcConnectorSetting(HttpServletRequest request,
      SelectionPeasSessionController selectionPeasSC) throws SecurityException,
      NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
      InvocationTargetException {
    String beanName = request.getParameter("beanName");
    String componentId = request.getParameter("componentId");
    String method = request.getParameter("method");
    String formIndex = request.getParameter("formIndex");
    String tableName = request.getParameter("tableName");

    StringBuffer fieldsNames = new StringBuffer();
    StringBuffer columnsNames = new StringBuffer();
    int i = 0;
    String fieldName = request.getParameter("fieldName" + i);
    String columnName = request.getParameter("columnName" + i);
    while (fieldName != null && fieldName.length() > 0) {
      if (i > 0) {
        fieldsNames.append("#");
        columnsNames.append("#");
      }
      fieldsNames.append(fieldName);
      columnsNames.append(columnName);
      i++;
      fieldName = request.getParameter("fieldName" + i);
      columnName = request.getParameter("columnName" + i);
    }

    ComponentSessionController componentSessionController = (ComponentSessionController) request
        .getSession(true).getAttribute(
        "Silverpeas_" + beanName + "_" + componentId);
    Method m = componentSessionController.getClass().getMethod(method, null);
    JdbcConnectorSetting jdbcSetting = (JdbcConnectorSetting) m.invoke(
        componentSessionController, null);

    selectionPeasSC.updateJdbcParameters(jdbcSetting, tableName, columnsNames
        .toString(), formIndex, fieldsNames.toString());
  }

}
