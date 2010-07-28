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
                document.getElementById("enabledStat").style.backgroundColor="#F2F2F2";
                document.getElementById("enabledStat").disabled=true;
                document.statForm.submit();


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




    <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onload="javasript:enableField()">




        <view:window>
            <table width="100%" border="0">
                <tr>
                    <td width="20%" >
                        <view:frame>
                            <view:board>
                                <%@include file="invitationNavigation.jsp" %>
                            </view:board>
                        </view:frame>
                    </td>
                    <td width="80%" >
                        <view:frame>
                            <table width="100%"  border="0"  >
                                <tr><td id="profil80" height="600px" style="vertical-align: top">
                                        <table width="100%"  border="0" >
                                            <tr>
                                                <td id="profilHead" height="100" width="100%" style="vertical-align: top">
                                                    <view:board>

                                                        <%@include file="invitationHead.jsp" %>

                                                    </view:board>

                                                </td>
                                            </tr>
                                            <tr>
                                                <td id="profilCore" align="left" height="500" width="100%" style="vertical-align: top">

                                                    <view:board>
                                                        <%@include file="invitationBody.jsp" %>
                                                    </view:board>
                                                </td>
                                            </tr>
                                        </table>
                                    </td></tr>

                            </table>

                        </view:frame>
                    </td>
                </tr>
            </table>
        </view:window>

    </body>
</html>