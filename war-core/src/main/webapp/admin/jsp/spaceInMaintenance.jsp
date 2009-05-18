<%@ include file="importFrameSet.jsp" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<html>
<head>
<%
String userId = m_MainSessionCtrl.getUserId();

Window 		window 		= gef.getWindow();
BrowseBar 	browseBar 	= window.getBrowseBar();
Frame 		frame 		= gef.getFrame();

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
            <td width="80" align="right" valign="middle"><img src="../../admin/jsp/icons/1px.gif"></td>
            <td width="111" valign="middle"><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr> 
            <td width="80" align="right" valign="top" nowrap><img src="../../admin/jsp/icons/1px.gif"></td>
            <td width="111" valign="top"><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr> 
            <td width="80" align="right" valign="top" nowrap><img src="../../admin/jsp/icons/1px.gif"></span></td>
            <td width="111" valign="top"><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr> 
            <td colspan="2">&nbsp;</td>
          </tr>
          <tr> 
            <td colspan="2"><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr bgcolor="#FFFFFF"> 
            <td align="center">&nbsp;</td>
            <td align="center"><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr> 
            <td colspan="2" align="center" bgcolor="#FFFFFF"> 
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1"></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </form>
    </td>
    <td width="23%" nowrap><div align="left" class="textePetitBold"></div></td>
    <td width="32%" align="center"></td>
  </tr>
  <tr align="center"> 
    <td colspan="3"><img src="../../admin/jsp/icons/fond_logoTrameAppInM2.gif"> </td>
  </tr>
  <tr align="center"> 
    <td colspan="3" bgcolor="#CCCCCC"><img src="../../admin/jsp/icons/1px.gif"></td>
  </tr>
  <tr align="right"> 
    <td colspan="3"><font size="1" face="Verdana, Arial, Helvetica, sans-serif" color="#999999"><%=message.getString("homePage.trademark")%></font></td>
  </tr>
  <tr align="center"> 
    <td colspan="3"><br><br>
 <div align="center" class="textePetitBold"><%=message.getString("homePage.spaceMaintenanceOn")%></div><br>
<div align="center" class="textePetitBold"><img src="../../util/icons/attachment_to_upload.gif"></div><br>
&nbsp;&nbsp;
<div align="center"><%=message.getString("homePage.feedback")%><br>
<%
 for (int i = 0; i < listAdmins.length; i++)
 {
     out.println("<a href=\"mailto:"+listAdmins[i].geteMail()+"\"><img src=\"../../admin/jsp/icons/icoOutilsMail.gif\" align=\"absmiddle\" border=0>"+listAdmins[i].getDisplayedName()+"</a><br>");
 }
%>
</div>
 </td>
  </tr>
</table>
</body>
</html>