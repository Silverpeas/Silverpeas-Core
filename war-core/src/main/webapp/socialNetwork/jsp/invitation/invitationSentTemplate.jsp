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
<%@ include file="check.jsp" %>
<html>
    <head>
        <view:looknfeel />
            <style type="text/css">
      #navigation {
        float: left;
        width: 20%;
        margin: 2px;
        padding: 2px;
      }
      #content {
        float: left;
        width: 75%;
        margin: 2px;
        padding: 2px;
      }
      #navigation #myInfo{
        float: right;
      }

      #navigation .box {
        background-color:#FFFFFF;
        background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/fondBoard.jpg);
        background-position:right top;
        background-repeat:no-repeat;
        margin-left:auto;
        margin-right:auto;
        margin-bottom: 15px;
      }
      #navigation ul {
        margin: 0;
        padding: 0;
      }

      #navigation li{
        display:list-item;
        list-style-type:none;
        padding: 3px;
      }
       #navigation li{
        display:list-item;
        list-style-type:none;
        padding: 5px;
      }
      #navigation li A:hover {
       font-weight: bold;
      }
    </style>
        <title><fmt:message key="invitation.action.title" /> </title>
        <script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
        <script type="text/javascript" src="/silverpeas/util/javaScript/checkForm.js"></script>

        <script language="JavaScript">

            function enableField()
            {
                if(document.getElementById("enabledStat").disabled==false)
                {
                    document.getElementById("enabledStat").style.backgroundColor="#DEDEDE"
                    document.getElementById("enabledStat").disabled=true;


                }
                else{

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
   <body onload="javasript:enableField()">
<view:window>
      <div id="navigation">
        <view:board>
          <%@include file="invitationNavigation.jsp" %>
        </view:board>
      </div>
      <div id="content">
        <view:board>
          <%@include file="invitationSentBody.jsp" %>
        </view:board>
      </div>
    </view:window>
    </body>
</html>