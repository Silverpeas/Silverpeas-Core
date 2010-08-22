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