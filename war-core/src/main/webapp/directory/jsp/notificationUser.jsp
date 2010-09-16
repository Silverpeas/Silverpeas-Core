<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager" %>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"  />
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML" />



<%

    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        "SessionGraphicElementFactory");
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    String action = (String) request.getParameter("Action");
    String txtTitle = Encode.htmlStringToJavaString((String) request.getAttribute("txtTitle"));
    String txtMessage = Encode.htmlStringToJavaString((String) request.getAttribute("txtMessage"));
    String popupMode = (String) request.getParameter("popupMode");
    String editTargets = (String) request.getParameter("editTargets");
    String recipient = Encode.htmlStringToJavaString((String) request.getParameter("Recipient"));
    String name = ((String) request.getParameter("Name"));
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

    if (action == null) {
      action = "NotificationView";
    }
    if (txtTitle == null) {
      txtTitle = "123";
    }
    if (txtMessage == null) {
      txtMessage = "123";
    }
    if (popupMode == null) {
      popupMode = "Yes";
    }
    if (editTargets == null) {
      editTargets = "No";
    }
    System.out.println(
        "action=" + action + "===========popupMode=" + popupMode + "====recipient=" + recipient);
    if ((action.equals("SendMessage") || action.equals("CancelSendMessage")) && popupMode.equals(
        "Yes")) {

%>
<HTML>
  <BODY
    onLoad="javascript:window.close()">
  </BODY>
</HTML>
<% } else {%>
<html>
  <head>

    <title><fmt:message key="notificationUser.title" /> <%= " " + name%> </title>
    <view:looknfeel />

  </head>
  <body onLoad="document.notificationSenderForm.txtTitle.focus();">
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript">

      function Submit(){

        SP_openUserPanel('about:blank', 'OpenUserPanel', 'menubar=no,scrollbars=no,statusbar=no');

        //document.notificationSenderForm.action = "<%=m_context + URLManager.getURL(URLManager.CMP_NOTIFICATIONUSER)%>"+"SetTarget";
        document.notificationSenderForm.target = "OpenUserPanel";
        document.notificationSenderForm.submit();
      }
      function ClosePopup(){
        window.close()
      }
      function OpenPopup(){
        SP_openWindow('<%=m_context + "/directory/jsp/notificationUser.jsp"%>', 'strWindowName', '400', '400', 'true');
                 
                 
      }

      function SubmitWithAction(action,verifParams)
      {
        var title = stripInitialWhitespace(document.notificationSenderForm.txtTitle.value);
        var errorMsg = "";
        //window.alert("txtMessage="+document.notificationSenderForm.txtMessage.value+ " txtTitle="+document.notificationSenderForm.txtTitle.value);

        if (verifParams)
        {
          if (isWhitespace(title))
            errorMsg = "<fmt:message key="GML.thefield" bundle="${GML}"/>"+ " <fmt:message key="notificationUser.object" />"+ " <fmt:message key="GML.isRequired" bundle="${GML}"/>";
        }
        if (errorMsg == "")
        {
          document.notificationSenderForm.action = action;
          document.notificationSenderForm.submit();
        }
        else
        {
          window.alert(errorMsg);
        }
      }


    </script>


    <center>


      <view:board>
        <form name="notificationSenderForm" Action=""  Method="post">

          <tr>
            <td valign="baseline" align=left  class="txtlibform">
              <fmt:message key="notificationUser.object" /> :
            </td>
            <td align=left valign="baseline">
              <input type="text" name="txtTitle" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" size="50" value="">
              <img border="0" src="<%=mandatoryField%>" width="5" height="5" alt="mandatoryField" />
            </td>
          </tr>

          <tr>
            <td class="txtlibform" valign="top">
              <fmt:message key="notificationUser.message" /> :
            </td>
            <td align=left valign="top" class="txtnav">
              <textarea type="text" name="txtMessage"  value="" cols="49" rows="4">

              </textarea>
            </td>
          </tr>
          <tr>
            <td colspan="2">
	    (<img border="0" src="<%=mandatoryField%>" width="5" height="5" alt="mandatoryField" /> <fmt:message key="GML.requiredField" bundle="${GML}"/>)
            </td>
          </tr>
          <input type="hidden" name="Recipient" value="<%=recipient%>">


        </form>
        <div align="center">
          <%
                        ButtonPane buttonPane = gef.getButtonPane();
                        buttonPane.addButton((Button) gef.getFormButton("Envoyer",
                            "javascript:SubmitWithAction('SendMessage',true)", false));
                        buttonPane.addButton((Button) gef.getFormButton("Cancel",
                            "javascript:SubmitWithAction('CancelSendMessage',false)",
                            false));
                        out.println(buttonPane.print());
          %>
        </div>
      </view:board>

    </center>

  </body>
</html>
<% }%>
