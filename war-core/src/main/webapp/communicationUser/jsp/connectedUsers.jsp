<%@ include file="checkCommunicationUser.jsp" %>

<%
	ArrayLine arrayLine = null;
	Iterator   iter = null;
	Collection cResultData = (Collection) request.getAttribute("ConnectedUsersList");
	OrganizationController orgaController = new OrganizationController();
%>

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<!--[ JAVASCRIPT ]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<SCRIPT LANGUAGE="JAVASCRIPT">
<!--
	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '500', '250','scrollbars=yes, resizable, alwaysRaised');
	}

  function ConfirmAndSend(targetURL,textToDisplay)
  {
      if (window.confirm(textToDisplay))
      {
          window.location.href = targetURL;
      }
  }

//--------------------------------------------------------------------------------------DoIdle
ID = window.setTimeout("DoIdle();", <%=settings.getString("refreshList")%>*1000);
function DoIdle()
{ self.location.href = "Main"; }


//CBO : ADD
function manageWindow(page,nom,largeur,hauteur,options)
{
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
}

function enterPopup(userId)
{
	manageWindow('OpenDiscussion?userId='+userId,'popupDiscussion'+userId,'650', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
//CBO : FIN ADD

//-->
</SCRIPT>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<%
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<CENTER>
<%

	String icoMonitor = m_context + "/util/icons/monitor.gif";
	String icoNotify = m_context + "/util/icons/talk2user.gif";

	ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("");
	arrayColumn1.setSortable(false);
	arrayPane.addArrayColumn(resources.getString("user"));
    if (settings.getBoolean("displayColumnLanguage",false))
    {
		arrayColumn1 = arrayPane.addArrayColumn(resources.getString("language"));
		arrayColumn1.setSortable(true);
    }
	arrayColumn1 = arrayPane.addArrayColumn("");
    ArrayCellText cellText;         

    if (cResultData != null)
    {
		long currentTime = new Date().getTime();        
		iter = cResultData.iterator();
		while (iter.hasNext())
		{
			SessionInfo item = (SessionInfo) iter.next();
			if (!item.m_User.getId().equals(communicationScc.getUserId()))
			{
				List userList = new ArrayList();
				userList.add(item.m_User.getId());
				Hashtable usersLanguages = orgaController.getUsersLanguage(userList);

				arrayLine = arrayPane.addArrayLine();
				arrayLine.addArrayCellText("<div align=right><img src=\""+icoMonitor+"\" border=0></div>");
				arrayLine.addArrayCellText(item.m_User.getDisplayedName());
				if (settings.getBoolean("displayColumnLanguage",false))
					arrayLine.addArrayCellText(usersLanguages.get(item.m_User.getId()).toString());

				//CBO : UPDATE
				//arrayLine.addArrayCellText("<div align=left><a href=#><img alt=\"" + resources.getString("notifyUser") + "\" src=\""+icoNotify+"\" border=0 onclick=\"javascript:openSPWindow('NotifyUser?theUserId=" + item.m_User.getId() + "','DisplayNotifySession')\"></A></div>");				
				arrayLine.addArrayCellText("<div align=left><a href=#><img alt=\"" + resources.getString("notifyUser") + "\" src=\""+icoNotify+"\" border=0 onclick=\"javascript:enterPopup('" + item.m_User.getId() + "')\"></A></div>");
			}
		}
		out.println(arrayPane.print());
    }
    out.println(resources.getString("refreshedTime") + "&nbsp;" + settings.getString("refreshList")+ "&nbsp;" + resources.getString("seconds") + "<BR>");
%>
</CENTER>
<%       
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<!-- CBO : REMOVE -->
<!--<form name="userConnected" action="Main" method="post"></form>-->

</BODY>
</HTML>