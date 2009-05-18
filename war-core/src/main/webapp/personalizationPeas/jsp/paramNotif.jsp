<%@ include file="checkPersonalization.jsp" %>

<%
  String action;
  String componentId = "";
  String notificationId = "";

  action = (String) request.getParameter("Action");

  if (action == null) {
    action = "NotificationView";
  }
  if (action.equals("addPref"))
  {
    componentId = request.getParameter("componentId");
    notificationId = request.getParameter("notificationId");
    if ((componentId != null) && (componentId.length() > 0) && (notificationId != null) && (notificationId.length() > 0))
    {
        personalizationScc.addPreference(componentId,null,notificationId);
    }
    action = "NotificationView";
  }
  if (action.equals("delete"))
  {
    String id = request.getParameter("id");
    if ((id != null) && (id.length() > 0))
    {
        personalizationScc.deletePreference(id);
    }
    action = "NotificationView";
  }
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</HEAD>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5  onload="javascript:resizePopup(750,430);">
<%
    browseBar.setComponentName(resource.getString("PersonalizationTitleTab1"));
    browseBar.setPath(resource.getString("browseBar_Path3"));

	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation(addGuideline, resource.getString("operationPane_addguideline"), "paramNotif2.jsp");
	
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>

<!-- Add commun code that display the Rules list -->
<%@ include file="paramNotif_Commun.jsp.inc" %>

<br>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close()", false));
	out.print(buttonPane.print());
%>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</CENTER>
</BODY>
</HTML>