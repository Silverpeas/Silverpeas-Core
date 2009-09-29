<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="importFrameSet.jsp" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<html>
<head>
<%
String userId = m_MainSessionCtrl.getUserId();

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
Frame frame = gef.getFrame();

// Get Emails Admins
String[] idAdmins = new String[0];
idAdmins = m_MainSessionCtrl.getOrganizationController().getAdministratorUserIds(userId);
UserDetail[] listAdmins = m_MainSessionCtrl.getOrganizationController().getUserDetails(idAdmins);

%>
<title></title>
<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr> 
    <td valign="top" width="45%"> 

 <table width="101" border="0" cellspacing="0" cellpadding="0">
          <tr> 
            <td width="80" align="right" valign="middle"><img src="icons/1px.gif"></td>
            <td width="111" valign="middle"><img src="icons/1px.gif"></td>
          </tr>
          <tr> 
            <td width="80" align="right" valign="top" nowrap><img src="icons/1px.gif"></td>
            <td width="111" valign="top"><img src="icons/1px.gif"></td>
          </tr>
          <tr> 
            <td width="80" align="right" valign="top" nowrap><img src="icons/1px.gif"></span></td>
            <td width="111" valign="top"><img src="icons/1px.gif"></td>
          </tr>
          <tr> 
            <td colspan="2">&nbsp;</td>
          </tr>
          <tr> 
            <td colspan="2"><img src="icons/1px.gif"></td>
          </tr>
          <tr bgcolor="#FFFFFF"> 
            <td align="center">&nbsp;</td>
            <td align="center"><img src="icons/1px.gif"></td>
          </tr>
          <tr> 
            <td colspan="2" align="center" bgcolor="#FFFFFF"> 
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td><img src="icons/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td><img src="icons/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td><img src="icons/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td><img src="icons/1px.gif" width="1" height="1"></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </form>
    </td>
    <td width="23%" nowrap><div align="left" class="textePetitBold"></div></td>
  </tr>
  <tr align="center"> 
    <td colspan="2"><img src="icons/fond_logoTrameAppInM2.gif"> </td>
  </tr>
  <tr align="center"> 
    <td colspan="2" bgcolor="#CCCCCC"><img src="icons/1px.gif"></td>
  </tr>
  <tr align="right"> 
    <td colspan="2"><font size="1" face="Verdana, Arial, Helvetica, sans-serif" color="#999999"><%=message.getString("homePage.trademark")%></font></td>
  </tr>
  <tr align="center"> 
    <td colspan="2"><br><br>
 <div align="center" class="textePetitBold"><%=message.getString("homePage.maintenanceOn")%></div><br>
<div align="center" class="textePetitBold"><img src="../../util/icons/attachment_to_upload.gif"></div><br>
&nbsp;&nbsp;
<div align="center"><%=message.getString("homePage.feedback")%><br>
<%
 for (int i = 0; i < listAdmins.length; i++)
 {
     out.println("<a href=\"mailto:"+listAdmins[i].geteMail()+"\"><img src=\"icons/icoOutilsMail.gif\" align=\"absmiddle\" border=0>"+listAdmins[i].getDisplayedName()+"</a><br>");
 }
%>
</div>
 <br><a href="../../Login.jsp"><img src="icons/icoOutilsEnter.gif" align="absmiddle" border="0"></a>&nbsp;<a href="../../Login.jsp"><%=message.getString("homePage.loginBack")%></a>
 </td>
  </tr>
</table>
<br>
<br>
</body>
</html>