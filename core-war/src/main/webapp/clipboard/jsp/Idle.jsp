<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.web.clipboard.control.ClipboardSessionController" %>

<%@ page import="java.util.Enumeration" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp" %>

<%
  ClipboardSessionController clipboardSC =
      (ClipboardSessionController) request.getAttribute("clipboardScc");
  String javascripTask = "";
  if (clipboardSC != null) {
    clipboardSC.doIdle(Integer.parseInt(clipboardSC.getIntervalInSec()));
    javascripTask = clipboardSC.getJavaScriptTaskForHiddenFrame(request);
  }

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <%if(StringUtil.isDefined(javascripTask)){%>
  <view:includePlugin name="jquery"/>
  <view:includePlugin name="tkn"/>
  <%}%>
  <view:script src="/util/javaScript/silverpeas.js"/>
  <script type="text/javascript">
    var counter = 0;
    <%
       if (clipboardSC != null)
       {
            out.println("var interval = " + clipboardSC.getIntervalInSec() + ";");
       }
       else
       {
            out.println("var interval = 5;");
       }
    %>

    // call Update function in 1 second after first load
    ID = window.setTimeout("DoIdle(" + interval + ");", interval * 1000);

    //--------------------------------------------------------------------------------------DoIdle
    // Idle function
    function DoIdle() {
      counter++;
      self.location.href = "../../Rclipboard/jsp/Idle.jsp?message=IDLE";
    }

    //--------------------------------------------------------------------------------------DoTask
    // Do taks javascript function
    function DoTask() {
      <%
      if (clipboardSC != null) {
        String MessageError = clipboardSC.getMessageError();
        if (MessageError != null) {
          out.println("alert ('" + MessageError + "')");
        }
        out.println(javascripTask);
      }
      %>
    }

    //--------------------------------------------------------------------------------------test
    // Developer test
    function test() {
      //window.alert ('clipboardName='+top.ClipboardWindow.name);
      status = top.ClipboardWindow.document.pasteform.compR.value;
    }
  </script>
</head>
<body onload="DoTask();">
<pre>
Frame cachee, Time = <%if (clipboardSC != null) {out.print(String.valueOf(clipboardSC.getCounter()));}%> <a href="../../Rclipboard/jsp/Idle.jsp?message=IDLE">idle...</a>
<%
  Enumeration values = request.getParameterNames();
  String sep = "";
  while (values.hasMoreElements()) {
    String name = (String) values.nextElement();
    if (name != null) {
      String value = Encode.forHtml(request.getParameter(name));
      if (name.compareTo("submit") != 0) {
        if (value != null) {
          out.print(sep + name + "=" + value);
        } else {
          out.print(sep + name + "=null");
        }
        sep = "&";
      }
    }
  }
%>
	<a href="javascript:onClick=test()">test...</a>
	</pre>
<%if (clipboardSC != null) {
  out.println(clipboardSC.getHTMLFormForHiddenFrame(request));
}
%>
<!-- SessionId pour securisation pages Web -->
<form name="ctrl" action="">
  <input type="hidden" name="sessionId" value="<%=session.getId()%>"/>
</form>
</body>
</html>