<div class="box">
  <div class="boxTitle">
    <span>${keyContacts}</span>
  </div>
  <div class="boxAction">
    <div class="boxActionLeft">
      <a href="javascript:getAllContactsDirectory('${urlContactsDirectory}')"><span>${contactsNumber} ${keyContacts}</span></a>
    </div>
    <div class="boxActionRight">
      <a href="javascript:getAllContactsDirectory('${urlContactsDirectory}')"><span>${showAll}</span></a>
    </div>

  </div>

  <div class="boxContent">
    <c:forEach items="${contacts}" var="contact">
      <div class="boxContact">
        <a href="${urlProfil}${contact.userId}">
          <img src="<c:url value="${contact.profilPhoto}" />" >
               <br><span>${contact.lastName}</span><br>
          <span>${contact.firstName}</span>
        </a>
      </div>
    </c:forEach>
  </div>
</div>
<div class="box">
  <div class="boxTitle">
    <span>${keyCommonContacts}</span>
  </div>
  <div class="boxAction">
    <div class="boxActionLeft">
      <a href="javascript:getCommonContactsDirectory('${urlCommonContactsDirectory}')"><span>${commonContactsNumber} ${keyContacts}</span></a>
    </div>
    <div class="boxActionRight">
      <a href="javascript:getCommonContactsDirectory('${urlCommonContactsDirectory}')"><span>${showAll}</span></a>
    </div>
  </div>
  <div class="boxContent">
    <c:forEach items="${commonContacts}" var="contact">
      <div class="boxContact">
        <a href="${urlProfil}${contact.userId}">
          <img src="<c:url value="${contact.profilPhoto}" />" >
               <br><span>${contact.lastName}</span><br>
          <span>${contact.firstName}</span>
        </a>
      </div>
    </c:forEach>
  </div>
</div>

<%--<div class="box">
  <div class="boxTitle">
    <span>${keyCommonContacts}</span>
  </div>
  <div class="boxAction">
    <div class="boxActionLeft">
      <a href="#"><span>${commonContactsNumber} ${keyContacts}</span></a>
    </div>
    <div class="boxActionRight">
      <a href="#"><span>${showAll}</span></a>
    </div>

  </div>
<
  <div class="boxContent">
    <c:forEach items="${commonContacts}" var="contact">
      <div class="boxContact">
        <a href="${urlProfil}${commonContacts.userId}">
          <img src="<c:url value="${commonContacts.profilPhoto}" />" >
          <br><span>${commonContacts.lastName}</span><br>
          <span>${commonContacts.firstName}</span>
        </a>
      </div>
    </c:forEach>

  </div>

</div>
--%>