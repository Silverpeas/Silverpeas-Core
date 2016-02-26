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
<%
List<Domain> allDomains = (List<Domain>)request.getAttribute("allDomains");
Domain currentDomain = (Domain) request.getAttribute("CurrentDomain");
String currentDomainId = "";
if (currentDomain != null) {
  currentDomainId = currentDomain.getId();
}
%>

<%@ include file="check.jsp" %>
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
    <%
        String URLForContent = (String)request.getAttribute("URLForContent");

        if ((URLForContent != null) && (URLForContent.length() > 0))
        {
            out.println("parent.domainContent.location = \"" + URLForContent + "\"");
        }
    %>
}

</script>
</head>
<body marginheight="0" marginwidth="0" leftmargin="0" topmargin="5" bgcolor="#FFFFFF" onload="javascript:viewDomain()">
<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr>
    <td width="100%" class="intfdcolor13"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
    <td rowspan="3" colspan="2" class="intfdcolor"><img src="<%=resource.getIcon("JDP.anglehtdt")%>"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor4"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="6"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>" width="7" height="1"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%">
        <table width="100%" border="0" cellspacing="2" cellpadding="0">
          <tr>
            <td width="100%"><img align="absmiddle" width="3" height="1" src="<%=resource.getIcon("JDP.px")%>"><span class="domains-label"><%=resource.getString("JDP.domains")%> : </span></td>
          </tr>
        </table>
		</td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor13">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor4"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor4">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="3"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%">
		<table border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <td>&nbsp;</td>
            <td width="100%"><span class="txtnote">
						<table cellpadding=0 cellspacing=0 border=0 width=100%>
						<tr><td>
		<% if (allDomains.size() > 1) { %>
			<form name="domainsNamesForm" Action="domainNavigation" Method="POST">
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
					</td></tr>
					</table>
              </span></td>
          </tr>
        </table>
		</td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor4">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor13">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor4"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="3"></td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%">
		<table border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <td>&nbsp;</td>
            <td width="100%"><span class="txtnote">
						<table cellpadding=0 cellspacing=2 border=0 width=100%>
						<tr><td>
						<%
                            Group[] allRootGroups = (Group[])request.getAttribute("allRootGroups");
							String icon = null;
							for (Group group : allRootGroups) {
								icon = resource.getIcon("JDP.group");
								if (group.isSynchronized()) {
									icon = resource.getIcon("JDP.groupSynchronized");
								}
								%>
								<img src="<%=resource.getIcon("JDP.px")%>" align="absmiddle" height="2"><br><img src="<%=icon%>" align=absmiddle  alt="<%=resource.getString("GML.groupe")%>" title="<%=resource.getString("GML.groupe")%>">&nbsp;<a href="javascript:viewGroup('<%=group.getId()%>')"><%=EncodeHelper.javaStringToHtmlString(group.getName())%></a><br>
								<%
                            }
						%>
						</td></tr>
						</table>
            </span></td>
          </tr>
        </table>
		</td>
    <td><img src="<%=resource.getIcon("JDP.px")%>"></td>
    <td class="intfdcolor"><img src="<%=resource.getIcon("JDP.px")%>"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor51"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="6"></td>
    <td rowspan="3" colspan="2" class="intfdcolor51"><img src="<%=resource.getIcon("JDP.anglebasdt")%>"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor4"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
</tr>
<tr class="intfdcolor13">
   <td width="100%"><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
</tr>
</table>
</body>
</html>