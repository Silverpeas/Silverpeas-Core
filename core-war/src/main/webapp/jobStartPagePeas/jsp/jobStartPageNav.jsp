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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
    DisplaySorted[] m_Spaces = (DisplaySorted[])request.getAttribute("Spaces");
    DisplaySorted[] m_SubSpaces = (DisplaySorted[])request.getAttribute("SubSpaces");
    DisplaySorted[] m_Components = (DisplaySorted[])request.getAttribute("SpaceComponents");
    DisplaySorted[] m_SubComponents = (DisplaySorted[])request.getAttribute("SubSpaceComponents");
    Boolean haveToRefreshMainPage = (Boolean)request.getAttribute("haveToRefreshMainPage");
    String currentSpaceId = (String)request.getAttribute("CurrentSpaceId");
    String currentSubSpaceId = (String)request.getAttribute("CurrentSubSpaceId");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script language="JavaScript1.2">
function viewSpace(){
    window.parent.startPageContent.location.href="StartPageInfo";
}
function changeSpace(spaceId){
    window.location.href="GoToSpace?Espace="+spaceId;
}
</SCRIPT>
<style type="text/css">
.component-icon {
	margin: 1px;
	vertical-align: middle;
}

#space-icon {
	vertical-align: middle;
}
</style>
</head>
<%
    if ((haveToRefreshMainPage != null) && (haveToRefreshMainPage.booleanValue()))
    {
%>
<body onload="javascript:viewSpace()" id="admin-treeview">
<%
    }
    else
    {
%>
<body id="admin-treeview">
<%
    }
%>
<form name="privateDomainsForm" action="jobStartPageNav">
<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr class="intfdcolor">
		<td>
			<span class="treeview-label"><%=resource.getString("GML.domains")%> : </span>
		</td>
</tr>
<tr class="intfdcolor51" >
    <td>
		<div class="treeview_selectSpace">
		<input name="privateSubDomain" type="hidden" />
			<img src="<%=resource.getIcon("JSPP.px")%>" height="20" width="0" align="middle" />
			<span class="selectNS"><select name="privateDomain" size=1 onchange="javascript:changeSpace(document.privateDomainsForm.privateDomain.value)">
			  <option value=""><%=resource.getString("JSPP.Choose")%></option>
			  <option value="">--------------------</option>
			  <%
					for(int nI = 0; nI < m_Spaces.length; nI++)
					{
						out.println(m_Spaces[nI].htmlLine);
					}
			%>
			</select></span>
	<%
		if (currentSpaceId != null)
		{
	%>
			<a href="javascript:changeSpace(<%=currentSpaceId%>)"><img id="space-icon" src="<%=resource.getIcon("JSPP.homeSpaceIcon")%>" align="middle" alt="<%=resource.getString("JSPP.BackToMainSpacePage")%>" title="<%=resource.getString("JSPP.BackToMainSpacePage")%>"/></a>
	<%
		}
		else
		{
	%>
			<a href="javascript:changeSpace('')"><img id="space-icon" src="<%=resource.getIcon("JSPP.homeSpaceIcon")%>" align="middle" alt="<%=resource.getString("JSPP.BackToMainSpacePage")%>" title="<%=resource.getString("JSPP.BackToMainSpacePage")%>"/></a>
	<%
		}
	%>
		</div>
	</td>
</tr>
<%
    if (currentSpaceId != null)
    {
%>
<tr class="intfdcolor51">
    <td>
		<div class="treeview_contentSpace">
          <%
                for(int nI = 0; nI < m_SubSpaces.length; nI++)
                {
                    out.println(m_SubSpaces[nI].htmlLine);
                    if ((currentSubSpaceId != null) && (currentSubSpaceId.equals(m_SubSpaces[nI].id)))
                    {
                        for(int nJ = 0; nJ < m_SubComponents.length; nJ++)
                        {
                            out.println(m_SubComponents[nJ].htmlLine);
                        }
                    }
                }
        %>
          <%
                for(int nI = 0; nI < m_Components.length; nI++)
                {
                    out.println(m_Components[nI].htmlLine);
                }
        %>
	</td>
</tr>
<%
    }
%>
</table>
</form>

<script language="javascript">
window.location.href="#<%=currentSubSpaceId%>";
</script>
</body>
</html>