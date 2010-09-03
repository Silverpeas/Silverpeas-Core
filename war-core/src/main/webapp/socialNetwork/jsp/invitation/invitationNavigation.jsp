<div class="box" >
  <ul>
    <li>
      <div id="myInfo">
        <a href="${urlProfil}${id}">
          <span>${user.lastName} ${user.firstName}</span>
        </a>
      </div>
      <a href="${urlProfil}${id}">
        <img src="<c:url value="${user.profilPhoto}" />"  alt="viewUser" />
      </a>
    </li>
  </ul>
</div>
<div class="box" id="relationshipActionNewsFeed">
  <ul>
    <li >
      <a href="${urlNewsFeed}"><span><fmt:message key="newsFeed.newsFeed" /></span></a>
    </li>
    <li>
      <a href="${urlContactsDirectory}"><span><fmt:message key="newsFeed.contacts" /></span></a>
    </li>
    <li>
      <a href="${urlDirectory}"><span><fmt:message key="newsFeed.directory" /></span></a>
    </li>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

  </ul>

</div>