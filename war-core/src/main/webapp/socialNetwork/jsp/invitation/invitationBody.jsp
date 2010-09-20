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


<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<view:tabs>
  <view:tab label="<%=multilang.getString("invitation.tab.receive")%>" action="Main" selected="true" />
  <view:tab label="<%=multilang.getString("invitation.tab.sent")%>" action="InvitationsSent" selected="false" />
</view:tabs>
<ol id="listBody">
  <%
      for (int i = 0; i < invitationUsers.size(); i++) {
        InvitationUser invitationUser = (InvitationUser) invitationUsers.get(i);
  %>
  <li style="margin: 3px; ">
	<view:board>
    <!--      <div id="body">-->
    <!--        <tr>-->
    <div id="photo">
      <a href="${urlProfil}<%=invitationUser.getUserDetail().getUserId()%>">
        <img src="<%=m_context + invitationUser.getUserDetail().getProfilPhoto()%>" border="0" alt="viewUser" class="avatar"/>
      </a>
    </div>
    <!--          <div id="information">-->
    <div id ="information" >
      <b>
        <a href="${urlProfil}<%=invitationUser.getUserDetail().getUserId()%>"
           class="link"><%=invitationUser.getUserDetail().getDisplayedName()%>
        </a>
      </b><br/>
      <b>
      <fmt:message key="invitation.dateInvitation" /></b> <%=DateUtil.getOutputDateAndHour(invitationUser.getInvitation().getInvitationDate(), "fr")%>
      <br/><br/>
      <%if (StringUtil.isDefined(invitationUser.getInvitation().getMessage())) { %>
      	<%=EncodeHelper.javaStringToHtmlParagraphe(invitationUser.getInvitation().getMessage())%>
      <% } %>
    </div>
    <div id="button">
      <div id="message">
        <b>  <a href="#" style="color: blue"><fmt:message key="userInvitation.sendMessage" /></a></b><br/><br/>
      </div>
      <%
              ButtonPane buttonPane = gef.getButtonPane();
              buttonPane.addButton((Button) gef.getFormButton(multilang.getString(
                  "invitation.action.confirme"), "AcceptInvitation?UserId=" + invitationUser.
                  getInvitation().getId(), false));
              buttonPane.addButton((Button) gef.getFormButton(multilang.getString(
                  "invitation.action.ignore"), "IgnoreInvitation?UserId=" + invitationUser.
                  getInvitation().getId(), false));

              out.println(buttonPane.print().replaceAll(
                  "width=\"100\"", " ").replaceAll("<table",
                  "<table align=\"right\" "));
      %>
    </div>
</li>
</view:board>
<%
    }

%>
</ol>
