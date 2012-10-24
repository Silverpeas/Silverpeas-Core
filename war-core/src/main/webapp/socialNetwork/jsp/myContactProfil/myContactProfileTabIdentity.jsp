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

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.StringUtil"%>
    
<%
ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
%>

<!-- sousNav  --> 
<div class="sousNavBulle">
	<p><fmt:message key="profil.subnav.display" /> : <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a> <!-- <a href="#">Personnelles</a> <a href="#">Personnelles</a> --></p>
</div><!-- /sousNav  --> 

<div class="tab-content">
<table width="100%" cellspacing="0" cellpadding="5" border="0">
<%
	if (userFull != null) {
       	//  récupérer toutes les propriétés de ce User
        String[] properties = userFull.getPropertiesNames();

        String property = null;
        for (int p = 0; p < properties.length; p++) {
         property = properties[p];
         if (StringUtil.isDefined(userFull.getValue(property)) && resource.getSetting(property, true)) {
        %>
            <tr>
              <td class="txtlibform"><%= userFull.getSpecificLabel(resource.getLanguage(), property)%> :</td>
              <td >
                <%=userFull.getValue(property)%>
              </td>
            </tr>
		<%
            }
        }
	}
%>
</table>
</div>