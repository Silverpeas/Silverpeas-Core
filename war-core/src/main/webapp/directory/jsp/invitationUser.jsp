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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.silverpeas.directory.model.Member"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"  />

<%
            ResourceLocator multilang = new ResourceLocator("com.silverpeas.directory.multilang.DirectoryBundle", request.getLocale());
            GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
            String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
            String action = request.getParameter("Action");
            String popupMode = request.getParameter("popupMode");

            Member user = (Member) request.getAttribute("User");
            String name = user.getUserDetail().getDisplayedName();

            if (action == null) {
                action = "NotificationView";
            }

            if (popupMode == null) {
                popupMode = "Yes";
            }

           
            if ((action.equals("SendInvitation") || action.equals("CancelSendInvitation")) && popupMode.equals("Yes")) {

%>


<HTML>
    <BODY
        onLoad="javascript:window.close()">
    </BODY>
</HTML>
<% } else {%>
<html>
    <head>
        <title><%= multilang.getString("userInvitation.titleprefix")+" "+ name+" "+multilang.getString("userInvitation.titleSuffix") %></title>
        <view:looknfeel />
    </head>
    <body onload="toggleZoneMessage();">
        <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
        <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
        <script language="JavaScript">
            function SubmitWithAction()
            {
                document.invitationSenderForm.action = "SendInvitation";
                document.invitationSenderForm.submit();
                window.close();
            }
        </script>
        <table border="0" width="100%">
            <tr>
                <td align="center">
                    <view:board>
                        <form name="invitationSenderForm" Action="" Method="POST">
                        <table>
                            <tr>
                                <td>
                                	<% if (!user.haveAvatar()) { %>
				                    	<img src="<%=m_context + user.getProfilPhoto()%>" alt="viewUser" class="defaultAvatar"/>
				                    <% } else { %>
				                    	<img src="<%=m_context + user.getProfilPhoto()%>" alt="viewUser" class="avatar"/>
				                    <% } %>
                                </td>
                                <td>
                                    <b><%=name%></b> <%=multilang.getString("userInvitation.remark") %><br/><br/>
                                    <%=multilang.getString("userInvitation.message")%> :<br/>
                                    <textarea name="txtMessage" cols="49" rows="6"></textarea>
                                </td>
                            </tr>
                            <input type="hidden" name="Recipient" value="<%=user.getId()%>"/>
                            </table>
                        </form>
                    </view:board>
                </td>
            </tr>
            <tr>
                <td align="center">
                        <div align="center">
                            <%
	                            ButtonPane buttonPane = gef.getButtonPane();
	                            buttonPane.addButton((Button) gef.getFormButton(multilang.getString("userInvitation.action.send"), "javascript:SubmitWithAction()", false));
	                            buttonPane.addButton((Button) gef.getFormButton(multilang.getString("userInvitation.action.cancel"), "javascript:window.close()", false));
	                            out.println(buttonPane.print());
                            %>
                        </div>
                </td>
            </tr>
        </table>
    </body>
</html>
<% }%>