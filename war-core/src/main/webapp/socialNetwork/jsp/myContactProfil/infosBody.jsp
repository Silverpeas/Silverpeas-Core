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
<view:tabs>
  <view:tab label="${wall}" action="ALL?userId=${id}" selected="false" />
  <view:tab label="${infos}" action="MyInfos?userId=${id}" selected="true" />
  <view:tab label="${events}" action="MyEvents?userId=${id}" selected="false" />
  <view:tab label="${publications}" action="MyPubs?userId=${id}" selected="false" />
  <view:tab label="${photos}" action="MyPhotos?userId=${id}" selected="false" />
</view:tabs> 
<view:frame title="Informations professionnelles &amp; Coordonnées ">
  <view:board>
    <table id="userFullInfo" border="0" cellspacing="0" cellpadding="5" width="100%">
      <tr>
        <td class="txtlibform" valign="baseline" width="30%"><fmt:message key="GML.position" bundle="${GML}"/></td>
        <td valign="baseline">
          <fmt:message key="${snUserFull.userFull.accessLevel}" var="position" />
          <fmt:message key="${position}" bundle="${GML}"/>
        </td>
      </tr>
      <tr>
        <td class="txtlibform" valign="baseline" width="30%"><fmt:message key="GML.eMail" bundle="${GML}"/></td>
        <td >
          ${snUserFull.userFull.eMail}
        </td>
      </tr>
      <c:forEach items="${propertiesKey}" var="propertys" varStatus="status">
        <tr>
          <td class="txtlibform" valign="baseline" width="30%">
            ${propertiesKey[status.index]}
          </td>
          <td>
            ${propertiesValue[status.index]}
          </td>
        </tr>
      </c:forEach>
    </table>
  </view:board>
</view:frame> 
          <script type="text/javascript">
  desabledFields();
</script>


