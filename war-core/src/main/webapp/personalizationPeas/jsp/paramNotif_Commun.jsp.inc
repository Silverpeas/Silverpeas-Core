<%  ArrayList notifPreferences = personalizationScc.getNotifPreferences() ; %>

<script>
  function editPref(id){
      SP_openWindow("editPreference.jsp?id=" + id,"addPrefs","600","250","scrollable=yes");
  }
  function resizePopup(largeur,hauteur){
	window.resizeTo(largeur,hauteur);
  }
  function deleteRegle(id){
	if (window.confirm("<%=resource.getString("MessageSuppressionRegle")%>")) {
	   location.href = "paramNotif.jsp?id=" + id + "&Action=delete";
	}
  }
</script>

<CENTER>
<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%"><TR><TD NOWRAP>
	<span  class="txttitrecol"><%=resource.getString("default")%> :</span>
     &#149;&nbsp;<%=Encode.javaStringToHtmlString(personalizationScc.getDefaultAddress().getProperty("name"))%>&nbsp;&#149;
</TD></TR></TABLE>
<%
	// Arraypane notif
	ArrayPane notif = gef.getArrayPane("personalization", "paramNotif.jsp", request,session);
	notif.setVisibleLineNumber(10);
	ArrayColumn arrayColumn1 = notif.addArrayColumn(resource.getString("composant"));
	arrayColumn1.setSortable(true);
	ArrayColumn arrayColumn3 = notif.addArrayColumn(resource.getString("dest"));
	arrayColumn3.setSortable(true);
	ArrayColumn arrayColumn4 = notif.addArrayColumn(resource.getString("arrayPane_Operations"));
	arrayColumn4.setSortable(false);
	
	Properties p = null;
	ArrayLine arrayLine = null;
	IconPane actions = null;
	Icon modifier = null;
	Icon del = null;
	Icon tst = null;
	for (int i=0 ; i<notifPreferences.size() ; i++) {
	
	  p = (Properties) notifPreferences.get(i) ;
	  arrayLine = notif.addArrayLine();
	  arrayLine.addArrayCellText(Encode.javaStringToHtmlString(p.getProperty("component")));
	  arrayLine.addArrayCellText(Encode.javaStringToHtmlString(p.getProperty("notifAddress")));

      // Ajout des icones de modification et de suppression
      actions = gef.getIconPane();

      if (p.getProperty("canEdit").equalsIgnoreCase("true"))
      {
          modifier = actions.addIcon();
          modifier.setProperties(modif, resource.getString("GML.modify") , "javascript:editPref(" + p.getProperty("id") + ")");
      }
      else
      {
          modifier = actions.addIcon();
          modifier.setProperties(ArrayPnoColorPix, "" , "");
      }

      if (p.getProperty("canDelete").equalsIgnoreCase("true"))
      {
          del = actions.addIcon();
          del.setProperties(delete, resource.getString("GML.delete") , "javascript:deleteRegle('"+ p.getProperty("id") + "')");
      }
      else
      {
          del = actions.addIcon();
          del.setProperties(ArrayPnoColorPix, "" , "");
      }

      if (p.getProperty("canTest").equalsIgnoreCase("true"))
      {
          tst = actions.addIcon();
          tst.setProperties(test, resource.getString("iconPane_Test") , "paramNotif.jsp?id=" + p.getProperty("id") + "&Action=test");
      }
      else
      {
          tst = actions.addIcon();
          tst.setProperties(ArrayPnoColorPix, "" , "");
      }

	  arrayLine.addArrayCellIconPane(actions);
	}
%>

<%out.println(notif.print());%>
