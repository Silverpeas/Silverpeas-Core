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

<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="java.util.Vector"%>

<%
   ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
   Vector searchDomains = (Vector) request.getAttribute("searchDomains");
   String currentSearchDomainId = (String) request.getAttribute("currentSearchDomainId");
   currentSearchDomainId = (currentSearchDomainId==null) ? "SILVERPEAS" : currentSearchDomainId;
%>

<SCRIPT Language="Javascript">
	function calculateAction()
	{
		var index = document.searchDomainChoice.searchDomainId.selectedIndex;
		var value = document.searchDomainChoice.searchDomainId.options[index].value;
		if (value!="SILVERPEAS")
		{
			document.searchDomainChoice.action = "SpecificDomainView";
		}
		else
		{
			document.searchDomainChoice.action = "GlobalView";
		}
		document.searchDomainChoice.submit();
	}
</SCRIPT>

<TABLE CELLPADDING=1 CELLSPACING=0 BORDER=0 WIDTH="98%">
	<TR>
		<TD class="textePetitBold" nowrap><%=resource.getString("pdcPeas.searchDomain")%> :&nbsp;</td>
		<td>
			<table cellpadding=2 cellspacing=1 border=0 width="100%"  bgcolor=000000>
				<tr>
					<form name="searchDomainChoice" method="POST" action="..." onSubmit="calculateAction()">
						<td class="intfdcolor" align="center" nowrap width="100%" height="24"> 
							<span class="selectNS"> 
							<select name="searchDomainId" onChange="calculateAction()">
							   <% for (int i=0; searchDomains!=null && i<searchDomains.size() ; i++) 
								  {
									  String[] domain = (String[]) searchDomains.get(i);%>
								   <option <%=currentSearchDomainId.equals(domain[2])?"selected":""%> 	value="<%=domain[2]%>"><%=domain[0]%></option>
								<% } %>
							</select>
							</span>
						</td>
					</form>
				</tr>
			</table>
		</td>
		<td width="100%"> 
		&nbsp;
		</td>
	</tr>
</table>
<br>
