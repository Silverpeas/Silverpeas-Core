<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle basename="org.silverpeas.mylinks.multilang.myLinksBundle" var="profile"/>

<%@ include file="check.jsp"%>
<%
  Collection<LinkDetail> links = (Collection<LinkDetail>) request.getAttribute("Links");
  String url = (String) request.getAttribute("UrlReturn");
  String instanceId = (String) request.getAttribute("InstanceId");
  boolean appScope = StringUtil.isDefined(instanceId);

  String action = "CreateLink";
  
  String reloadPageURL = "/RmyLinksPeas/jsp/Main";
  if (appScope) {
    reloadPageURL = "/RmyLinksPeas/jsp/ComponentLinks?InstanceId="+instanceId;
  }
%>
<%@page import="com.silverpeas.util.StringUtil"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<view:includePlugin name="popup"/>
<link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
<script type="text/javascript">
$.i18n.properties({
  name: 'myLinksBundle',
  path: webContext + '/services/bundles/org/silverpeas/mylinks/multilang/',
  language: getUserLanguage(),
  mode: 'map'
});

function addLink() {
  cleanMyLinkForm()
  $("#linkFormId").attr('action', 'CreateLink');
  createLinkPopup();
}

function editLink(id) {
  cleanMyLinkForm()
  $("#linkFormId").attr('action', 'UpdateLink');

  var ajaxUrl = webContext + '/services/mylinks/' + id;
  jQuery.ajax({
    url: ajaxUrl,
    type: 'GET',
    contentType: "application/json",
    cache: false,
    dataType: "json",
    async: true,
    success: function(result) {
      if ( window.console && window.console.log ) {
        console.log("Update mylink identifier = #" + result.linkId);
      }
      $("#hiddenLinkId").val(result.linkId);
      $("#urlId").val(result.url);
      $("#nameId").val(result.name);
      $("#descriptionId").val(result.description);
      $("#visibleId").prop('checked', result.visible);
      $("#popupId").prop('checked', result.popup);
    }
  });
  updateLinkPopup();
}

function cleanMyLinkForm() {
  $("#linkFormId").attr('action', '');
  $("#hiddenLinkId").val("");
  $("#urlId").val("");
  $("#nameId").val("");
  $("#descriptionId").val("");
  $("#visibleId").prop('checked', false);
  $("#popupId").prop('checked', false);
}

function deleteSelectLinksConfirm() {
	if (confirm('<fmt:message key="myLinks.deleteSelection"/>')) {
    	document.readForm.mode.value = 'delete';
    	document.readForm.submit();
  	}
}

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;
  var url = $("#urlId").val().trim();
  var name = $("#nameId").val().trim();

  if (url == "") {
    errorMsg+="  - '<fmt:message key="myLinks.url"/>'  <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }
  if (name == "") {
    errorMsg+="  - '<fmt:message key="GML.nom"/>'  <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }

  switch(errorNb) {
      case 0 :
          result = true;
          break;
      case 1 :
          errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
      default :
          errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
  }
  return result;
}

function createLinkPopup() {
  $('#mylink-popup-content').popup('validation', {
      title : "<fmt:message key="myLinks.addLink" />",
      width : "700px",
      isMaxWidth: false,
    callback : function() {
      if ( window.console && window.console.log ) {
        console.log("User create new link !!!");
      }
      if (isCorrectForm()) {
        createLink();
        return true;
      } else {
        return false;
      }
    }
  });
}

function updateLinkPopup() {
  $('#mylink-popup-content').popup('validation', {
      title : "<fmt:message key="myLinks.updateLink" />",
      width : "700px",
      isMaxWidth: false,
    callback : function() {
      if ( window.console && window.console.log ) {
        console.log("User update the following link identifier = " + $("#linkId").attr('value'));
      }
      if (isCorrectForm()) {
        updateLink();
        return true;
      } else {
        return false;
      }
    }
  });
}

function updateLink() {
  var ajaxUrl = webContext + '/services/mylinks/' + $("#hiddenLinkId").val();

  var cleanUrl = $("#urlId").val().replace(webContext, '');
  var updatedLink = {
      "description": $("#descriptionId").val(),
      "linkId": $("#hiddenLinkId").val(),
      "name": $("#nameId").val(),
      "url": cleanUrl,
      "popup": $("#popupId").is(":checked"),
      "uri": '',
      "visible": isVisible(),
      "instanceId": getAppId()
  };
  jQuery.ajax({
    url: ajaxUrl,
    type: 'PUT',
    data: $.toJSON(updatedLink),
    contentType: "application/json",
    cache: false,
    dataType: "json",
    async: true,
    success: function(result) {
      notySuccess(getString('myLinks.updateLink.messageConfirm'));
      reloadPage();
    }
  });
}

function isVisible() {
  if ($("#visibleId")) {
    return $("#visibleId").is(":checked");
  }
  return false;
}

function getAppId() {
  <% if (appScope) { %>
  	return "<%=instanceId%>";
  <% } else { %>
    return "";
  <% } %>
}

function createLink() {
  var ajaxUrl = webContext + '/services/mylinks/';

  var cleanUrl = $("#urlId").val().replace(webContext, '');
  var newLink = {
      "linkId": '',
      "description": $("#descriptionId").val(),
      "name": $("#nameId").val(),
      "url": cleanUrl,
      "popup": $("#popupId").is(":checked"),
      "uri": '',
      "visible": isVisible(),
      "instanceId": getAppId()
  };
  jQuery.ajax({
    url: ajaxUrl,
    type: 'POST',
    data: $.toJSON(newLink),
    contentType: "application/json",
    cache: false,
    dataType: "json",
    async: true,
    success: function(result) {
      notySuccess(getString('myLinks.messageConfirm'));
      reloadPage();
    }
  });
}

function reloadPage() {
  setTimeout(function(){ location.href= webContext + '<%=reloadPageURL%>'; }, 1000);
}
</script>
</head>
<body>
<%
   String bBar = resource.getString("myLinks.links");
   if (appScope) {
     bBar = resource.getString("myLinks.linksByComponent");
   }
   browseBar.setComponentName(bBar);

   operationPane.addOperationOfCreation(resource.getIcon("myLinks.addLink"), resource.getString("myLinks.addLink"), "javaScript:addLink()");

   operationPane.addOperation(resource.getIcon("myLinks.deleteLinks"), resource.getString("myLinks.deleteLinks"), "javaScript:deleteSelectLinksConfirm()");

   ButtonPane buttonPane = gef.getButtonPane();
   Button returnButton = gef.getFormButton(resource.getString("myLinks.retour"), url, false);

   out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<form name="readForm" action="DeleteLinks" method="post">
  <input type="hidden" name="mode"/>
<%
   ArrayPane arrayPane = gef.getArrayPane("linkList", "ViewLinks", request, session);
   arrayPane.setSortableLines(true);
   arrayPane.addArrayColumn(resource.getString("GML.nom"));
   arrayPane.addArrayColumn(resource.getString("GML.description"));
   ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("GML.operations"));
   columnOp.setSortable(false);

   // Fill ArrayPane with links
   for (LinkDetail link : links) {
     ArrayLine line = arrayPane.addArrayLine();
     int linkId = link.getLinkId();
     String lien = link.getUrl();
     String name = link.getName();
     String desc = link.getDescription();

     if (!StringUtil.isDefined(name)) {
       name = lien;
     }
     if (!StringUtil.isDefined(desc)) {
       desc = "";
     }
     // Add context before link if needed
     if (lien.indexOf("://") == -1) {
       lien = m_context + lien;
     }
     ArrayCellLink monLien = line.addArrayCellLink(name, lien);
     if (link.isPopup()) {
       monLien.setTarget("_blank");
     }

     line.addArrayCellText(desc);

     IconPane iconPane = gef.getIconPane();
     Icon updateIcon = iconPane.addIcon();
     updateIcon.setProperties(resource.getIcon("myLinks.update"), resource
         .getString("myLinks.updateLink"), "javaScript:onClick=editLink('" + linkId + "')");
     line.addArrayCellText(updateIcon.print() +
         "&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"linkCheck\" value=\"" +
         link.getLinkId() + "\"/>");
   }

   out.println(arrayPane.print());
   if (instanceId != null) {
     buttonPane.addButton(returnButton);
     out.println("<br/><center>" + buttonPane.print() + "</center><br/>");
   }
%>
</form>
</view:frame>
<%
	out.println(window.printAfter());
%>

<div id="mylink-popup-content" style="display: none">
<form name="linkForm" action="" method="post" id="linkFormId">
<label id="url_label" class="label-ui-dialog" for="url"><fmt:message key="myLinks.url"/></label>
<span class="champ-ui-dialog">
  <input id="urlId" name="url" size="60" maxlength="150" type="text" />&nbsp;<img alt="obligatoire" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
</span>
<label id="name_label" class="label-ui-dialog" for="name"><fmt:message key="GML.nom"/></label>
<span class="champ-ui-dialog">
  <input id="nameId" name="name" size="60" maxlength="150" type="text" />&nbsp;<img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5"/>
  <input type="hidden" name="linkId" value="" id="hiddenLinkId"/>
</span>
<label id="description_label" class="label-ui-dialog" for="description"><fmt:message key="GML.description" /></label>
<span class="champ-ui-dialog">
  <input type="text" name="description" size="60" maxlength="150" value="" id="descriptionId" />
</span>
<% if (!appScope) { %>
<label id="visible_label" class="label-ui-dialog" for="visible"><fmt:message key="myLinks.visible"/></label>
<span class="champ-ui-dialog">
  <input type="checkbox" name="visible" value="true" id="visibleId"/>
</span>
<% } %>
<label id="popup_label" class="label-ui-dialog" for="popup"><fmt:message key="myLinks.popup"/></label>
<span class="champ-ui-dialog">
  <input type="checkbox" name="popup" value="true" id="popupId"/>
</span>

<label id="mandatory_label"><img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5"/> : <fmt:message key="GML.mandatory"/></label>

</form>
</div>

</body>
</html>