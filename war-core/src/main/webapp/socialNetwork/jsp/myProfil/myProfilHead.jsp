<%--<%@ include file="check.jsp" %>--%>

<div id="photoAndInfo">
  <!--  <div id ="photoandInfo">-->
  <div id="profilHeadPhote">
    <img src="<c:url value="${snUserFull.profilPhoto}"/>" style="vertical-align: top" width="80" height="90" border="0" alt="viewUser" />
  </div>

  <div id="profilHeadinfo">
    <b>${snUserFull.userFull.lastName} ${snUserFull.userFull.firstName}</b><br/><br/>
    ${snUserFull.phone}<br/><br/>
    ${snUserFull.userFull.eMail}<br/><br/>
    <c:if test="${snUserFull.connected=='true'}">
      <img  src="<c:url value="/directory/jsp/icons/connected.jpg" />" border="1" width="10" height="10"
            alt="connected"/> <fmt:message key="directory.connected"  /> ${snUserFull.duration}

    </c:if>
    <c:if test="${snUserFull.connected=='false'}">
      <img  src="<c:url value="/directory/jsp/icons/deconnected.jpg" />" border="1" width="10" height="10"
            alt="connected"/>

    </c:if>

    <br/>
  </div>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
  <div id="profilHeadAction">

    <div class="StatusDiv" id="status">
    </div>
    <script type="text/javascript">
      getLastStatus('${urlGetLastStatus}');
    </script>
    <div id="actionStatus">

      <a  href="#"  onclick="javascript:enableStatusZone()">

        <img  src="<c:url value="/directory/jsp/icons/edit_button.gif" />" width="10" height="10"
              alt="connected" />
      </a>
    </div>
    <!-- </div>-->

  </div>


</div>
<div id="boxes">
  <div id="dialog" class="window">
    <div class="barre">
      <a href="#"class="close">Fermer</a>
    </div>    
    <br/>
    <view:board>
      <form name="photoForm" action="validateChangePhoto" Method="post" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
        <div>

          <div class="txtlibform"><fmt:message key="directory.photo" />:</div>
          <div><input type="file" name="WAIMGVAR0" size="60"/></div>


        </div>
      </form>
    </view:board>
    <div class="formButton">
<!--      <div class="button">-->
       <br/>
       <br/>
        --><fmt:message key="directory.buttonValid" var="valid"/>
        <view:button label="${valid}" action="javascript:document.photoForm.submit();" disabled="false" />
       
    </div>
  </div>
  <!-- Mask to cover the whole screen -->
  <div id="mask"></div>
</div>

