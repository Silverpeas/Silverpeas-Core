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
    <script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
    <script type="text/javascript">

      function enableField()
      {
        if(document.getElementById("enabledStat").disabled==false)
        {
          document.getElementById("enabledStat").style.backgroundColor="#DEDEDE"
          document.getElementById("enabledStat").disabled=true;
        } else{
          document.getElementById("enabledStat").style.backgroundColor="#FFFFFF";
          document.getElementById("enabledStat").focus();
          document.getElementById("enabledStat").disabled=false;
        }
      }
      function desabledField()
      {
        document.getElementById("enabledStat").style.backgroundColor="#F2F2F2"
        document.getElementById("enabledStat").disabled=true;
        document.statForm.submit();
      }
      afficheButtonEdit(visible)
      {
        document.getElementById("actionEditStat").style.display = 'none';
      }
      function toggleZoneMessage() {
        if( document.getElementById("zoneMessage").style.display=='none' ){
          document.getElementById("actionZoneMessage").style.display = 'none';
          document.getElementById("zoneMessage").style.display = '';
        }else{
          document.getElementById("actionZoneMessage").style.display = '';
          document.getElementById("zoneMessage").style.display = 'none';

        }
      }

    </script>
  </head>
  <body id="invitation">
    <script type="text/javascript">
      enableField()
    </script>
    <view:window>
      <div id="navigation">
        <view:board>
          <%@include file="invitationNavigation.jsp" %>
        </view:board>
      </div>
      <div id="content">
	      <%@include file="invitationSentBody.jsp" %>
      </div>
    </view:window>
  </body>
</html>