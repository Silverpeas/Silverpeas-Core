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

<%@page import="org.silverpeas.core.admin.component.model.LocalizedComponent"%>
<%@page import="org.apache.commons.io.FilenameUtils"%>
<%@page import="org.silverpeas.core.security.encryption.cipher.CryptoException"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

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
	visibilitySpaces = template.getSpaces();
	visibilityApplications = template.getApplications();
	visibilityInstances = template.getInstances();
	action = "UpdateTemplate";
}

Button validateButton 	= gef.getFormButton(resource.getString("GML.validate"), "javascript:onclick=sendData();", false);
Button cancelButton 	= gef.getFormButton(resource.getString("GML.cancel"), "Main", false);
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
	if (isCorrectForm()) {
		document.templateForm.submit();
	}
}

function isCorrectForm() {
	var errorMsg = "";
	var errorNb = 0;
	var title = stripInitialWhitespace(document.templateForm.Name.value);
	if (title == "") {
         errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
         errorNb++;
	}
	$("#Visibility_Spaces").val(getTags($("#template-spaces-visibility").tagit("tags")));
	$("#Visibility_Instances").val(getTags($("#template-instances-visibility").tagit("tags")));
	switch(errorNb) {
	case 0 :
		result = true;
		break;
	case 1 :
		errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
		window.alert(errorMsg);
		result = false;
		break;
	default :
		errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
		window.alert(errorMsg);
		result = false;
		break;
	}
	return result;
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

$(function () {

	var tagTriggerKeys = ['enter', 'comma', 'tab', 'semicolon', 'space'];

	$('#template-spaces-visibility').tagit({triggerKeys:tagTriggerKeys});
	$('#template-instances-visibility').tagit({triggerKeys:tagTriggerKeys});

});
</script>
</head>
<body id="template-header">
<%
browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"), "Main");
browseBar.setPath(resource.getString("templateDesigner.template"));

TabbedPane tabbedPane = gef.getTabbedPane();
if (template != null) {
	tabbedPane.addTab(resource.getString("templateDesigner.fields"), "ViewTemplate", false);
}
tabbedPane.addTab(resource.getString("templateDesigner.template.specifications"), "#", true);

out.println(window.printBefore());

out.println(tabbedPane.print());
%>
<view:frame>
<% if (cryptoException != null) { %>
<div class="inlineMessage-nok"><%=cryptoException.getMessage()%></div>
<% } %>
<form name="templateForm" action="<%=action%>" method="post" enctype="multipart/form-data">
<fieldset id="main" class="skinFieldset">
<legend><%=resource.getString("templateDesigner.header.fieldset.main") %></legend>
<div class="fields">
<table cellpadding="5" width="100%">
<tr>
<td class="txtlibform"><%=resource.getString("GML.name")%> :</td><td><input type="text" name="Name" value="<%=name%>" size="60"/><input type="hidden" name="Scope" value="0"/>&nbsp;<img border="0" src="<%=resource.getIcon("templateDesigner.mandatory")%>" width="5" height="5"/></td>
</tr>
<% if (template != null) { %>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.file")%> :</td><td><%=fileName%></td>
</tr>
<% } %>
<tr>
<td class="txtlibform"><%=resource.getString("GML.description")%> :</td><td><input type="text" name="Description" value="<%=description%>" size="60"/></td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.searchable")%> :</td><td><input type="checkbox" name="Searchable" value="true" <%=searchable%>/></td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.image")%> :</td><td><input type="text" name="Image" value="<%=thumbnail%>" size="60"/></td>
</tr>
<% if (encryptionAvailable) { %>
<tr>
  <td class="txtlibform"><%=resource.getString("templateDesigner.header.encrypted")%> :</td>
  <td><input type="checkbox" name="Encrypted" value="true" <%=encrypted%>/></td>
</tr>
<% } %>
</table>
</div>
</fieldset>

<fieldset id="visibility" class="skinFieldset">
<legend><%=resource.getString("templateDesigner.visibility") %></legend>
<div class="fields">
<table cellpadding="5" width="100%">
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.visible")%> :</td><td><input type="checkbox" name="Visible" value="true" <%=visible%>/></td>
</tr>
<tr id="spaces-visibility">
	<td class="txtlibform"><%=resource.getString("templateDesigner.header.visible.spaces")%> :</td>
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
<tr id="applications-visibility">
	<td class="txtlibform"><%=resource.getString("templateDesigner.header.visible.applications")%> :</td>
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
<tr id="instances-visibility">
	<td class="txtlibform"><%=resource.getString("templateDesigner.header.visible.instances")%> :</td>
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

<fieldset id="customization" class="skinFieldset">
<legend><%=resource.getString("templateDesigner.header.fieldset.customization") %></legend>
<div class="inlineMessage">
<%=resource.getString("templateDesigner.header.customization.overview") %><br/>
<%=resource.getString("templateDesigner.header.customization.help") %>
</div>
<div class="fields">
<table cellpadding="5" width="100%">
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.header.customization.view")%> :</td>
<td>
	<% if (template != null && template.isViewLayerExist()) { %>
		<div id="ExistingViewLayer">
			<a href="<%=URLUtil.getApplicationURL()%>/FormLayer/<%=FilenameUtils.getBaseName(template.getFileName())%>?Layer=view.html" target="_blank">view.html</a>
			<a href="javascript:deleteLayer('ViewLayer')" title="<%=resource.getString("GML.delete")%>"><img src="../../util/icons/delete.gif" alt="<%=resource.getString("GML.delete")%>" /></a><br/>
		</div>
	<% } %>
	<input type="file" name="ViewLayer" /><input type="hidden" id="DeleteViewLayer" name="DeleteViewLayer" value="false"/>
</td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.header.customization.update")%> :</td>
<td>
	<% if (template != null && template.isUpdateLayerExist()) { %>
		<div id="ExistingUpdateLayer">
			<a href="<%=URLUtil.getApplicationURL()%>/FormLayer/<%=FilenameUtils.getBaseName(template.getFileName())%>?Layer=update.html" target="_blank">update.html</a>
			<a href="javascript:deleteLayer('UpdateLayer')" title="<%=resource.getString("GML.delete")%>"><img src="../../util/icons/delete.gif" alt="<%=resource.getString("GML.delete")%>" /></a><br/>
		</div>
	<% } %>
	<input type="file" name="UpdateLayer" /><input type="hidden" id="DeleteUpdateLayer" name="DeleteUpdateLayer" value="false"/>
</td>
</tr>
</table>
</div>
</fieldset>

</form>
<%
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);
out.println("<br/>"+buttonPane.print());
%>
</view:frame>
<% out.println(window.printAfter()); %>
</body>
</html>