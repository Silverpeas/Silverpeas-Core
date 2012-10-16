<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.silverpeas.util.StringUtil"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
if (mainSessionCtrl == null) {
  String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}
ResourceLocator message = GeneralPropertiesManager.getGeneralMultilang(mainSessionCtrl.getFavoriteLanguage());
String m_context = URLManager.getApplicationURL();

Button btn = gef.getFormButton(message.getString("GML.validate"), "javascript:setPath()", false);

String targetElementIdHidden = request.getParameter("elementHidden");
String targetElementIdVisible = request.getParameter("elementVisible");
String scope = request.getParameter("scope");
%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<% out.println(gef.getLookStyleSheet()); %>
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
<script type="text/javascript" src="<%=m_context %>/util/javaScript/jquery/jquery.jstree.js"></script>
<script type="text/javascript">
$(function () {
	// TO CREATE AN INSTANCE
	// select the tree container using jQuery
	$("#explorer")
		// call `.jstree` with the options object
		.jstree({
			"core" : {
				"load_open" : true
			},
 			"json_data" : { 
				"ajax" : {
	                "type": 'GET',
	                "url": function (node) {
	                    var nodeId = "";
	                    var url = "<%=URLManager.getFullApplicationURL(request)%>/Explorer/scope/<%=scope%>";
	                    if (node != -1) {
	                    	url = "<%=URLManager.getFullApplicationURL(request)%>/Explorer";
	                        nodeId = node.attr('id');
	                        url += "/componentid/"+node.attr('instanceId')+"/id/" + nodeId;
	                    }
	                    return url;
	                },
	                "success": function (new_data) {
	                    return new_data;
	                }
	            }
			},
			"types" : {
				"valid_children" : [ "default" ],
				types : {
					"user" : {
						"max_children"	: -1,
						"max_depth"		: -1,
						"valid_children": "all",
						
						"icon" : {
							"image" : "<%=URLManager.getFullApplicationURL(request)%>/util/javaScript/jquery/themes/default/d.png",
							"position" : "-56px -37px"
						},

						// Bound functions - you can bind any other function here (using boolean or function)
						"hover_node" 	: false,
						"select_node"	: false
					},
					"user-root" : {
						"max_children"	: -1,
						"max_depth"		: -1,
						"valid_children": "all",
						
						"icon" : {
							"image" : "<%=URLManager.getFullApplicationURL(request)%>/util/icons/folder.gif"
						},

						// Bound functions - you can bind any other function here (using boolean or function)
						"hover_node" 	: false,
						"select_node"	: false
					},
					"admin-root" : {
						"icon" : {
							"image" : "<%=URLManager.getFullApplicationURL(request)%>/util/icons/folder.gif"
						}
					},
					"publisher-root" : {
						"icon" : {
							"image" : "<%=URLManager.getFullApplicationURL(request)%>/util/icons/folder.gif"
						}
					},
					"writer-root" : {
						"icon" : {
							"image" : "<%=URLManager.getFullApplicationURL(request)%>/util/icons/folder.gif"
						}
					}
				}
			},
			"themes" : {
				"theme" : "default",
				"dots" : false,
				"icons" : true
			},
			// the `plugins` array allows you to configure the active plugins on this instance
			"plugins" : ["themes","json_data","ui","types"]
		});
	$("#explorer").bind("select_node.jstree", function (e, data) {
		// data.inst is the instance which triggered this event
		var path = data.inst.get_path(data.rslt.obj, false);
		var newPath = "";
		for (i = 0; i<path.length; i++) {
			if (i != 0) {
				newPath += " > ";
			}
			newPath += path[i];
		}
		$("#explicitPath").val(newPath);
		$("#nodeId").val(data.rslt.obj.attr("instanceId")+"-"+data.rslt.obj.attr("id"));		
	});
});

function setPath() {
	window.opener.document.getElementById("<%=targetElementIdVisible%>").value = $("#explicitPath").val();
	window.opener.document.getElementById("<%=targetElementIdHidden%>").value = $("#nodeId").val();
	window.close();
}
</script>
</head>
<body>
<div class="selection"><%=message.getString("GML.selection") %> : <input type="text" id="explicitPath" size="40"/> <%=btn.print() %></div>
<div id="explorer" class="demo" style="height:100px;">
</div>
<input type="hidden" id="nodeId"/>
</body>
</html>