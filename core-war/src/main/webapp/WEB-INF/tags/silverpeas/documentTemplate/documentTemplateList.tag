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

<%@ tag import="org.silverpeas.core.admin.user.model.User" %>
<%@ tag import="org.silverpeas.core.webapi.viewer.PreviewEntity" %>
<%@ tag import="org.silverpeas.core.viewer.service.PreviewService" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<jsp:useBean id="userLanguage" type="java.lang.String"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="list" required="true"
              type="java.util.List<org.silverpeas.core.documenttemplate.DocumentTemplate>"
              description="List of document template" %>

<%@ attribute name="readonly" required="false"
              type="java.lang.Boolean"
              description="If read only, no modification action are provided. False by default." %>
<c:if test="${readonly == null}">
  <c:set var="readonly" value="${false}"/>
</c:if>

<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="GML.preview.file" var="previewLabel"/>
<fmt:message key="GML.view.file" var="viewLabel"/>

<ul class="document-template-list">
  <c:forEach var="documentTemplate" items="${list}" varStatus="status">
    <jsp:useBean id="documentTemplate" type="org.silverpeas.core.documenttemplate.DocumentTemplate"/>
    <c:set var="id" value="${documentTemplate.id}"/>
    <c:set var="documentTemplateName" value="${documentTemplate.getName(userLanguage)}"/>
    <c:set var="documentTemplateNameSanitized" value="${silfn:sanitizeHtml(documentTemplateName)}"/>
    <c:set var="documentTemplateDesc" value="${documentTemplate.getDescription(userLanguage)}"/>
    <c:set var="openCallback" value="${documentTemplate.persisted ? 'modify' : 'select'}DocumentTemplate('${id}', '${silfn:sanitizeHtml(silfn:escapeJs(documentTemplateName))}')"/>
    <li onclick="${openCallback}" id="${id}">
      <a class="title" href="javascript:${openCallback}">
          ${documentTemplateName}
      </a>
      <div class="document-template-thumb">
        <c:set var="documentTemplatePreview" value="<%=PreviewEntity.createFrom(PreviewService.get().getPreview(documentTemplate.getViewerContext(userLanguage)))%>"/>
        <jsp:useBean id="documentTemplatePreview" type="org.silverpeas.core.webapi.viewer.PreviewEntity"/>
        <img src="${documentTemplatePreview.URL}" alt="${documentTemplateNameSanitized}">
        <c:if test="${not empty documentTemplate.restrictedToSpaceIds}">
          <c:set var="newStringArray" value="<%=new String[0]%>"/>
          <div class="restricted-space-ids inlineMessage">${fn:join(documentTemplate.restrictedToSpaceIds.toArray(newStringArray), ', ')}</div>
        </c:if>
        <c:if test="${not empty documentTemplateDesc}">
          <div class="description">${documentTemplateDesc}</div>
        </c:if>
      </div>
      <div class="actions">
        <a class="preview-button preview-file"
           href="javascript:void(0)"
           data-view-service="preview"
           data-document-id="${documentTemplatePreview.documentId}"
           data-document-type="${documentTemplatePreview.documentType}"
           title="${previewLabel}"></a>
        <a class="view-button view-file"
           href="javascript:void(0)"
           data-view-service="view"
           data-document-id="${documentTemplatePreview.documentId}"
           data-document-type="${documentTemplatePreview.documentType}"
           title="${viewLabel}"></a>
        <c:if test="${not readonly}">
          <a class="delete-button"
             href="javascript:void(0)"
             data-document-template-id="${id}"
             data-document-template-name="${documentTemplateNameSanitized}"
             title="${deleteLabel}"></a>
        </c:if>
      </div>
    </li>
  </c:forEach>
</ul>
<script type="text/javascript">
  whenSilverpeasReady(function() {
    sp.element.querySelectorAll(".document-template-list .preview-button.preview-file,.view-button.view-file").forEach(function($a) {
      $a.onclick = function(e) {
        e.preventDefault();
        e.stopPropagation();
        viewDocumentTemplate(this);
      };
    });
    sp.element.querySelectorAll(".document-template-list .delete-button").forEach(function($a) {
      $a.onclick = function(e) {
        e.preventDefault();
        e.stopPropagation();
        deleteDocumentTemplate(this.dataset.documentTemplateId, this.dataset.documentTemplateName);
      };
    });
    const $list = jQuery(".document-template-list");
    if ($list.sortable('instance')) {
      $list.sortable('destroy');
    }
    $list.sortable({
      opacity: 0.4,
      cursor: 'move',
      update: function() {
        const ids = jQuery(this).sortable('toArray');
        sp.ajaxRequest('sort')
            .byPostMethod()
            .send({
              ids : ids
            })
            ['catch'](smoothReload);
      }
    });
    if (!window.viewDocumentTemplate) {
      window.viewDocumentTemplate = function(target) {
        jQuery(target)[target.dataset.viewService]("document", {
          documentId: target.dataset.documentId,
          documentType: target.dataset.documentType,
          lang: '${userLanguage}'
        });
      };
    }
  });
</script>