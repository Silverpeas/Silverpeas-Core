<%--<%@ include file="check.jsp" %>--%>
<table width="100%"  border="0" >
  <tr>
    <td id="profilHeadPhote" height="100" width="100" style="vertical-align: top; background-repeat: no-repeat; background-image:url(http://localhost:8000/silverpeas/directory/jsp/icons/Photo_profil.jpg)"  >
      <div id="photoDiv" >
        <img src="<c:url value="${snUserFull.profilPhoto}"/>" width="80" height="90" border="0" />
      </div>
    </td>


    <td id="profilHeadinfo" height="100" width="40%" align="left" style="vertical-align: middle">
      <b>${snUserFull.userFull.lastName} ${snUserFull.userFull.firstName}</b><br><br>
      ${snUserFull.phone}<br><br>
      ${snUserFull.userFull.eMail}<br><br>
  <c:if test="${snUserFull.connected=='true'}">
    <img  src="<c:url value="/directory/jsp/icons/connected.jpg" />" border="1" width="10" height="10"
          alt="connected"/> <fmt:message key="directory.connected"  /> ${snUserFull.duration}

  </c:if>
  <c:if test="${snUserFull.connected=='false'}">
    <img  src="<c:url value="/directory/jsp/icons/deconnected.jpg" />" border="1" width="10" height="10"
          alt="connected"/>

  </c:if>

  <br>
  </td>
  <td id="profilHeadAction" height="100" width="60%" align="right" style="vertical-align: top">

    <table width="80px"  border="0" align="right" cellspacing="0" >

      <tr>
        <td align="right" width="50px" c>
          <div class="StatusDiv">

          </div>
          <script>
           
            getLastStatus('${urlGetLastStatus}');
          </script>
        </td>
        <td  id="" align="right" style="vertical-align: top">

          <a  href="#"  onclick="javascript:enableStatusZone()">

            <img  src=" <c:url value="/directory/jsp/icons/edit_button.gif" />" width="10" height="10"
                  alt="connected"/>
          </a>
        </td>

      </tr>


    </table>

  </td>

</tr>
</table>

<div id="boxes">
  <div id="dialog" class="window">
    <div align="right">
      <a href="#"class="close">Fermer</a>
    </div>
    
    <br>
    <view:board>
      <form Name="photoForm" action="valdateChangePhoto" Method="post" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
        <table cellpadding="5" width="100%">
          <tr>
            <td class="txtlibform"><fmt:message key="directory.photo" />:</td>
          <td><input type="file" name="WAIMGVAR0" size="60"></td>
          </tr>
          <tr>
            <td class="txtlibform" colspan="2" align="center">
              <br>
              <br>
          <fmt:message key="directory.buttonValid" var="valid"/>
         <view:button label="${valid}" action="javascript:document.photoForm.submit();" disabled="false" />
          <%--<input type="submit" value="Valider" >--%>
          </td>
          </tr>
        </table>
      </form>
    </view:board>
  </div>
  <!-- Mask to cover the whole screen -->
  <div id="mask"></div>
</div>

