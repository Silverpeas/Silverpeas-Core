<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    Group 	grObject 				= (Group)request.getAttribute("groupObject");
    String 	groupsPath 				= (String)request.getAttribute("groupsPath");
    boolean isDomainRW 				= ((Boolean)request.getAttribute("isDomainRW")).booleanValue();
    boolean isDomainSync 			= ((Boolean)request.getAttribute("isDomainSync")).booleanValue();
    boolean isGroupManagerHere		= ((Boolean)request.getAttribute("isGroupManagerOnThisGroup")).booleanValue();
    boolean isGroupManager			= ((Boolean)request.getAttribute("isOnlyGroupManager")).booleanValue();
    boolean isGroupManagerDirectly	= ((Boolean)request.getAttribute("isGroupManagerDirectlyOnThisGroup")).booleanValue();    
    
    boolean showTabs		= false;
    
    String thisGroupId = grObject.getId();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));

    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
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
	        operationPane.addOperation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
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
    	        operationPane.addOperation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
    	        
    			//User operations
    			operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.userManage"),"displayAddRemoveUsers?Idgroup="+thisGroupId);
    		}
    		else
    		{
    			//Group operations
    	        operationPane.addOperation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
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
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script language="JavaScript">
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

if (showTabs)
{
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.description"), "groupContent?Idgroup="+thisGroupId, true);
	tabbedPane.addTab(resource.getString("JDP.roleManager"), "groupManagersView?Id="+thisGroupId, false);
	out.println(tabbedPane.print());
}

out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr valign="baseline">
		<%
			String icon = resource.getIcon("JDP.group");
			if (grObject.isSynchronized())
				icon = resource.getIcon("JDP.groupSynchronized");
		%>
		<td><img src="<%=icon%>" alt="<%=resource.getString("GML.groupe") %>" title="<%=resource.getString("GML.groupe")%>" align="absmiddle"></td>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.name")%> :</td>
		<td align=left valign="baseline" width="100%"><%=Encode.javaStringToHtmlString(grObject.getName())%></td>
	</tr>
	<tr>			
	    <td></td>
		<td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.description") %> :</td>
		<td align=left valign="baseline" width="100%"><%=Encode.javaStringToHtmlString(grObject.getDescription())%></td>
	</tr>
	<% if (grObject.getRule() != null) { %>
	<tr>			
    	<td></td>
    	<td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("JDP.synchroRule") %> :</td>
    	<td align=left valign="baseline" width="100%"><%=Encode.javaStringToHtmlString(grObject.getRule())%></td>
    </tr>
    <% } %>
</table>
<%
out.println(board.printAfter());
%>
<br>	
<%
    boolean[] pageNavigation = (boolean[])request.getAttribute("pageNavigation");
	boolean toPrintBackGroup = pageNavigation[0];
	boolean toPrintNextGroup = pageNavigation[1];
	boolean toPrintBackUser = pageNavigation[2];
	boolean toPrintNextUser = pageNavigation[3];

    if (toPrintBackGroup || toPrintNextGroup)
    {
%>
	<table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
		<tr align="center" class="buttonColorDark">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
		<tr align="center" class="intfdcolor4">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
		<tr align="center"> 
			<td class="ArrayNavigation" height="20">
		<%
		if(toPrintBackGroup)
			out.println("<a href=\""+(String)request.getAttribute("myComponentURL")+"groupToBackGroup\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
		if(toPrintNextGroup)
			out.println("<a href=\""+(String)request.getAttribute("myComponentURL")+"groupToNextGroup\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
		%>	
			</td>
		</tr>
		<tr align="center" class="buttonColorDark">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
		<tr align="center" class="intfdcolor1">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
	</table>
<%
    }

	if (!grObject.isSynchronized())
	{
		ArrayPane arrayPane = gef.getArrayPane("groupe", "groupContent.jsp", request, session);
		Group[] subGroups = (Group[])request.getAttribute("subGroups");
	
		arrayPane.setVisibleLineNumber(-1);
		//arrayPane.setTitle(resource.getString("JDP.groups"));

		arrayPane.addArrayColumn("&nbsp;");
		arrayPane.addArrayColumn(resource.getString("GML.nom"));
		arrayPane.addArrayColumn(resource.getString("GML.users"));
		arrayPane.addArrayColumn(resource.getString("GML.description"));
		arrayPane.setSortable(false);

		if (subGroups != null)
		{
			Group group = null;
			for(int i=0; i<subGroups.length; i++){
				//création des ligne de l'arrayPane
				group = subGroups[i];
		    	if (group != null)
		    	{
					ArrayLine arrayLine = arrayPane.addArrayLine();
					IconPane iconPane1 = gef.getIconPane();
					Icon groupIcon = iconPane1.addIcon();
					if (group.isSynchronized())
						groupIcon.setProperties(resource.getIcon("JDP.groupSynchronized"), resource.getString("GML.groupe"), "");
			        else
			        	groupIcon.setProperties(resource.getIcon("JDP.group"), resource.getString("GML.groupe"), "");
					arrayLine.addArrayCellIconPane(iconPane1);
					arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(group.getName()), (String)request.getAttribute("myComponentURL")+"groupContent?Idgroup="+group.getId());
			        arrayLine.addArrayCellText(group.getNbUsers());
			        arrayLine.addArrayCellText(Encode.javaStringToHtmlString(group.getDescription()));
		    	}
			}
		} 	
		out.println(arrayPane.print());
	}

	out.println("<BR/>");
	
	if (toPrintBackUser || toPrintNextUser)
    {
%>
	<table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
		<tr align="center" class="buttonColorDark">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
		<tr align="center" class="intfdcolor4">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
		<tr align="center"> 
			<td class="ArrayNavigation" height="20">
		<%
		if(toPrintBackUser)
			out.println("<a href=\""+(String)request.getAttribute("myComponentURL")+"groupToBackUser\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
		if(toPrintNextUser)
			out.println("<a href=\""+(String)request.getAttribute("myComponentURL")+"groupToNextUser\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
		%>	
			</td>
		</tr>
		<tr align="center" class="buttonColorDark">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
		<tr align="center" class="intfdcolor1">
			<td><img src="<%=resource.getIcon("JDP.px")%>" width="1" height="1"></td>
		</tr>
	</table>
<%
    }

  ArrayPane arrayPaneUser = gef.getArrayPane("groupe", "groupContent.jsp", request, session);
  String[][] subUsers = (String[][])request.getAttribute("subUsers");

  arrayPaneUser.setVisibleLineNumber(-1);
//  arrayPaneUser.setTitle(resource.getString("GML.users"));

  arrayPaneUser.addArrayColumn("&nbsp;");
  arrayPaneUser.addArrayColumn(resource.getString("GML.lastName"));
  arrayPaneUser.addArrayColumn(resource.getString("GML.surname"));
//  arrayPaneUser.addArrayColumn(resource.getString("GML.operation"));
  arrayPaneUser.setSortable(false);

  if (subUsers != null)
  {
      for(int i=0; i<subUsers.length; i++){
          //création des ligne de l'arrayPane
          ArrayLine arrayLineUser = arrayPaneUser.addArrayLine();
          IconPane iconPane1User = gef.getIconPane();
          Icon userIcon = iconPane1User.addIcon();
          userIcon.setProperties(resource.getIcon("JDP.user"), resource.getString("GML.user"), "");
          arrayLineUser.addArrayCellIconPane(iconPane1User);
          arrayLineUser.addArrayCellLink(subUsers[i][1], (String)request.getAttribute("myComponentURL") + "userContent?Iduser=" + subUsers[i][0]);
          arrayLineUser.addArrayCellText(subUsers[i][2]);
//          arrayLineUser.addArrayCellText("<input type=checkbox name=UserChecked value='"+subUsers[i][0]+"'>");
        }
  } 	
  out.println(arrayPaneUser.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>