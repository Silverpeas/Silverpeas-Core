<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%--<%@ include file="check.jsp" %>--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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


