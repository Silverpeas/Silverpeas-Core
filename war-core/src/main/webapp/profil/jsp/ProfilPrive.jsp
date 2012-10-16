<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>


<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">profil_Silverpeas<c:out value="${componentId}" />
</c:set>
<c:set var="browseContext" value="${requestScope.browseContext}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />
<fmt:message key="profil.evenement" var="evenement" />
<fmt:message key="profil.date" var="date" />
<fmt:message key="profil.titre" var="titre" />
<fmt:message key="profil.descripion" var="description" />


<html>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">

<view:window>
	<view:tabs>
		<view:tab label="${evenement}" action="Main" selected="true" />
	</view:tabs>

	<li><view:board>
		<table>
			<tbody>
				<tr>
					<c:forEach items="${requestScope.evenements}" var="evenements">
						<tr>
							<td style="vertical-align: top">
							<div>
							<b style="top: 10px" title="${titre}">${evenements.title}</b> <br />
							<b style="top: 10px" title="${date}">${evenements.date}</b> <br />
							<br />
							<c:out value="${evenements.description}" />
							<a href="${evenements.url}"> ${evenements.title}</a>						
							</div>
							</td>
						</tr>
					</c:forEach>

				</tr>
			</tbody>
		</table>
	</view:board></li>


</view:window>

</body>
</html>