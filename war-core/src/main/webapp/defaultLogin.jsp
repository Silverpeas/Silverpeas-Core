<%@ include file="headLog.jsp" %>

<%
String errorCode = request.getParameter("ErrorCode");
if (errorCode == null || errorCode.equals("null"))
	errorCode = "";
String errorText = "";
boolean affiche = false;

String domainId = request.getParameter("DomainId");
request.setAttribute("Silverpeas_DomainId", domainId);
%>

<html>
<head>
<title><%=generalMultilang.getString("GML.popupTitle")%></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="<%=styleSheet%>">
<style type="text/css">
<!--
body {  background-attachment: fixed; background-image: url(admin/jsp/icons/rayure.gif); background-repeat: repeat-x}
-->
</style>

<SCRIPT LANGUAGE= "JavaScript">
<!---
// Public domain cookie code written by: 
// Bill Dortch, hIdaho Design
// (bdortch@netw.com) 
function getCookieVal (offset) {
         var endstr = document.cookie.indexOf (";", offset); 
            if (endstr == -1) 
            endstr = document.cookie.length; 
         return unescape(document.cookie.substring(offset, endstr)); 
         }

function GetCookie (name) {
         var arg = name + "=";
         var alen = arg.length; 
         var clen = document.cookie.length; 
         var i = 0; 
         while (i < clen) {
         var j = i + alen; 
             if (document.cookie.substring(i, j) == arg) 
             return getCookieVal(j); 
         i = document.cookie.indexOf(" ", i) + 1; 
             if (i == 0) break; 
             }

     return null; 
     }

form_name="EDform";
var n4 = window.Event ? true : false;
var sended = false

function process_keypress(e) {

var whichCode = -1

   if (n4) var whichCode = e.which
   else  
   if (window.event.type == "keypress") whichCode = window.event.keyCode
   if (whichCode == 13){ 

     doc_form = eval("document."+form_name);
       if (doc_form!=null && !sended) {
          if (doc_form.do_it.value == "decode_input_image") doc_form.do_it.value="inscriptions_req"
           {
              sended = true;
              doc_form.submit();
           }
        }
     }
  }
if (n4)
{
document.captureEvents(Event.KEYPRESS);
document.onkeypress = process_keypress;
}

function checkForm()
{
	<% if (authenticationSettings.getString("cookieEnabled").toLowerCase().equals("true")) { %>
		if (GetCookie("svpPassword") != document.EDform.Password.value)
		{
			document.EDform.cryptedPassword.value = "";
		}
		else
		{
			if (document.EDform.storePassword.checked)
				document.EDform.storePassword.click();
		}		
	<% } %>
	document.EDform.action="AuthenticationServlet";
	document.EDform.submit();
}
-->
</script>
</head>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr> 
    <td valign="top" width="45%"> 
      <form name="EDform" action="javascript:checkForm();" method="POST">
        <input type=hidden name="do_it" value="decode_input_image">
        <input type=hidden name="DEFAULT_ACTION2" value="jf_inscriptions_req">
        <input type="hidden" name="cryptedPassword">
   
    <table width="220" border="0" cellspacing="0" cellpadding="0" class="intfdcolor">
          <tr> 
            <td width="80" align="right" valign="middle"><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Login :&nbsp;</span></td>
            <td width="111" valign="middle"> 
<%@ include file="inputLogin.jsp" %>
            </td>
          </tr>
          <tr> 
            <td width="80" align="right" valign="top" nowrap><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Password :&nbsp;</span></td>
            <td width="111" valign="top"> 
<%@ include file="inputPassword.jsp" %>
            </td>
          </tr>
          <% 
          	if (domains != null && domains.size() == 1) 
          	{
          		//Il n'y a qu'un domaine. Pas besoin de l'afficher.
          %>
          		<tr><td colspan="2"><input type="hidden" name="DomainId" value="<%=domainIds.get(0)%>"/></td></tr>
          <% } else { %>
	          <tr> 
	            <td width="80" align="right" valign="top" nowrap><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Domain :&nbsp;</span></td>
	            <td width="130" valign="top"> 
	            	<%@ include file="selectDomain.jsp.inc" %>
	            </td>
	          </tr>
	      <% } %>
          <% 
          if (!errorCode.equals("") && !errorCode.equals("4"))
          {
          	%>  
          	<tr> 
          		<td colspan="2" align="center" valign="top" nowrap><span class="txtpetitblanc"><%=homePageBundle.getString(errorCode)%></td>
        	</tr>
        <% } %>
	<% 
	if (authenticationSettings.getString("cookieEnabled").toLowerCase().equals("true"))
	{ %>
        <tr> 
            <td colspan="2" align="left"><span class="txtpetitblanc"><b><%=homePageBundle.getString("storePassword")%></b></span>
              &nbsp;<input type="checkbox" name="storePassword" value="Yes">
            </td>
        </tr>
<%	} %>
          <tr> 
            <td colspan="2">&nbsp;</td>
          </tr>
          <tr> 
            <td colspan="2" bgcolor="#000000"><img src="<%=m_context%>/util/icons/colorPix/1px.gif"></td>
          </tr>
          <tr bgcolor="#FFFFFF"> 
            <td class="intfdcolor51" align="center">&nbsp;</td>
            <td class="intfdcolor51" align="center"> 
              <input type=image src="<%=m_context%>/util/icons/login_fl.gif" border="0" name="image">
            </td>
          </tr>
          <tr> 
            <td colspan="2" class="intfdcolor51" align="center" bgcolor="#FFFFFF"> 
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td class="intfdcolor1"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td class="intfdcolor12"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td class="intfdcolor13"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td class="intfdcolor51"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" width="1" height="1"></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </form>
    </td>
    <td width="23%"><img src="admin/jsp/icons/1px.gif"></td>
    <td width="32%" align="center">&nbsp;</td>
  </tr>
  <tr align="center"> 
    <td colspan="3"><img src="<%=logo%>"> </td>
  </tr>
  <tr align="center"> 
    <td colspan="3" bgcolor="#CCCCCC"><img src="admin/jsp/icons/1px.gif"></td>
  </tr>
  <tr align="right"> 
    <td colspan="3"><font size="1" face="Verdana, Arial, Helvetica, sans-serif" color="#999999"><%=generalMultilang.getString("GML.trademark")%>&nbsp;&nbsp;&nbsp;</font></td>
  </tr>
</table>
<p>&nbsp;</p></body>
<script language="javascript">
	nbCookiesFound=0;
	var domainId = <%=domainId%>;

	/* Si le domainId n'est pas dans la requête, alors récupération depuis le cookie */
	if(domainId == null && GetCookie("defaultDomain") != null)
    { <%
		for (int i = 0 ; i < domains.size() && domains.size() > 1; i++)
		{ %>
        if (GetCookie("defaultDomain").toString() == "<%=((String)domainIds.get(i))%>")
            EDform.DomainId.options[<%=i%>].selected = true;
	<%  }%>
    }

    if(GetCookie("svpLogin") != null)
    {
    	nbCookiesFound = nbCookiesFound + 1;
    	document.EDform.Login.value = GetCookie("svpLogin").toString();
	}    

<%	if (authenticationSettings.getString("cookieEnabled").toLowerCase().equals("true"))
	{ %>
	    if(GetCookie("svpPassword") != null)
	    {
	    	nbCookiesFound = nbCookiesFound + 1;
	    	document.EDform.Password.value = GetCookie("svpPassword").toString();
		}    
<%	} %>
	if (nbCookiesFound==2)
	{
		document.EDform.cryptedPassword.value = "Yes";
		<%
			if (request.getParameter("logout") == null && authenticationSettings.getString("autoSubmit").toLowerCase().equals("true"))
			{
		%>
				document.EDform.submit();
		<%
			}
		%>
	}
    else
    {
    	document.EDform.Password.value = '';
    	document.EDform.Login.focus();
    }
    document.EDform.Login.focus();
</script>
</html>
