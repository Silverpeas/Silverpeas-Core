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
        ArrayList	axisList		= (ArrayList)	request.getAttribute("Axis");
		String		Id				= (String)		request.getAttribute("uniqueId");
        String		component_id	= (String)		request.getAttribute("component_id");

        String		idDescription	= "";
        Axis		axis			= null;
        ArrayList	axisValues		= new ArrayList();
		String		valueName		= "";
		String		valueId			= "";
        //String		parentId		= "";
		int			valueLevel		= -1;
        String		valueDescr		= "";
		Value		value			= null;
        //boolean		isChild			= false;
        boolean		isFind			= false;
		String		increment		= "";
        String		aClass			= "";
        String		uniqueId		= "";
        String		pathValue		= "";

		// pour la surbrillance
		if (Id == null)
			Id = "";
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
var plusImage = new Image();
    plusImage.src = "<%=resource.getIcon("pdcPeas.plus")%>";
var minImage = new Image();
    minImage.src = "<%=resource.getIcon("pdcPeas.minus")%>";
var prevId=null;

function F7_swapClass(){ //v1.4 by PVII
	var i,x,tB,j=0,tA=new Array(),arg=F7_swapClass.arguments;
	if (document.all[arg[1]])
		document.all[arg[1]].className=(document.all[arg[1]].className==arg[3])?arg[2]:arg[3];
	if (document.all[arg[4]]) {
		if (arg[0]==1)
			document.all[arg[4]].src=(document.all[arg[4]].src==minImage.src)?plusImage.src:minImage.src;
		else if (arg[0]==0)
			document.all[arg[4]].src=minImage.src;
	}
}

function loadDescr(id) {
	document.all.desc.value		= document.all[id].title;
	document.all.uniqueId.value = id.substr(1);
	document.all[id].className	= 'axe1';
	if (prevId != null && prevId != id) {
		document.all[prevId].className = 'axe';
	}
	prevId = id;
}

function openNodes(vKey,vPath) {
	var treeId,vId,cnt=0,cnt1=2,val='-1';
	prevId	= 'a'+vKey;
	treeId	= vKey.substr(0,vKey.indexOf('_'));
	vId		= vKey.substr(vKey.indexOf('_')+1);
	while(val.length > 0) {
		val = vPath.substr(vPath.indexOf('/',cnt)+1,vPath.indexOf('/',cnt1)-vPath.indexOf('/',cnt)-1);
		if (val == vId)
			break;
		F7_swapClass(0,'mn'+treeId+'_'+val,'showIt','showIt','but'+treeId+'_'+val);
		cnt		= cnt+val.length+1;
		cnt1	= cnt1+val.length+1;
	}
	var top = document.all['a'+treeId+'_'+val].offsetTop+ document.all['a'+treeId+'_'+val].offsetHeight + 20;
	if(top > document.all.divMain.clientHeight)
		document.all.divMain.scrollTop = top - document.all.divMain.clientHeight;
	else
		document.all.divMain.scrollTop = 0;
}

function submitForm() {
	location.replace("<%=m_context%>/RpdcSearch/jsp/searchInit?uniqueId="+document.all.uniqueId.value+"&component_id=<%=component_id%>");
}
//-->
</script>
<style type="text/css">
<!--

.axe:hover {
	font-size: 10px;
	font-weight: normal;
	color: White;
	background-color : navy;
	text-decoration: none;
	border:1 solid  rgb(255,150,0);
}

.axe {
	font-size: 10px;
	font-weight: normal;
	color: navy;
	background-color : White;
	text-decoration: none;
	border:1 solid  rgb(150,150,150);
}
.axe1 {
	font-size: 10px;
	font-weight: bold;
	color: White;
	background-color : Navy;
	text-decoration: none;
	border:1 solid  rgb(150,150,150);
}
.showIt {
	display: block;
}
.hideIt {
	display: none;
}
-->
</style>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcConsultation"));
    out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdcPeas.Navigation"),"#",true);
    tabbedPane.addTab(resource.getString("GML.search"),"javascript:submitForm()",false);

	out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>
<CENTER>
<%=boardStart%>
<table border=0 cellpadding=10 cellspacing=0 height="100%">
<tr>
<td  width="60%" valign="top"><NOBR>
<DIV id=divMain align='left' style='display:block;height:95%;width:100%;overflow:auto;position:absolute;background-image:url(<%=resource.getIcon("pdcPeas.trame")%>)'>
<%
for (int a=0; a<axisList.size(); a++) {
   axis			= (Axis)		axisList.get(a);
   axisValues	= (ArrayList)	axis.getValues();
   if (axisValues != null) {
    for (int i = 0; i<axisValues.size(); i++) {
       value		= (Value) axisValues.get(i);
       valueName	= value.getName();
	   valueId		= value.getPK().getId();
       valueLevel	= value.getLevelNumber();
       valueDescr	= value.getDescription();
       increment	= "";
       uniqueId		= value.getTreeId()+"_"+valueId;
       aClass		= "class=\"axe\"";
       if (uniqueId.equals(Id)) {
			idDescription	= valueDescr;
            aClass			= "class=\"axe1\"";
            isFind			= true;
            pathValue		= value.getFullPath();
       }

       for (int j = 0; j < valueLevel; j++) {
			increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";
       }

       if (valueLevel == 0) {
			//this is the root
           out.print("<p><img id=\"but"+uniqueId+"\"");
           if (axisValues.size()>1) {
				//there is almost one value under the root
               out.println(" onClick=\"F7_swapClass(1,'mn"+uniqueId+"','showIt','hideIt','but"+uniqueId+"');\" src="+resource.getIcon("pdcPeas.minus")+" width=\"15\" align=\"absmiddle\">&nbsp;<a id=\"a"+uniqueId+"\" "+aClass+" title=\""+EncodeHelper.javaStringToHtmlString(valueDescr)+"\" href=\"javaScript:loadDescr('a"+uniqueId+"')\">&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">");
               out.println("<div id=\"mn"+uniqueId+"\" class=\"showIt\">");
           } else {
				//there is no value under the root
               out.println(" src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;<a id=\"a"+uniqueId+"\" "+aClass+" title=\""+EncodeHelper.javaStringToHtmlString(valueDescr)+"\" href=\"javaScript:loadDescr('a"+uniqueId+"')\">&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">");
           }
       } else {
           if ((i+1 < axisValues.size()) && ((Value)axisValues.get(i+1)).getLevelNumber() > valueLevel) {
				//there is a child value
               out.print(increment+"<img id=\"but"+uniqueId+"\"");
               out.println(" onClick=\"F7_swapClass(1,'mn"+uniqueId+"','showIt','hideIt','but"+uniqueId+"');\" src="+resource.getIcon("pdcPeas.plus")+" width=\"15\" align=\"absmiddle\">&nbsp;<a id=\"a"+uniqueId+"\""+aClass+" title=\""+EncodeHelper.javaStringToHtmlString(valueDescr)+"\" href=\"javaScript:loadDescr('a"+uniqueId+"')\">&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><br>");
               out.println("<div id=\"mn"+uniqueId+"\" class=\"hideIt\">");
           } else {
				//there is no child values
		out.println(increment+"<img src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;<a id=\"a"+uniqueId+"\" "+aClass+" title=\""+EncodeHelper.javaStringToHtmlString(valueDescr)+"\" href=\"javaScript:loadDescr('a"+uniqueId+"')\">&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><BR>");

		//fermeture eventuelle des balises div
		int diffLevel = 0;
			if (i+1 < axisValues.size()) {
				diffLevel = Math.abs(((Value)axisValues.get(i+1)).getLevelNumber() - valueLevel);
			} else {
				diffLevel = valueLevel;
			}
				for (int l=0; l<diffLevel; l++)
				{
					out.println("</div>");
				}
           }
       }
    }
   }
}
%>
</DIV></NOBR>
</td>
<td width="40%" valign="top" >
<table><tr><td valign="top"><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>" width="1" height="280"></td>
<td valign="top">
<span class="textePetitBold"><%=resource.getString("pdcPeas.definition")%> :</span><br>
<TEXTAREA NAME="desc" id="desc" readonly COLS=60 ROWS=22 WRAP=physical><%=idDescription%></textarea>
</td></tr></table>
</td></tr>
</table>
<%=boardEnd%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<input type="hidden" name="uniqueId" value="<%=Id%>">
<script>
<% if (isFind) {%>
    openNodes('<%=Id%>','<%=pathValue%>');
<% }  %>
</script>
</BODY>
</HTML>