<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<c:set var="component" value="${requestScope.WAComponent}"/>
<jsp:useBean id="component" type="org.silverpeas.core.admin.component.model.WAComponent"/>

<c:set var="checkedAttr"> checked="checked"</c:set>

<%!

void displayParameter(LocalizedParameter parameter, MultiSilverpeasBundle resource, JspWriter out) throws java.io.IOException {
	out.println("<li class='field' id='"+parameter.getName()+"'>");
	String help = parameter.getHelp();
	if (help != null) {
		help = WebEncodeHelper.javaStringToHtmlString(help);
		out.print("<img src=\""+resource.getIcon("JSPP.instanceHelpInfo")+"\" title=\""+help+"\" class=\"parameterInfo\"/>");
	}

	out.println("<label class='txtlibform'>"+parameter.getLabel()+"</label>");

	out.println("<div class='champs'>");

	String disabled = "disabled=\"disabled\"";
	if (parameter.isAlwaysUpdatable() || parameter.isUpdatableOnCreationOnly()) {
      disabled = "";
	}

	if (parameter.isCheckbox()) {
		String checked = "";
		if (StringUtil.getBooleanValue(parameter.getValue())) {
			checked = "checked=\"checked\"";
		}
		out.println("<input type=\"checkbox\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\" "+checked+" "+disabled+">");
    if (parameter.getWarning().isPresent()) {
      out.println("<div style=\"display: none;\" id=\"warning-" + parameter.getName() + "\">" + parameter.getWarning().get().getValue() + "</div>");
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
      if (parameter.getWarning().isPresent()) {
        out.println("<div style=\"display: none;\" id=\"warning-" + parameter.getName() + "\"" +
            " initialParamValue=\"" + parameter.getValue() + "\">" + parameter.getWarning().get().getValue() + "</div>");
      }
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
	            checked = "checked=\"checked\"";
	          }
	          out.println("<input type=\"radio\" name=\"" + parameter.getName() + "\" value=\"" + value + "\"" + checked + ">");
	          out.println(name + "&nbsp;<br/>");
		}
		} else {
			out.println(parameter.getValue());
		}
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

		out.println("<input type=\"text\" name=\""+parameter.getName()+"\" size=\""+sSize+"\" maxlength=\"399\" value=\""+WebEncodeHelper.javaStringToHtmlString(value)+"\" "+disabled+"/>");

		if (mandatory) {
			out.println("&nbsp;<img src=\""+resource.getIcon("mandatoryField")+"\" width=\"5\" height=\"5\" border=\"0\"/>");
		}
	}
	out.println("</div></li>");
}

%>

<%
AllComponentParameters 	parameters 			= (AllComponentParameters) request.getAttribute("Parameters");
ComponentInst[] brothers 			= (ComponentInst[]) request.getAttribute("brothers");
String 			spaceId				= (String) request.getAttribute("CurrentSpaceId");
boolean isInHeritanceEnable = JobStartPagePeasSettings.isInheritanceEnable;

String m_JobPeas = component.getLabel(resource.getLanguage());

String m_ComponentIcon = iconsPath+"/util/icons/component/"+component.getName()+"Small.gif";

browseBar.setSpaceId(spaceId);
browseBar.setClickable(false);
browseBar.setPath(resource.getString("JSPP.creationInstance"));
%>

<view:sp-page>
<view:sp-head-part withFieldsetStyle="true" withCheckFormScript="true">
<view:link href="/jobStartPagePeas/jsp/stylesheet/component.css"/>
<view:includePlugin name="qtip"/>
<view:includePlugin name="popup"/>
<view:script src="/jobStartPagePeas/jsp/javascript/component.js"/>
<view:script src="/jobStartPagePeas/jsp/javascript/messages.js"/>
<script type="text/javascript">
function B_ANNULER_ONCLICK() {
	location.href = "ListComponent";
}

/*****************************************************************************/
function B_VALIDER_ONCLICK() {
	ifCorrectFormExecute(function() {
		<% for(LocalizedParameter parameter : parameters.getParameters().getVisibleParameters()) {
			if (parameter.isCheckbox()) {
			%>
      if (document.infoInstance.<%=parameter.getName()%>.checked) {
		    document.infoInstance.<%=parameter.getName()%>.value = "yes";
      } else {
        document.infoInstance.<%=parameter.getName()%>.value = "no";
      }
    <% } %>
			document.infoInstance.<%=parameter.getName()%>.disabled = false;
		<% } %>
		  document.infoInstance.submit();
  });
}

/*****************************************************************************/

function ifCorrectFormExecute(callback) {
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
	for(LocalizedParameter parameter : parameters.getParameters().getVisibleParameters()) {
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

	switch(errorNb){
		case 0 :
        callback.call(this);
		    break;
		case 1 :
		    errorMsg = "<%=resource.getString("ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
        jQuery.popup.error(errorMsg);
		    break;
		default :
		    errorMsg = "<%=resource.getString("ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
        jQuery.popup.error(errorMsg);
	}
}

function toDoOnLoad() {
  setTimeout(function() {
    document.infoInstance.NameObject.focus();
  }, 0);
}
</script>
</view:sp-head-part>
<view:sp-body-part id="admin-component" onLoad="toDoOnLoad()" cssClass="page_content_admin">
<form name="infoInstance" action="EffectiveCreateInstance" method="post">
	<input type="hidden" name="ComponentName" value="<%=component.getName()%>"/>
<%
out.println(window.printBefore());
%>
<view:frame>
<fieldset class="skinFieldset">
	<legend class="without-img"><img src="<%=m_ComponentIcon%>" class="componentIcon" alt=""/>&nbsp;<%=m_JobPeas%></legend>

  <% if (I18NHelper.isI18nContentEnabled()) { %>
  <ul class="fields">
    <li class="field">
      <label for="<%=I18NHelper.HTMLSelectObjectName%>" class="txtlibform"><%=resource.getString("GML.language")%></label>
      <div class="champs">
        <%=I18NHelper.getHTMLSelectObject(resource.getLanguage(), null, null) %>
      </div>
    </li>
  </ul>
  <% } %>

  <ul class="fields">
		<li class="field entireWidth">
		  <label class="txtlibform"><%=resource.getString("GML.name")%> </label>
		  <div class="champs"><input type="text" name="NameObject" id="compoName" size="60" maxlength="60" value=""/>&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"/></div>
	  </li>

	  <li class="field entireWidth">
		  <label class="txtlibform"><%=resource.getString("GML.description")%></label>
		  <div class="champs"><textarea name="Description" id="compoDesc" rows="3" cols="59"></textarea></div>
	  </li>

    <li class="field">
      <label class="txtlibform"><%=resource.getString("JSPP.ComponentPlace")%> :</label>
      <div class="champs">
        <select name="ComponentBefore" id="ComponentBefore">
          <% for (ComponentInst brother : brothers) { %>
              <option value="<%=brother.getId()%>"><%=WebEncodeHelper.javaStringToHtmlString(brother.getLabel())%></option>
          <% } %>
          <option value="-1" selected><%=resource.getString("JSPP.PlaceLast")%></option>
        </select>
      </div>
    </li>

    <% if (isInHeritanceEnable) { %>
      <li class="field entireWidth">
        <label class="txtlibform"><%=resource.getString("JSPP.inheritanceBlockedComponent") %></label>
        <div class="champs">
          <input class="radio" type="radio" name="InheritanceBlocked" value="true"${component.inheritSpaceRightsByDefault ? '' : checkedAttr} /> <%=resource.getString("JSPP.inheritanceComponentNotUsed")%><br/>
          <input class="radio newline" type="radio" name="InheritanceBlocked" value="false"${component.inheritSpaceRightsByDefault ? checkedAttr : ''}  /> <%=resource.getString("JSPP.inheritanceComponentUsed")%>
        </div>
      </li>
    <% } %>

  </ul>
</fieldset>
<% if (parameters.isVisible()) { %>
	 <fieldset class="skinFieldset parameters">
      <legend><%=resource.getString("JSPP.parameters") %></legend>

  <ul class="fields">
<%
	for(LocalizedParameter parameter : parameters.getUngroupedParameters().getVisibleParameters()) {
		displayParameter(parameter, resource, out);
	}
%>
<%
	for (LocalizedGroupOfParameters group : parameters.getGroupsOfParameters()) { %>
	<li class="group-field">
			<label class="group-field-name"><%=group.getLabel() %></label>
			<% if (StringUtil.isDefined(group.getDescription())) { %>
			<p class="group-field-description"><%=group.getDescription() %></p>
	        <% } %>
	        <ul>
	        <%
			for(LocalizedParameter parameter : group.getParameters().getVisibleParameters()) {
				displayParameter(parameter, resource, out);
			}
		%>
		</ul>
	</li>
     <% } %>

	</ul>
	</fieldset>
<% } %>

	<% for(LocalizedParameter parameter : parameters.getHiddenParameters()) {  %>
		<input type="hidden" name="<%=parameter.getName() %>" value="<%=parameter.getValue() %>"/>
	<% } %>

	<div class="legend"><img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%></div>

<%
	out.println("<br/>");
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
	out.println(buttonPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
</form>
</view:sp-body-part>
</view:sp-page>