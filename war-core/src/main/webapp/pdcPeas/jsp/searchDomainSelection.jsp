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
