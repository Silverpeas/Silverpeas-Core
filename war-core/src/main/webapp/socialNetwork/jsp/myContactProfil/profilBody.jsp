<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
    <view:tab label="${wall}" action="ALL?userId=${id}" selected="${type=='ALL'}" />
    <view:tab label="${infos}" action="MyInfos?userId=${id}" selected="false" />
    <view:tab label="${events}" action="MyEvents?userId=${id}" selected="${type=='EVENT'}" />
     <view:tab label="${publications}" action="MyPubs?userId=${id}" selected="${type=='PUBLICATION'}" />
    <view:tab label="${photos}" action="MyPhotos?userId=${id}" selected="${type=='PHOTO'}" />
</view:tabs>
<div class="SocialInformations" id="SocialInformationsId">

</div>
<c:url var="urlServlet" value="/RmyContactJSON?userId=${id}&type=${type}&offset=" />
<div id="getNext">
<view:board>
<a href="#" onclick="javascript:getNext('${urlServlet}');">Next</a>
</view:board>
</div>


<script>

       getNext('${urlServlet}');
       

</script>
