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
<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
    <view:tab label="${wall}" action="ALL?userId=${id}" selected="${type=='ALL'}" />
    <view:tab label="${infos}" action="MyInfos?userId=${id}" selected="false" />
    <view:tab label="${events}" action="MyEvents?userId=${id}" selected="${type=='EVENT'}" />
     <view:tab label="${publications}" action="MyPubs?userId=${id}" selected="${type=='PUBLICATION'}" />
    <view:tab label="${photos}" action="MyPhotos?userId=${id}" selected="${type=='PHOTO'}" />
</view:tabs>
<div id="SocialInformations" class="SocialInformations">

</div>

<div id="socialIconsZone">
  <table cellpadding="10">
    <tr>
    <c:url var="SNIcon" value="/socialNetwork/jsp/icons/" />
    <c:choose>
      <c:when test="${type=='ALL'}">
        <td>
        <img src="${SNIcon}PUBLICATION_new.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.new.pub"/>
        </td>
        <td>
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.update.pub"/>
        </td>
        <td>
        <img src="${SNIcon}STATUS.gif" width="16" height="16" />  :<fmt:message key="profil.icon.staus"/>
        </td>
      </c:when>
      <c:when test="${type=='EVENT'}">
        <td>
        <img src="${SNIcon}EVENT_private.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.private.event"/>
        </td>
        <td>
        <img src="${SNIcon}EVENT_public.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.public.event"/>
        </td>
      </c:when>
      <c:when test="${type=='PUBLICATION'}">
        <td>
        <img src="${SNIcon}PUBLICATION_new.gif" align="bottom" width="16" height="16" />  :<fmt:message key="profil.icon.new.pub"/>
        </td>
        <td>
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.update.pub"/>
        </td>
      </c:when>
      <c:otherwise>

      </c:otherwise>
    </c:choose>
    </tr>
  </table>
</div>

<c:url var="urlServlet" value="/RmyContactJSON" />
<div id="getNext">

  <b><a href="#" style="color: blue" onclick="javascript:getNext('${urlServlet}','${id}','${type}');"><fmt:message
        key="profil.getNext"/></a></b>

</div>

<script type="text/javascript">
  getNext('${urlServlet}','${id}','${type}');
</script>
