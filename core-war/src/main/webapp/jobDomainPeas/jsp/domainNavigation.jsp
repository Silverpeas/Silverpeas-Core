<%--

    Copyright (C) 2000 - 2017 Silverpeas

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
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %><%--

    Copyright (C) 2000 - 2017 Silverpeas

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
List<Domain> allDomains = (List<Domain>)request.getAttribute("allDomains");
Domain currentDomain = (Domain) request.getAttribute("CurrentDomain");
String currentDomainId = "";
if (currentDomain != null) {
  currentDomainId = currentDomain.getId();
}
%>

<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script language="JavaScript1.2">
function viewGroup(arg){
	parent.domainContent.location = "groupSet?Idgroup="+arg;
}

function viewDomain()
{
  <c:if test="${empty requestScope.domainRefreshCurrentLevel}">
    <%
        String URLForContent = (String)request.getAttribute("URLForContent");

        if ((URLForContent != null) && (URLForContent.length() > 0))
        {
            out.println("parent.domainContent.location = \"" + URLForContent + "\"");
        }
    %>
  </c:if>
}

function refreshCurrentLevel() {
  parent.domainBar.location = "domainRefreshCurrentLevel";
}

</script>
</head>
<body class="domainNavigation" onload="javascript:viewDomain()">
<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr class="intfdcolor">
    <td width="100%">
      <span class="domains-label"><%=resource.getString("JDP.domains")%> : </span>
	</td>
</tr>
<tr class="intfdcolor51">
    <td width="100%">
		
		<% if (allDomains.size() > 1) { %>
			<form class="domainsNamesForm" name="domainsNamesForm" Action="domainNavigation" Method="POST">
	                    <span class="selectNS">
	                    <select name="Iddomain" size="1" onchange="javascript:document.domainsNamesForm.submit()">
	                    <option value=""><%=resource.getString("GML.select")%></option>
	                    <option value="">-----------------</option>
	                    <% for(int n = 0; n < allDomains.size(); n++) {
					Domain domain = allDomains.get(n);
					String domainName = domain.getName();
					if (domain.isMixedOne()) {
					  domainName = resource.getString("JDP.domainMixt");
					}
					String selected = "";
					if (domain.getId().equals(currentDomainId)) {
					  selected = " selected=\"selected\"";
					}
					if (n == 1) {
						out.println("<option value=\"\">-----------------</option>");
					}
	                            out.println("<option value=\"" + domain.getId() + "\" " + selected + ">" + domainName + "</option>");
	                        }
	                    %>
	                    </select></span>
                    </form>
		<% } else { %>
			<span class="txtlibform"><%=allDomains.get(0).getName()%></span>
		<% } %>
		</td>
</tr>
<tr class="intfdcolor51">
    <td width="100%">
						<%
              Group[] allRootGroups = (Group[])request.getAttribute("allRootGroups");
							String icon = null;
							for (Group group : allRootGroups) {
								icon = resource.getIcon("JDP.group");
								if (group.isSynchronized()) {
									icon = resource.getIcon("JDP.groupSynchronized");
								}
								%>
								
                <img class="GroupIcon" src="<%=icon%>"  alt="<%=resource.getString("GML.groupe")%>" title="<%=resource.getString("GML.groupe")%>" />
                &nbsp;<a href="javascript:viewGroup('<%=group.getId()%>')"><%=WebEncodeHelper.javaStringToHtmlString(group.getName()) + " (" + group.getTotalNbUsers() + ")"%></a><br>
								<%
                            }
						%>
	</td>
</tr>
</table>
</body>
</html>