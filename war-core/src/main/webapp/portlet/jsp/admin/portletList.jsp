<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.portlet.model.*"%>
<%@ page import="com.stratelia.silverpeas.portlet.*"%>

<%@ include file="language.jsp" %>

<%
  PortletComponent[] portletList = (PortletComponent[]) request.getAttribute("portletList") ;
  String col=request.getParameter("col") ;
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
</HEAD>


<script language="JavaScript">
//pour netscape 6.2.0
if( navigator.appName == "Netscape" )      
{
    self.scrollbars.visible=true;
}


<!--
function addPorletAndClose(instanceId, spaceId) {
window.opener.parent.frames[1].location.href = "addPortlet?col=<%=col%>&instanceId=" + instanceId +"&spaceId=" + spaceId;
  window.close() ;
  return true ;
}

//-->
</script>
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="intfdcolor">
      <table width="100%" border="0" cellpadding="3" cellspacing="1">
        <tr> 
          <td colspan="3"><span class="domainName">
<%
  String target ;

   // if it's a column creation
   if (col==null) {
     target = "adminMain" ;
     col="-1" ;
%>

<%=messageBundle.getString("addToNewCol")%>

<% } else {
     target = "column" + col ;

%>

<%=messageBundle.getString("addToCol")%> <%=col%>

<% }%>
</span></td>
        </tr>
        <tr valign="top">
          <td nowrap align="right" class="intfdcolor51">&nbsp;</td>
          <td class="intfdcolor51" nowrap><span class=txtnav><%=messageBundle.getString("portlet")%></span></td>
          <td class="intfdcolor51" width="100%"><span class=txtnav><%=messageBundle.getString("desc")%></span></td>
        </tr>
<%
  for (int i=0 ; i<portletList.length ; i++) {
    PortletComponent instance = portletList[i] ;
%>
        <tr valign="top"> 
          <td nowrap align="right" class="intfdcolor4"><img src="../../util/icons/portlet/1px.gif" width="50" height="1" ><a href="javascript:onClick=addPorletAndClose('<%=instance.id%>','<%=instance.spaceId%>')"><img src="../../util/icons/portlet/portlet.gif" border="0"></a></td>
          <td class="intfdcolor4" nowrap><a href= "javascript:onClick=addPorletAndClose('<%=instance.id%>','<%=instance.spaceId%>')"><%=instance.name%> 
            </a></td>
          <td class="intfdcolor4" width="100%"><%=instance.description%></td>
        </tr>
 <% } %>
      </table>
    </td>
  </tr>
</table>
</BODY>
</HTML>
