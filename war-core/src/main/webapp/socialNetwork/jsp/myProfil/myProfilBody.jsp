<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
    <view:tab label="${wall}" action="ALL" selected="${type=='ALL'}" />
    <view:tab label="${infos}" action="MyInfos" selected="false" />
    <view:tab label="${events}" action="MyEvents" selected="${type=='EVENT'}" />
     <view:tab label="${publications}" action="MyPubs" selected="${type=='PUBLICATION'}" />
    <view:tab label="${photos}" action="MyPhotos" selected="${type=='PHOTO'}" />
</view:tabs>
<div class="SocialInformations" id="SocialInformationsId">

</div>                         
<c:url var="urlServlet" value="/RmyProfilJSON?type=${type}&offset=" />
<div id="getNext">
<view:board>
<a href="#" onclick="javascript:getNext('${urlServlet}');">Next</a>
</view:board>
</div>


<script>

       getNext('${urlServlet}');
       

</script>
