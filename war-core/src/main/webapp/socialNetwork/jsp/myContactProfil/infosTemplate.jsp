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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="invitation.action.title" /> </title>
    <view:looknfeel />
    <link rel="stylesheet" type="text/css" href="<c:url value="/directory/jsp/directoryPopup.css"/>"/>    
    <script type="text/javascript" src="<c:url value="/directory/jsp/directory.js" />" ></script>
    <script type="text/javascript">
      var properties =new Array();
      <c:forEach items="${properties}" var="property" varStatus="status">
        properties.push("<c:out value='${properties[status.index]}' escapeXml='false' />");
      </c:forEach>

      <%--*****************   profil body *******************************************--%>
      
        function desabledFields()
        {

          for( i=0; i<properties.length;i++)
          {
            var id=properties[i];
            document.getElementById(id).style.backgroundColor="#F2F2F2";
            document.getElementById(id).disabled=true;
          }
          document.getElementById("myInfoAction").style.visibility='hidden' ;
        }
       
        function afficheButtonEdit(visible)
        { document.getElementById("actionEditStat").style.display = 'none';
        }
      <%--*****************   profil Head *******************************************--%>
        

        function getLastStatus(url)
        {
          $.getJSON(url, function(data) {
            $('.StatusDiv').empty();
            $('.StatusDiv').append(data.status);
          });
        }
    </script>
  </head>
  <body id="privateProfile">
    <view:window>
      <div id="navigation">
        <%@include file="profilNavigation.jsp" %>
      </div>
      <div id="contentAndHeader">
        <div id="header">
          <view:board>
            <%@include file="profilHead.jsp" %>
          </view:board>
        </div>
        <div id="content">
            <%@include file="infosBody.jsp" %>
        </div>
      </div>
<div id="boxesDirectory">
       <div class="windowDirectory" id="directory" >
        <div id="directoryHeader">
          <a href="#"class="close">Fermer</a>
        </div>
        <div id="indexAndSearch"><div id="search">
            <form name="search1" action="javascript:directory('searchByKey')" method="post">
              <input type="text" name="key" value="" id="key" size="40" maxlength="60"
                     style="height: 20px"  />
              <img src="<c:url value="/directory/jsp/icons/advsearch.jpg"/>"
                width="10" height="10" alt="advsearch" />
            </form>
          </div>
        </div>
        <div id="users">

        </div>
      </div>
                  <!-- Mask to cover the whole screen -->
                  <div id="maskDirectory" class="maskClass"></div>
      </div>
    </view:window>
  </body>
</html>