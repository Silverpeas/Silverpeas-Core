<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@page import="org.silverpeas.core.admin.component.model.LocalizedComponent"%>
<%@page import="org.apache.commons.io.FilenameUtils"%>
<%@page import="org.silverpeas.core.security.encryption.cipher.CryptoException"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message key="GML.delete" var="labelDelete"/>

<c:set var="utilization" value="${requestScope.Utilization}"/>
<c:set var="template" value="${requestScope.Template}"/>
<c:set var="creationMode" value="${template == null}"/>
<c:if test="${not creationMode}">
  <jsp:useBean id="template" type="org.silverpeas.core.contribution.template.publication.PublicationTemplate"/>
</c:if>

<%@ include file="check.jsp" %>
<%
PublicationTemplate template = (PublicationTemplate) request.getAttribute("Template");
List<LocalizedComponent> components = (List<LocalizedComponent>) request.getAttribute("ComponentsUsingForms");
boolean encryptionAvailable = (Boolean) request.getAttribute("EncryptionAvailable");
CryptoException cryptoException = (CryptoException) request.getAttribute("CryptoException");

String name = "";
String description = "";
String thumbnail = "";
String fileName = "";
String visible = "";
String encrypted = "";
String searchable = "";
String action = "AddTemplate";
String directoryUsage = "";
List<String> visibilityDomains = null;
List<String> visibilityGroups = null;
List<String> visibilitySpaces = null;
List<String> visibilityApplications = null;
List<String> visibilityInstances = null;

if (template != null) {
	name = template.getName();
	description = template.getDescription();
	thumbnail = template.getThumbnail();
	fileName = template.getFileName();
	if (template.isVisible()) {
		visible = "checked=\"checked\"";
	}
	if (template.isDataEncrypted()) {
		encrypted = "checked=\"checked\"";
	}
	if (template.isSearchable()) {
		searchable = "checked=\"checked\"";
	}
	if (template.isDirectoryUsage()) {
    directoryUsage = "checked=\"checked\"";
  }
	visibilitySpaces = template.getSpaces();
	visibilityApplications = template.getApplications();
	visibilityInstances = template.getInstances();
	visibilityDomains = template.getDomains();
  visibilityGroups = template.getGroups();
	action = "UpdateTemplate";
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="tags" />
<style type="text/css">
.txtlibform {
	width: 250px;
}

.skinFieldset legend {
	padding: 0px 10px;
}

ul.tagit {
	width: 376px;
	margin: 0px;
	padding: 0px;
}
#template-apps-visibility {
	list-style-type: none;
	padding: 0px;
	margin: 0px;
	width: 450px;
}
#template-apps-visibility li {
	width: 50%;
	float: left;
}

#template-apps-visibility li img {
	padding: 1px 2px 0px 0px;
}
</style>
<script type="text/javascript">
function sendData() {
	var errorMsg = "";
	var errorNb = 0;
	var title = stripInitialWhitespace(document.templateForm.Name.value);
	if (title == "") {
         errorMsg+=" - '<fmt:message key="GML.name"/>' <fmt:message key="GML.MustBeFilled"/>\n";
         errorNb++;
	}
	$("#Visibility_Spaces").val(getTags($("#template-spaces-visibility").tagit("tags")));
	$("#Visibility_Instances").val(getTags($("#template-instances-visibility").tagit("tags")));
  $("#Visibility_Domains").val(getTags($("#template-domains-visibility").tagit("tags")));
  $("#Visibility_Groups").val(getTags($("#template-groups-visibility").tagit("tags")));
	switch(errorNb) {
	  case 0 :
      document.templateForm.submit();
		  break;
	  case 1 :
		  errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
      jQuery.popup.error(errorMsg);
		  break;
	  default :
		  errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
	}
}

function getTags(tags) {
	var string = "";
	for (var i in tags) {
		string += tags[i].value + " ";
	}
	return string;
}

function deleteLayer(id) {
	$('#Delete'+id).val('true');
	$('#Existing'+id).hide();
}

function toDuplicate() {
  $('#duplicateDialog').popup('validation', {
    title : "<fmt:message key="GML.action.duplicate"/>",
    callback : function() {
      if (StringUtil.isNotDefined($("#DuplicatedFormName").val())) {
        SilverpeasError.add("'<fmt:message key="GML.name"/>' <fmt:message key="GML.MustBeFilled"/>");
        return !SilverpeasError.show();
      }
      document.DuplicateForm.submit();
    }
  });
}

function deleteForm() {
  $('#deleteDialog').popup('validation', {
    title : "${labelDelete}",
    callback : function() {
      return document.DeletionForm.submit();
    }
  });
}

$(function () {

	var tagTriggerKeys = ['enter', 'comma', 'tab', 'semicolon', 'space'];

	$('#template-spaces-visibility').tagit({triggerKeys:tagTriggerKeys});
	$('#template-instances-visibility').tagit({triggerKeys:tagTriggerKeys});
  $('#template-domains-visibility').tagit({triggerKeys:tagTriggerKeys});
  $('#template-groups-visibility').tagit({triggerKeys:tagTriggerKeys});

  <%if (template != null && template.isDirectoryUsage()) { %>
    $(".notApplicableToDirectory").hide();
    $(".applicableToDirectory").show();
  <% } else { %>
  $(".applicableToDirectory").hide();
  <% } %>
  $("#DirectoryUsage").click(function() {
    $(".notApplicableToDirectory").toggle();
    $(".applicableToDirectory").toggle();
  });

  <c:if test="${not creationMode and template.locked}">
    $("input").attr("disabled", true);
    $("input", $(document.DuplicateForm)).attr("disabled", false);
    $(".layer-delete").css("display", "none");
  </c:if>

});
</script>
</head>
<body id="template-header" class="page_content_admin">

<fmt:message var="toolName" key="templateDesigner.toolName"/>
<fmt:message var="list" key="templateDesigner.templateList"/>
<fmt:message var="labelTemplate" key="templateDesigner.template"/>
<c:if test="${not creationMode}">
  <c:set var="labelTemplate" value="${template.name}"/>
</c:if>
<view:browseBar>
  <view:browseBarElt label="${toolName}"/>
  <view:browseBarElt link="Main" label="${list}"/>
  <view:browseBarElt label="${labelTemplate}"/>
</view:browseBar>

<c:if test="${not creationMode}">
  <view:operationPane>
    <fmt:message var="labelDuplicate" key="GML.action.duplicate"/>
    <view:operation action="javascript:toDuplicate()" altText="${labelDuplicate}"/>
    <c:if test="${empty utilization && !template.locked}">
      <view:operation action="javascript:deleteForm()" altText="${labelDelete}"/>
    </c:if>
  </view:operationPane>
</c:if>

<view:window>

  <c:if test="${not creationMode}">
    <c:if test="${template.locked}">
      <div class="inlineMessage">
        <fmt:message key="templateDesigner.form.locked.help"/>
      </div>
    </c:if>
    <view:tabs>
      <fmt:message key="templateDesigner.fields" var="labelTabFields"/>
      <fmt:message key="templateDesigner.template.specifications" var="labelTabSpec"/>
        <view:tab label="${labelTabFields}" action="ViewTemplate" selected="false"/>
      <view:tab label="${labelTabSpec}" action="#" selected="true"/>
    </view:tabs>
  </c:if>

<view:frame>
<% if (cryptoException != null) { %>
<div class="inlineMessage-nok"><%=cryptoException.getMessage()%></div>
<% } %>
<form name="templateForm" action="<%=action%>" method="post" enctype="multipart/form-data">
<fieldset id="main" class="skinFieldset">
<legend><fmt:message key="templateDesigner.header.fieldset.main"/></legend>
<div class="fields">
<table cellpadding="5" width="100%">
<tr>
<td class="txtlibform"><fmt:message key="GML.name"/> </td><td><input type="text" name="Name" value="<%=name%>" size="60"/><input type="hidden" name="Scope" value="0"/>&nbsp;<img border="0" src="<%=resource.getIcon("templateDesigner.mandatory")%>" width="5" height="5"/></td>
</tr>
<% if (template != null) { %>
<tr>
<td class="txtlibform"><fmt:message key="templateDesigner.file"/> </td><td><%=fileName%></td>
</tr>
<% } %>
<tr>
<td class="txtlibform"><fmt:message key="GML.description"/> </td><td><input type="text" name="Description" value="<%=description%>" size="60"/></td>
</tr>
<tr>
<td class="txtlibform"><fmt:message key="templateDesigner.searchable"/> </td><td><input type="checkbox" name="Searchable" value="true" <%=searchable%>/></td>
</tr>
<tr>
<td class="txtlibform"><fmt:message key="templateDesigner.image"/> </td><td><input type="text" name="Image" value="<%=thumbnail%>" size="60"/></td>
</tr>
<% if (encryptionAvailable) { %>
<tr>
  <td class="txtlibform"><fmt:message key="templateDesigner.header.encrypted"/> </td>
  <td><input type="checkbox" name="Encrypted" value="true" <%=encrypted%>/></td>
</tr>
<% } %>
</table>
</div>
</fieldset>

<fieldset id="visibility" class="skinFieldset">
<legend><fmt:message key="templateDesigner.visibility"/></legend>
<div class="fields">
<table cellpadding="5" width="100%">
<tr>
<td class="txtlibform"><fmt:message key="templateDesigner.visible"/> </td><td><input type="checkbox" name="Visible" value="true" <%=visible%>/></td>
</tr>
<tr>
  <td class="txtlibform"><fmt:message key="templateDesigner.header.directory"/></td>
  <td><input type="checkbox" name="DirectoryUsage" id="DirectoryUsage" value="true" <%=directoryUsage%>/></td>
</tr>
<tr id="domains-visibility" class="applicableToDirectory">
  <td class="txtlibform"><fmt:message key="templateDesigner.header.visible.domains"/> </td>
  <td>
    <ul id="template-domains-visibility">
      <% if (visibilityDomains != null)  { %>
      <% for (String domain : visibilityDomains) { %>
      <li data-value="<%=domain%>"><%=domain%></li>
      <% } %>
      <% } %>
    </ul>
    <input type="hidden" id="Visibility_Domains" name="Visibility_Domains"/>
  </td>
</tr>
<tr id="groups-visibility" class="applicableToDirectory">
  <td class="txtlibform"><fmt:message key="templateDesigner.header.visible.groups"/> </td>
  <td>
    <ul id="template-groups-visibility">
      <% if (visibilityGroups != null)  { %>
      <% for (String groupId : visibilityGroups) { %>
      <li data-value="<%=groupId%>"><%=groupId%></li>
      <% } %>
      <% } %>
    </ul>
    <input type="hidden" id="Visibility_Groups" name="Visibility_Groups"/>
  </td>
</tr>
<tr id="spaces-visibility" class="notApplicableToDirectory">
	<td class="txtlibform"><fmt:message key="templateDesigner.header.visible.spaces"/> </td>
	<td>
		<ul id="template-spaces-visibility">
		<% if (visibilitySpaces != null)  { %>
			<% for (String space : visibilitySpaces) { %>
				<li data-value="<%=space%>"><%=space%></li>
			<% } %>
		<% } %>
		</ul>
		<input type="hidden" id="Visibility_Spaces" name="Visibility_Spaces"/>
	</td>
</tr>
<tr id="applications-visibility" class="notApplicableToDirectory">
	<td class="txtlibform"><fmt:message key="templateDesigner.header.visible.applications"/> </td>
	<td><ul id="template-apps-visibility">
		<% for (LocalizedComponent component : components) {
			String checked = "";
			if (visibilityApplications != null && visibilityApplications.contains(component.getName())) {
			  checked = "checked=\"checked\"";
			}
		%>
			<li><input type="checkbox" name="Visibility_Applications" value="<%=component.getName() %>" <%=checked %>/><img src="<%=URLUtil.getApplicationURL() %>/util/icons/component/<%=component.getName() %>Small.gif" alt=""/><%= component.getLabel() %></li>
		<% } %>
		</ul>
	</td>
</tr>
<tr id="instances-visibility" class="notApplicableToDirectory">
	<td class="txtlibform"><fmt:message key="templateDesigner.header.visible.instances"/> </td>
	<td>
		<ul id="template-instances-visibility">
			<% if (visibilityInstances != null)  { %>
				<% for (String instance : visibilityInstances) { %>
					<li data-value="<%=instance%>"><%=instance%></li>
				<% } %>
			<% } %>
		</ul>
		<input type="hidden" id="Visibility_Instances" name="Visibility_Instances"/>
	</td>
</tr>
</table>
</div>
</fieldset>

  <c:if test="${not creationMode}">
    <fieldset id="utilization" class="skinFieldset">
      <legend><fmt:message key="templateDesigner.form.utilization"/></legend>
      <c:choose>
        <c:when test="${empty utilization}">
          <div class="inlineMessage"><fmt:message key="templateDesigner.form.utilization.none"/></div>
        </c:when>
        <c:otherwise>
          <fmt:message var="utilApp" key="GML.component"/>
          <fmt:message var="utilNb" key="templateDesigner.form.utilization.nb"/>
          <view:arrayPane var="form-utilization" numberLinesPerPage="1000">
            <view:arrayColumn title="${utilApp}" sortable="false"/>
            <view:arrayColumn title="${utilNb}" sortable="false"/>
            <c:forEach var="appId" items="${utilization.keySet()}">
              <view:arrayLine>
                <view:arrayCellText>
                  <c:choose>
                    <c:when test="${appId == 'directory'}">
                      <fmt:message key="templateDesigner.header.directory"/>
                    </c:when>
                    <c:otherwise>
                      <view:componentPath componentId="${appId}" language="${language}" link="true"/>
                    </c:otherwise>
                  </c:choose>
                </view:arrayCellText>
                <view:arrayCellText>${utilization.get(appId)}</view:arrayCellText>
              </view:arrayLine>
            </c:forEach>
          </view:arrayPane>
        </c:otherwise>
      </c:choose>
    </fieldset>
  </c:if>

<fieldset id="customization" class="skinFieldset">
<legend><fmt:message key="templateDesigner.header.fieldset.customization"/></legend>
<div class="inlineMessage">
  <fmt:message key="templateDesigner.header.customization.overview"/><br/>
  <fmt:message key="templateDesigner.header.customization.help"/>
</div>
<div class="fields">
<table cellpadding="5" width="100%">
<tr>
<td class="txtlibform"><fmt:message key="templateDesigner.header.customization.view"/> </td>
<td>
	<% if (template != null && template.isViewLayerExist()) { %>
		<div id="ExistingViewLayer">
			<a href="<%=URLUtil.getApplicationURL()%>/FormLayer/<%=FilenameUtils.getBaseName(template.getFileName())%>?Layer=view.html" target="_blank">view.html</a>
			<a href="javascript:deleteLayer('ViewLayer')" title="${labelDelete}" class="layer-delete"><img src="../../util/icons/delete.gif" alt="${labelDelete}" /></a><br/>
		</div>
	<% } %>
	<input type="file" name="ViewLayer" /><input type="hidden" id="DeleteViewLayer" name="DeleteViewLayer" value="false"/>
</td>
</tr>
<tr>
<td class="txtlibform"><fmt:message key="templateDesigner.header.customization.update"/> </td>
<td>
	<% if (template != null && template.isUpdateLayerExist()) { %>
		<div id="ExistingUpdateLayer">
			<a href="<%=URLUtil.getApplicationURL()%>/FormLayer/<%=FilenameUtils.getBaseName(template.getFileName())%>?Layer=update.html" target="_blank">update.html</a>
			<a href="javascript:deleteLayer('UpdateLayer')" title="${labelDelete}" class="layer-delete"><img src="../../util/icons/delete.gif" alt="${labelDelete}" /></a><br/>
		</div>
	<% } %>
	<input type="file" name="UpdateLayer" /><input type="hidden" id="DeleteUpdateLayer" name="DeleteUpdateLayer" value="false"/>
</td>
</tr>
</table>
</div>
</fieldset>

</form>

  <c:if test="${creationMode or not template.locked}">
    <view:buttonPane>
      <fmt:message var="labelButtonValidate" key="GML.validate"/>
      <fmt:message var="labelButtonCancel" key="GML.cancel"/>
      <view:button label="${labelButtonValidate}" action="javascript:onclick=sendData();"/>
      <view:button label="${labelButtonCancel}" action="Main"/>
    </view:buttonPane>
  </c:if>

</view:frame>
</view:window>

<div id="duplicateDialog" style="display:none">
<form name="DuplicateForm" action="DuplicateForm" method="post">
  <div>
    <label id="name_label" class="label-ui-dialog" for="DuplicatedFormName"><fmt:message key="GML.nom"/></label>
    <div class="champ-ui-dialog">
      <input id="DuplicatedFormName" name="DuplicatedFormName" size="60" maxlength="150" type="text"/>&nbsp;<img alt="obligatoire" src="<c:url value='/util/icons/mandatoryField.gif' />" height="5" width="5"/>
    </div>
  </div>

  <div id="mandatory_label">
    (<img border="0" src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
    : <fmt:message key="GML.mandatory"/>)
  </div>
</form>
</div>

<div id="deleteDialog" style="display: none">
  <form name="DeletionForm" action="RemoveTemplate" method="post">
    <fmt:message key="templateDesigner.form.delete.confirm"/>
  </form>
</div>

</body>
</html>