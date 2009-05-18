<%@ include file="checkPersonalization.jsp" %>

<% 
    //Récupération des paramètres
    String action	= (String) request.getParameter("Action");
	String id		= (String) request.getParameter("id");
	String testExplanation = "";

    // Liste des adresses de notification pour ce user.
    ArrayList notifAddresses = null ; 

    //Mise a jour de l'espace
    if (action != null)
    {
        if (action.equals("test")) {
          personalizationScc.testNotifAddress(id) ;
		  if (id.equals("-10")) {
				testExplanation = resource.getString("TestPopUpExplanation");
		  } else if (id.equals("-12")) {
				testExplanation = resource.getString("TestSilverMailExplanation");
		  } else {
				testExplanation = resource.getString("TestSMTPExplanation");
		  }
          action = "NotificationView";
        }
        if (action.equals("setDefault")) {
          personalizationScc.setDefaultAddress(id) ;
          action = "NotificationView";
        }
        if (action.equals("delete"))  {
          personalizationScc.deleteNotifAddress(id) ;
          action = "NotificationView";
        }
    }
    else
        action = "NotificationView";
    notifAddresses = personalizationScc.getNotificationAddresses() ;
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script>
function editNotif(id){
	SP_openWindow("editNotification.jsp?id=" + id,"addNotif","600","250","scrollbars=yes");
}
function paramNotif(){
	SP_openWindow("paramNotif.jsp","paramNotif","750","400","scrollbars=yes");
}
function deleteCanal(id){
	if (window.confirm("<%=resource.getString("MessageSuppressionCanal")%>")) {
	   location.href = "personalization_Notification.jsp?id=" + id + "&Action=delete";
	}
}
</script>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(resource.getString("MesNotifications"));
	browseBar.setPath(resource.getString("ParametrerNotification"));
	
	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation(addProtocol, resource.getString("operationPane_addadress"), "javascript:editNotif(-1)");
	operationPane.addLine();
	operationPane.addOperation(paramNotif, resource.getString("operationPane_paramnotif"), "javascript:paramNotif()");
	out.println(window.printBefore());
	    
	//Onglets
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("LireNotification"), m_context + URLManager.getURL(URLManager.CMP_SILVERMAIL) + "Main", false);
	tabbedPane.addTab(resource.getString("ParametrerNotification"), "personalization_Notification.jsp?Action=LanguageView", true);
	out.println(tabbedPane.print());
	
	out.println(frame.printBefore());
%>	
<!-- AFFICHAGE HEADER -->
<CENTER>
<%
	if (testExplanation.length() > 0)
	{
			out.println("<font color=red><B>"+testExplanation+"<B></font><BR><BR>");
	}

    IconPane actions;

	// Arraypane notif
	ArrayPane notif = gef.getArrayPane("personalization", "personalization_Notification.jsp", request,session);
	ArrayColumn arrayColumn00 = notif.addArrayColumn(resource.getString("arrayPane_Default"));
	arrayColumn00.setSortable(false);
	ArrayColumn arrayColumn0 = notif.addArrayColumn(resource.getString("arrayPane_Nom"));
	arrayColumn0.setSortable(true);
	ArrayColumn arrayColumn3 = notif.addArrayColumn(resource.getString("arrayPane_Adresse"));
	arrayColumn3.setSortable(true);
	ArrayColumn arrayColumn4 = notif.addArrayColumn(resource.getString("arrayPane_Operations"));
	arrayColumn4.setSortable(false);
	
	Properties p = null;
	ArrayLine arrayLine = null;
	Icon def = null;
	for (int i=0 ; i<notifAddresses.size() ; i++) {
	
	  p = (Properties) notifAddresses.get(i) ;
	  arrayLine = notif.addArrayLine();

      // Ajout l'icone de default
      actions = gef.getIconPane();

      def = actions.addIcon();
      if (p.getProperty("isDefault").equalsIgnoreCase("true"))
      {
            def.setProperties(on_default, "" , "");
      }
      else
      {
            def.setProperties(off_default, resource.getString("iconPane_Default"), "personalization_Notification.jsp?id=" + p.getProperty("id") + "&Action=setDefault");
      }
      
      arrayLine.addArrayCellIconPane(actions);
	  arrayLine.addArrayCellText(Encode.javaStringToHtmlString(p.getProperty("name")));  
	  arrayLine.addArrayCellText(Encode.javaStringToHtmlString(p.getProperty("address")));
	  

      // Ajout des icones de modification et de suppression
      actions = gef.getIconPane();

      if (p.getProperty("canEdit").equalsIgnoreCase("true"))
      {
          Icon modifier = actions.addIcon();
          modifier.setProperties(modif, resource.getString("GML.modify") , "javascript:editNotif(" + p.getProperty("id") + ")");
      }
      else
      {
          Icon modifier = actions.addIcon();
          modifier.setProperties(ArrayPnoColorPix, "" , "");
      }

      if (p.getProperty("canDelete").equalsIgnoreCase("true"))
      {
          Icon del = actions.addIcon();
          del.setProperties(delete, resource.getString("GML.delete") , "javascript:deleteCanal('"+ p.getProperty("id") + "')");
      }
      else
      {
          Icon del = actions.addIcon();
          del.setProperties(ArrayPnoColorPix, "" , "");
      }

      if (p.getProperty("canTest").equalsIgnoreCase("true"))
      {
          Icon tst = actions.addIcon();
          tst.setProperties(test, resource.getString("iconPane_Test") , "personalization_Notification.jsp?id=" + p.getProperty("id") + "&Action=test");
      }
      else
      {
          Icon tst = actions.addIcon();
          tst.setProperties(ArrayPnoColorPix, "" , "");
      }

	  arrayLine.addArrayCellIconPane(actions);
	}
	
	out.println(notif.print());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	
</BODY>
</HTML>