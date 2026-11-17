<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<c:if test="${not empty publications}">
<div class="secteur-container latest-publications">
  <div class="latest-publications-main-container">
    <ul class="last-publication-list">
      <c:forEach var="publication" items="${publications}">
        <li>
          <a class="sp-link publication-name" href="${publication.permalink}"><c:out value="${publication.name}"/></a>
          <view:componentPath componentId="${publication.instanceId}" includeComponent="false"/>
          <span class="user-publication"><view:username userId="${publication.updaterId}"/></span>
          <p class="description-publication"><c:out value="${publication.description}"/></p>
        </li>
      </c:forEach>
    </ul>
  </div>
</div>
</c:if>
