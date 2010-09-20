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

<div id="photoAndInfo">
  <div id="profilHeadPhote">
    <img src="<c:url value="${snUserFull.profilPhoto}"/>" border="0" alt="viewUser" />
  </div>
  <div id="profilHeadinfo">
    <span class="username">${snUserFull.userFull.firstName} ${snUserFull.userFull.lastName}</span>
    <span class="connection">
	    <c:if test="${snUserFull.connected=='true'}">
	      <img src="<c:url value="/directory/jsp/icons/connected.jpg" />" border="1" width="10" height="10"
	            alt="connected"/> <fmt:message key="directory.connected"  /> ${snUserFull.duration}
	    </c:if>
	    <c:if test="${snUserFull.connected=='false'}">
	      <img src="<c:url value="/directory/jsp/icons/deconnected.jpg" />" border="1" width="10" height="10" alt="connected"/>
	    </c:if>
    </span>
  </div>
  <div id="profilHeadAction">
    <div class="StatusDiv" id="status"/>
    <script type="text/javascript">
      getLastStatus();
    </script>
  </div>
</div>

<div id="boxes">
  <div id="dialog" class="window">
    <div class="barre">
      <a href="#"class="close">Fermer</a>
    </div>    
    <br/>
    <view:board>
      <form name="photoForm" action="validateChangePhoto" Method="post" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
        <div>
          <div class="txtlibform"><fmt:message key="directory.photo" />:</div>
          <div><input type="file" name="WAIMGVAR0" size="60"/></div>
        </div>
      </form>
    </view:board>
    <div class="formButton">
       <br/>
       <br/>
        <fmt:message key="directory.buttonValid" var="valid"/>
        <view:button label="${valid}" action="javascript:document.photoForm.submit();" disabled="false" />
    </div>
  </div>
  <!-- Mask to cover the whole screen -->
  <div id="mask"></div>
</div>

