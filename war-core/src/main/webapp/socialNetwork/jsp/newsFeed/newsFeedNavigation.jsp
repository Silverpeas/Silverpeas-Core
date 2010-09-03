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
<%@ include file="check.jsp" %>

<div class="box" >
  <ul>
    <li>
      <div id="myInfo">
        <a href="${urlProfil}${id}">
          <span>${user.lastName} ${user.firstName}</span>
        </a>
      </div>
      <a href="${urlProfil}${id}">
        <img src="<c:url value="${user.profilPhoto}" />" width="70" height="80" alt="viewUser" />
      </a>
    </li>

  </ul>

</div>
<div class="box" id="actionNewsFeed">
  <ul>
    <li class="ItemOn">
      <a href="#" name="ALL"><span>${newsFeed}</span></a>
    </li>
    <li>
      <a href="#" name="PUBLICATION"><span>${publications}</span></a>
    </li>
    <li>
      <a href="#" name="EVENT"> <span>${events}</span></a>
    </li>
    <li>
      <a href="#" name="PHOTO"><span>${photos}</span></a>
    </li>
    <li>
      <a href="#" name="RELATIONSHIP"><span>${relations}</span></a>
    </li>
  </ul>

</div>
<div class="box" id="relationshipActionNewsFeed">
  <ul>
    <li>
      <a href="#" onclick="javascript:getAllUsersDirectory()"><span><fmt:message key="newsFeed.directory" /></span></a>
    </li>
    <li>
      <a href="#" onclick="javascript:getAllContactsDirectory()"><span><fmt:message key="newsFeed.contacts" /></span></a>
    </li>
    <li >
      <a href="${urlInvitationReceived}"><span><fmt:message key="invitation.tab.receive" /></span></a>
    </li>
    <li>
      <a href="${urlInvitationSent}"><span><fmt:message key="invitation.tab.sent" /></span></a>
    </li>



  </ul>

</div>
