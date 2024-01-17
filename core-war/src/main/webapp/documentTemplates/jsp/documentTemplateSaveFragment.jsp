<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ page import="org.silverpeas.core.ui.DisplayI18NHelper" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="zoneId" value="${sessionScope['SilverSessionController'].favoriteZoneId}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message var="nameLabel" key="GML.name"/>
<fmt:message var="descriptionLabel" key="GML.description"/>
<fmt:message var="nameMandatoryError" key="docTemplate.save.name.error.mandatory"/>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<c:set var="documentTemplate" value="${requestScope.documentTemplate}"/>
<jsp:useBean id="documentTemplate" type="org.silverpeas.core.documenttemplate.DocumentTemplate"/>

<c:set var="languages" value="<%=DisplayI18NHelper.getLanguages()%>"/>

<div class="document-template-form">
  <view:form name="document-template-form" action="#" method="POST">
    <div class="fields">
      <div class="field">
        <label class="txtlibform">${nameLabel}</label>
        &nbsp;<img src="${mandatoryIcons}" width="5" height="5" alt="">
      </div>
      <c:forEach var="language" items="${languages}">
        <div class="field languages">
          <label class="txtlibform" for="doc_template_name_${language}">${language}</label>
          <div class="champs">
            <input id="doc_template_name_${language}" name="name-${language}" value="${documentTemplate.existNameTranslationIn(language) ? documentTemplate.getName(language) : ''}" size="50" maxlength="255">
          </div>
        </div>
      </c:forEach>
      <div class="field">
        <label class="txtlibform">${descriptionLabel}</label>
      </div>
      <c:forEach var="language" items="${languages}">
        <div class="field languages">
          <label class="txtlibform" for="doc_template_description_${language}">${language}</label>
          <div class="champs">
            <textarea id="doc_template_description_${language}" name="description-${language}" cols="50" rows="3" maxlength="255">${documentTemplate.existDescriptionTranslationIn(language) ? documentTemplate.getDescription(language) : ''}</textarea>
          </div>
        </div>
      </c:forEach>
      <div class="field file-upload">
        <view:fileUpload multiple="false" infoInputs="false" jqueryFormSelector="form[name='document-template-form']"/>
        <c:if test="${not documentTemplate.persisted}">
          <img src="${mandatoryIcons}" width="5" height="5" alt="">
        </c:if>
      </div>
    </div>
  </view:form>
  <div class="legend">
    <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
    <fmt:message key='GML.requiredField'/>
  </div>
  <script type="text/javascript">
    function checkDocumentTemplateForm(callback) {
      const data = sp.form.serializeJson("form[name='document-template-form']");
      const _fileUploadApi = jQuery(".fileUpload").fileUpload('api');
      try {
        _fileUploadApi.checkNoFileSending();
      } catch (errorMsg) {
        notyInfo(errorMsg);
        return sp.promise.rejectDirectlyWith();
      }
      let nameFilled = false;
      <c:forEach var="language" items="${languages}">
      nameFilled = nameFilled || StringUtil.isDefined(data['name-${language}']);
      </c:forEach>
      if (!nameFilled) {
        SilverpeasError.add('${nameMandatoryError}');
      }
      <c:if test="${not documentTemplate.persisted}">
      if (_fileUploadApi.serializeArray().length === 0) {
        SilverpeasError.add("'<fmt:message key="GML.file"/>' <fmt:message key="GML.MustBeFilled"/>");
      }
      </c:if>
      if (!SilverpeasError.show()) {
        return callback(data);
      }
      return sp.promise.rejectDirectlyWith();
    }
    setTimeout(function() {
      document.querySelector('.document-template-form .languages input').select();
    }, 0);
  </script>
</div>
