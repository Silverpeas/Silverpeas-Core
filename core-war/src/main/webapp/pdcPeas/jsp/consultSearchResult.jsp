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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPdc.jsp"%>
<%
    String		Id					= (String) request.getAttribute("uniqueId");
    String		query				= (String) request.getAttribute("query");
    String		typeSearch			= (String) request.getAttribute("type");
    ArrayList	axisList			= (ArrayList)request.getAttribute("Axis");
    String		component_id		= (String) request.getAttribute("component_id");

	if ( query != null )
    {
        while ( query.startsWith("*") )
        {
            if ( query.length() > 1 )
                query = query.substring(1);
            else
                query = "";
        }

        while ( query.length() > 0 && "*".equals(query.substring(query.length()-1)) )
        {
            if ( query.length() > 1 )
                query = query.substring(0, query.length()-1);
            else
                query = "";
        }
    }

    Axis		axis		= null;
    Value		value		= null;
    String		fullPath	= "";
    String		listStarts	= null;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script type="text/javascript">
<!--
function MM_reloadPage(init) {  //Updated by PVII. Reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) history.go(0);
}

MM_reloadPage(true);

function onLoad() {
    if ('<%=typeSearch%>'==document.all.search[0].value)
		document.all.search[0].checked = true;
    else
		document.all.search[1].checked = true;
}

function viewDescription(description,fullPath) {
	document.all.descr.value = description;
	document.all.fullPath.innerText = fullPath;
}

function submitForm() {
	var type;
	if (document.all.query.value == "" && document.all.search[0].checked)  {
		document.all.query.value = "*";
	}
	if (document.all.search[0].checked)
		type=document.all.search[0].value;
	else
		type=document.all.search[1].value;

	location.replace("<%=m_context%>/RpdcSearch/jsp/searchResult?uniqueId=<%=Id%>&type="+type+"&query="+escape(document.all.query.value)+"&component_id=<%=component_id%>");
}
//-->
</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onload="onLoad();">
<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcConsultation"));
    out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdcPeas.Navigation"),"AxisTree?uniqueId="+Id+"&component_id="+component_id,false);
    tabbedPane.addTab(resource.getString("GML.search"),"#",true);
	out.println(tabbedPane.print());

    out.println(frame.printBefore());
%>
<CENTER>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<tr><td>
<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
<tr><td align="center"><!--TABLE SAISIE-->
<table border="0" cellspacing="0" cellpadding="5" class="intfdcolor4" width="100%">
<tr>
<td valign="top" nowrap align="left"><span class="txtlibform"><%=resource.getString("pdcPeas.requete")%> :</span></td>
<td align="left"><input type="text" name="query" size="50" value="<%=EncodeHelper.javaStringToHtmlString(query)%>"></td>
<td width="1%"><%
  out.println("<CENTER>");
  ButtonPane buttonPane = gef.getButtonPane();
  Button validateButton = (Button) gef.getFormButton(resource.getString("GML.search"), "javascript:submitForm()", false);
  buttonPane.addButton(validateButton);
  buttonPane.setHorizontalPosition();
  out.println(buttonPane.print());
  out.println("</CENTER>");
%>
</td><td width="100%"></td>
</tr>
<tr>
<td valign="top" align="absmiddle" nowrap><span class="txtlibform"><%=resource.getString("pdcPeas.typerecherche")%> :</span></td>
<td align="left" valign="top" align="absmiddle"><input type="radio" name="search" value="filter" align="absmiddle">&nbsp;Filtre&nbsp;&nbsp;&nbsp;<input type="radio" name="search" value="fullText" align="absmiddle">&nbsp;Plein texte&nbsp;
</td><td></td><td></td>
</tr></table></td></tr>
</table></td></tr>
</table>
</center>
<br>
<CENTER>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<tr><td>
<table border="0" cellspacing="0" cellpadding="2" class="contourintfdcolor" width="100%"><!--tabl1-->
<tr><td align="center">
<table border="0" cellspacing="0" cellpadding="0">
<tr><td valign="top" align="left" width="50%" height="100%">
<%
    ArrayPane pane = gef.getArrayPane("PdcAxisView", "", request, session);
    pane.setRoutingAddress("?query="+query+"&type="+typeSearch+"&uniqueId="+Id+"&component_id="+component_id);
    /*if (listStarts == null)
        pane.addArrayColumn(resource.getString(""));
    else
        pane.addArrayColumn(resource.getString("pdcPeas.pertinence"));*/
    pane.addArrayColumn(resource.getString("pdcPeas.valeur"));
    pane.setSortable(false);
    pane.setVisibleLineNumber(15);

    String stars = "";
    ArrayLine line = null;
	for (int a=0; a<axisList.size(); a++) {
		value = (Value) axisList.get(a);
		if (value != null) {
			fullPath = value.getStringFullPath("/");
			line = pane.addArrayLine();
			//line.addArrayCellText(stars);
			line.addArrayCellText("<a href=\"javascript:viewDescription('"+EncodeHelper.javaStringToJsString(value.getDescription())+"','"+EncodeHelper.javaStringToJsString(fullPath)+"')\"><span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(value.getName())+"</span></a>");
		}

	}

    out.println(pane.print());
%>
</td>
<td width="50%" valign="top" >
	<table><tr><td valign="top"><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>" width="1" height="280"></td>
	<td valign="top">
		<span class="textePetitBold"><%=resource.getString("pdcPeas.emplacement")%> :</span><br><label id=fullPath></label><br>
		<span class="textePetitBold"><%=resource.getString("pdcPeas.definition")%> :</span><br>
		<TEXTAREA NAME="descr" COLS=70 ROWS=22 WRAP=physical readonly></textarea>
	</td></tr></table>
</td>
</tr></table>
		    </td>
	      </tr>
	    </table>
	  </td>
    </tr>
</table>
</CENTER>

<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>