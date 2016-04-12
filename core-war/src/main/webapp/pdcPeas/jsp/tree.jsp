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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPdc.jsp"%>
<%
		Axis 	axis 			= (Axis) request.getAttribute("Axis");
		String 	Id 				= (String) request.getAttribute("ValueId"); // pour la surbrillance
		String 	displayLanguage = (String) request.getAttribute("DisplayLanguage");

		List 	userRights 		= (List) request.getAttribute("UserRights");
		boolean kmAdmin 		= ((Boolean)request.getAttribute("IsAdmin")).booleanValue() ;

		AxisHeader 	header 		= axis.getAxisHeader();
		String 		axisId 		= header.getPK().getId();
		List 		axisValues 	= axis.getValues();
		String 		valueName 	= "";
		String 		valueId 	= "";
		int 		valueLevel 	= -1;
		Value 		value 		= null;
		String 		increment 	= "";

		// pour la surbrillance
		if (Id == null)
			Id = "";
		String beginFont = "<font style=\"font-size : 10px; font-weight : bold; background-color : Navy; color : White; text-decoration : none; border:0 solid  rgb(255,150,0);\">&nbsp;&nbsp;&nbsp;";
		String endFont = "&nbsp;&nbsp;&nbsp;</font>";
%>
<html>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script language="JavaScript">

	// This function open a silverpeas window
	function openSPWindow(valueId,fonction,windowName){
		SP_openWindow(fonction, windowName, '700', '500','scrollbars=yes, resizable, alwaysRaised');
		document.refresh.ValueId.value = valueId;
		document.refresh.submit();
	}

</script>
<style type="text/css">
<!--

.axe:hover {
	font-size: 10px;
	font-weight: normal;
	color: White;
	background-color : navy;
	text-decoration: none;
	border-width: 1px;
	border-color: gray;
}

.axe {
	font-size: 10px;
	font-weight: normal;
	color: navy;
	background-color: White;
	text-decoration: none;
	border-width: 1px;
	border-style: solid;
	border-color: gray;
}
-->
</style>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">

<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
	browseBar.setPath("<a href=\"Main\">"+resource.getString("pdcPeas.allAxis")+"</a>");
	browseBar.setExtraInformation(header.getName(displayLanguage));
	browseBar.setI18N("ChangeLanguageView?Id="+axisId, displayLanguage);

    out.println(window.printBefore());
    out.println(frame.printBefore());

	if (userRights != null && userRights.size() < 1 && !kmAdmin)
		out.println("<br/><center><b><font color='red'>"+ resource.getString("pdcPeas.noRights")+ "</font></b></center><br/><br/>");

	out.println(board.printBefore());
%>
	<CENTER>
  <table border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td background="<%=resource.getIcon("pdcPeas.trame")%>">
        <%
        int levelRights = 1000;
		for (int i = 0; i<axisValues.size(); i++)
		{
			value 		= (Value) axisValues.get(i);
			valueName 	= value.getName(displayLanguage);
			valueId 	= value.getPK().getId();
			valueLevel 	= value.getLevelNumber();
			increment	= "";

			if (valueLevel <= levelRights)
				levelRights = 1000;
			if (userRights != null && userRights.contains(valueId) && (levelRights > valueLevel))
				levelRights = valueLevel;

			for (int j = 0; j < valueLevel; j++)
				increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";

			if(valueId.equals(Id))
				out.println(increment+"<img src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;<a class=\"axe\" href=\"javaScript:openSPWindow("+valueId+",'ViewValue?Id="+valueId+"&AxisId="+axisId+"&DisplayLanguage="+displayLanguage+"','windowValue')\">"+beginFont+EncodeHelper.javaStringToHtmlString(valueName)+ endFont + "</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><BR>");
			else
				if (levelRights < 1000 || kmAdmin)
					out.println(increment+"<img src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;<a class=\"axe\" href=\"javaScript:openSPWindow("+valueId+",'ViewValue?Id="+valueId+"&AxisId="+axisId+"&DisplayLanguage="+displayLanguage+"','windowValue')\">&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName) +"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><BR>");
				else
					out.println(increment+"<img src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><BR>");

		}
	  %>
      </td>
    </tr>
  </table>
	</CENTER>
<%
	out.println(board.printAfter());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="refresh" action="ViewAxis" method="post">
<input type="hidden" name="Id" value="<%=axisId%>">
<input type="hidden" name="ValueId" >
</form>
</BODY>
</HTML>