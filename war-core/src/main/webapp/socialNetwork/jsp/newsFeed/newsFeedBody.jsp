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
<div>
  <ul class="newsFeedBodyTitle" >
    <li class="ItemOn">
      <a href="#">${newsFeed}</a>
    </li>
  </ul>
</div>

<div class="SocialInformations" id="SocialInformationsId">

</div>

<%--<div align="center">
  <table cellpadding="10">
    <tr>


    <c:url var="SNIcon" value="/socialNetwork/jsp/icons/" />
    <c:choose>
      <c:when test="${type=='ALL'}">
        <td>
        <img src="${SNIcon}PUBLICATION_new.gif"  width="16" height="16">  :<fmt:message key="profil.icon.new.pub"/>
        </td>
        <td>
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16">  :<fmt:message key="profil.icon.update.pub"/>
        </td>
        <td>
        <img src="${SNIcon}STATUS.gif" width="16" height="16">  :<fmt:message key="profil.icon.staus"/>
        </td>
      </c:when>
      <c:when test="${type=='EVENT'}">
        <td>
        <img src="${SNIcon}EVENT_private.gif"  width="16" height="16">  :<fmt:message key="profil.icon.private.event"/>
        </td>
        <td>
        <img src="${SNIcon}EVENT_public.gif"  width="16" height="16">  :<fmt:message key="profil.icon.public.event"/>
        </td>
      </c:when>
      <c:when test="${type=='PUBLICATION'}">
        <td>
        <img src="${SNIcon}PUBLICATION_new.gif" align="bottom" width="16" height="16">  :<fmt:message key="profil.icon.new.pub"/>
        </td>
        <td>
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16">  :<fmt:message key="profil.icon.update.pub"/>
        </td>
      </c:when>
      <c:otherwise>

      </c:otherwise>
    </c:choose>
    </tr>
  </table>
</div>--%>
<div id="getNext">
  <b><a href="#" style="color: blue" onclick="javascript:getNext('${urlServlet}');"><fmt:message
        key="profil.getNext"/></a></b>
</div>


<div class="directory" id="directory" >
  <div id="indexAndSearch"><div id="search">
      <form name="search" action="javascript:directory('searchByKey')" method="post">
        <input type="text" name="key" value="" id="key" size="40" maxlength="60"
               style="height: 20px"  />
        <img
          src="<c:url value="/directory/jsp/icons/advsearch.jpg"/>"
          width="10" height="10" alt="advsearch" />
      </form>
    </div>
  </div>
  <div id="users">

  </div>
</div>
<script>
  getNext('${urlServlet}');
</script>
