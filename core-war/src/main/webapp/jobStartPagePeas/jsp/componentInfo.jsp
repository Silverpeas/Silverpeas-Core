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
<%@page import="org.apache.commons.lang3.BooleanUtils"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.i18n.I18NHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ include file="check.jsp" %>

<%!

void displayParameter(LocalizedParameter parameter, MultiSilverpeasBundle resource, JspWriter out) throws java.io.IOException {
  out.println("<li class='field' id='"+parameter.getName()+"'>");
  String help = parameter.getHelp();
	if (help != null) {
	  help = EncodeHelper.javaStringToHtmlString(help);
		out.print("<img src=\""+resource.getIcon("JSPP.instanceHelpInfo")+"\" title=\""+help+"\" class=\"parameterInfo\"/>");
	}

	out.println("<label class='txtlibform'>");
	out.println(parameter.getLabel());
	out.println("</label>");
	out.println("<div class='champs'>");

	// Value
	boolean isSelect = parameter.isSelect() || parameter.isXmlTemplate();
	if (parameter.isCheckbox()) {
		String checked = "";
		if (StringUtil.getBooleanValue(parameter.getValue())){
			checked = "checked=\"checked\"";
        }
		out.println("<input type=\"checkbox\" name=\""+parameter.getName()+"\" value=\""+parameter.getValue()+"\" "+checked+" disabled=\"disabled\"/>");
	} else if (isSelect) {
		List<LocalizedOption> options = parameter.getOptions();
		if (options != null) {
			for (LocalizedOption option : options) {
				String value = option.getValue();
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value.toLowerCase())) {
					out.println(option.getName());
				}
			}
		}
	} else if (parameter.isRadio()) {
		List<LocalizedOption> radios = parameter.getOptions();
		if (radios != null) {
	      for (int i=0; i<radios.size(); i++) {
          LocalizedOption radio = radios.get(i);
				String value = radio.getValue();
				String checked = "";
				if (parameter.getValue() != null && parameter.getValue().toLowerCase().equals(value) || i==0) {
					checked = "checked=\"checked\"";
                }
				out.println("<input type=\"radio\" name=\""+parameter.getName()+"\" value=\""+value+"\" "+checked+" disabled=\"disabled\"/>");
				out.println(radio.getName()+"&nbsp;");
			}
		}
	} else {
		if (StringUtil.isDefined(parameter.getValue())) {
			out.println(parameter.getValue());
		}
	}
	out.println("</div></li>");
}
%>

<%
ComponentInst 	compoInst 			= (ComponentInst) request.getAttribute("ComponentInst");
WAComponent 		m_JobPeas 			= (WAComponent) request.getAttribute("JobPeas");
AllComponentParameters parameters = (AllComponentParameters) request.getAttribute("Parameters");
List<ProfileInst> m_Profiles 		= (List<ProfileInst>) request.getAttribute("Profiles");
boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
int				scope				= ((Integer) request.getAttribute("Scope")).intValue();
int	 			maintenanceState 	= (Integer) request.getAttribute("MaintenanceState");
boolean			popupMode			= BooleanUtils.toBoolean((Boolean) request.getAttribute("PopupMode"));
boolean descDefined = StringUtil.isDefined(compoInst.getDescription(resource.getLanguage()));

if (scope == JobStartPagePeasSessionController.SCOPE_FRONTOFFICE) {
  //use default breadcrumb
  browseBar.setSpaceJavascriptCallback(null);
  browseBar.setComponentJavascriptCallback(null);
}

TabbedPane tabbedPane = gef.getTabbedPane();

browseBar.setComponentId(compoInst.getId());
browseBar.setExtraInformation(resource.getString("GML.description"));
browseBar.setI18N(compoInst, resource.getLanguage());

operationPane.addOperation(resource.getIcon("JSPP.instanceUpdate"),resource.getString("JSPP.ComponentPanelModifyTitle"),"javascript:onClick=updateInstance()");
if (scope == JobStartPagePeasSessionController.SCOPE_BACKOFFICE) {
	operationPane.addOperation(resource.getIcon("JSPP.ComponentOrder"),resource.getString("JSPP.ComponentOrder"),"javascript:onClick=openPopup('PlaceComponentAfter', 750, 230)");
	operationPane.addLine();
	if (JobStartPagePeasSettings.useComponentsCopy) {
		operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.component.copy"),"javascript:onclick=clipboardCopy()");
		if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) {
			operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.component.cut"),"javascript:onclick=clipboardCut()");
		}
		operationPane.addLine();
	}
	operationPane.addOperation(resource.getIcon("JSPP.instanceDel"),resource.getString("JSPP.ComponentPanelDeleteTitle"),"javascript:onClick=deleteInstance()");
}

tabbedPane.addTab(resource.getString("GML.description"),"#",true);

for (ProfileInst theProfile : m_Profiles) {
	String profile = theProfile.getLabel();
	tabbedPane.addTab(profile,"RoleInstance?IdProfile="+theProfile.getId()+"&NameProfile="+theProfile.getName()+"&LabelProfile="+theProfile.getLabel(),false);
}

window.setPopup(popupMode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<link type="text/css" href="stylesheet/component.css" rel="stylesheet" />
<view:includePlugin name="qtip"/>
<view:includePlugin name="popup"/>
<script type="text/javascript" src="javascript/component.js"></script>
<script type="text/javascript">
var currentLanguage = "<%=compoInst.getLanguage()%>";
<%
	for (String lang : compoInst.getTranslations().keySet()) {
		out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(EncodeHelper.javaStringToHtmlString(compoInst.getLabel(lang)))+"\";\n");
		out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(EncodeHelper.javaStringToHtmlString(compoInst.getDescription(lang)))+"\";\n");
	}
%>

function showTranslation(lang) {
	<%=I18NHelper.updateHTMLLinks(compoInst)%>
	$(".title-principal").html(eval("name_"+lang));
  $(".descriptionType").html(eval("desc_"+lang));
	currentLanguage = lang;
}

function openPopup(action, larg, haut) {
	url = action;
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function deleteInstance() {
  jQuery.popup.confirm(
      "<%=resource.getStringWithParams("JSPP.MessageSuppressionInstance", EncodeHelper.escapeXml(compoInst.getLabel()))%>",
      function() {
        var $form = jQuery('#infoInstance');
        jQuery('#ComponentNum', $form).val('<%=compoInst.getId()%>');
        $form.attr('action', 'DeleteInstance').submit();
      });
}

function updateInstance() {
  var $form = jQuery('#infoInstance');
  jQuery('#ComponentNum', $form).val('<%=compoInst.getId()%>');
  jQuery('#Translation', $form).val(currentLanguage);
  $form.attr('action', 'UpdateInstance').submit();
}

function clipboardCopy() {
    top.IdleFrame.location.href = 'copy?Object=Component&Id=<%=compoInst.getId()%>';
}

function clipboardCut() {
    top.IdleFrame.location.href = 'Cut?Type=Component&Id=<%=compoInst.getId()%>';
}
</script>
</head>
<body id="admin-component">

<%
out.println(window.printBefore());
out.println(tabbedPane.print());

%>
<view:frame>
  <% if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) { %>
	<div class="inlineMessage"> <%=resource.getString("JSPP.maintenanceStatus."+maintenanceState)%> </div>
	<br clear="all"/>
  <% } %>
  <form id="infoInstance" name="infoInstance" action="" method="post">
    <input id="ComponentNum" name="ComponentNum" type="hidden"/>
    <input id="Translation"  name="Translation"  type="hidden"/>

    <div class="rightContent">
      <% if (!popupMode) { %>
        <% if (scope == JobStartPagePeasSessionController.SCOPE_FRONTOFFICE) { %>
          <div id="backToApplication">
            <a class="navigation-button" href="<%=compoInst.getPermalink() %>"><span><%=resource.getString("JSPP.application.back")%></span></a>
          </div>
        <% } else { %>
          <div id="goToApplication">
            <a class="navigation-button" href="<%=compoInst.getPermalink() %>"><span><%=resource.getString("JSPP.application.go")%></span></a>
          </div>
        <% } %>
      <% } %>
      <viewTags:displayLastUserCRUD permalink="<%=compoInst.getPermalink() %>"
                                    displayHour="true"
                                    createDate="<%=compoInst.getCreateDate() %>" createdById="<%=compoInst.getCreatorUserId() %>"
                                    updateDate="<%=compoInst.getUpdateDate() %>" updatedById="<%=compoInst.getUpdaterUserId() %>"/>
    </div>

    <div class="principalContent">
      <h2 class="title-principal"><%=EncodeHelper.javaStringToHtmlString(compoInst.getLabel(resource.getLanguage()))%></h2>
      <div class="general-info">
        <img class="icons-application" alt="" src="../../util/icons/component/<%=compoInst.getName()%>Big.png" />
        <div class="general-info-type">
          <b>Type : </b><%=m_JobPeas.getLabel(resource.getLanguage())%>
          <% if (descDefined) { %>
          <img class="applicationInfo" src="../../util/icons/info.gif" title="<%=EncodeHelper.javaStringToHtmlString(m_JobPeas.getDescription(resource.getLanguage()))%>"/>
          <% } %>
        </div>
        <% if (isInHeritanceEnable) { %>
        <div class="general-info-droit">
          <b><%=resource.getString("JSPP.inheritanceBlockedComponent") %> : </b>
          <% if (compoInst.isInheritanceBlocked()) { %>
            <%=resource.getString("JSPP.inheritanceComponentNotUsed")%>
          <% } else { %>
            <%=resource.getString("JSPP.inheritanceComponentUsed")%>
          <% } %>
        </div>
        <% } %>
      </div>
      <% if (descDefined) { %>
        <p class="descriptionType"><%=EncodeHelper.javaStringToHtmlParagraphe(compoInst.getDescription(resource.getLanguage()))%></p>
      <% } else { %>
        <div class="inlineMessage"><%=m_JobPeas.getDescription(resource.getLanguage())%></div>
      <% } %>
    </div>

    <% if (parameters.isVisible()) { %>
      <fieldset class="skinFieldset parameters readOnly">
      <legend><%=resource.getString("JSPP.parameters") %></legend>
      <ul class="fields">
        <%
			    for(LocalizedParameter parameter : parameters.getUngroupedParameters().getVisibleParameters()) {
				    displayParameter(parameter, resource, out);
			    }

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
  </form>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>