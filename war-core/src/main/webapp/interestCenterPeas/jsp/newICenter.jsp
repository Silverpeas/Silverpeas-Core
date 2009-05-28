<%@ include file="checkICenter.jsp" %>
<%
//Récupération des paramètres
String action	= (String) request.getAttribute("action");
String icName	= (String)request.getAttribute("icName"); //End View
icName=(icName==null)?"":icName;

Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button okButton		= (Button) gef.getFormButton(resource.getString("GML.ok"), "javascript:onClick=sendData()", false);
%>
  <HTML>
    <HEAD>
    <TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <%
        out.println(gef.getLookStyleSheet());
    %>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>

    <script LANGUAGE="JavaScript" TYPE="text/javascript">
    function onLoad() {
     // this mode is for opening confirmation window
     <% if ("needConfirm".equals(action))  {%>
          url = "usedICenter.jsp";
          width = "400";
          height = "200";
          SP_openWindow(url,"usedICenter",width,height,"");
       <%} else {%>
        // this mode is for opening edit request name form
          document.icForm.icName.focus();
       <%}%>
    }

    // fucntion submits data to server in order to check is there already request with the same
    function sendData() {
      if (document.icForm.icName.value != "") {
        document.icForm.action.value = "check";
        document.icForm.submit();
      } else {
        alert('<%=resource.getString("EmtyICName")%>');
        document.icForm.icName.focus();
      }
    }
     // closes current window and submits AdvancedSearch form on parent window
      function closeAndReplace() {
        window.opener.document.AdvancedSearch.mode.value = "SaveRequest";
        window.opener.document.AdvancedSearch.requestName.value = "<%=Encode.javaStringToHtmlString(icName)%>";
        window.opener.document.AdvancedSearch.submit();
        window.close();
      }
  </script>
  </HEAD>

<%  // just close window and submit AdvancedSearch form
    if ("save".equals(action)) { %>
		<BODY onLoad="closeAndReplace()">
		</BODY>
		</HTML>
<% } else {
// open window for editing request name or for opening confirmation window
%>
    <BODY onLoad="onLoad()">
    <%
        out.println(window.printBefore());
        out.println(frame.printBefore());
		out.println(board.printBefore());
    %>
	<TABLE>
	<FORM NAME="icForm" action="<%=m_context%><%=URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS)%>newICenter.jsp">
	 <tr>
		<td nowrap align="left" class="txtlibform"><%=resource.getString("RequestName")%> :</td>
		<td align="left"><input type="text" name="icName" size="60" value="<%=icName%>"><input type="hidden" name="action"></td>
	 </tr>
	 </FORM>
	 </TABLE>
    <%
		out.println(board.printAfter());

        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(okButton);
        buttonPane.addButton(cancelButton);

		out.println("<BR><center>");
        out.println(buttonPane.print());
		out.println("</center>");

        out.println(frame.printAfter());
        out.println(window.printAfter());
    %>
    </BODY>
    </HTML>
<% } %>