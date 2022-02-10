<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>

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
LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.form.multilang.formBundle", language);

Button btn = gef.getFormButton(message.getString("GML.validate"), "javascript:setPath()", false);

String targetElementIdHidden = Encode.forHtml(request.getParameter("elementHidden"));
String targetElementIdVisible = Encode.forHtml(request.getParameter("elementVisible"));
String scope = request.getParameter("scope");
String[] componentIds = scope.split(",");
boolean publicationsPicker = StringUtil.getBooleanValue(request.getParameter("publicationsPicker"));
if (publicationsPicker) {
  btn = gef.getFormButton(message.getString("GML.validate"), "javascript:setPublications()", false);
}

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
  <view:includePlugin name="pagination"/>
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

.selection .sp_button {
  margin-left: 10px;
}

.selection .selectionContent {
  text-align: center;
}

#explorer {
	background-color: #FFFFFF;
	padding-top: 10px;
}

#publicationSelection {
  float: left;
  line-height: 19px;
  padding-right: 5px;
}

div.pageNav .pages_indication {
  display: none;
}

ul#publicationsList li {
  list-style-type: none;
  padding-bottom: 5px;
}

ul#publicationsList li.publication .name {
  margin-left: 10px;
}

ul#publicationsList li.publication .description {
  display: none;
}

/*** rightPane ****/

.wrap{
  width: 100%;
  border: 0;
  font-size: 0;
  height:100%
}

.wrap .resizable{
  width:28%;
  overflow:auto;
  padding-right: 2em;
}
.resizable1{
  z-index:10;
  position:relative;
  height:100%;
}
.resizable1 #truc{
  z-index:10;
  position: fixed;
  top:4em;
  bottom: 0;
  overflow:auto;
  width: inherit;
}
.wrap .resizable.resizable2{
  width:70%;
  padding:0;
  display:block;
  position: fixed;
  <% if (availableComponents.size() > 1) { %>
  top:7em;
  <% } else { %>
  top:4em;
  <% } %>
  bottom: 0;
  overflow:auto;
  right:0;
  z-index:5;
}
@media only screen and (max-width: 1100px) {
  .wrap .resizable{
    width:38%;
  }
  .wrap .resizable.resizable2{
    width:60%;
  }

}
@media only screen and (min-width: 1500px) {
  .wrap .resizable{
    width:18%;
  }
  .wrap .resizable.resizable2{
    width:80%;
  }
}

.wrap #rightSide.resizable > div {
  margin:0 2em 1em 2em;
}

.wrap .ui-resizable-e{
  background:#f2f2f2 url("../../util/icons/splitter/layout_sprite_vertical.png") 1px center no-repeat ;
  width: 7px;
  display:block !important;
  cursor: e-resize;
  right:0;
  top: 0;
  bottom: 0;
  z-index: inherit !important;
}

/******** on style la scroll bar pour alléger l'ensemble ********/
/******************** WEBKIT **************/
#explorer::-webkit-scrollbar {
  width: 7px;
  height:7px;
}
#explorer::-webkit-scrollbar-track {
  -webkit-box-shadow:0;
  background-color:#f1f1f1;
}
#explorer::-webkit-scrollbar-thumb {
  background-color: #d6d6d6;
  border-radius:0.5em;
}
#explorer::-webkit-scrollbar-button {
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

  <% if (publicationsPicker) { %>
    initSelectionOfPublications();
  <% } %>
});

function processPubId(checkbox) {
  if (checkbox.checked) {
    if (!selectedPublications.includes(checkbox.value)) {
      selectedPublications.push(checkbox.value);
      selectedPublicationNames.push(checkbox.nextSibling.textContent)
    }
  } else {
    var index = selectedPublications.indexOf(checkbox.value);
    selectedPublications.splice(index, 1);
    selectedPublicationNames.splice(index, 1);
  }
  showSelection();
}

function showSelection() {
  $("#selectionCounter").text(selectedPublications.length);
}

function initSplitter() {
  // init splitter
  $(".resizable1").resizable({
    autoHide: false,
    handles: 'e',
    maxWidth: 500,
    resize: function (e, ui) {
      var parent = ui.element.parent();
      var remainingSpace = parent.width() - ui.element.outerWidth();
      var divTwo = ui.element.next();
      var divTwoWidth = (remainingSpace - (divTwo.outerWidth() - divTwo.width())) / parent.width() * 100 + "%";
      divTwo.width(divTwoWidth);
    },
    stop: function (e, ui) {
      var parent = ui.element.parent();
      ui.element.css({
        width: ui.element.width() / parent.width() * 100 + "%"
      });
    }
  });
}

function isSpecialFolder(id) {
  return id === '1' || id === 'tovalidate' || id === 'notvisibleContributions';
}

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
                if (isSpecialFolder(child.id)) {
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

      <% if (publicationsPicker) { %>
      //show publications of folder
      displayPublications(selectedComponentId, selectedNodeId);
      <% } %>
    }
  });

  <% if (publicationsPicker) { %>
  initSplitter();
  <% } %>

}

var selectedComponentId;
var selectedNodeId;
var selectedPublications = [];
var selectedPublicationNames = [];
var nbPublicationsPerPage = 25;

function setPath() {
	window.opener.document.getElementById("<%=targetElementIdVisible%>").value = $("#explicitPath").val();
	window.opener.document.getElementById("<%=targetElementIdHidden%>").value = selectedComponentId+"-"+selectedNodeId;
	window.close();
}

function setPublications() {
  window.opener.document.getElementById("<%=targetElementIdVisible%>").value = selectedPublicationNames.join("\n");
  window.opener.document.getElementById("<%=targetElementIdHidden%>").value = selectedPublications.join(",");
  window.close();
}

function initSelectionOfPublications() {
  var rawSelection = window.opener.document.getElementById("<%=targetElementIdHidden%>").value;
  var publicationNames = window.opener.document.getElementById("<%=targetElementIdVisible%>").value;
  if (rawSelection !== "") {
    selectedPublications = rawSelection.split(",");
    selectedPublicationNames = publicationNames.split("\n");
  }
  showSelection();
}

function displayPublications(componentId, nodeId) {
  var url = webContext+"/services/private/publications/"+componentId+"?node="+nodeId+"&withAttachments=false";
  $.getJSON(url,
      function(data) {
        try {
          var items = "";
          $("#publications #publicationsList").empty();
          var nbPublis = 0;
          for (var i = 0; data != null && i < data.length; i++) {
            var li = displayPublication(data[i]);
            if (li.length > 0) {
              $("#publications #publicationsList").append(li);
              nbPublis++;
            }
          }
          // display pagination if needed
          if (nbPublis > nbPublicationsPerPage) {
            $('#pagination').show();
            $('#pagination').smartpaginator({
              totalrecords: parseInt(data.length),
              recordsperpage: nbPublicationsPerPage,
              datacontainer: 'publicationsList',
              dataelement: 'li.publication',
              theme: 'pageNav' });
          } else {
            $('#pagination').hide();
          }
          // display message about empty folder
          if (nbPublis === 0) {
            $("#empty-folder").show();
          } else {
            $("#empty-folder").hide();
          }
        } catch (e) {
          //do nothing
          alert(e);
        }
      }, 'json');
}

function displayPublication(publication) {
  var checkboxValue = publication.componentId+":"+"publication"+":"+publication.id;
  var checkboxChecked = "";
  if (selectedPublications.includes(checkboxValue)) {
    checkboxChecked = "checked=\"checked\"";
  }
  var li = "<li class=\"publication\">";
  li += "<input type=\"checkbox\" "+checkboxChecked+" onclick='processPubId(this)' value=\""+checkboxValue+"\"/>";
  li += "<span class=\"name\">"+publication.name+"</span>";
  li += "<p class=\"description\">"+publication.description.replace("\n", "<br/>")+"</p>";
  li += "</li>";
  return li;
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

<% if (!publicationsPicker) { %>
<div class="selection"><%=message.getString("GML.selection") %> : <input type="text" id="explicitPath" size="80"/> <%=btn.print() %></div>
<div id="explorer" class="demo"></div>
<% } else { %>

<div class="selection"><div class="selectionContent"><span id="selectionCounter"></span> <%=message.getString("field.explorer.selection")%><%=btn.print() %></div></div>

<div class="wrap">
  <div class="resizable resizable1">
    <div id="explorer" class="demo"></div>
  </div>
  <div id="rightSide" class="resizable resizable2">
    <div id="publications">
      <div class="container">
        <ul id="publicationsList"></ul>
        <span id="empty-folder" class="inlineMessage"><%=message.getString("field.explorer.folder.empty")%></span>
        <div id="pagination"></div>
      </div>
    </div>
  </div>
</div>

<% } %>

</body>
</html>
