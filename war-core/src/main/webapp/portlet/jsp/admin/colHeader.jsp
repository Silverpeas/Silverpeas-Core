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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>


<%@ include file="language.jsp" %>


<jsp:useBean id="column" scope="request" class="com.stratelia.silverpeas.portlet.SpaceColumn"/>

<%

   String pxSelected ;
   String percentSelected ;
   String startSelected ;

   if (column.getColumnWidth().toUpperCase().endsWith("PX")) {
     pxSelected = "selected" ;
     percentSelected = "" ;
     startSelected = "" ;
   } else if (column.getColumnWidth().endsWith("%")) {
     percentSelected = "selected" ;
     pxSelected = "" ;
     startSelected = "" ;
   } else {
     startSelected = "selected" ;
     percentSelected = "" ;
     pxSelected = "" ;
   }
%>


<html>
<head>
<title>Untitled Document</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JavaScript">
<!--
function OnResizeUpdate()
{
  largeurType = window.document.formSize.widthType.value ;
  if (largeurType == "px") {
    window.document.formSize.textWidth.value = document.body.clientWidth + largeurType ; // document.body.clientHeight;
  } else if (largeurType == "%") {
    window.document.formSize.textWidth.value = Math.round((document.body.clientWidth * 100) / window.parent.parent.document.body.clientWidth)  + largeurType;
  } else {
    window.document.formSize.textWidth.value = "*" ;
  }
  return true;
}

function OnChangeUpdate() {
  OnResizeUpdate() ;
}

function validation() {
  re = /(\d+(%|px))|\*/i ;
  valeur = window.document.formSize.textWidth.value ;
  if (valeur.replace(re, "Ok") != "Ok") {
    alert('<%=getMessage("colSizeFormat")%>') ;
  } else {
    document.formSize.submit() ;
  }
}

//-->
</script>

<% out.println(gef.getLookStyleSheet()); %>
<script language="javascript" src="../../util/javaScript/animation.js"></script>
</head>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" 
      onLoad="MM_preloadImages('../../util/icons/portlet/addPortletOk.gif')"
      onresize='javascript:OnResizeUpdate()'
>
<center>
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td><img src="../../util/icons/portlet/arrowResizeLeft.gif"></td>
      <td width="100%"> 
        <table width="100%" border="0" cellspacing="0" cellpadding="2" bgcolor="#AFB8C9">
          <form name="formSize" id="formSize" action="setColSize">
            <tr align="center"> 
              <td> <a href="#" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('addPortlet','','../../util/icons/portlet/addPortletOk.gif',1)" 
                         onFocus="this.blur()"
onClick="openDialog('portletList?col=<%=request.getParameter("col")%>&spaceId=<%=request.getParameter("spaceId")%>','popUpPortlet',400,250,'scrollbars=yes,resizable=yes,help:no')"> 


								<img name="addPortlet" border="0" src="../../util/icons/portlet/addPortlet.gif" width="25" height="18"> 
                </a></td>
              <td>
<%		
        
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton((Button) gef.getFormButton(getMessage("fixColWidth"), "javascript:validation()", false));
		out.println(buttonPane.print());
		
%>

                <!--input type="submit" name="cmdFixer" value="Fixer la largeur" size="1"-->
              </td>
              <td> 
                <input type="text" name="textWidth" size="5" maxlength="5" style="background: DEDEDE; font-family: Verdana; font-size: 10px" value="<%=column.getColumnWidth()%>">
              </td>
              <td> 
                <select name="widthType" style="background: DEDEDE; font-family: Verdana; font-size: 10px"
            onChange='javascript:OnChangeUpdate()'
            >
                  <option value="%" <%=percentSelected%> >%</option>
                  <option value="px" <%=pxSelected%> >px</option>
                  <option value="*" <%=startSelected%> >*</option>
                </select>
              </td>
              <td width="100%">&nbsp;</td>
            </tr>
            <input type="hidden" name="col" value="<%=request.getParameter("col")%>">
          </form>
        </table>
      </td>
      <td><img src="../../util/icons/portlet/arrowResizeRight.gif"></td>
    </tr>
  </table>
  </center>
</body>
</html>
