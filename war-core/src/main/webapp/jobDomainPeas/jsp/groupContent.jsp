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
    "http://www.silverpeas.org/legal/licensing"

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
	Domain  domObject 				= (Domain)request.getAttribute("domainObject");
    Group 	grObject 				= (Group)request.getAttribute("groupObject");
    String 	groupsPath 				= (String)request.getAttribute("groupsPath");
    boolean isDomainRW 				= ((Boolean)request.getAttribute("isDomainRW")).booleanValue();
    boolean isDomainSync 			= ((Boolean)request.getAttribute("isDomainSync")).booleanValue();
    boolean isGroupManagerHere		= ((Boolean)request.getAttribute("isGroupManagerOnThisGroup")).booleanValue();
    boolean isGroupManager			= ((Boolean)request.getAttribute("isOnlyGroupManager")).booleanValue();
    boolean isGroupManagerDirectly	= ((Boolean)request.getAttribute("isGroupManagerDirectlyOnThisGroup")).booleanValue();    
    
    boolean showTabs		= false;
    
    String thisGroupId = grObject.getId();

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    if (groupsPath != null)
        browseBar.setPath(groupsPath);
    
    if (grObject.isSynchronized())
    {
    	//Group operations
        operationPane.addOperation(resource.getIcon("JDP.groupUpdate"),resource.getString("JDP.groupUpdate"),"displayGroupModify?Idgroup="+thisGroupId);
        operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("JDP.groupDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.groupDelConfirm")+"','groupDelete?Idgroup="+thisGroupId+"')");
        
        operationPane.addLine();
        operationPane.addOperation(resource.getIcon("JDP.groupSynchro"),resource.getString("JDP.groupSynchro"), "groupSynchro?Idgroup="+thisGroupId);
        
        showTabs = true;
    } 
    else if (isDomainRW) 
    {
    	if (!isGroupManager)
    	{
    		showTabs = true;
    		
	        // Group operations
	        operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
	        operationPane.addOperation(resource.getIcon("JDP.groupUpdate"),resource.getString("JDP.groupUpdate"),"displayGroupModify?Idgroup="+thisGroupId);
	        operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("JDP.groupDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.groupDelConfirm")+"','groupDelete?Idgroup="+thisGroupId+"')");
	
	        // User operations
			operationPane.addLine();
	        operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.userManage"),"displayAddRemoveUsers?Idgroup="+thisGroupId);
    	}
    	else if (isGroupManagerHere)
    	{
    		if (grObject.getSuperGroupId() == null)
    		{
    			//Group operations
    	        operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
    	        
    			//User operations
    			operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.userManage"),"displayAddRemoveUsers?Idgroup="+thisGroupId);
    		}
    		else
    		{
    			//Group operations
    	        operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
    	        operationPane.addOperation(resource.getIcon("JDP.groupUpdate"),resource.getString("JDP.groupUpdate"),"displayGroupModify?Idgroup="+thisGroupId);
    	        
    	        if (!isGroupManagerDirectly)
    	        	operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("JDP.groupDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.groupDelConfirm")+"','groupDelete?Idgroup="+thisGroupId+"')");

    	        // User operations
    			operationPane.addLine();
    	        operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.userManage"),"displayAddRemoveUsers?Idgroup="+thisGroupId);
    		}
    		
    		showTabs = true;
    	}
    }
    if (isDomainSync)
    {
        // Group operations
		operationPane.addLine();
        operationPane.addOperation(resource.getIcon("JDP.groupSynchro"),resource.getString("JDP.groupSynchro"),"groupSynchro?Idgroup="+thisGroupId);
        operationPane.addOperation(resource.getIcon("JDP.groupUnsynchro"),resource.getString("JDP.groupUnsynchro"),"groupUnSynchro?Idgroup="+thisGroupId);
    }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript">
function ConfirmAndSend(textToDisplay,targetURL)
{
    if (window.confirm(textToDisplay))
    {
        window.location.href = targetURL;
    }
}
</script>
</head>
<body>
<%
out.println(window.printBefore());
if (showTabs) {
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.description"), "groupContent?Idgroup="+thisGroupId, true);
	tabbedPane.addTab(resource.getString("JDP.roleManager"), "groupManagersView?Id="+thisGroupId, false);
	out.println(tabbedPane.print());
}
%>
<view:frame>
<view:board>
<table cellpadding="5" cellspacing="0" border="0" width="100%">
	<tr valign="baseline">
		<%
			String icon = resource.getIcon("JDP.group");
			if (grObject.isSynchronized())
				icon = resource.getIcon("JDP.groupSynchronized");
		%>
		<td><img src="<%=icon%>" alt="<%=resource.getString("GML.groupe") %>" title="<%=resource.getString("GML.groupe")%>" align="absmiddle"/></td>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.name")%> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(grObject.getName())%></td>
	</tr>
	<tr>			
	    <td></td>
		<td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.description") %> :</td>
		<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(grObject.getDescription())%></td>
	</tr>
	<% if (grObject.getRule() != null) { %>
	<tr>			
    	<td></td>
    	<td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("JDP.synchroRule") %> :</td>
    	<td align=left valign="baseline" width="100%"><%=EncodeHelper.javaStringToHtmlString(grObject.getRule())%></td>
    </tr>
    <% } %>
</table>
</view:board>
<view:areaOfOperationOfCreation/>
<%
	if (!grObject.isSynchronized()) {
		ArrayPane arrayPane = gef.getArrayPane("groupe", "groupContent.jsp", request, session);
		Group[] subGroups = (Group[])request.getAttribute("subGroups");
	
		arrayPane.setVisibleLineNumber(JobDomainSettings.m_GroupsByPage);
		arrayPane.setTitle(resource.getString("JDP.groups"));

		arrayPane.addArrayColumn("&nbsp;");
		arrayPane.addArrayColumn(resource.getString("GML.nom"));
		arrayPane.addArrayColumn(resource.getString("GML.users"));
		arrayPane.addArrayColumn(resource.getString("GML.description"));
		arrayPane.setSortable(false);

		if (subGroups != null) {
			Group group = null;
			for(int i=0; i<subGroups.length; i++){
				//cr�ation des ligne de l'arrayPane
				group = subGroups[i];
		    	if (group != null) {
					ArrayLine arrayLine = arrayPane.addArrayLine();
					IconPane iconPane1 = gef.getIconPane();
					Icon groupIcon = iconPane1.addIcon();
					if (group.isSynchronized())
						groupIcon.setProperties(resource.getIcon("JDP.groupSynchronized"), resource.getString("GML.groupe"), "");
			        else
			        	groupIcon.setProperties(resource.getIcon("JDP.group"), resource.getString("GML.groupe"), "");
					arrayLine.addArrayCellIconPane(iconPane1);
					arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(group.getName()), (String)request.getAttribute("myComponentURL")+"groupContent?Idgroup="+group.getId());
			        arrayLine.addArrayCellText(group.getNbUsers());
			        arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(group.getDescription()));
		    	}
			}
		} 	
		out.println(arrayPane.print());
	}

	out.println("<br/>");

  ArrayPane arrayPaneUser = gef.getArrayPane("users", "groupContent.jsp", request, session);
  String[][] subUsers = (String[][])request.getAttribute("subUsers");

  arrayPaneUser.setVisibleLineNumber(JobDomainSettings.m_UsersByPage);
  arrayPaneUser.setTitle(resource.getString("GML.users"));

  arrayPaneUser.addArrayColumn("&nbsp;");
  arrayPaneUser.addArrayColumn(resource.getString("GML.lastName"));
  arrayPaneUser.addArrayColumn(resource.getString("GML.surname"));
  arrayPaneUser.setSortable(false);

  if (subUsers != null) {
      for(int i=0; i<subUsers.length; i++){
          //cr�ation des ligne de l'arrayPane
          ArrayLine arrayLineUser = arrayPaneUser.addArrayLine();
          IconPane iconPane1User = gef.getIconPane();
          Icon userIcon = iconPane1User.addIcon();
          userIcon.setProperties(resource.getIcon("JDP.user"), resource.getString("GML.user"), "");
          arrayLineUser.addArrayCellIconPane(iconPane1User);
          arrayLineUser.addArrayCellLink(subUsers[i][1], (String)request.getAttribute("myComponentURL") + "userContent?Iduser=" + subUsers[i][0]);
          arrayLineUser.addArrayCellText(subUsers[i][2]);
        }
  } 	
  out.println(arrayPaneUser.print());
%>
</view:frame>
<% out.println(window.printAfter()); %>
</body>
</html>