<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
  <view:tab label="${wall}" action="ALL" selected="${type=='ALL'}" />
  <view:tab label="${infos}" action="MyInfos" selected="false" />
  <view:tab label="${events}" action="MyEvents" selected="${type=='EVENT'}" />
  <view:tab label="${publications}" action="MyPubs" selected="${type=='PUBLICATION'}" />
  <view:tab label="${photos}" action="MyPhotos" selected="${type=='PHOTO'}" />
</view:tabs>
<div class="SocialInformations" id="SocialInformations">

</div>
<div id="socialIconsZone">

    <c:url var="SNIcon" value="/socialNetwork/jsp/icons/" />
    <c:choose>
      <c:when test="${type=='ALL'}">
        <div class="icon">
        <img src="${SNIcon}PUBLICATION_new.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.new.pub"/>
        </div>
        <div class="icon">
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.update.pub"/>
        </div>
        <div class="icon">
        <img src="${SNIcon}STATUS.gif" width="16" height="16" />  :<fmt:message key="profil.icon.staus"/>
        </div>
      </c:when>
      <c:when test="${type=='EVENT'}">
        <div class="icon">
        <img src="${SNIcon}EVENT_private.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.private.event"/>
        </div>
        <div class="icon">
        <img src="${SNIcon}EVENT_public.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.public.event"/>
        </div>
      </c:when>
      <c:when test="${type=='PUBLICATION'}">
        <div class="icon">
        <img src="${SNIcon}PUBLICATION_new.gif" align="bottom" width="16" height="16" />  :<fmt:message key="profil.icon.new.pub"/>
        </div>
        <div class="icon">
        <img src="${SNIcon}PUBLICATION_update.gif"  width="16" height="16" />  :<fmt:message key="profil.icon.update.pub"/>
        </div>
      </c:when>
      <c:otherwise>

      </c:otherwise>
    </c:choose>
</div>

<c:url var="urlServlet" value="/RmyProfilJSON" />
<div id="getNext">

  <b><a href="#" style="color: blue" onclick="javascript:getNext('${urlServlet}','${type}');"><fmt:message
        key="profil.getNext"/></a></b>

</div>

<script type="text/javascript">

  getNext('${urlServlet}','${type}');
       

</script>
