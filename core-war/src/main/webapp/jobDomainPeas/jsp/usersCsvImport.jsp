<%--

    Copyright (C) 2000 - 2022 Silverpeas

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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle"/>

<c:set var="fieldLabelsToImport" value="${requestScope.FieldLabelsToImport}"/>
<jsp:useBean id="fieldLabelsToImport" type="java.util.HashMap<java.lang.String, java.lang.String>"/>

<c:set var="mandatoryFieldLabels" value="${fieldLabelsToImport.get('MANDATORY')}"/>
<c:set var="complementaryFieldLabels" value="${fieldLabelsToImport.get('COMPLEMENTARY')}"/>
<c:if test="${not empty complementaryFieldLabels}">
  <c:set var="complementaryFieldLabels" value=";${complementaryFieldLabels}"/>
</c:if>
<c:set var="personalizedFieldLabels" value="${fieldLabelsToImport.get('PERSONALIZED')}"/>
<c:if test="${not empty personalizedFieldLabels}">
  <c:set var="personalizedFieldLabels" value=";${personalizedFieldLabels}"/>
</c:if>

<%
  Domain domObject = (Domain) request.getAttribute("domainObject");
  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain=" + domObject.getId());
  browseBar.setPath((String) request.getAttribute("groupsPath"));
%>

<fmt:message key="GML.yes" var="yesLabel" />
<fmt:message key="GML.no" var="noLabel" />
<fmt:message key="JDP.csvImport.help" var="fullMessage">
  <fmt:param value="<%=domObject.getPropFileName()%>"/>
  <fmt:param value="${mandatoryFieldLabels}${complementaryFieldLabels}${personalizedFieldLabels}"/>
</fmt:message>
<fmt:message key="JDP.csvImport.help" var="withoutPersonalizedMessage">
  <fmt:param value="<%=domObject.getPropFileName()%>"/>
  <fmt:param value="${mandatoryFieldLabels}${complementaryFieldLabels}"/>
</fmt:message>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true" withFieldsetStyle="true">
<script type="text/javascript">
function SubmitWithVerif() {
  const csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
  let errorMsg = "";
  if (isWhitespace(csvFilefld)) {
    errorMsg = "<%=resource.getString("JDP.missingFieldStart")+resource.getString("JDP.csvFile")+resource.getString("JDP.missingFieldEnd")%>";
  } else {
    const ext = csvFilefld.substring(csvFilefld.length - 4);
    if (ext.toLowerCase() !== ".ssv") {
      errorMsg = "<%=resource.getString("JDP.errorCsvFile")%>";
    }
  }
  if (errorMsg === "") {
    $.progressMessage();
    document.csvFileForm.submit();
  } else {
    jQuery.popup.error(errorMsg);
  }
}

$(document).ready(function(){
  <c:if test="${not empty personalizedFieldLabels}">
  $('.yesno').each(function() {
    const $yesno = $(this);
    $yesno.parent().find('input').on('change', function() {
      if ($(this).is(':checked')) {
        $yesno.html('${silfn:escapeJs(yesLabel)}');
      } else {
        $yesno.html('${silfn:escapeJs(noLabel)}');
      }
    });
  })
  $('#importExtraFormId').on('change', function() {
    if ($(this).is(':checked')) {
      $("#usersCsvImportInfo").html('${silfn:escapeJs(fullMessage)}');
    } else {
      $(this).parent().find('.yesno').html('${silfn:escapeJs(noLabel)}');
      $("#usersCsvImportInfo").html('${silfn:escapeJs(withoutPersonalizedMessage)}');
    }
  });
  </c:if>
  $('#sendEmailId').on('change', function() {
    if ($(this).is(':checked')) {
      $("#form-row-extra-message").show();
    } else {
      $("#form-row-extra-message").hide();
    }
  });
});
</script>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin">
<view:window>
<div class="inlineMessage" id="usersCsvImportInfo">${fullMessage}</div>
<form name="csvFileForm" action="usersCsvImport" method="post" enctype="multipart/form-data">
  <fieldset id="identity-main" class="skinFieldset">
    <legend style="display: none"></legend>
    <div class="fields">
      <div class="field" id="form-row-csvFile">
        <label class="txtlibform" for="file_upload_id"><%=resource.getString("JDP.csvFile") %></label>
        <div class="champs">
          <input type="file" name="file_upload" id="file_upload_id" size="50" maxlength="50"/>&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5" alt=""/>
        </div>
      </div>
      <div class="field" id="form-row-ignoreFirstLine">
        <label class="txtlibform" for="ignoreFirstLineId"><%=resource.getString("JDP.csvImport.ignoreFirstLine") %></label>
        <div class="champs">
          <input type="checkbox" name="ignoreFirstLine" id="ignoreFirstLineId" value="true" />&nbsp;<span class="yesno">${yesLabel}</span>
        </div>
      </div>
      <c:if test="${not empty personalizedFieldLabels}">
        <div class="field" id="form-row-importExtraForm">
          <label class="txtlibform" for="importExtraFormId"><fmt:message key="JDP.csvImport.extraForm" /></label>
          <div class="champs">
            <input type="checkbox" name="importExtraForm" id="importExtraFormId" checked="checked" value="true" />&nbsp;<span class="yesno">${yesLabel}</span>
          </div>
        </div>
      </c:if>
      <div class="field" id="sendEmailTRid">
        <label class="txtlibform" for="sendEmailId"><fmt:message key="JDP.sendEmail" /></label>
        <div class="champs">
          <input type="checkbox" name="sendEmail" id="sendEmailId" value="true" />&nbsp;<span class="yesno">${yesLabel}</span>
        </div>
      </div>
      <div class="field" id="form-row-extra-message" style="display: none">
        <label class="txtlibform" for="extraMessageId"><fmt:message key="JDP.sendEmail.message"/></label>
        <div class="champs">
          <fmt:message key="JDP.sendEmail.message.help" var="extraMessageHelp"/>
          <textarea rows="3" cols="50" name="extraMessage" id="extraMessageId" placeholder="${extraMessageHelp}"></textarea>
        </div>
      </div>
    </div>
  </fieldset>
  <div class="legend">
    <img src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5" alt=""/>
    : <%=resource.getString("GML.requiredField")%>
  </div>
</form>
  <view:buttonPane>
    <view:button label='<%=resource.getString("GML.validate")%>' action="javascript:SubmitWithVerif()"/>
    <view:button label='<%=resource.getString("GML.cancel")%>' action="domainContent"/>
  </view:buttonPane>
</view:window>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>