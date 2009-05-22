<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.agenda.view.*"%>
<%@ page import="com.stratelia.webactiv.calendar.model.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%@ include file="checkAgenda.jsp.inc" %>
<%
  String form 		= request.getParameter("indiceForm");
  String elem 		= request.getParameter("indiceElem");
  String nameElem 	= request.getParameter("nameElem");
  String idElem 	= request.getParameter("idElem");
  String jsFunction = request.getParameter("JSCallback");
  String action 	= request.getParameter("Action");

  if (action != null && !"null".equals(action)) {
    if (action.equals("NextMonth")) {
      agenda.nextMonth();
    }
    else if (action.equals("PreviousMonth")) {
      agenda.previousMonth();
    }
  } else {
  	List nonSelectableDays = (List) session.getAttribute("Silverpeas_NonSelectableDays");
  	if (nonSelectableDays != null)
  	{
  		session.removeAttribute("Silverpeas_NonSelectableDays");
  		agenda.setNonSelectableDays(nonSelectableDays);
	} else {
		agenda.setNonSelectableDays(null);
	}
  }

  	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(agenda.getLanguage());
%>
<HTML>
<HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<Script language="JavaScript">
function selectDay(day) 
{
  var indiceF = "<%=form%>";
  var indiceE = "<%=elem%>";
  <% if (StringUtil.isDefined(idElem)) { %>
	  window.opener.document.getElementById("<%=idElem%>").value = day;
  <% } else if (StringUtil.isDefined(nameElem)) { %>
  	  nameElement = '<%=nameElem%>';
	  window.opener.document.forms[indiceF].elements[nameElement].value = day;
  <% } else if (StringUtil.isDefined(jsFunction)){ 
	  out.println("window.opener."+jsFunction+"(day);");
  } else { %>
	  window.opener.document.forms[indiceF].elements[indiceE].value = day;
  <% } %>
  window.close();
}

function gotoNextMonth()
{
    document.calendarForm.Action.value = "NextMonth";
    document.calendarForm.submit();
}

function gotoPreviousMonth()
{
    document.calendarForm.Action.value = "PreviousMonth";
    document.calendarForm.submit();
}

function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v3.0
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}

</script>
</HEAD>
<BODY id="agenda">
<%
	out.println(board.printBefore());
%>
<div align="center">
	<table border="0" cellspacing="0" cellpadding="3" width="100%" align="center">
		<tr><td>
<%
  CalendarHtmlView calendar = new CalendarHtmlView(m_context);
  
  Calendar today = Calendar.getInstance();
  if ( (today.get(Calendar.MONTH) == agenda.getStartMonth()) &&
       (today.get(Calendar.YEAR) == agenda.getStartYear())  ) 
  {
    SchedulableCount count = new SchedulableCount(1, String.valueOf(today.get(Calendar.DAY_OF_MONTH)) );
    calendar.add(count);
  }
  calendar.setWeekDayStyle("class=\"ongletOn\"");
  calendar.setMonthDayStyle("class=\"intfdcolor4\"");
  calendar.setMonthSelectedDayStyle("class=\"intfdcolor5\"");
  out.println(calendar.getHtmlView(agenda.getCurrentDay(), agenda));
%>
    </td>
  </tr>
</table>
<%
	out.println(board.printAfter());
%>
<br/>
<center>
<%
  Button button = null;
  button = graphicFactory.getFormButton(agenda.getString("fermer"), "javascript:onClick=window.close();", false);

  out.println(button.print());
%>
</center>
<FORM NAME="calendarForm" ACTION="<%=m_context+URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp" METHOD="POST">
  <input type="hidden" name="Action">
  <input type="hidden" name="indiceForm" value="<%=form%>">
  <input type="hidden" name="indiceElem" value="<%=elem%>">
  <input type="hidden" name="nameElem" value="<%=nameElem%>">
  <input type="hidden" name="idElem" value="<%=idElem%>">
  <input type="hidden" name="JSCallback" value="<%=jsFunction%>">
</FORM>
</div>
</BODY>
</HTML>