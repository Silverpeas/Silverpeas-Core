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
<div>
  <ul class="newsFeedBodyTitle" >
    <li class="ItemOn">
      <a href="#">${newsFeed}</a>
    </li>
  </ul>
</div>

<div class="SocialInformations" id="SocialInformationsId">

</div>

<div id="getNext">
  <b><a href="#" style="color: blue" onclick="javascript:getNext('${urlServlet}');"><fmt:message
        key="profil.getNext"/></a></b>
</div>


<div class="directory" id="directory" >
  <div id="indexAndSearch"><div id="search">
      <form name="searchInNewsFeed" action="javascript:directory('searchByKey')" method="post">
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
<script type="text/javascript">  
  getNext('${urlServlet}');
</script>
