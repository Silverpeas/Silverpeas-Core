<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
    <view:tab label="${wall}" action="ALL" selected="false" />
    <view:tab label="${infos}" action="MyInfos" selected="true" />
    <view:tab label="${events}" action="MyEvents" selected="false" />
    <view:tab label="${publications}" action="MyPubs" selected="false" />
    <view:tab label="${photos}" action="MyPhotos" selected="false" />
</view:tabs>

<view:board  >
    <view:frame >

        <view:frame title="Informations personnelles">

            <view:board >

                <table border="0" cellspacing="0" cellpadding="5" width="100%">


                    <tr>
                        <td class="txtlibform" valign="baseline" width="30%"><fmt:message key="GML.lastName" bundle="${GML}"/></td>
                        <td >
                            ${snUserFull.userFull.lastName}
                        </td>
                    </tr>

                    <tr>
                        <td class="txtlibform" valign="baseline" width="30%"><fmt:message key="GML.firstName" bundle="${GML}"/></td>
                        <td >
                            ${snUserFull.userFull.firstName}
                        </td>
                    </tr>

                </table>


            </view:board>
        </view:frame>
        <view:frame title="Informations professionnelles & Coordonnées ">
            <a  id="myInfoUpdate" href="#"  onclick="javascript:enableFields()">
                <img  src=" <c:url value="/directory/jsp/icons/edit_button.gif" />" width="15" height="15"
                      alt="connected"/>
            </a>
            <view:board>
                <form method="POST" name="myInfoUpdateForm" action="">
                    <table border="0" cellspacing="0" cellpadding="5" width="100%">

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
                                <td class="txtlibform" valign="baseline" width="30%">
                                    ${propertiesKey[status.index]}
                                </td>
                                <td >
                                    <input type="text" id="${properties[status.index]}" name="prop_${properties[status.index]}" size="50" maxlength="99" value="${propertiesValue[status.index]}">
                                </td>
                            </tr>
                        </c:forEach>
                 </table>
                </form>
            </view:board>
            <table border="0" align="center" id="myInfoAction">
                <tr>
                    <td>
                        <fmt:message key="GML.validate" bundle="${GML}" var="validate" />
                        <view:button label="${validate}" action="javascript:submitUpdate()"  />
                    </td>
                    <td > <fmt:message key="GML.cancel" bundle="${GML}" var="cancel" />
                        <view:button label="${cancel}" action="MyInfos"  />
                    </td>
                </tr>
            </table>
        </view:frame>
    </view:frame>
</view:board>
<script>
    desabledFields();
</script>


