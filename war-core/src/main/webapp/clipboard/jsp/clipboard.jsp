<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.clipboard.model.*"%>
<%@ page import="com.stratelia.webactiv.util.indexEngine.model.*"%>

<%@ include file="checkClipboard.jsp.inc" %>

<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<HTML>
<HEAD>
<TITLE>Presse-papier</TITLE>
<% out.println(graphicFactory.getLookStyleSheet()); %>
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
  // Ferme la fenetre et envoie l'info à l'appelant
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
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="init();" onUnload="BeforeClosing();">

<form name="pasteform" action="" method="post" target="MyMain">
  <input type="hidden" name="compR" value="<%=clipboardSC.getComponentRooterName()%>">
  <input type="hidden" name="SpaceFrom" value="<%=clipboardSC.getSpaceId()%>">
  <input type="hidden" name="ComponentFrom" value="<%=clipboardSC.getComponentId()%>">
  <input type="hidden" name="JSPPage" value="<%=clipboardSC.getJSPPage()%>">
  <input type="hidden" name="TargetFrame" value="<%=clipboardSC.getTargetFrame()%>">
  <input type="hidden" name="message">
  <input type="hidden" name="temp">
<%
	Window 	window 	= graphicFactory.getWindow();
	Frame 	frame 	= graphicFactory.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(clipboardSC.getString("clipboard"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<center>
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
		  ClipboardSelection 	clipObject 	= null;
		  IndexEntry 			indexEntry 	= null;
		  ArrayLine 			line 		= null;
		  String				icon		= null;
		  String				link		= null;
          while (infosI.hasNext()) {
			clipObject 	= (ClipboardSelection) infosI.next();
            indexEntry 	= (IndexEntry) clipObject.getTransferData (ClipboardSelection.IndexFlavor);
            line 		= arrayPane.addArrayLine();
            
            if ("node".equalsIgnoreCase(indexEntry.getObjectType()))
            {
            	icon = resources.getIcon("node");
            	link = URLManager.getSimpleURL(URLManager.URL_TOPIC, indexEntry.getObjectId(), indexEntry.getComponent(), true);
            } else {
            	icon = resources.getIcon("publi");
            	link = URLManager.getSimpleURL(URLManager.URL_PUBLI, indexEntry.getObjectId(), indexEntry.getComponent(), true);
            }
            line.addArrayCellText("<img src=\""+icon+"\" border=\"0\"/>");
            line.addArrayCellLink(Encode.javaStringToHtmlString(indexEntry.getTitle()), "javaScript:view('"+link+"');");
            //line.addArrayCellText(Encode.javaStringToHtmlString(indexEntry.getTitle()));
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
</FORM>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>