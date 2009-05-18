<%@ include file="checkPersonalization.jsp" %>

<%
  String action ;
  String id ;
  String componentId = "";
  String notificationId = "";
  id = request.getParameter("id") ;
  action = (String) request.getParameter("Action");

  if ((id == null) || (id.equalsIgnoreCase("-1"))) {
    id = "" ;
  }

  if (action == null) {
    action = "NotificationView";
  }
  
  if (action.equals("addPref"))
  {
        componentId = request.getParameter("componentId");
        notificationId = request.getParameter("notificationId");
        if ((componentId != null) && (componentId.length() > 0) && (notificationId != null) && (notificationId.length() > 0))
        {
            if (id.length() > 0) // Edit mode and not create mode -> remove the previous one before
            {
                personalizationScc.deletePreference(id);
            }
            personalizationScc.addPreference(componentId,null,notificationId);
        }
        %>
        <HTML>
          <HEAD>
          <script language="Javascript">
            function closeAndReplace() {
              window.opener.location.href = "paramNotif.jsp";
              window.close();
            }
          </script>
          </HEAD>
          <BODY onLoad="closeAndReplace()">
          </BODY>
        </HTML>
        <%
  }
  else 
  {
        if (id != "")
        {
            Properties p = personalizationScc.getNotifPreference(id) ;
            componentId = p.getProperty("componentId");
            notificationId = p.getProperty("notifAddressId");
        }
  }
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<script>
  function validate() {
        document.personalizationForm.submit() ;
  }
</script>

</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
    browseBar.setComponentName(resource.getString("MesNotifications"));
    browseBar.setPath(resource.getString("browseBar_Path4"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>

<CENTER>
      <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
<form name="personalizationForm" Action="editPreference.jsp?Action=addPref&id=<%=id%>" Method="POST">        
        <input type="hidden" name="id" value="<%=id%>">
        <tr>
          <td class="txtlibform">
            <%=resource.getString("GML.jobPeas")%> :
          </td>
          <td align=left valign="baseline" class="txtnav">
              <select name="componentId">
                   <% out.println(personalizationScc.buildOptions(personalizationScc.getInstanceList(), componentId, null)); %>
              </select>
          </td>
        </tr>
        
        <tr>			
          <td valign="baseline" align=left  class="txtlibform">
            <%=resource.getString("dest")%> :
          </td>
          <td align=left valign="baseline">
              <select name="notificationId">
                   <% out.println(personalizationScc.buildOptions(personalizationScc.getNotificationAddresses(), notificationId, null)); %>
              </select>
          </td>
        </tr>
</form>        
      </table>
<BR>
<%
	out.println(board.printAfter());
	
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=validate();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close();", false));
    out.println("<BR><center>"+buttonPane.print()+"</center>");
%>
</CENTER>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>