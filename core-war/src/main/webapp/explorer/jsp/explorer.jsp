<%--

    Copyright (C) 2000 - 2019 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>


<%@ page import="org.silverpeas.core.util.ResourceLocator "%>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>
<%@ page import="org.silverpeas.core.admin.component.model.ComponentInstLight" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
if (mainSessionCtrl == null) {
  String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}
String language = mainSessionCtrl.getFavoriteLanguage();
LocalizationBundle message = ResourceLocator.getGeneralLocalizationBundle(language);

Button btn = gef.getFormButton(message.getString("GML.validate"), "javascript:setPath()", false);

String targetElementIdHidden = request.getParameter("elementHidden");
String targetElementIdVisible = request.getParameter("elementVisible");
String scope = request.getParameter("scope");
String[] componentIds = scope.split(",");

// retain only available instances for current user
List<ComponentInstLight> availableComponents = new ArrayList<>();
for (String componentId : componentIds) {
  if (OrganizationController.get().isComponentAvailableToUser(componentId, mainSessionCtrl.getUserId())) {
    availableComponents.add(OrganizationController.get().getComponentInstLight(componentId));
  }
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<style type="text/css">
.selection input {
	display:inline-table;
	display:inline\9;
	vertical-align:middle
}

.selection {
	padding: 10px 5px;
	border-bottom: 1px solid #EAEAEA;
}

.selection a {
  padding: 1px 10px;
  font-size: 9pt !important;
  height: 17px;
  line-height: 17px;
}

#explorer {
	background-color: #FFFFFF;
	padding-top: 10px;
}
</style>
  <view:script src="/util/javaScript/jquery/jstree.min.js"/>
  <view:link href="/util/javaScript/jquery/themes/default/style.min.css"/>
  <view:link href="/util/javaScript/jquery/themes/default/explorer.css"/>
<script type="text/javascript">
$(function () {
  <% if (!availableComponents.isEmpty()) { %>
    var firstComponentId = "<%=availableComponents.get(0).getId()%>";
    initTree(firstComponentId);
  <% } %>

  $("select").change(function() {
    initTree($(this).val());
  });
});

function initTree(instanceId) {
  $("#explorer").jstree('destroy');
  var rootId = "0";
  $("#explorer").jstree({
    "core" : {
      force_text : false,
      "data" : {
        "url" : function(node) {
          var url = webContext + "/services/folders/" + instanceId + "/" + rootId;
          if (node && node.id !== '#') {
            url = webContext + "/services/folders/" + node.original.attr['componentId'] + "/" + node.id;
          }
          return url;
        }, "success" : function(newData) {
          if (newData) {
            if (newData.children) {
              for (var i = 0; i < newData.children.length; i++) {
                var child = newData.children[i];
                if (child.id === '1' || child.id === 'tovalidate') {
                  child.state.hidden = true;
                } else {
                  child.children = true;
                }
              }
            }
          }
          return newData;
        }
      },
      "check_callback" : false,
      "themes" : {
        "dots" : false,
        "icons" : true
      },
      "multiple" : false
    }
  });

  $("#explorer").on("select_node.jstree", function(e, data) {
    if (data.node.type !== 'user' && data.node.type !== 'user-root') {
      var path = data.instance.get_path(data.node.id, false, false);
      var newPath = "";
      for (i = 0; i < path.length; i++) {
        if (i != 0) {
          newPath += " > ";
        }
        newPath += path[i];
      }
      $("#explicitPath").val(newPath);
      selectedComponentId = data.node.original.attr["componentId"];
      selectedNodeId = data.node.id;

      //open folder in treeview
      data.instance.open_node(data.node);
    }
  });
}

var selectedComponentId;
var selectedNodeId;

function setPath() {
	window.opener.document.getElementById("<%=targetElementIdVisible%>").value = $("#explicitPath").val();
	window.opener.document.getElementById("<%=targetElementIdHidden%>").value = selectedComponentId+"-"+selectedNodeId;
	window.close();
}
</script>
</head>
<body>
<% if (availableComponents.size() > 1) {%>
<div class="selection"><%=message.getString("GML.component")%> :
  <select>
    <% for (ComponentInstLight availableComponent : availableComponents) { %>
    <option value="<%=availableComponent.getId()%>"><%=availableComponent.getLabel(language)%></option>
    <% } %>
  </select>
</div>
<% } %>
<div class="selection"><%=message.getString("GML.selection") %> : <input type="text" id="explicitPath" size="80"/> <%=btn.print() %></div>
<div id="explorer" class="demo" style="height:100px;"/>
</body>
</html>