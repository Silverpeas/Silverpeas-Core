<%@ include file="check.jsp" %>
<% 
Collection 				subscribeThemes 	= (Collection) request.getAttribute("SubscribeThemeList");
String 					userId 				= (String) request.getAttribute("userId");
String 					action				= (String) request.getAttribute("action");

OrganizationController 	organizationCtrl 	= sessionController.getOrganizationController();
final String 			rootPath			= resource.getString("Path");

boolean isReadOnly = false;
if ( action != null && action.equals("showUserSubscriptions")) 
{
    isReadOnly = true;
}

String language = resource.getLanguage();
%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function deleteSelectThemeConfirm() 
{
	var boxItems = document.readForm.themeCheck;
	if (boxItems != null)
	{
		var nbBox = boxItems.length;
		var sendIt = false;
        if ((nbBox == null) && (boxItems.checked == true)){
        	sendIt = true;
        } else{
        	for (i=0;i<boxItems.length ;i++ ){
            	if (boxItems[i].checked == true){
            		sendIt = true;
            	}
            }
        }
        
		if (sendIt && areYouSure())
		{
	    	document.readForm.mode.value = 'delete';
	    	document.readForm.submit();
	  	}
	}
}

function areYouSure()
{
    return confirm("<%=resource.getString("confirmDeleteSubscription")%>");
}

</script>
</head>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<form name="readForm" action="DeleteTheme" method="POST">
<input type="hidden" name="mode">

<%
	browseBar.setComponentName(rootPath);
	
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdc"), "subscriptionList.jsp?userId="+userId, false);
	tabbedPane.addTab(resource.getString("thematique"), "#", true);

	if (!isReadOnly) 
		operationPane.addOperation(resource.getIcon("icoDelete") , resource.getString("DeleteSC"),"javascript:deleteSelectThemeConfirm()");
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());

	ArrayPane arrayPane = gef.getArrayPane("ViewSubscriptionTheme", "ViewSubscriptionTheme", request, session);
	arrayPane.addArrayColumn(resource.getString("emplacement"));
	if (!isReadOnly) 
	{
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("Operations"));
		columnOp.setSortable(false);
	}
		
	// remplissage de l'ArrayPane avec les abonnements
	if (subscribeThemes != null && subscribeThemes.size() != 0) 
	{
		Iterator it = (Iterator) subscribeThemes.iterator();
		while (it.hasNext()) 
		{
			Collection path = (Collection) it.next();
			Iterator j = path.iterator();
			String rootName = "";
			String link = "";
			String delete = "";
			String spaceId = null;
            String componentId = null;
            ComponentInstLight componentInst = null;
			ArrayLine line = arrayPane.addArrayLine();
			while (j.hasNext())
			{
				NodeDetail node = (NodeDetail) j.next();
				String name = "";
				if (link.equals(""))
				{
					link = node.getLink();
					delete = node.getNodePK().getId() + "-" + node.getNodePK().getComponentName();
				}
				if (node.getNodePK().getId().equals("0"))
				{
					// on est à la racine, on recherche le nom de l'espace et de l'instance du composant
					componentId = node.getNodePK().getComponentName();
					if (componentInst == null)
	              		componentInst = organizationCtrl.getComponentInstLight(componentId);
					SpaceInstLight spaceInst = organizationCtrl.getSpaceInstLightById(componentInst.getDomainFatherId());
					name = spaceInst.getName() + " > " + componentInst.getLabel();
				}
				else 
					name = node.getName(language);
				if (rootName.length() == 0)
					rootName = name;
				else
					rootName = name + " > " + rootName;
			}
			if (!isReadOnly)
				line.addArrayCellLink(rootName,link);
			else
				line.addArrayCellText(rootName);
			IconPane iconPane = gef.getIconPane();
			if (!isReadOnly) 
        		line.addArrayCellText("&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"themeCheck\" value=\""+ delete +"\">");
		}
	}
			
	out.println(arrayPane.print());
  
  	out.println(frame.printAfter());
	out.println(window.printAfter());
  %>

</form>
<form name="subscribeThemeForm" action="" Method="POST">
	<input type="hidden" name="Id">
</form>
</body>
</html>