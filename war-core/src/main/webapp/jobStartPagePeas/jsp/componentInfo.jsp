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

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>

<%@ include file="check.jsp" %>

<%!

final String PARAM_TYPE_CHECKBOX 	= "checkbox";
final String PARAM_TYPE_SELECT		= "select";
final String PARAM_TYPE_RADIO		= "radio";

void displayParameter(SPParameter parameter, ResourcesWrapper resource, JspWriter out) throws java.io.IOException
{
	String help = parameter.getHelp(resource.getLanguage());
	if (help != null) {
		//help = Encode.javaStringToJsString(help);
		out.println("<td align=\"left\">");
		out.print("<img src=\""+resource.getIcon("JSPP.instanceHelpInfo")+"\" title=\""+help+"\" class=\"parameterInfo\"/>");
		out.println("</td>");
	} else {
		out.println("<td align=\"left\" width=\"15\">&nbsp;</td>");
	}

	out.println("<td class=\"textePetitBold\" nowrap valign=\"center\">");
	out.println(parameter.getLabel()+" : ");
	out.println("</td>");
	out.println("<td align=\"left\" valign=\"top\">");
	
	// Value
	boolean isCheckbox = PARAM_TYPE_CHECKBOX.equals(parameter.getType());
	boolean isSelect = PARAM_TYPE_SELECT.equals(parameter.getType()) || SPParameter.TYPE_XMLTEMPLATES.equals(parameter.getType());
	boolean isRadio = PARAM_TYPE_RADIO.equals(parameter.getType());
	if (isCheckbox) {
		String checked = "";
		if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals("yes"))
			checked = "checked";
		out.println("<input type=\"checkbox\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\" "+checked+" disabled/>");
	}
	else if (isSelect)
	{
		ArrayList options = parameter.getOptions();
		if (options != null)
		{
			for (int i=0; i<options.size(); i++)
			{
				ArrayList option = (ArrayList) options.get(i);
				String name = (String) option.get(0);
				String value = (String) option.get(1);
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value.toLowerCase())) {
				  	out.println(name);
				}
			}		
		}
	}
	else if (isRadio)
	{
		ArrayList radios = parameter.getOptions();
		if (radios != null)
		{
	      for (int i=0; i<radios.size(); i++)
	      {
	      		ArrayList radio = (ArrayList) radios.get(i);
				String name = (String) radio.get(0);
				String value = (String) radio.get(1);
				String checked = "";
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value) || i==0)
					checked = "checked";
				out.println("<input type=\"radio\" name=\""+parameter.getName()+"\" value=\""+value+"\" "+checked+" disabled/>");
				out.println(name+"&nbsp;");
			}		
		}
	}
	else {
		out.println(parameter.getValue());
	}
	out.println("</td>");
}
%>

<%
ComponentInst 	compoInst 			= (ComponentInst) request.getAttribute("ComponentInst");
String 			m_JobPeas 			= (String) request.getAttribute("JobPeas");
List 			parameters 			= (List) request.getAttribute("Parameters");
ArrayList 		m_Profiles 			= (ArrayList) request.getAttribute("Profiles");
boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();

String m_ComponentIcon = iconsPath+"/util/icons/component/"+compoInst.getName()+"Small.gif";

TabbedPane tabbedPane = gef.getTabbedPane();

browseBar.setComponentId(compoInst.getId());
browseBar.setExtraInformation(resource.getString("GML.description"));	
browseBar.setI18N(compoInst, resource.getLanguage());

operationPane.addOperation(resource.getIcon("JSPP.instanceUpdate"),resource.getString("JSPP.ComponentPanelModifyTitle"),"javascript:onClick=updateInstance(800, 350)");
operationPane.addOperation(resource.getIcon("JSPP.ComponentOrder"),resource.getString("JSPP.ComponentOrder"),"javascript:onClick=openPopup('PlaceComponentAfter', 750, 230)");
if (JobStartPagePeasSettings.useComponentsCopy) {
	operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("GML.copy"),"javascript:onClick=clipboardCopy()");
}
operationPane.addOperation(resource.getIcon("JSPP.instanceDel"),resource.getString("JSPP.ComponentPanelDeleteTitle"),"javascript:onClick=deleteInstance()");

tabbedPane.addTab(resource.getString("GML.description"),"#",true);
Iterator i = m_Profiles.iterator();
ProfileInst theProfile = null;
String profile = null;
String prof = null;

while (i.hasNext()) {
	theProfile = (ProfileInst) i.next();
	profile = theProfile.getLabel();
	prof = resource.getString(profile.replace(' ', '_'));
	if (prof.equals(""))
		prof = profile;
	
	tabbedPane.addTab(prof,"RoleInstance?IdProfile="+theProfile.getId()+"&NameProfile="+theProfile.getName()+"&LabelProfile="+theProfile.getLabel(),false);
}	
%>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="javascript/component.js"></script>
<script type="text/javascript">
<!--
var currentLanguage = "<%=compoInst.getLanguage()%>";
<%
	String lang = "";
	Iterator codes = compoInst.getTranslations().keySet().iterator();
	while (codes.hasNext())
	{
		lang = (String) codes.next();
		out.println("var name_"+lang+" = \""+Encode.javaStringToJsString(compoInst.getLabel(lang))+"\";\n");
		out.println("var desc_"+lang+" = \""+Encode.javaStringToJsString(compoInst.getDescription(lang))+"\";\n");
	}
%>

function showTranslation(lang)
{
	<%=I18NHelper.updateHTMLLinks(compoInst)%>
	
	document.getElementById("compoName").innerHTML = eval("name_"+lang);
	document.getElementById("compoDesc").innerHTML = eval("desc_"+lang);
	
	currentLanguage = lang;
}

function openPopup(action, larg, haut) 
{
	url = action;
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function deleteInstance() {	
    if (window.confirm("<%=resource.getString("JSPP.MessageSuppressionInstanceBegin")+" "+Encode.javaStringToJsString(compoInst.getLabel())+" "+resource.getString("JSPP.MessageSuppressionInstanceEnd")%>")) { 
    	location.href = "DeleteInstance?ComponentNum=<%=compoInst.getId()%>";
	}
}

function updateInstance(larg, haut) 
{
	url = "UpdateInstance?ComponentNum=<%=compoInst.getId()%>&Translation="+currentLanguage;
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function clipboardCopy() {
    top.IdleFrame.location.href = 'copy?Object=Component&Id=<%=compoInst.getId()%>';
}
-->
</script>
</head>
<body id="admin-component">
<form name="infoInstance" action="" method="post">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table cellpadding="5" cellspacing="0" border="0" width="100%">
	<tr>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.type") %> :</td>
		<td align="left" width="100%"><img src="<%=m_ComponentIcon%>" class="componentIcon" alt=""/>&nbsp;<%=m_JobPeas%></td>
	</tr>
	<tr>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.name") %> :</td>
		<td align="left" valign="baseline" width="100%" id="compoName"><%=compoInst.getLabel(resource.getLanguage())%></td>
	</tr>
	<% if (StringUtil.isDefined(compoInst.getDescription(resource.getLanguage()))) { %>
		<tr>
			<td class="textePetitBold" nowrap="nowrap" valign="top"><%=resource.getString("GML.description") %> :</td>
			<td align="left" valign="top" width="100%" id="compoDesc"><%=Encode.javaStringToHtmlParagraphe(compoInst.getDescription(resource.getLanguage()))%></td>
		</tr>
	<% } %>
	<% if (compoInst.getCreateDate() != null) { %>
	<tr>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.creationDate") %> :</td>
		<td align="left" valign="baseline" width="100%">
			<%=resource.getOutputDateAndHour(compoInst.getCreateDate())%>
			<% if (compoInst.getCreator() != null) { %> 
				<%=resource.getString("GML.by") %> <%=compoInst.getCreator().getDisplayedName() %>
			<% } %>
		</td>
	</tr>
	<% } %>
	<% if (compoInst.getUpdateDate() != null) { %>
	<tr>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.updateDate") %> :</td>
		<td align="left" valign="baseline" width="100%">
			<%=resource.getOutputDateAndHour(compoInst.getUpdateDate())%>
			<% if (compoInst.getUpdater() != null) { %>  
				<%=resource.getString("GML.by") %> <%=compoInst.getUpdater().getDisplayedName() %>
			<% } %>
		</td>
	</tr>
	<% } %>
	<% if (isInHeritanceEnable) { %>
	<tr>
		<td class="textePetitBold" nowrap="nowrap" valign="top"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
		<td align="left" valign="baseline" width="100%">
		<% if (compoInst.isInheritanceBlocked()) { %>
			<%=resource.getString("JSPP.inheritanceComponentNotUsed")%>
		<% } else { %>
			<%=resource.getString("JSPP.inheritanceComponentUsed")%>
		<% } %>
		</td>
	</tr>
	<% } %>
</table>
<% if (parameters.size() > 0) { %>
	<div id="parameters-header">
		<span class="txtlibform"><%=resource.getString("JSPP.parameters") %></span>
	</div>
<% } %>
<table border="0">
	<tr>
	<%
	boolean on2Columns = false;
	if (parameters.size() >= 5)
		on2Columns = true;
	
	SPParameter parameter = null;
	if (on2Columns)
	{
		out.println("<td>");
		out.println("<table border=\"0\" width=\"100%\">");
		for(int nI=0; parameters != null && nI < parameters.size(); nI++)
		{
			parameter = (SPParameter) parameters.get(nI);
			if (nI%2 == 0)
				out.println("<tr>");

			displayParameter(parameter, resource, out);

			if (nI%2 != 0)
				out.println("</tr>");
			else
				out.println("<td width=\"40px\">&nbsp;</td>");
		}
		if (parameters.size()%2 != 0)
		{
			out.println("<td>&nbsp;</td><td>&nbsp;</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("</td>");
	} else {
		for(int nI=0; parameters != null && nI < parameters.size(); nI++)
		{
			parameter = (SPParameter) parameters.get(nI);

			out.println("<tr>");
			displayParameter(parameter, resource, out);
			out.println("</tr>");
		}
	}
%>	
	</tr>
	</table>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</form>
</body>
</html>