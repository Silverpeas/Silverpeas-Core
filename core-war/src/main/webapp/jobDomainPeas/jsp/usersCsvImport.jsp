<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %><%--

    Copyright (C) 2000 - 2019 Silverpeas

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

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle"/>

<%
	Domain 		domObject 		= (Domain)request.getAttribute("domainObject");

  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
  browseBar.setPath((String)request.getAttribute("groupsPath"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel withCheckFormScript="true" withFieldsetStyle="true"/>
<script language="JavaScript">
function SubmitWithVerif() {
  var csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
  var errorMsg = "";
  if (isWhitespace(csvFilefld)) {
    errorMsg = "<%=resource.getString("JDP.missingFieldStart")+resource.getString("JDP.csvFile")+resource.getString("JDP.missingFieldEnd")%>";
  } else {
    var ext = csvFilefld.substring(csvFilefld.length - 4);
    if (ext.toLowerCase() != ".csv") {
      errorMsg = "<%=resource.getString("JDP.errorCsvFile")%>";
    }
  }
  if (errorMsg == "") {
    document.csvFileForm.submit();
  } else {
    jQuery.popup.error(errorMsg);
  }
}

$(document).ready(function(){
  $("#form-row-extra-message").hide();
  $('#sendEmailId').on('change', function() {
    if ($(this).is(':checked')) {
      $("#form-row-extra-message").show();
    } else {
      $("#form-row-extra-message").hide();
    }
  });
});
</script>
</head>
<body class="page_content_admin">
<%
  out.println(window.printBefore());
%>
<div class="inlineMessage">
  <fmt:message key="JDP.csvImport.help">
    <fmt:param value="<%=domObject.getPropFileName()%>"/>
  </fmt:message>
</div>
<form name="csvFileForm" action="usersCsvImport" method="post" enctype="multipart/form-data">
  <fieldset id="identity-main" class="skinFieldset">
    <div class="fields">
      <div class="field" id="form-row-csvFile">
        <label class="txtlibform"><%=resource.getString("JDP.csvFile") %></label>
        <div class="champs">
          <input type="file" name="file_upload" size="50" maxlength="50"/>&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/>
        </div>
      </div>
      <div class="field" id="sendEmailTRid">
        <label class="txtlibform"><fmt:message key="JDP.sendEmail" /></label>
        <div class="champs">
          <input type="checkbox" name="sendEmail" id="sendEmailId" value="true" />&nbsp;<fmt:message key="GML.yes" />
        </div>
      </div>
      <div class="field" id="form-row-extra-message">
        <label class="txtlibform"><fmt:message key="JDP.sendEmail.message"/></label>
        <div class="champs">
          <fmt:message key="JDP.sendEmail.message.help" var="extraMessageHelp"/>
          <textarea rows="3" cols="50" name="extraMessage" placeholder="${extraMessageHelp}"></textarea>
        </div>
      </div>
    </div>
  </fieldset>
  <div class="legend">
    <img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/>
    : <%=resource.getString("GML.requiredField")%>
  </div>
</form>
<%
  ButtonPane bouton = gef.getButtonPane();
  bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif()", false));
  bouton.addButton(gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
  out.println(bouton.print());
  out.println(window.printAfter());
%>
</body>
</html>