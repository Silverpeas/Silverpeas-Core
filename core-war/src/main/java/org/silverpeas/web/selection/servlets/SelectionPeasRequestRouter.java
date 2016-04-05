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

package org.silverpeas.web.selection.servlets;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionExtraParams;
import org.silverpeas.core.web.selection.CacheType;
import org.silverpeas.web.selection.control.SelectionPeasSessionController;
import org.silverpeas.core.web.selection.jdbc.JdbcConnectorSetting;
import org.silverpeas.core.admin.user.model.UserDetail;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Class declaration
 * @author
 */
public class SelectionPeasRequestRouter extends
    ComponentRequestRouter<SelectionPeasSessionController> {

  private static final long serialVersionUID = -1531692630305784345L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public SelectionPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SelectionPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return "selectionPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param selectionPeasSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, SelectionPeasSessionController selectionPeasSC,
      HttpRequest request) {
    String destination;


    try {
      if (function.equals("Main")) {
        selectionPeasSC.initSC(request.getParameter("SelectionType"));
        if (Selection.TYPE_JDBC_CONNECTOR.equals(selectionPeasSC.getSelectionType())) {
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

        if (Selection.TYPE_USERS_GROUPS.equals(selectionPeasSC.getSelectionType())) {
          request.setAttribute("formName", selectionPeasSC.getSelection().getHtmlFormName());
          request.setAttribute("elementId", selectionPeasSC.getSelection().getHtmlFormElementId());
          request.setAttribute("elementName",
              selectionPeasSC.getSelection().getHtmlFormElementName());

          if (!selectionPeasSC.isMultiSelect()) {
            UserDetail user = selectionPeasSC.getUserDetail(selectionPeasSC.getSelection().
                getFirstSelectedElement());
            if (user != null) {
              request.setAttribute("user", user);
            }
          } else {
            UserDetail[] users = selectionPeasSC.getOrganisationController().getUserDetails(
                selectionPeasSC.
                getSelection().getSelectedElements());
            if (users != null && users.length > 0) {
              request.setAttribute("users", users);
            }
          }
        }
        destination = "closeWrapper.jsp";
      } else if (function.equals("Cancel")) {
        request.setAttribute("HostUrl", selectionPeasSC.getCancelURL());
        destination = "goBack.jsp";
      } else if (function.equals("ZoomToElementInfos")) {
        String elementId = request.getParameter("elementId");
        request.setAttribute("infos", selectionPeasSC.getInfos(CacheType.CM_ELEMENT, elementId));
        request.setAttribute("contentText",
            selectionPeasSC.getContentText(CacheType.CM_ELEMENT));
        request.setAttribute("contentColumns", selectionPeasSC.getContentColumns(
            CacheType.CM_ELEMENT));
        request.setAttribute("content", selectionPeasSC.getContent(
            CacheType.CM_ELEMENT, elementId));
        request.setAttribute("action", "ZoomToElementInfos?elementId="
            + elementId);
        destination = "elementView.jsp";
      } else if (function.equals("ZoomToSetInfos")) {
        String setId = request.getParameter("elementId");
        request.setAttribute("infos", selectionPeasSC.getInfos(
            CacheType.CM_SET, setId));
        request.setAttribute("contentText", selectionPeasSC.getContentText(CacheType.CM_SET));
        request.setAttribute("contentColumns",
            selectionPeasSC.getContentColumns(CacheType.CM_SET));
        request.setAttribute("content", selectionPeasSC.getContent(
            CacheType.CM_SET, setId));
        request.setAttribute("action", "ZoomToSetInfos?elementId=" + setId);
        destination = "elementView.jsp";
      } else if (function.equals("BrowseOperation")) {
        destination = doBrowseOperation(request.getParameter("SpecificOperation"), selectionPeasSC,
            request);
      } else if (function.equals("CartDoOperation")) {
        destination = doCartOperation(
            request.getParameter("SpecificOperation"), selectionPeasSC, request);
      } else {
        destination = function;
      }

      if (!destination.startsWith("/")) {
        request.setAttribute("isSetSelectable", selectionPeasSC.isSetSelectable());
        request.setAttribute("isElementSelectable", selectionPeasSC.
            isElementSelectable());
        request.setAttribute("isMultiSelect", selectionPeasSC.isMultiSelect());
        request.setAttribute("HostSpaceName", selectionPeasSC.getHostSpaceName());
        request.setAttribute("HostComponentName", selectionPeasSC.getHostComponentName());
        request.setAttribute("HostPath", selectionPeasSC.getHostPath());
        request.setAttribute("ToPopup", selectionPeasSC.isPopup());

        // Prepare the parameters
        if (destination.equals("selectionPeas.jsp")) {
          request.setAttribute("setsColumnsHeader", selectionPeasSC.getColumnsHeader(
              CacheType.CM_SET));
          request.setAttribute("elementsColumnsHeader", selectionPeasSC.getColumnsHeader(
              CacheType.CM_ELEMENT));

          if (Selection.TYPE_USERS_GROUPS.equals(selectionPeasSC.getSelectionType())) {
            request.setAttribute("operationsToDisplay", selectionPeasSC.getOperations(
                "DisplayBrowse"));
          }

          if (Selection.TYPE_JDBC_CONNECTOR.equals(selectionPeasSC.getSelectionType())) {
            request.setAttribute("isDisplaySets", Boolean.FALSE);
            request.setAttribute("isZoomToSetValid", Boolean.FALSE);
            request.setAttribute("isZoomToElementValid", Boolean.TRUE);
            request.setAttribute("externalFunctionName", "updateField");
            SelectionExtraParams selectionParams = selectionPeasSC.getSelection().getExtraParams();
            request.setAttribute("referenceColumnsNames", selectionParams.getParameter(
                "columnsNames"));
            request.setAttribute("originFormIndex", Integer.valueOf(selectionParams.getParameter(
                "formIndex")));
            request.setAttribute("originFieldsNames", selectionParams.getParameter("fieldsNames"));
          } else {
            request.setAttribute("isDisplaySets", Boolean.TRUE);
            request.setAttribute("isZoomToSetValid", Boolean.TRUE);
            request.setAttribute("isZoomToElementValid", Boolean.FALSE);
            request.setAttribute("originFormIndex", 0);
            request.setAttribute("originFieldName", "");
          }

          request.setAttribute("setMiniFilterSelect", selectionPeasSC.getMiniFilterString(
              CacheType.CM_SET));
          request.setAttribute("elementMiniFilterSelect", selectionPeasSC.getMiniFilterString(
              CacheType.CM_ELEMENT));
          request.setAttribute("setText", selectionPeasSC.getText(CacheType.CM_SET));
          request.setAttribute("elementText", selectionPeasSC.getText(CacheType.CM_ELEMENT));
          request.setAttribute("pageSetNavigation", selectionPeasSC.getNavigation(
              CacheType.CM_SET));
          request.setAttribute("pageElementNavigation", selectionPeasSC.getNavigation(
              CacheType.CM_ELEMENT));
          request.setAttribute("setsToDisplay", selectionPeasSC.getPage(CacheType.CM_SET));
          request.setAttribute("elementsToDisplay",
              selectionPeasSC.getPage(CacheType.CM_ELEMENT));

          request.setAttribute("selectedNumber", selectionPeasSC.getSelectedNumber());
          request.setAttribute("SetPath", selectionPeasSC.getSetPath());
        } else if (destination.equals("selectionCart.jsp")) {
          request.setAttribute("setsColumnsHeader", selectionPeasSC.getCartColumnsHeader(
              CacheType.CM_SET));
          request.setAttribute("elementsColumnsHeader", selectionPeasSC.getCartColumnsHeader(
              CacheType.CM_ELEMENT));

          request.setAttribute("operationsToDisplay", selectionPeasSC.getOperations("DisplayCart"));
          request.setAttribute("setMiniFilterSelect", selectionPeasSC.getCartMiniFilterString(
              CacheType.CM_SET));
          request.setAttribute("elementMiniFilterSelect", selectionPeasSC.getCartMiniFilterString(
              CacheType.CM_ELEMENT));
          request.setAttribute("setText", selectionPeasSC.getCartText(CacheType.CM_SET));
          request.setAttribute("elementText", selectionPeasSC.getCartText(CacheType.CM_ELEMENT));
          request.setAttribute("pageSetNavigation", selectionPeasSC.getCartNavigation(
              CacheType.CM_SET));
          request.setAttribute("pageElementNavigation", selectionPeasSC.getCartNavigation(
              CacheType.CM_ELEMENT));
          request.setAttribute("setsToDisplay", selectionPeasSC.getCartPage(CacheType.CM_SET));
          request.setAttribute("elementsToDisplay", selectionPeasSC.getCartPage(
              CacheType.CM_ELEMENT));
          request.setAttribute("isZoomToSetValid", Boolean.FALSE);
          request.setAttribute("isZoomToElementValid", Boolean.FALSE);
        }

        destination = "/selectionPeas/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }

  protected String doCartOperation(String op, SelectionPeasSessionController selectionPeasSC,
      HttpRequest request) {
    selectionPeasSC.setCartSelected(CacheType.CM_SET, getValues(request.getParameter(
        "SelectedSets")), getValues(request.getParameter("NonSelectedSets")));
    selectionPeasSC.setCartSelected(CacheType.CM_ELEMENT, getValues(request.getParameter(
        "SelectedElements")), getValues(request.getParameter("NonSelectedElements")));

    if ("GENERICPANELChangePage".equals(op)) {
      return "selectionCart.jsp";
    }
    if (op != null && op.startsWith("GENERICPANELMINIFILTER")) {
      selectionPeasSC.setCartMiniFilter(request.getParameter("miniFilter"
          + op.substring("GENERICPANELMINIFILTER".length())), op.substring("GENERICPANELMINIFILTER"
          .
          length()));
      return "selectionCart.jsp";
    }
    if ("RemoveSelectedFromCart".equals(op)) {
      selectionPeasSC.removeSelectedFromCart();
      return getDestination("Validate", selectionPeasSC, request);
    }
    if ("RemoveAllFromCart".equals(op)) {
      selectionPeasSC.removeAllFromCart();
      return getDestination("Validate", selectionPeasSC, request);
    }
    if (StringUtil.isDefined(op)) {
      return getDestination(op, selectionPeasSC, request);
    }
    return getDestination("Validate", selectionPeasSC, request);
  }

  protected String doBrowseOperation(String op, SelectionPeasSessionController selectionPeasSC,
      HttpRequest request) {
    if (selectionPeasSC.isMultiSelect()) {
      selectionPeasSC.setSelected(CacheType.CM_SET,
          getValues(request.getParameter("SelectedSets")),
          getValues(request.getParameter("NonSelectedSets")));
      selectionPeasSC.setSelected(CacheType.CM_ELEMENT, getValues(request.getParameter(
          "SelectedElements")), getValues(request.getParameter("NonSelectedElements")));
    }


    if ("GENERICPANELChangePage".equals(op)) {
      return "selectionPeas.jsp";
    }
    if ("GENERICPANELZOOMTOSET".equals(op)) {
      selectionPeasSC.setParentSet(request.getParameter("setId"));
      selectionPeasSC.setMiniFilter("", "_0_0"); // reset filter
      return "selectionPeas.jsp";
    }
    if ((op != null) && (op.startsWith("GENERICPANELMINIFILTER"))) {
      selectionPeasSC.setMiniFilter(request.getParameter("miniFilter" + op.substring(
          "GENERICPANELMINIFILTER".length())), op.substring("GENERICPANELMINIFILTER".length()));
      return "selectionPeas.jsp";
    }
    if (StringUtil.isDefined(op)) { // Operation
      return getDestination(op, selectionPeasSC, request);
    }
    String setId = request.getParameter("setId");
    String elementId = request.getParameter("elementId");

    if (StringUtil.isDefined(setId) && !"undefined".equals(setId)) {
      selectionPeasSC.setOneSelected(CacheType.CM_SET, setId);
    }
    if (StringUtil.isDefined(elementId) && !"undefined".equals(elementId)) {
      selectionPeasSC.setOneSelected(CacheType.CM_ELEMENT, elementId);
    }


    if (selectionPeasSC.getSelection().getHtmlFormName() != null) {
      return getDestination("ValidateAndSetOpener", selectionPeasSC, request);
    }
    return getDestination("Validate", selectionPeasSC, request);
  }

  protected String[] getFilters(HttpServletRequest request) {
    ArrayList<String> filters = new ArrayList<String>();
    int i = 0;
    String theValue = request.getParameter("filter" + Integer.toString(i));
    while (theValue != null) {
      filters.add(theValue);
      i++;
      theValue = request.getParameter("filter" + Integer.toString(i));
    }
    return filters.toArray(new String[filters.size()]);
  }

  protected Set<String> getValues(String param) {
    HashSet<String> selected = new HashSet<String>();
    if (param != null) {
      StringTokenizer tokenizer = new StringTokenizer(param, ",");
      while (tokenizer.hasMoreTokens()) {
        String theValue = tokenizer.nextToken();
        if (StringUtil.isDefined(theValue)) {
          selected.add(theValue);
        }
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

    StringBuilder fieldsNames = new StringBuilder();
    StringBuilder columnsNames = new StringBuilder();
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

    ComponentSessionController componentSessionController = (ComponentSessionController) request.
        getSession(true).getAttribute("Silverpeas_" + beanName + "_" + componentId);
    Method m = componentSessionController.getClass().getMethod(method, ArrayUtil.EMPTY_CLASS_ARRAY);
    JdbcConnectorSetting jdbcSetting = (JdbcConnectorSetting) m.invoke(componentSessionController,
         ArrayUtil.EMPTY_CLASS_ARRAY);

    selectionPeasSC.updateJdbcParameters(jdbcSetting, tableName, columnsNames.toString(),
        formIndex,
        fieldsNames.toString());
  }
}
