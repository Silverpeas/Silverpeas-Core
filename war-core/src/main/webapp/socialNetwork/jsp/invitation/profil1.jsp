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
        <script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
        <script type="text/javascript" src="/silverpeas/util/javaScript/checkForm.js"></script>

        <script language="JavaScript">


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




    <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">




        <view:window>
            <table width="100%" border="0">
                <tr>
                    <td width="20%" >
                        <view:frame>
                            <view:board>
                                <table width="100%"  border="0" >
                                    <tr>
                                        <td id="profil80" height="600px" style="vertical-align: top">
                                            <table width="100%"  border="0" >
                                                <tr>
                                                    <td id="profilHead" height="20%" style="vertical-align: top">
                                                        <view:board></view:board>

                                                    </td>
                                                </tr> 
                                                <tr>
                                                    <td id="profilCore" height="80%" style="vertical-align: top">
                                                        <view:board></view:board>

                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>

                                </table>
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
                                                        <table width="100%"  border="0" >
                                                            <tr>
                                                                <td id="profilHeadPhote" height="100" width="100" style="vertical-align: top">
                                                                    <img src="<%=m_context + "/directory/jsp/icons/Photo_profil.jpg"%>" style="vertical-align: top" width="80" height="90" border="0" alt="viewUser" />
                                                                </td>
                                                                <td id="profilHeadinfo" height="100" width="40%" align="left" style="vertical-align: middle">
                                                                    <b>Nom Prénom</b><br><br>
                                                                    Tél<br><br>
                                                                    Email@email.com<br><br>
                                                                    <img src="<%=m_context%>/directory/jsp/icons/connected.jpg" width="10" height="10"
                                                                         alt="connected"/> <fmt:message key="directory.connected"/><br>






                                                                </td>
                                                                <td id="profilHeadAction" height="100" width="60%" align="right" style="vertical-align: top">
                                                                   
                                                                    <table width="80px"  border="0" align="right" >
                                                                        
                                                                        <tr>
                                                                            <td align="right" width="50px" >
                                                                                <a href="#">
                                                                                    <img src=""
                                                                                </a>
                                                                                <textarea type="text" name="statMessage"  value="" cols="50" rows="3"  >

                                                                                </textarea>
                                                                                 
                                                                            </td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td align="right" width="50px">

                                                                                 
                                                                                     <div align="center" >
                                                                                <input type="button" value="Valider">
                                                                                <input type="button" value="Annuler">
                                                                                </div>
                                                                                
                                                                            </td>
                                                                        </tr>
                                                                          
                                                                    </table>
                                                                                  
                                                                    </td>

                                                                </tr>
                                                            </table>
                                                    </view:board>

                                                </td>
                                            </tr>
                                            <tr>
                                                <td id="profilCore" height="500" width="100%" style="vertical-align: top">
                                                    <view:board>

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