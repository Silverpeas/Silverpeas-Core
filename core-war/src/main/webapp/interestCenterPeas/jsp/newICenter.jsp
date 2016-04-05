<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkICenter.jsp" %>
<%
String action	= (String) request.getAttribute("action");
String icName	= (String)request.getAttribute("icName"); //End View
icName=(icName==null)?"":icName;

Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button okButton		= (Button) gef.getFormButton(resource.getString("GML.ok"), "javascript:onClick=sendData()", false);
%>
  <HTML>
    <HEAD>
    <TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
    <view:looknfeel withCheckFormScript="true"/>
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
        window.opener.document.AdvancedSearch.requestName.value = "<%=EncodeHelper.javaStringToHtmlString(icName)%>";
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
	<FORM NAME="icForm" action="<%=m_context%><%=URLUtil.getURL(URLUtil.CMP_INTERESTCENTERPEAS)%>newICenter.jsp">
	 <tr>
		<td nowrap align="left" class="txtlibform"><%=resource.getString("RequestName")%> :</td>
    <td align="left"><input type="text" name="icName" size="60" value="<%=EncodeHelper.javaStringToHtmlString(icName)%>"><input type="hidden" name="action"></td>
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