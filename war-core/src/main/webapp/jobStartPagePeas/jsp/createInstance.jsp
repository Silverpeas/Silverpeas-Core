<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@page import="com.silverpeas.admin.localized.LocalizedComponent"%>
<%@page import="com.silverpeas.admin.localized.LocalizedOption"%>
<%@page import="com.silverpeas.admin.localized.LocalizedParameter"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%!

void displayParameter(LocalizedParameter parameter, ResourcesWrapper resource, JspWriter out) throws java.io.IOException {
	// Value

	boolean isSelect =parameter.isSelect() ||parameter.isXmlTemplate();
	String help = parameter.getHelp();
	if (help != null) {
		help = EncodeHelper.javaStringToHtmlString(help);
		out.println("<td valign=\"top\" align=\"left\">");
		out.print("<img src=\""+resource.getIcon("JSPP.instanceHelpInfo")+"\" title=\""+help+"\" class=\"parameterInfo\"/>");
		out.println("</td>");
	} else {
		out.println("<td align=left width=15>&nbsp;</td>");
	}

	out.println("<td class=\"intfdcolor4\" nowrap valign=\"top\" align=\"left\">");
	out.println("<span class=\"txtlibform\">"+parameter.getLabel()+" : </span>");
	out.println("</td>");
	out.println("<td class=\"intfdcolor4\" align=\"left\" valign=\"top\">");

	String disabled = "disabled";
	if (parameter.isAlwaysUpdatable() || parameter.isUpdatableOnCreationOnly()) {
      disabled = "";
    }

	if (parameter.isCheckbox()) {
		String checked = "";
		if (StringUtil.getBooleanValue(parameter.getValue())) {
			checked = "checked";
		}
		out.println("<input type=\"checkbox\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\" "+checked+" "+disabled+"/>");
    if (StringUtil.isDefined(parameter.getWarning())) {
      out.println("<div style=\"display: none;\" id=\"warning-"+parameter.getName()+"\">"+parameter.getWarning()+"</div>");
    }
	}
	else if (isSelect)
	{
		List options = parameter.getOptions();
		if (options != null)
		{
			out.println("<select name=\""+parameter.getName()+"\">");
			if (!parameter.isMandatory()) {
			  out.println("<option value=\"\"></option>");
			}
			String selected = "";
			for (int i=0; i<options.size(); i++)
			{
				LocalizedOption option = (LocalizedOption) options.get(i);
				String name = option.getName();
				String value = option.getValue();
				selected = "";
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value.toLowerCase())) {
				  selected="selected=\"selected\"";
				}
				out.println("<option value=\""+value+"\" "+selected+">"+name+"</option>");
			}
			out.println("</select>");
		}
	}
	else if (parameter.isRadio())
	{
		List radios = parameter.getOptions();
		if (radios != null)
		{
			for (int i=0; i<radios.size(); i++)
			{
				LocalizedOption radio = (LocalizedOption) radios.get(i);
				String name = radio.getName();
				String value =  radio.getValue();
				String checked = "";
				if (i==0) {
					checked = "checked";
				}
				out.println("<input type=\"radio\" name=\""+parameter.getName()+"\" value=\""+value+"\" "+checked+"/>");
				out.println(name+"&nbsp;<br>");
			}
		}
	}
	else {
		// check if parameter is mandatory or not
		boolean mandatory = parameter.isMandatory();

		String sSize = "60";
		if (parameter.getSize() != null && parameter.getSize().intValue() > 0) {
			sSize = parameter.getSize().toString();
		}

		out.println("<input type=\"text\" name=\""+parameter.getName()+"\" size=\""+sSize+"\" maxlength=\"399\" value=\""+EncodeHelper.javaStringToHtmlString(parameter.getValue())+"\" "+disabled+"/>");

		if (mandatory)
		{
			out.println("&nbsp;<img src=\""+resource.getIcon("mandatoryField")+"\" width=\"5\" height=\"5\" border=\"0\"/>");
		}
	}

	out.println("</td>");
}

%>

<%
LocalizedComponent 	component 			= (LocalizedComponent) request.getAttribute("WAComponent");
List 			parameters 			= (List) request.getAttribute("Parameters");
List 			hiddenParameters 	= (List) request.getAttribute("HiddenParameters");
ComponentInst[] brothers 			= (ComponentInst[]) request.getAttribute("brothers");
String 			spaceId				= (String) request.getAttribute("CurrentSpaceId");

String m_JobPeas = component.getLabel();
String m_ComponentType = component.getName();

String m_ComponentIcon = iconsPath+"/util/icons/component/"+m_ComponentType+"Small.gif";

LocalizedParameter parameter = null;

browseBar.setSpaceId(spaceId);
browseBar.setClickable(false);
browseBar.setPath(resource.getString("JSPP.creationInstance"));
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<view:includePlugin name="qtip"/>
<view:includePlugin name="popup"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="javascript/component.js"></script>
<script type="text/javascript" src="javascript/messages.js"></script>
<script type="text/javascript">

function B_ANNULER_ONCLICK() {
	location.href = "ListComponent";
}

/*****************************************************************************/
function B_VALIDER_ONCLICK() {
	if (isCorrectForm()) {
		<%
		for(int nI=0; parameters != null && nI < parameters.size(); nI++)
		{
			parameter = (LocalizedParameter) parameters.get(nI);
			if (parameter.isCheckbox()) {
			%>
                if (document.infoInstance.<%=parameter.getName()%>.checked)
		        	document.infoInstance.<%=parameter.getName()%>.value = "yes";
                 else
                  document.infoInstance.<%=parameter.getName()%>.value = "no";
		    <%
			}
			%>
			document.infoInstance.<%=parameter.getName()%>.disabled = false;
		<%
		}
		%>
		document.infoInstance.submit();
	}
}

/*****************************************************************************/

function isCorrectForm() {
	var errorMsg = "";
	var errorNb = 0;

	var name = stripInitialWhitespace(document.infoInstance.NameObject.value);
	var desc = document.infoInstance.Description;

	if (isWhitespace(name)) {
		errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("MustContainsText")%>\n";
		errorNb++;
	}

	var textAreaLength = 400;
	var s = desc.value;
	if (! (s.length <= textAreaLength)) {
		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("ContainsTooLargeText")+"400 "+resource.getString("Characters")%>\n";
	   	errorNb++;
	}

	<%
	for(int nI=0; parameters != null && nI < parameters.size(); nI++)
	{
		parameter = (LocalizedParameter) parameters.get(nI);
		if (parameter.isMandatory() && !parameter.isRadio())
		{
		%>
			var paramValue = stripInitialWhitespace(document.infoInstance.<%=parameter.getName()%>.value);
			if (isWhitespace(paramValue)) {
				errorMsg+="  - '<%=parameter.getLabel()%>' <%=resource.getString("MustContainsText")%>\n";
				errorNb++;
			}
		<%
		}
	}
	%>

	switch(errorNb)
	{
		case 0 :
		    result = true;
		    break;
		case 1 :
		    errorMsg = "<%=resource.getString("ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
		    window.alert(errorMsg);
		    result = false;
		    break;
		default :
		    errorMsg = "<%=resource.getString("ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
		    window.alert(errorMsg);
		    result = false;
		    break;
	}
	return result;
}

function toDoOnLoad() {
    document.infoInstance.NameObject.focus();
}
</script>
</HEAD>
<BODY id="admin-component" onload="javascript:toDoOnLoad()">
<FORM NAME="infoInstance" action="EffectiveCreateInstance" METHOD="POST">
	<input type="hidden" name="ComponentName" value="<%=component.getName()%>"/>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.type")%> :</td>
		<td><img src="<%=m_ComponentIcon%>" class="componentIcon" alt=""/>&nbsp;<%=m_JobPeas%></td>
	</tr>
	<%=I18NHelper.getFormLine(resource)%>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
		<td><input type="text" name="NameObject" size="60" maxlength="60">&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td valign="top"><textarea name="Description" rows="4" cols="59"></textarea></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("JSPP.ComponentPlace")%> :</td>
		<td>
			<SELECT name="ComponentBefore" id="ComponentBefore">
				<%
					for (int i = 0; i < brothers.length; i++)
					{
						out.println("<OPTION value=\"" + brothers[i].getId() + "\">" + brothers[i].getLabel() + "</OPTION>");
					}
				%>
				<OPTION value="-1" selected><%=resource.getString("JSPP.PlaceLast")%></OPTION>
			</SELECT>
		</td>
	</tr>
</table>
<% if (parameters.size() > 0) { %>
	<div id="parameters-header">
		<span class="txtlibform"><%=resource.getString("JSPP.parameters") %></span>
	</div>
<% } %>
<table border=0>
<tr>
<%
	boolean on2Columns = false;
	if (parameters.size() >= 5)
		on2Columns = true;

	if (on2Columns)
	{
		out.println("<td>");
		out.println("<table border=0 width=\"100%\">");
		for(int nI=0; parameters != null && nI < parameters.size(); nI++)
		{
			parameter = (LocalizedParameter) parameters.get(nI);
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
			parameter = (LocalizedParameter) parameters.get(nI);
			out.println("<tr>");
			displayParameter(parameter, resource, out);
			out.println("</tr>");
		}
	}

	for(int nI=0; hiddenParameters != null && nI < hiddenParameters.size(); nI++)
	{
		parameter = (LocalizedParameter) hiddenParameters.get(nI);

		out.println("<input type=\"hidden\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\"/>\n");
	}
%>
	<tr align="left">
		<td colspan="3">(<img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</td>
	</tr>
	</table>
<%
	out.println(board.printAfter());
	out.println("<br/>");
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
	out.println("<center>"+buttonPane.print()+"</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</FORM>

</BODY>
</HTML>