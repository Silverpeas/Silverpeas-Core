<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message key="GML.name" var="nameLabel"/>
<fmt:message key="GML.MustBeFilled" var="mustBeFilledErrMsg"/>
<fmt:message key="workflowDesigner.confirmGenerateComponentDescriptorJS" var="confirmGenerateMsg"/>
<fmt:message key="workflowDesigner.confirmProcessReferencedJS" var="confirmUpdateMsg"/>
<%
  String strProcessFileName = (String) request.getAttribute("ProcessFileName");
  String strCurrentTab = "ModifyWorkflow";
  ArrayPane processPane = gef.getArrayPane("processPane", strCurrentTab, request, session);
  ProcessModel processModel = (ProcessModel) request.getAttribute("ProcessModel");
  boolean fExistingProcess = !(Boolean) request.getAttribute("IsANewProcess");
%>

<c:set var="strComponentDescriptor" value="${requestScope.componentDescriptor}"/>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">

    function sendData() {
      // If saving a process model that is referenced in component descriptor
      // give a warning.
      if (isCorrectlyFilled()) {
        <c:choose>
        <c:when test="${silfn:isDefined(strComponentDescriptor)}">
        jQuery.popup.confirm('${silfn:escapeJs(confirmUpdateMsg)}', function() {
          document.workflowHeaderForm.submit();
        });
        </c:when>
        <c:otherwise>
        document.workflowHeaderForm.submit();
        </c:otherwise>
        </c:choose>
      }
    }

    function isCorrectlyFilled() {
      const title = stripInitialWhitespace(document.workflowHeaderForm.name.value);
      if (StringUtil.isNotDefined(title)) {
        SilverpeasError.add("<strong>${silfn:escapeJs(nameLabel)}</strong> ${silfn:escapeJs(mustBeFilledErrMsg)}")
      }
      return !SilverpeasError.show();
    }

    /**
     * If there is a component descriptor, confirm that you really want to overwrite it.
     */
    function editComponentDescription() {
      <c:choose>
      <c:when test="${silfn:isDefined(strComponentDescriptor)}">
      jQuery.popup.confirm('${silfn:escapeJs(confirmGenerateMsg)}', function() {
        location.href = "GenerateComponentDescription";
      });
      </c:when>
      <c:otherwise>
      location.href = "GenerateComponentDescription";
      </c:otherwise>
      </c:choose>
    }
  </script>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin">
<%
  browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
  browseBar.setComponentName(resource.getString("workflowDesigner.workflowHeader"), strCurrentTab);

  processPane.setTitle(resource.getString("workflowDesigner.workflowHeader"));

  // add an operation:  Generate the Component descriptor xml file.
  // Only if the description has not yet been defined but the workflow has already been saved
  //
  if (fExistingProcess) {
    operationPane.addOperation(resource.getIcon("workflowDesigner.generate.componentDescriptor"),
        resource.getString("workflowDesigner.generate.componentDescriptor"),
        "javascript:editComponentDescription();");
  }

  addContextualDesignation(operationPane, resource, "labels", "workflowDesigner.add.label",
      strCurrentTab);
  addContextualDesignation(operationPane, resource, "descriptions",
      "workflowDesigner.add.description", strCurrentTab);

  row = processPane.addArrayLine();
  cellText = row.addArrayCellText(resource.getString("GML.name"));
  cellText.setStyleSheet("txtlibform");
  cellInput = row.addArrayCellInputText("name", processModel.getName());
  cellInput.setSize("50");

  if (fExistingProcess) {
    row = processPane.addArrayLine();
    cellText = row.addArrayCellText(resource.getString("GML.path"));
    cellText.setStyleSheet("txtlibform");
    row.addArrayCellText(strProcessFileName);
  }

  out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ModifyWorkflow"/>
<view:frame>
<!-- help -->
<div class="inlineMessage">
	<table border="0"><tr>
		<td valign="absmiddle"><img border="0" src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
		<td><%=resource.getString("workflowDesigner.help.workflowHeader") %></td>
	</tr></table>
</div>
<br clear="all"/>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="post">
  <%
    out.println(processPane.print());
  %>
  <!-- Labels -->
  <br/>
  <designer:contextualDesignationList
      designations="<%=processModel.getLabels()%>"
      context="labels"
      parentScreen="<%=strCurrentTab%>"
      columnLabelKey="GML.label"
      paneTitleKey="workflowDesigner.list.label"/>

  <!-- Descriptions -->
  <br/>
  <designer:contextualDesignationList
      designations="<%=processModel.getDescriptions()%>"
      context="descriptions"
      parentScreen="<%=strCurrentTab%>"
      columnLabelKey="GML.description"
      paneTitleKey="workflowDesigner.list.description"/>

</form>
<designer:buttonPane cancelAction="Main"/>
</view:frame>
<%
  out.println(window.printAfter());
%>
</view:sp-body-part>
</view:sp-page>