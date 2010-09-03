<%--<%@ include file="check.jsp" %>--%>
<div id="photoAndInfo">
  <div id="photo">
<img src="<c:url value="${snUserFull.profilPhoto}"/>" style="vertical-align: top" width="80" height="90" border="0" alt="viewUser" />
  </div>
  <div id="info">
    <ul>
      <li >
        <span class="userName">${snUserFull.userFull.lastName} ${snUserFull.userFull.firstName}</span>
      </li>
      <li>
        <span>${snUserFull.phone}</span>
      </li>
      <li>
        <span>${snUserFull.userFull.eMail}</span>
      </li>
      <li>
        <span>
          <c:if test="${snUserFull.connected=='true'}">
            <img  src="<c:url value="/directory/jsp/icons/connected.jpg" />" border="1" width="10" height="10"
                  alt="connected"/> <fmt:message key="directory.connected"  /> ${snUserFull.duration}

          </c:if>
          <c:if test="${snUserFull.connected=='false'}">
            <img  src="<c:url value="/directory/jsp/icons/deconnected.jpg" />" border="1" width="10" height="10"
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
                  alt="connected"/>

          </c:if>
        </span>
      </li>
    </ul>
  </div>
</div>
<div id="status" class="StatusDiv">

</div>
<script type="text/javascript">  
              getLastStatus('${urlGetLastStatus}');
</script>