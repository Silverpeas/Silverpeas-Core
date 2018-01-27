<%--

    Copyright (C) 2000 - 2018 Silverpeas

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
<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
if (mainSessionCtrl == null) {
  String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}
LocalizationBundle message = ResourceLocator.getGeneralLocalizationBundle(mainSessionCtrl.getFavoriteLanguage());
String m_context = URLUtil.getApplicationURL();

Button btn = gef.getFormButton(message.getString("GML.validate"), "javascript:setPath()", false);

String targetElementIdHidden = request.getParameter("elementHidden");
String targetElementIdVisible = request.getParameter("elementVisible");
String scope = request.getParameter("scope");
String resultType = request.getParameter("resultType");
if (!StringUtil.isDefined(resultType)) {
  resultType = "default";
}
boolean dedicatedToWriters = StringUtil.getBooleanValue(request.getParameter("DedicatedToWriters"));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<style type="text/css">
.selection table,
.selection input {
	display:inline-table;
	display:inline\9;
	vertical-align:middle
}

.selection table {
	margin-top: 4px;
}

.selection {
	padding: 10px 5px;
	border-bottom: 1px solid #EAEAEA;
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
  // TO CREATE AN INSTANCE
  // select the tree container using jQuery
  $("#explorer").jstree({
    "core" : {
      force_text : false,
      "data" : {
        "url" : function(node) {
          var nodeId = "";
          var url = "<%=URLUtil.getFullApplicationURL(request)%>/Explorer/scope/<%=scope%>";
          if (node && node.id !== '#') {
            url = "<%=URLUtil.getFullApplicationURL(request)%>/Explorer";
            nodeId = node.id;
            url += "/componentid/" + node.original.attr['instanceId'] + "/id/" + nodeId;
          }
          return url;
        }, "success" : function(new_data) {
          new_data.children = true;
          return new_data;
        }
      },
      "check_callback" : false,
      "themes" : {
        "dots" : false,
        "icons" : true
      },
      "multiple" : false
    },
    "types" : {
      "default" : {
        "valid_children" : ["default"]
      },
      <% if (dedicatedToWriters) { %>
      "user" : {
        "max_children" : -1,
        "max_depth" : -1,
        "icon" : "silverpeas-locked"
      }, <% } %>
      "user-root" : {
        "max_children" : -1,
        "max_depth" : -1,
        "icon" : "<%=URLUtil.getFullApplicationURL(request)%>/util/icons/folder.gif"
      },
      "admin-root" : {
        "icon" : "<%=URLUtil.getFullApplicationURL(request)%>/util/icons/folder.gif"
      },
      "publisher-root" : {
        "icon" : "<%=URLUtil.getFullApplicationURL(request)%>/util/icons/folder.gif"
      },
      "writer-root" : {
        "icon" : "<%=URLUtil.getFullApplicationURL(request)%>/util/icons/folder.gif"
      }
    },
    // the `plugins` array allows you to configure the active plugins on this instance
    "plugins" : ["types"]
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
      <% if ("path".equals(resultType)) { %>
      $("#result").val(data.node.original.attr["path"]);
      <% } else { %>
      $("#result").val(data.node.original.attr["instanceId"] + "-" + data.node.id);
      <% } %>
    }
  });
});

function setPath() {
	window.opener.document.getElementById("<%=targetElementIdVisible%>").value = $("#explicitPath").val();
	window.opener.document.getElementById("<%=targetElementIdHidden%>").value = $("#result").val();
	window.close();
}
</script>
</head>
<body>
<div class="selection"><%=message.getString("GML.selection") %> : <input type="text" id="explicitPath" size="40"/> <%=btn.print() %></div>
<div id="explorer" class="demo" style="height:100px;">
</div>
<input type="hidden" id="result"/>
</body>
</html>