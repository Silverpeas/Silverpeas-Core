<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.index.indexing.model.IndexEntry"%>
<%@ page import="org.silverpeas.core.clipboard.ClipboardSelection"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>

<%@ include file="checkClipboard.jsp" %>

<%@page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<HTML>
<HEAD>
<view:looknfeel/>
<script language="javascript" src="../../util/javaScript/formUtil.js"></script>
<SCRIPT language="JavaScript">

//--------------------------------------------------------------------------------------SelectClipboardObject
function SelectClipboardObject ( index , field ) {
  //document.pasteform.txtOutput.value = index + ", " + field.value + ", " + field.checked;
  opener.top.IdleFrame.location.href = '../../Rclipboard/jsp/selectObject.jsp?Id='+index+'&Status='+field.checked;
}

//--------------------------------------------------------------------------------------BeforeClosing
function BeforeClosing () {
  opener.top.ClipboardWindowClosed = true;
}

//--------------------------------------------------------------------------------------ClipboardClose
function ClipboardClose () {
  BeforeClosing ();
  window.close();
}

//--------------------------------------------------------------------------------------ClipboardDoPaste
function ClipboardDoPaste () {
  // Ferme la fenetre et envoie l'info a l'appelant
  // deprecated
  opener.top.ClipboardWindowClosed = true;
  document.pasteform.action = "../../Rclipboard/jsp/selectionpaste.jsp";
  document.pasteform.message.value = "REFRESH";// message pour la idle frame, id "rafraichir le composant"
  document.pasteform.temp.value = window.clipboardData.getData("Text");
  document.pasteform.target = "IdleFrame";
  document.pasteform.submit();
  window.close();
}

//--------------------------------------------------------------------------------------ClipboardDoDelete
function ClipboardDoDelete () {
  //opener.top.ClipboardWindowClosed = false;
  document.pasteform.action = "../../Rclipboard/jsp/delete.jsp";
  document.pasteform.target = "_self";
  document.pasteform.submit();
}

//--------------------------------------------------------------------------------------ShowHiddenFrame
function ShowHiddenFrame () {
	//opener.top.IdleFrame.height = 500;
	alert (opener.top.IdleFrame.style.height);
	return false;
}

//--------------------------------------------------------------------------------------init
function init () {
	opener.top.ClipboardWindow = window;
	opener.top.ClipboardWindowClosed = false;
}

function view(url)
{
	opener.top.location.href = url;
	ClipboardClose();
}
</SCRIPT>
</head>
<body onLoad="init();" onUnload="BeforeClosing();">
<fmt:message var="tmp" key="clipboard"/>
<view:browseBar componentId="" path="${tmp}"/>
<view:window popup="true">
  <view:frame>

<form name="pasteform" action="" method="post" target="MyMain">
  <input type="hidden" name="compR" value="<%=clipboardSC.getComponentRooterName()%>">
  <input type="hidden" name="SpaceFrom" value="<%=clipboardSC.getSpaceId()%>">
  <input type="hidden" name="ComponentFrom" value="<%=clipboardSC.getComponentId()%>">
  <input type="hidden" name="JSPPage" value="<%=clipboardSC.getJSPPage()%>">
  <input type="hidden" name="TargetFrame" value="<%=clipboardSC.getTargetFrame()%>">
  <input type="hidden" name="message">
  <input type="hidden" name="temp">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
  <tr>
    <td valign="top" align="center" width="100%"> <!-- SEPARATION NAVIGATION / CONTENU DU COMPOSANT -->
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
          <td><img src="icons/1px.gif" height="7"></td>
        </tr>
      </table>
      <table border="0" cellspacing="0" cellpadding="1" width="100%">
        <tr>
          <td>
          <%
          ArrayPane arrayPane = graphicFactory.getArrayPane("quickinfoList", pageContext);
          arrayPane.addArrayColumn(clipboardSC.getString("vide"));
          arrayPane.addArrayColumn(clipboardSC.getString("titre"));
          arrayPane.addArrayColumn(clipboardSC.getString("composant"));
          arrayPane.addArrayColumn(clipboardSC.getString("vide"));

		  Collection 			infos 		= clipboardSC.getObjects();
          Iterator 				infosI		= infos.iterator();
		  int 					index 		= 0;
		  ClipboardSelection clipObject 	= null;
		  IndexEntry 			indexEntry 	= null;
		  ArrayLine 			line 		= null;
		  String				icon		= null;
		  String				link		= null;
		String iconComponent = null;

		while (infosI.hasNext()) {
				clipObject 	= (ClipboardSelection) infosI.next();
        indexEntry 	= (IndexEntry) clipObject.getTransferData (ClipboardSelection.IndexFlavor);
        line 		= arrayPane.addArrayLine();

        if ("node".equalsIgnoreCase(indexEntry.getObjectType()))
        {
		icon = resources.getIcon("node");
		link = URLUtil.getSimpleURL(URLUtil.URL_TOPIC, indexEntry.getObjectId(), indexEntry.getComponent(), true);
        } else if ("component".equalsIgnoreCase(indexEntry.getObjectType()))
        {
		icon = resources.getIcon("component");
		iconComponent = m_context + "/util/icons/component/" + URLUtil.getComponentNameFromComponentId(indexEntry.getComponent()) +"Small.gif";
		link = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, indexEntry.getObjectId(), indexEntry.getComponent(), true);
        } else {
		icon = resources.getIcon("publi");
		link = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, indexEntry.getObjectId(), indexEntry.getComponent(), true);
        }
        line.addArrayCellText("<img src=\""+ icon+"\" border=\"0\"/>");
        if ("component".equalsIgnoreCase(indexEntry.getObjectType()))
	        line.addArrayCellLink("<img src=\""+ iconComponent+"\" border=\"0\"/>&nbsp;" + Encode
              .javaStringToHtmlString(indexEntry.getTitle()), "javaScript:view('"+link+"');");
        else
	        line.addArrayCellLink(Encode.javaStringToHtmlString(indexEntry.getTitle()), "javaScript:view('"+link+"');");

				line.addArrayCellText(clipboardSC.getComponentLabel(indexEntry.getComponent()));
				if (clipObject.isSelected ())
					line.addArrayCellText("<input type=checkbox checked name='clipboardId"+String.valueOf(index)+"' value='' onclick='SelectClipboardObject("+String.valueOf(index)+", this)'>");
				else
					line.addArrayCellText("<input type=checkbox name='clipboardId"+String.valueOf(index)+"' value='' onclick='SelectClipboardObject("+String.valueOf(index)+", this)'>");

				index++;
       }
          out.println(arrayPane.print());
          %>
          </td>
        </tr>
      </table>
<br>
        <!-- BOUTON DE FORMULAIRE-->
      <table width="1%" border="0" cellspacing="0" cellpadding="5">
          <tr align="center">
            <td align="right">
              <%
                Button button = graphicFactory.getFormButton(clipboardSC.getString("reset"), "javascript:onClick=ClipboardDoDelete()", false);
                out.println(button.print());
              %>
			</td>
			<td align="left">
              <%
                button = graphicFactory.getFormButton(clipboardSC.getString("fermer"), "javascript:onClick=ClipboardClose()", false);
                out.println(button.print());
              %>
            </td>
          </tr>
        </table>
    </td>
  </tr>
</table>
</form>
</view:frame>
</view:window>
</BODY>
</HTML>