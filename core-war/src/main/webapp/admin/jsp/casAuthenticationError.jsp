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

<%@ page import="org.silverpeas.core.util.ResourceLocator"
%>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%
	SettingBundle general = ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
	LocalizationBundle generalMultilang = ResourceLocator.getGeneralLocalizationBundle(
			request.getLocale().getLanguage());
	String sURI = request.getRequestURI();
	String sServletPath = request.getServletPath();
	String sPathInfo = request.getPathInfo();
	if (sPathInfo != null)
	{
	    sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));
	}
	String m_context = "../../.." + sURI.substring(0, sURI.lastIndexOf(sServletPath));

	String styleSheet = general.getString("defaultStyleSheet", m_context + "/util/styleSheets/globalSP.css");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><%=generalMultilang.getString("GML.popupTitle")%></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<link rel="stylesheet" href="<%=styleSheet%>"/>
</head>

<body>
	<table cellpadding="0" cellspacing="2" border="0" width="98%" class="intfdcolor">
		<tr>
			<td class="intfdcolor4" align="center">
				<br/>
				<span class="txtnav"><%=generalMultilang.getString("GML.ForbiddenAccess")%></span>
				<br/>
				<br/>
			</td>
		</tr>
	</table>
</body>
</html>