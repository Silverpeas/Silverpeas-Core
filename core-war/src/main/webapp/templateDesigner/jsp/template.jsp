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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="check.jsp" %>

<%
Form 				 formUpdate = (Form) request.getAttribute("Form");
DataRecord 			data 		= (DataRecord) request.getAttribute("Data");
PagesContext		context	= (PagesContext) request.getAttribute("context");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="popup"/>
<% formUpdate.displayScripts(out, context); %>
<script type="text/javascript">
function deleteField(fieldName) {
  $("#templateForm #fieldName").val(fieldName);
  $("#templateForm").submit();
}

function editField(fieldName,displayer) {
    openFieldWindow("EditField?FieldName=" + fieldName, displayer);
}

function openWindow(displayer) {
    openFieldWindow("NewField?Displayer=" + displayer, displayer);
}

function openFieldWindow(url, displayer) {
	if (displayer === 'pdc') {
		SP_openWindow(url, "fieldWindow", "700", "450", "directories=0, menubar=0, toolbar=0, scrollbars=yes, alwaysRaised");
	} else {
		$.ajax({
			url: url,
			async: false,
			type: "GET",
			dataType: "html",
			success: function(data) {
				$('#fieldArea').html(data);
			}
		});

		$('#fieldDialog').popup('validation', {
			title : "<%=resource.getString("templateDesigner.field")%>",
		    callback : function() {
		      return sendData();
		    }
		});
	}
}

$(document).ready(function(){
	$(".fields").sortable({
		opacity: 0.4,
		handle: 'label',
		cursor: 'move',
		axis: 'y',
		update: function( event, ui ) {
			var data = $(this).sortable("serialize");
			$.ajax({
				url: webContext+"/AjaxTemplateDesigner/SortFields",
				data: data,
				async: false,
				cache : false,
				type: "POST",
				dataType: "text",
				success: function(data) {
					if (data !== "ok") {
					  notyError(data);
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					notyError(errorThrown);
			    }
			});
		}
	});
});
</script>
</head>
<body class="yui-skin-sam">
<%
browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"), "Main");
browseBar.setPath(resource.getString("templateDesigner.template"));

operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldText"), "javascript:openWindow('text')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldTextarea"), resource.getString("templateDesigner.newFieldTextarea"), "javascript:openWindow('textarea')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldURL"), resource.getString("templateDesigner.newFieldURL"), "javascript:openWindow('url')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldEmail"), resource.getString("templateDesigner.newFieldEmail"), "javascript:openWindow('email')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldWysiwyg"), resource.getString("templateDesigner.newFieldWysiwyg"), "javascript:openWindow('wysiwyg')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldRadio"), resource.getString("templateDesigner.newFieldRadio"), "javascript:openWindow('radio')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldCheckbox"), resource.getString("templateDesigner.newFieldCheckbox"), "javascript:openWindow('checkbox')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldList"), "javascript:openWindow('listbox')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldDate"), resource.getString("templateDesigner.newFieldDate"), "javascript:openWindow('date')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldTime"), resource.getString("templateDesigner.newFieldTime"), "javascript:openWindow('time')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldFile"), resource.getString("templateDesigner.newFieldFile"), "javascript:openWindow('file')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldImage"), resource.getString("templateDesigner.newFieldImage"), "javascript:openWindow('image')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldVideo"), resource.getString("templateDesigner.newFieldVideo"), "javascript:openWindow('video')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldMap"), resource.getString("templateDesigner.newFieldMap"), "javascript:openWindow('map')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldUser"), resource.getString("templateDesigner.newFieldUser"), "javascript:openWindow('user')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldMultipleUsers"), resource.getString("templateDesigner.newFieldMultipleUsers"), "javascript:openWindow('multipleUser')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldGroup"), resource.getString("templateDesigner.newFieldGroup"), "javascript:openWindow('group')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldAccessPath"), "javascript:openWindow('accessPath')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldExplorer"), "javascript:openWindow('explorer')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldLdap"), "javascript:openWindow('ldap')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldJdbc"), "javascript:openWindow('jdbc')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldPdc"), "javascript:openWindow('pdc')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldSequence"), "javascript:openWindow('sequence')");

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("templateDesigner.fields"), "#", true);
tabbedPane.addTab(resource.getString("templateDesigner.template.specifications"), "EditTemplate", false);

out.println(window.printBefore());
out.println(tabbedPane.print());
%>
<view:frame>
<form name="myForm" method="post" action="UpdateXMLForm" enctype="multipart/form-data">
	<%
		formUpdate.display(out, context, data);
	%>
</form>
</view:frame>
<%
    out.println(window.printAfter());
%>
<div id="fieldDialog" style="display:none">
<div id="fieldArea"></div>
</div>
<form id="templateForm" method="post" action="DeleteField">
<input id="fieldName" type="hidden" name="FieldName" />
</form>
</body>
</html>