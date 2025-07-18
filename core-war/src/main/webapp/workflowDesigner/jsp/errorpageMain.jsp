<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
    if (!response.isCommitted())
        response.resetBuffer();
%>
<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the explicit 'exception' object.
--%>

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.web.mvc.util.HomePageUtil"%>
<%@ page import="org.silverpeas.kernel.bundle.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.exception.SilverpeasException"%>

<%@ include file="check.jsp" %>

<%
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    String language = m_MainSessionCtrl.getFavoriteLanguage();
    LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);
	Exception exception = (Exception) request.getAttribute("javax.servlet.jsp.jspException");
	Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
	String exStr = HomePageUtil.getMessageToDisplay(exception , language);
	String detailedString = null;

    if ( exception instanceof SilverpeasException )
        detailedString = ( (SilverpeasException)exception ).getExtraInfos();
%>
<view:sp-page>
<view:sp-head-part>
<TITLE><%= generalMessage.getString("GML.popupTitle")%></TITLE>
</view:sp-head-part>

<view:sp-body-part>
<%
					out.println(window.printBefore());
					out.println(frame.printBefore());
%>
<div class="center">
<table class="intfdcolor">
    <tr><th></th></tr>
	<tr>
		<td class="intfdcolor4 nowrap">
			<div class="center">
				<br>
				<span class="txtnav">
					<% if (exStr != null)
                       {
                           out.print(exStr);
                       }

                       if ( detailedString != null )
                       {
                           out.println("<br>");
                           out.println(detailedString);
                       }
                    %>
				</span>
			</div>
			<br>
		</td>
	</tr>
	<!--  /form -->
</table>
<br>
<%
	      ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.back"), "javascript:history.go(-1);", false));
          out.println(buttonPane.print());
%>
</div>
<%
                out.println(frame.printAfter());
                out.println(window.printAfter());
%>
</view:sp-body-part>
</view:sp-page>