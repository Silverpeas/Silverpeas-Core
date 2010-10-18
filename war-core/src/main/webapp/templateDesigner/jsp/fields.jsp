<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%
Iterator fields = (Iterator) request.getAttribute("Fields");

%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript" type="text/javascript">
	function openWindow(displayer) {
	    openFieldWindow("NewField?Displayer=" + displayer, displayer);
	}
	
	function editField(fieldName,displayer) {
	    openFieldWindow("EditField?FieldName=" + fieldName, displayer);
	}

	function openFieldWindow(url, displayer) {
		var width = "570";
		var height = "350";
		if (displayer == 'radio' || displayer == 'checkbox' || displayer == 'listbox') {
			height = "450";
		} else if (displayer == 'ldap') {
			height = "600";
		} else if (displayer == 'jdbc') {
			height = "450";
		} else if (displayer == 'pdc') {
			height = "450";
			width = "700";
		}
		SP_openWindow(url, "fieldWindow", width, height, "directories=0, menubar=0, toolbar=0, alwaysRaised");
	}
	
	function move(direction, fieldName) {
		location.href="MoveField?FieldName="+fieldName+"&Direction="+direction;
	}
</script>
</head>
<body>
<%
browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"), "Main");
browseBar.setPath(resource.getString("templateDesigner.template"));

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.preview"), "ViewTemplate", false);
tabbedPane.addTab(resource.getString("templateDesigner.template"), "EditTemplate", false);
tabbedPane.addTab(resource.getString("templateDesigner.fields"), "ViewFields", true);

operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldText"), "javascript:openWindow('text')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldTextarea"), resource.getString("templateDesigner.newFieldTextarea"), "javascript:openWindow('textarea')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldURL"), resource.getString("templateDesigner.newFieldURL"), "javascript:openWindow('url')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldWysiwyg"), resource.getString("templateDesigner.newFieldWysiwyg"), "javascript:openWindow('wysiwyg')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldRadio"), resource.getString("templateDesigner.newFieldRadio"), "javascript:openWindow('radio')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldCheckbox"), resource.getString("templateDesigner.newFieldCheckbox"), "javascript:openWindow('checkbox')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldList"), "javascript:openWindow('listbox')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldDate"), resource.getString("templateDesigner.newFieldDate"), "javascript:openWindow('date')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldFile"), resource.getString("templateDesigner.newFieldFile"), "javascript:openWindow('file')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldImage"), resource.getString("templateDesigner.newFieldImage"), "javascript:openWindow('image')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldVideo"), resource.getString("templateDesigner.newFieldVideo"), "javascript:openWindow('video')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldUser"), resource.getString("templateDesigner.newFieldUser"), "javascript:openWindow('user')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldGroup"), resource.getString("templateDesigner.newFieldGroup"), "javascript:openWindow('group')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldLdap"), "javascript:openWindow('ldap')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldAccessPath"), "javascript:openWindow('accessPath')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldJdbc"), "javascript:openWindow('jdbc')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldList"), resource.getString("templateDesigner.newFieldPdc"), "javascript:openWindow('pdc')");
operationPane.addOperation(resource.getIcon("templateDesigner.newFieldText"), resource.getString("templateDesigner.newFieldSequence"), "javascript:openWindow('sequence')");

ArrayPane arrayPane = gef.getArrayPane("fieldList", "ViewFields?Scope=0", request, session);
ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resource.getString("GML.name"));
arrayColumn1.setSortable(false);
ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString("GML.type"));
arrayColumn2.setSortable(false);
ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resource.getString("templateDesigner.displayer"));
arrayColumn3.setSortable(false);
ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resource.getString("templateDesigner.operations"));
arrayColumn4.setSortable(false);

FieldTemplate field = null;
String fieldName = null;
boolean firstField = true;
while (fields.hasNext())
{
	field = (FieldTemplate) fields.next();
	fieldName = field.getFieldName();
	
	ArrayLine ligne = arrayPane.addArrayLine();
	
	ligne.addArrayCellText(fieldName);
	ligne.addArrayCellText(field.getTypeName());
	ligne.addArrayCellText(field.getDisplayerName());
	
	IconPane icon = gef.getIconPane();
	Icon updateIcon = icon.addIcon();
	updateIcon.setProperties(resource.getIcon("templateDesigner.smallUpdate"), resource.getString("GML.modify"), "javascript:editField('"+fieldName+"','"+field.getDisplayerName()+"')");
	Icon delIcon = icon.addIcon();
	delIcon.setProperties(resource.getIcon("templateDesigner.smallDelete"), resource.getString("GML.delete"), "DeleteField?FieldName="+fieldName);
	icon.setSpacing("30px");
	
	if (firstField) {
		Icon upIcon = icon.addEmptyIcon();
		firstField = false;
	} else {
		Icon upIcon = icon.addIcon();
        upIcon.setProperties(resource.getIcon("templateDesigner.arrowUp"), resource.getString("templateDesigner.up"), "javascript:onClick=move(-1, '"+fieldName+"')");
	}

	if (fields.hasNext()) {
		Icon downIcon = icon.addIcon();
        downIcon.setProperties(resource.getIcon("templateDesigner.arrowDown"), resource.getString("templateDesigner.down"), "javascript:onClick=move(1, '"+fieldName+"')");
	} else {
		Icon downIcon = icon.addEmptyIcon();
	}
	
	ligne.addArrayCellIconPane(icon);
}

	out.println(window.printBefore());
	
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(arrayPane.print());
%>

<%

out.println(frame.printAfter());
out.println(window.printAfter()); 
%>
</body>
</html>