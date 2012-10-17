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

<%@page import="com.silverpeas.admin.localized.LocalizedOption"%>
<%@page import="com.silverpeas.admin.localized.LocalizedParameter"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%!

void displayParameter(LocalizedParameter parameter, ResourcesWrapper resource, JspWriter out) throws java.io.IOException {
	String help = parameter.getHelp();

	if (help != null) {
		help = EncodeHelper.javaStringToHtmlString(help);
	  	out.println("<td valign=\"top\" align=\"left\">");
		out.print("<img src=\""+resource.getIcon("JSPP.instanceHelpInfo")+"\" title=\""+help+"\" class=\"parameterInfo\"/>");
		out.println("</td>");
	} else {
		out.println("<td width=\"15\">&nbsp;</td>");
	}
	out.println("<td class=\"intfdcolor4\" nowrap=\"nowrap\" valign=\"center\" align=\"left\">");
	out.println("<span class=\"txtlibform\">"+parameter.getLabel()+" : </span>");
	out.println("</td>");
	out.println("<td class=\"intfdcolor4\" align=\"left\" valign=\"top\">");

	String disabled = "disabled";
	if (parameter.isAlwaysUpdatable()) {
        disabled = "";
    }

	if (parameter.isCheckbox()) {
		String checked = "";
		if (StringUtil.getBooleanValue(parameter.getValue())) {
			checked = "checked";
		}
		out.println("<input type=\"checkbox\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\" "+checked+" "+disabled+">");
    if (StringUtil.isDefined(parameter.getWarning())) {
      out.println("<div style=\"display: none;\" id=\"warning-"+parameter.getName()+"\">"+parameter.getWarning()+"</div>");
    }
	} else if (parameter.isSelect() || parameter.isXmlTemplate()) {
		List<LocalizedOption> options = parameter.getOptions();
		if (options != null) {
			out.println("<select name=\""+parameter.getName()+"\">");
			if (!parameter.isMandatory()) {
			  out.println("<option value=\"\"></option>");
			}
			String selected = "";
			for (LocalizedOption option : options) {
				String name = option.getName();
				String value = option.getValue();
				selected = "";
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value.toLowerCase())) {
					selected = "selected=\"selected\"";
				}
				out.println("<option value=\""+value+"\" "+selected+">"+name+"</option>");
			}
			out.println("</select>");
		}
	} else if (parameter.isRadio()) {
		List<LocalizedOption> radios = parameter.getOptions();
		if (radios != null) {
			for (int i = 0; i < radios.size(); i++) {
	          LocalizedOption radio = radios.get(i);
	          String name = radio.getName();
	          String value = radio.getValue();
	          String checked = "";
	          if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value) || i == 0) {
	            checked = " checked";
	          }
	          out.println("<input type=\"radio\" name=\"" + parameter.getName() + "\" value=\"" + value + "\"" + checked + ">");
	          out.println(name + "&nbsp;<br/>");
        	}
		} else {
			out.println(parameter.getValue());
		}
		out.println("</td>");
	} else {
		// check if parameter is mandatory or not
		boolean mandatory = parameter.isMandatory();;

		String sSize = "60";
		if (parameter.getSize() != null && parameter.getSize().intValue() > 0) {
			sSize = parameter.getSize().toString();
		}

		String value = parameter.getValue();
		if (!StringUtil.isDefined(value)) {
		  value = "";
		}

		out.println("<input type=\"text\" name=\""+parameter.getName()+"\" size=\""+sSize+"\" maxlength=\"399\" value=\""+EncodeHelper.javaStringToHtmlString(value)+"\" "+disabled+"/>");

		if (mandatory) {
			out.println("&nbsp;<img src=\""+resource.getIcon("mandatoryField")+"\" width=\"5\" height=\"5\" border=\"0\"/>");
		}
	}
	out.println("</td>");
}

%>

<%
ComponentInst 	compoInst 			= (ComponentInst) request.getAttribute("ComponentInst");
String 			m_JobPeas 			= (String) request.getAttribute("JobPeas");
List<LocalizedParameter> parameters = (List<LocalizedParameter>) request.getAttribute("Parameters");
List<LocalizedParameter> hiddenParameters = (List<LocalizedParameter>) request.getAttribute("HiddenParameters");
List<ProfileInst> m_Profiles 		= (List<ProfileInst>) request.getAttribute("Profiles");
String			translation 		= (String) request.getParameter("Translation");
boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
int				scope				= ((Integer) request.getAttribute("Scope")).intValue();

if (scope == JobStartPagePeasSessionController.SCOPE_FRONTOFFICE) {
  //use default breadcrumb
  browseBar.setSpaceJavascriptCallback(null);
  browseBar.setComponentJavascriptCallback(null);
}

String m_ComponentIcon = iconsPath+"/util/icons/component/"+compoInst.getName()+"Small.gif";

browseBar.setComponentId(compoInst.getId());
browseBar.setExtraInformation(resource.getString("GML.modify"));
browseBar.setI18N(compoInst, resource.getLanguage());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.description"), "#", true);
for (ProfileInst theProfile : m_Profiles) {
	String profile = theProfile.getLabel();
	String prof = resource.getString(profile.replace(' ', '_'));
	if (prof.equals("")) {
		prof = profile;
	}
	tabbedPane.addTab(prof,"RoleInstance?IdProfile="+theProfile.getId()+"&NameProfile="+theProfile.getName()+"&LabelProfile="+theProfile.getLabel(),false);
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<view:includePlugin name="qtip"/>
<view:includePlugin name="popup"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript" src="javascript/component.js"></script>
<script type="text/javascript" src="javascript/messages.js"></script>
<script type="text/javascript">
function cancel() {
	location.href = "GoToCurrentComponent";
}

/*****************************************************************************/
function validate() {
	if (isCorrectForm()) {
		<%
		for(int nI=0; parameters != null && nI < parameters.size(); nI++) {
		  	LocalizedParameter parameter = parameters.get(nI);
			if (parameter.isCheckbox()) {
			%>
		    	if (document.infoInstance.<%=parameter.getName()%>.checked) {
		        	document.infoInstance.<%=parameter.getName()%>.value = "yes";
		    	} else {
		        	document.infoInstance.<%=parameter.getName()%>.value = "no";
		    	}
		    <%
			}
			%>
			document.infoInstance.<%=parameter.getName()%>.disabled = false;
		<% } %>
		document.infoInstance.submit();
	}
}

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
	for(int nI=0; parameters != null && nI < parameters.size(); nI++) {
	  	LocalizedParameter parameter = parameters.get(nI);
		if (parameter.isMandatory() && !parameter.isRadio()) {
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

	switch(errorNb) {
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

<%
for (String lang : compoInst.getTranslations().keySet()) {
	out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(compoInst.getLabel(lang))+"\";\n");
	out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(compoInst.getDescription(lang))+"\";\n");
}
%>

function showTranslation(lang) {
	showFieldTranslation('compoName', 'name_'+lang);
	showFieldTranslation('compoDesc', 'desc_'+lang);
}

function removeTranslation() {
	document.infoInstance.submit();
}

</script>
</head>
<body id="admin-component" onload="javascript:toDoOnLoad()">
<form name="infoInstance" action="EffectiveUpdateInstance" method="post">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table cellpadding="5" cellspacing="0" border="0" width="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.type")%> :</td>
		<td><img src="<%=m_ComponentIcon%>" class="componentIcon" alt=""/>&nbsp;<%=m_JobPeas%></td>
	</tr>
	<%=I18NHelper.getFormLine(resource, compoInst, translation)%>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
		<td><input type="text" name="NameObject" id="compoName" size="60" maxlength="60" value="<%=EncodeHelper.javaStringToHtmlString(compoInst.getLabel())%>">&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"></td>
	</tr>
	<tr>
		<td class="txtlibform" valign="top"><%=resource.getString("GML.description")%> :</td>
		<td><textarea name="Description" id="compoDesc" rows="3" cols="59"><%=EncodeHelper.javaStringToHtmlString(compoInst.getDescription())%></textarea></td>
	</tr>
	<% if (isInHeritanceEnable) { %>
	<tr>
		<td class="txtlibform" nowrap="nowrap" valign="top"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
		<td align="left" valign="baseline" width="100%">
		<% if (compoInst.isInheritanceBlocked()) { %>
			<input type="radio" name="InheritanceBlocked" value="true" checked="checked" /> <%=resource.getString("JSPP.inheritanceComponentNotUsed")%><br/>
			<input type="radio" name="InheritanceBlocked" value="false" /> <%=resource.getString("JSPP.inheritanceComponentUsed")%>
		<% } else { %>
			<input type="radio" name="InheritanceBlocked" value="true"/> <%=resource.getString("JSPP.inheritanceComponentNotUsed")%><br/>
			<input type="radio" name="InheritanceBlocked" value="false" checked="checked" /> <%=resource.getString("JSPP.inheritanceComponentUsed")%>
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
	if (parameters.size() >= 5) {
		on2Columns = true;
	}

	if (on2Columns) {
		out.println("<td>");
		out.println("<table border=\"0\" width=\"100%\">");
		for(int nI=0; parameters != null && nI < parameters.size(); nI++) {
		  	LocalizedParameter parameter = parameters.get(nI);
			if (nI%2 == 0) {
				out.println("<tr valign=\"middle\">");
			}

			displayParameter(parameter, resource, out);

			if (nI%2 != 0) {
				out.println("</tr>");
			} else {
				out.println("<td width=\"40px\">&nbsp;</td>");
			}
		}
		if (parameters.size()%2 != 0) {
			out.println("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("</td>");
	} else {
		for(LocalizedParameter parameter : parameters) {
		  	out.println("<tr valign=\"middle\">");
			displayParameter(parameter, resource, out);
			out.println("</tr>");
		}
	}

	for(int nI=0; hiddenParameters != null && nI < hiddenParameters.size(); nI++) {
	  	LocalizedParameter parameter = hiddenParameters.get(nI);
		out.println("<input type=\"hidden\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\"/>\n");
	}
%>
	<tr align="left">
		<td colspan="3"><br/>(<img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%>)</td>
	</tr>
</table>
<%
	out.println(board.printAfter());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton( gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=validate();", false));
	buttonPane.addButton( gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=cancel();", false));
	out.println("<br/><center>"+buttonPane.print()+"</center>");
	out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</form>
</body>
</html>