<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
<%@ page import="org.silverpeas.core.admin.component.model.ComponentInstLight" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInstLight" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
List removedSpaces 		= (List) request.getAttribute("Spaces");
List removedComponents 	= (List) request.getAttribute("Components");

operationPane.addOperation(resource.getIcon("JSPP.restoreAll"),resource.getString("JSPP.BinRestore"),"javascript:onClick=restore()");
operationPane.addOperation(resource.getIcon("JSPP.deleteAll"),resource.getString("JSPP.BinDelete"),"javascript:onClick=remove()");

browseBar.setComponentName(resource.getString("JSPP.Bin"));

boolean emptyBin = true;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("JSPP.Bin")%></title>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="qtip"/>
<view:includePlugin name="popup"/>
<script type="text/javascript">
<!--
function removeItem(id) {
  jQuery.popup.confirm("<%=resource.getString("JSPP.BinDeleteConfirm")%>", function() {
    $.progressMessage();
    location.href = "RemoveDefinitely?ItemId=" + id;
  });
}

function remove() {
  jQuery.popup.confirm("<%=resource.getString("JSPP.BinDeleteConfirmSelected")%>", function() {
    $.progressMessage();
    window.document.binForm.action = "RemoveDefinitely";
    window.document.binForm.submit();
  });
}

function restore() {
  jQuery.popup.confirm("<%=resource.getString("JSPP.BinRestoreSelected")%>", function() {
    $.progressMessage();
    window.document.binForm.action = "RestoreFromBin";
    window.document.binForm.submit();
  });
}

function restoreItem(id) {
  $.progressMessage();
  location.href = "RestoreFromBin?ItemId=" + id;
}

function jqCheckAll2(id, name)
{
   $("input[name='" + name + "'][type='checkbox']").attr('checked', $('#' + id).is(':checked'));
}

$(document).ready(function() {
  // By suppling no content attribute, the library uses each elements title attribute by default
  $('.item-path').qtip({
    content : {
      text : false,
      title : {
        text : "<%=resource.getString("GML.path")%>"
      }
    },
    style : {
      tip : true,
      classes : "qtip-shadow qtip-green"
    },
    position : {
      adjust : {
        method : "flip flip"
      },
      at : "bottom center",
      my : "top left",
      viewport : $(window)
    }
  });
});
-->
</script>
</head>
<body class="page_content_admin">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<form name="binForm" action="" method="post">
<%
	ArrayPane arrayPane = gef.getArrayPane("binContentSpaces", "ViewBin", request, session);
	arrayPane.addArrayColumn(resource.getString("GML.space"));
	arrayPane.addArrayColumn(resource.getString("JSPP.BinRemoveDate"));
	ArrayColumn columnOp = arrayPane.addArrayColumn("<span style=\"float:left\">"+resource.getString("GML.operation")+"</span> <input type=\"checkbox\" id=\"checkAllSpaces\" onclick=\"jqCheckAll2(this.id, 'SpaceIds')\" style=\"float:left;margin:0px;margin-left:5px;padding:0px;vertical-align:middle;background-color:none;\"/>");
	columnOp.setSortable(false);

	//Array of deleted spaces
	if (removedSpaces != null && removedSpaces.size() != 0)
	{
		emptyBin = false;
		Iterator it = (Iterator) removedSpaces.iterator();
		while (it.hasNext())
		{
			ArrayLine line = arrayPane.addArrayLine();
			SpaceInstLight space = (SpaceInstLight) it.next();
			ArrayCellText cellLabel = null;
			if (space.isRoot())
				cellLabel = line.addArrayCellText(space.getName());
			else
				cellLabel = line.addArrayCellText("<a href=\"#\" class=\"item-path\" title=\""+
            WebEncodeHelper.javaStringToJsString(space.getPath(" > "))+"\"/>"+WebEncodeHelper.javaStringToHtmlString(space.getName())+"</a>");
			cellLabel.setCompareOn(space.getName());
			ArrayCellText cell = line.addArrayCellText(resource.getOutputDateAndHour(space.getRemoveDate())+"&nbsp;("+space.getRemoverName()+")");
			cell.setCompareOn(space.getRemoveDate());

			IconPane iconPane = gef.getIconPane();
			Icon restoreIcon = iconPane.addIcon();
			restoreIcon.setProperties(resource.getIcon("JSPP.restore"), resource.getString("JSPP.BinRestore"), "javaScript:onclick=restoreItem('"+space.getId()+"')");
			Icon deleteIcon = iconPane.addIcon();
			deleteIcon.setProperties(resource.getIcon("JSPP.delete"), resource.getString("JSPP.BinDelete"), "javaScript:onClick=removeItem('"+space.getId()+"')");
			line.addArrayCellText(restoreIcon.print()+"&nbsp;&nbsp;&nbsp;"+deleteIcon.print()+"&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"SpaceIds\" value=\""+space.getId()+"\">");
		}
		out.println(arrayPane.print());
	}

	//Array of deleted components
	arrayPane = gef.getArrayPane("binContentComponents", "ViewBin", request, session);
	arrayPane.addArrayColumn(resource.getString("GML.component"));
	arrayPane.addArrayColumn(resource.getString("JSPP.BinRemoveDate"));
	columnOp = arrayPane.addArrayColumn("<span style=\"float:left\">"+resource.getString("GML.operation")+"</span> <input type=\"checkbox\" id=\"checkAllComponents\" onclick=\"jqCheckAll2(this.id, 'ComponentIds')\" style=\"float:left;margin:0px;margin-left:5px;padding:0px;vertical-align:middle;background-color:none;\"/>");
	columnOp.setSortable(false);

	if (removedComponents != null && removedComponents.size() != 0)
	{
		if (!emptyBin)
			out.println("<br/>");

		emptyBin = false;
		Iterator it = (Iterator) removedComponents.iterator();
		while (it.hasNext())
		{
			ArrayLine line = arrayPane.addArrayLine();
			ComponentInstLight component = (ComponentInstLight) it.next();
			line.addArrayCellText("<a href=\"#\" class=\"item-path\" title=\""+component.getPath(" > ")+"\"/>"+
					WebEncodeHelper.javaStringToHtmlString(component.getLabel())+"</a>");
			ArrayCellText cell = line.addArrayCellText(resource.getOutputDateAndHour(component.getRemoveDate())+"&nbsp;("+component.getRemoverName()+")");
			cell.setCompareOn(component.getRemoveDate());

			IconPane iconPane = gef.getIconPane();
			Icon restoreIcon = iconPane.addIcon();
			restoreIcon.setProperties(resource.getIcon("JSPP.restore"), resource.getString("JSPP.BinRestore"), "javaScript:onclick=restoreItem('"+component.getId()+"')");
			Icon deleteIcon = iconPane.addIcon();
			deleteIcon.setProperties(resource.getIcon("JSPP.delete"), resource.getString("JSPP.BinDelete"), "javaScript:onClick=removeItem('"+component.getId()+"')");
			line.addArrayCellText(restoreIcon.print()+"&nbsp;&nbsp;&nbsp;"+deleteIcon.print()+"&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"ComponentIds\" value=\""+component.getId()+"\">");
		}
		out.println(arrayPane.print());
	}

	if (emptyBin)
	{
		out.println(board.printBefore());
		out.println("<center>"+resource.getString("JSPP.BinEmpty")+"</center>");
		out.println(board.printAfter());
	}
%>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>