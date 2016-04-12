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
%>
<%@page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true"/>
<view:includePlugin name="popup"/>
<script type="text/javascript">

function addLink() {
  cleanMyLinkForm();
  $("#linkFormId").attr('action', 'CreateLink');
  createLinkPopup();
}

function editLink(id) {
  $.progressMessage();
  cleanMyLinkForm();
  $("#linkFormId").attr('action', 'UpdateLink');

  var ajaxUrl = webContext + '/services/mylinks/' + id;
  jQuery.ajax({
    url: ajaxUrl,
    type: 'GET',
    contentType: "application/json",
    cache: false,
    dataType: "json",
    async: false,
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
      updateLinkPopup();
    },
    error:function(request, textStatus, errorThrown) {
      if ( window.console && window.console.log ) {
        console.log("request.status=" + request.status);
        console.log("Cannot edit link because " + textStatus + ", error :" + errorThrown);
      }
      if (request.status == 403) {
        // maybe an attack
      }
    }
  });
  $.closeProgressMessage();
}

function saveArrayLinesOrder(e, ui) {
  $.progressMessage();
  var ajaxUrl = webContext + '/services/mylinks/saveLinesOrder';
  var positionData = {
    "position" : ui.item.index(), "linkId" : ui.item.find("input[name=hiddenLinkId]").val()
  };
  jQuery.ajax({
    url : ajaxUrl,
    type : 'POST',
    data : $.toJSON(positionData),
    contentType : "application/json",
    cache : false,
    dataType : "json",
    async : false,
    success : function(result) {
      notySuccess("<fmt:message key="myLinks.newPositionLink.messageConfirm" />");
    }
  });
  $.closeProgressMessage();
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
    $.progressMessage();
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
        submitLink();
        return true;
      }
      return false;
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
        submitLink();
        return true;
      }
      return false;
    }
  });
}

function submitLink() {
  $.progressMessage();
  var $linkUrl = $("#urlId");
  var cleanUrl = $linkUrl.val().replace(new RegExp("^" + webContext), '');
  $linkUrl.val(cleanUrl);
  $("#linkFormId").submit();
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
   arrayPane.setVisibleLineNumber(-1);
   arrayPane.setUpdateSortJavascriptCallback("saveArrayLinesOrder(e, ui)");
   arrayPane.addArrayColumn(resource.getString("GML.nom")).setSortable(false);
   arrayPane.addArrayColumn(resource.getString("GML.description")).setSortable(false);
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
       if (lien.indexOf("://") == -1 && !lien.startsWith("/website")) {
         lien = m_context + lien;
       }
       ArrayCellLink monLien = line.addArrayCellLink(EncodeHelper.javaStringToHtmlString(name), lien);
       if (link.isPopup()) {
         monLien.setTarget("_blank");
       }

     line.addArrayCellText(EncodeHelper.javaStringToHtmlString(desc));

     IconPane iconPane = gef.getIconPane();
     Icon updateIcon = iconPane.addIcon();
     updateIcon.setProperties(resource.getIcon("myLinks.update"), resource
         .getString("myLinks.updateLink"), "javaScript:onClick=editLink('" + linkId + "')");
     line.addArrayCellText(updateIcon.print() +
         "&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"linkCheck\" value=\"" +
         link.getLinkId() + "\"/>" +
         "<input type=\"hidden\" name=\"hiddenLinkId\" value=\"" + link.getLinkId() + "\"/>");
   }

   out.println(arrayPane.print());
   if (appScope) {
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
    <div class="table">
      <label id="url_label" class="label-ui-dialog" for="urlId"><fmt:message key="myLinks.url"/></label>

      <div class="champ-ui-dialog">
        <input id="urlId" name="url" size="60" maxlength="150" type="text"/>&nbsp;<img alt="obligatoire" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
      </div>
      <label id="name_label" class="label-ui-dialog" for="nameId"><fmt:message key="GML.nom"/></label>

      <div class="champ-ui-dialog">
        <input id="nameId" name="name" size="60" maxlength="150" type="text"/>&nbsp;<img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
        <input type="hidden" name="linkId" value="" id="hiddenLinkId"/>
      </div>
      <label id="description_label" class="label-ui-dialog" for="descriptionId"><fmt:message key="GML.description"/></label>

      <div class="champ-ui-dialog">
        <input type="text" name="description" size="60" maxlength="150" value="" id="descriptionId"/>
      </div>
      <% if (!appScope) { %>
      <label id="visible_label" class="label-ui-dialog" for="visibleId"><fmt:message key="myLinks.visible"/></label>

      <div class="champ-ui-dialog">
        <input type="checkbox" name="visible" value="true" id="visibleId"/>
      </div>
      <% } else { %>
      <input type="hidden" name="instanceId" value="<%=instanceId%>"/>
      <% } %>
      <label id="popup_label" class="label-ui-dialog" for="popupId"><fmt:message key="myLinks.popup"/></label>

      <div class="champ-ui-dialog">
        <input type="checkbox" name="popup" value="true" id="popupId"/>
      </div>
    </div>

    <div id="mandatory_label">
      (<img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
      : <fmt:message key="GML.mandatory"/>)
    </div>
  </form>
</div>
<view:progressMessage/>
</body>
</html>