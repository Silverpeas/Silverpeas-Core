<view:tabs >
  <view:tab label="${wall}" action="ALL?userId=${id}" selected="false" />
  <view:tab label="${infos}" action="MyInfos?userId=${id}" selected="true" />
  <view:tab label="${events}" action="MyEvents?userId=${id}" selected="false" />
  <view:tab label="${publications}" action="MyPubs?userId=${id}" selected="false" />
  <view:tab label="${photos}" action="MyPhotos?userId=${id}" selected="false" />
</view:tabs> 
<view:frame title="Informations professionnelles &amp; Coordonnées ">
  <view:board>
    <table id="userFullInfo" border="0" cellspacing="0" cellpadding="5" width="100%">
      <tr>
        <td class="txtlibform" valign="baseline" width="30%"><fmt:message key="GML.position" bundle="${GML}"/></td>
        <td valign="baseline">
          <fmt:message key="${snUserFull.userFull.accessLevel}" var="position" />
          <fmt:message key="${position}" bundle="${GML}"/>
        </td>
      </tr>
      <tr>
        <td class="txtlibform" valign="baseline" width="30%"><fmt:message key="GML.eMail" bundle="${GML}"/></td>
        <td >
          ${snUserFull.userFull.eMail}
        </td>
      </tr>
      <c:forEach items="${propertiesKey}" var="propertys" varStatus="status">
        <tr>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
          <td class="txtlibform" valign="baseline" width="30%">
            ${propertiesKey[status.index]}
          </td>
          <td >
            <input type="text" id="${properties[status.index]}" name="prop_${properties[status.index]}" size="50" maxlength="99" value="${propertiesValue[status.index]}" />
          </td>
        </tr>
      </c:forEach>
    </table>
  </view:board>
</view:frame> 
          <script type="text/javascript">
  desabledFields();
</script>


