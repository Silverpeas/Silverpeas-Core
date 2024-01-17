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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/documentTemplate" prefix="docTemplateTags" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="zoneId" value="${sessionScope['SilverSessionController'].favoriteZoneId}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="documentTemplates" value="${requestScope.documentTemplateList}"/>
<jsp:useBean id="documentTemplates" type="java.util.List<org.silverpeas.core.documenttemplate.DocumentTemplate>"/>

<fmt:message var="browseBarAll" key="docTemplate.breadcrumb"/>
<fmt:message var="add" key="GML.add"/>
<c:url var="addIconUrl" value="/util/icons/create-action/add-default.png"/>
<fmt:message var="refresh" key="documentTemplates.refresh"/>
<fmt:message var="modify" key="GML.modify"/>
<fmt:message var="delete" key="GML.delete"/>
<fmt:message var="deleteConfirm" key="GML.confirmation.delete">
  <fmt:param value="{{name}}"/>
</fmt:message>
<fmt:message var="noDocumentTemplate" key="documentTemplates.none"/>

<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="preview"/>
    <script type="text/javascript">
      let uriBase, ui;
      whenSilverpeasReady(function() {
        uriBase = webContext + '/RdocumentTemplates/jsp/';
        ui = new function() {
          this.window = top;
          this.form = top.sp.form;
          this.popup = top.sp.popup;
          this.spProgressMessage = top.spProgressMessage;
        };
      });
      function addDocumentTemplate() {
        ui.popup.load(uriBase + 'new').show('validation', {
          title : '${silfn:escapeJs(add)}',
          callback : function() {
            return performSave();
          }
        });
      }
      function modifyDocumentTemplate(id, name) {
        ui.popup.load(uriBase + 'modify/' + id).show('validation', {
          title : '${silfn:escapeJs(modify)} ' + name,
          callback : function() {
            return performSave(id);
          }
        });
      }
      function performSave(id) {
        return ui.window.checkDocumentTemplateForm(function(data) {
          ui.spProgressMessage.show();
          return new Promise(function(resolve) {
            sp.ajaxRequest(!id ? 'create' : ('update/' + id))
                .withParams(data)
                .byPostMethod()
                .send()
                .then(function() {
                  smoothReload();
                  resolve();
                }, ui.spProgressMessage.hide);
          });
        });
      }
      function deleteDocumentTemplate(id, name) {
        const confirmMsg = '${silfn:escapeJs(deleteConfirm)}'.replace('{{name}}', name)
        ui.popup.confirm(confirmMsg, function() {
          ui.spProgressMessage.show();
          return new Promise(function(resolve) {
            return sp.ajaxRequest('deleteSelected')
                .withParam('id', id)
                .byPostMethod()
                .send()
                .then(function() {
                  smoothReload();
                  resolve();
                }, ui.spProgressMessage.hide);
          });
        });
      }
      function refreshList() {
        ui.spProgressMessage.show();
        return sp.ajaxRequest('refreshList')
            .send()
            .then(smoothReload);
      }

      function smoothReload() {
        ui.spProgressMessage.show();
        sp.ajaxRequest('Main').send().then(function(request) {
          sp.updateTargetWithHtmlContent('#document-template-container', request.responseText, true);
          ui.spProgressMessage.hide();
        }, ui.spProgressMessage.hide)
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="page_content_admin document_template_admin">
    <view:browseBar extraInformations="${browseBarAll}"/>
    <view:operationPane>
      <view:operationOfCreation action="javascript:addDocumentTemplate()" icon="${addIconUrl}" altText="${add}"/>
      <view:operationSeparator/>
      <view:operation action="javascript:refreshList()" icon="" altText="${refresh}"/>
    </view:operationPane>
    <view:window>
      <view:frame>
        <view:areaOfOperationOfCreation/>
        <div id="document-template-container">
          <c:if test="${empty documentTemplates}">
            <div class="inlineMessage">${noDocumentTemplate}</div>
          </c:if>
          <docTemplateTags:documentTemplateList list="${documentTemplates}"/>
        </div>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>
