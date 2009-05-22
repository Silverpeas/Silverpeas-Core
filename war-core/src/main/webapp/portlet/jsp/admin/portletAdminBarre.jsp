<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ include file="language.jsp" %>

<html>

<head>
<jsp:useBean id="spaceModel" scope="request" class="com.stratelia.silverpeas.portlet.SpaceModel">
  <jsp:setProperty name="spaceModel" property="*" />
</jsp:useBean>
<title></title>
<meta http-equiv="Content-Type" content="text/html;">
<% out.println(gef.getLookStyleSheet()); %>
<script language="javascript" src="../../util/javaScript/animation.js"></script>
</head>

<body bgcolor="#ffffff" onLoad="MM_preloadImages('../../util/icons/portlet/addColOn.gif', '../../util/icons/portlet/saveFramesetOn.gif')" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<table width="100%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor>
  <tr>
    <td>
      <table width="100%" border="0" cellspacing="1" cellpadding="2">
        <tr class="intfdcolor51"> 

          <td><a href="#" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('addCol','','../../util/icons/portlet/addColOn.gif',1)" onFocus="this.blur()" onClick="openDialog('addColumnList?spaceId=<%=request.getParameter("spaceId")%>', 'popUpCol', 'scrollbars=auto,resizable=yes,width=400,height=250;help:no')" > 
            <img name="addCol" border="0" src="../../util/icons/portlet/addColOff.gif" alt="<%=getMessage("addAColumn")%>" align="absmiddle" title="<%=getMessage("addAColumn")%>"></a> 
          </td>

          <td><a href="save?spaceId=<%=request.getParameter("spaceId")%>" target="_parent" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('save','','../../util/icons/portlet/saveFramesetOn.gif',1)" onFocus="this.blur()"> 
            <img name="save" border="0" src="../../util/icons/portlet/saveFramesetOff.gif" alt="<%=getMessage("saveChanges")%>" title="<%=getMessage("saveChanges")%>"></a> 
          </td>

          <td><a href="admin1?spaceId=<%=request.getParameter("spaceId")%>" target="_parent" onFocus="this.blur()"> 
            <img src="../../util/icons/portlet/annulerModif.gif" alt="<%=getMessage("revert")%>" border="0" title="<%=getMessage("revert")%>"></a></td>
          <td width="100%" class="domainName" align="center" nowrap>Administration 
            de portlets</td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
