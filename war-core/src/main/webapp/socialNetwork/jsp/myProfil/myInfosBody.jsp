<%--<%@ include file="check.jsp" %>--%>

<view:tabs >
    <view:tab label="${wall}" action="ALL" selected="false" />
    <view:tab label="${infos}" action="MyInfos" selected="true" />
    <view:tab label="${events}" action="MyEvents" selected="false" />
    <view:tab label="${publications}" action="MyPubs" selected="false" />
    <view:tab label="${photos}" action="MyPhotos" selected="false" />
</view:tabs>

    <view:frame >  
      <view:frame title="Informations professionnelles &amp; Coordonnées ">
            <a  id="myInfoUpdate" href="#"  onclick="javascript:enableFields()">
                <img src="<c:url value="/directory/jsp/icons/edit_button.gif" />" width="15" height="15"
                      alt="connected" />
            </a>
            <view:board>
                <form method="post" name="myInfoUpdateForm" action="">
                    <div id="informations">
                      <div class="form">
                          <div class="attribut"><fmt:message key="GML.position" bundle="${GML}"/></div>
                                <fmt:message key="${snUserFull.userFull.accessLevel}" var="position" />
                                <fmt:message key="${position}" bundle="${GML}"/>
                      </div>
                        
                      <div class="form">
                        <div class="attribut"><fmt:message key="GML.eMail" bundle="${GML}"/></div>
                                ${snUserFull.userFull.eMail}
                      </div>
                       

                        <c:forEach items="${propertiesKey}" var="propertys" varStatus="status">

                             <div class="form">
                                <div class="attribut">
                                    ${propertiesKey[status.index]}
                                </div>
                                    <input type="text" id="${properties[status.index]}" name="prop_${properties[status.index]}" size="50" maxlength="99" value="${propertiesValue[status.index]}" />
                            </div>
                        </c:forEach>
                    
                    </div>
                    <!--<table border="0" cellspacing="0" cellpadding="5" width="100%">

                       
                 </table>
                --></form>
            </view:board>
            <div id="myInfoAction">
                    <div class="button">
                        <fmt:message key="GML.validate" bundle="${GML}" var="validate" />
                        <view:button label="${validate}" action="javascript:submitUpdate()"  />
                    </div>
                    <div class="button">
                     <fmt:message key="GML.cancel" bundle="${GML}" var="cancel" />
                        <view:button label="${cancel}" action="MyInfos"  />
                    </div>
            </div>
        </view:frame>
    </view:frame>
<script type="text/javascript">
    desabledFields();
</script>


