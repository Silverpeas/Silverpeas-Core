<%@ include file="check.jsp" %>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<% out.println(gef.getLookStyleSheet()); %>
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
            <td width="100%"><img align="absmiddle" width="3" height="1" src="<%=resource.getIcon("JDP.px")%>"><span class="txtpetitblanc"><%=resource.getString("JDP.domains")%> : </span></td>
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
            <%
                String[][] allDomains = (String[][])request.getAttribute("allDomains");
            	if (allDomains.length > 1)
            	{
            		%>
            		<form name="domainsNamesForm" Action="domainNavigation" Method="POST">	
	                    <span class="selectNS"> 
	                    <select name="Iddomain" size="1" onChange="javascript:document.domainsNamesForm.submit()">
	                    <option value=""><%=resource.getString("GML.select")%></option>
	                    <option value="">-----------------</option>
	                    <option value="<%=allDomains[0][0]%>" <%=allDomains[0][2]%>><%=allDomains[0][1]%></option>
	                    <%
	                        for(int n = 1; n < allDomains.length; n++)
	                        {
	                        	if (n == 1)
	                        		out.println("<option value=\"\">-----------------</option>");
	                            out.println("<option value=" + allDomains[n][0] + " " + allDomains[n][2] + ">" + allDomains[n][1] + "</option>");
	                        }
	                    %>
	                    </select></span>
                    </form>
            		<%
            	}
            	else
            	{
            		%>
            		<span class="txtlibform"><%=allDomains[0][1]%></span>
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
							Group group = null;
							String icon = null;
							for (int i=0; i < allRootGroups.length; i++) 
							{
								group = allRootGroups[i];
								icon = resource.getIcon("JDP.group");
								if (group.isSynchronized())
									icon = resource.getIcon("JDP.groupSynchronized");
								%>
								<img src="<%=resource.getIcon("JDP.px")%>" align="absmiddle" height="2"><br><img src="<%=icon%>" align=absmiddle  alt="<%=resource.getString("GML.groupe")%>" title="<%=resource.getString("GML.groupe")%>">&nbsp;<a href="javascript:viewGroup('<%=group.getId()%>')"><%=Encode.javaStringToHtmlString(group.getName())%></a><br>
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