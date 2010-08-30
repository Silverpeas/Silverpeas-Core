<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
    <view:tab label="${wall}" action="ALL?userId=${id}" selected="${type=='ALL'}" />
    <view:tab label="${infos}" action="MyInfos?userId=${id}" selected="false" />
    <view:tab label="${events}" action="MyEvents?userId=${id}" selected="${type=='EVENT'}" />
     <view:tab label="${publications}" action="MyPubs?userId=${id}" selected="${type=='PUBLICATION'}" />
    <view:tab label="${photos}" action="MyPhotos?userId=${id}" selected="${type=='PHOTO'}" />
</view:tabs>
<div id="SocialInformations" class="SocialInformations">

</div>

<div id="socialIconsZone">
  <table cellpadding="10">
    <tr>


    <c:url var="SNIcon" value="/socialNetwork/jsp/icons/" />
    <c:choose>
      <c:when test="${type=='ALL'}">
        <td>
        <img src="${SNIcon}PUBLICATION_new.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.new.pub"/>
        </td>
        <td>
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.update.pub"/>
        </td>
        <td>
        <img src="${SNIcon}STATUS.gif" width="16" height="16" />  :<fmt:message key="profil.icon.staus"/>
        </td>
      </c:when>
      <c:when test="${type=='EVENT'}">
        <td>
        <img src="${SNIcon}EVENT_private.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.private.event"/>
        </td>
        <td>
        <img src="${SNIcon}EVENT_public.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.public.event"/>
        </td>
      </c:when>
      <c:when test="${type=='PUBLICATION'}">
        <td>
        <img src="${SNIcon}PUBLICATION_new.gif" align="bottom" width="16" height="16" />  :<fmt:message key="profil.icon.new.pub"/>
        </td>
        <td>
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.update.pub"/>
        </td>
      </c:when>
      <c:otherwise>

      </c:otherwise>
    </c:choose>
    </tr>
  </table>
</div>

<c:url var="urlServlet" value="/RmyContactJSON" />
<div id="getNext">

  <b><a href="#" style="color: blue" onclick="javascript:getNext('${urlServlet}','${id}','${type}');"><fmt:message
        key="profil.getNext"/></a></b>

</div>

<script type="text/javascript">
  getNext('${urlServlet}','${id}','${type}');
</script>