<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkPersonalization.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="validationMessage" value="${requestScope.validationMessage}" />
<c:set var="testExplanation" value="${requestScope.testExplanation}"/>
<c:set var="isMultiChannelNotif" value="${requestScope.multichannel}"/>
<c:set var="notifAddresses" value="${requestScope.notificationAddresses}"/>
<c:url value="/RSILVERMAIL/jsp/Main" var="silvermailURL"/>
<c:url value="/RSILVERMAIL/jsp/SentUserNotifications" var="sentNotificationsURL"/>
<fmt:message key="MesNotifications" var="myNotifications"/>
<fmt:message key="ParametrerNotification" var="parametrize"/>
<fmt:message key="operationPane_addadress" var="addAddress"/>
<fmt:message key="operationPane_paramnotif" var="op_parametrizeNotif"/>
<fmt:message key="LireNotification" var="viewNotif"/>
<fmt:message key="SentUserNotifications" var="sentNotif"/>
<fmt:message key="ParametrerNotification" var="parametrizeNotif"/>
<fmt:message key="arrayPane_Default" var="defaultColumn"/>
<fmt:message key="arrayPane_Nom" var="nameColumn"/>
<fmt:message key="arrayPane_Adresse" var="addressColumn"/>
<fmt:message key="arrayPane_Operations" var="operationsColumn"/>
<fmt:message key="GML.validate" var="buttonValidationLabel"/>
<fmt:message key="notifications.channel.none.warning" var="noChannelWarning"/>

<view:script src="/util/javaScript/checkForm.js"/>
<view:sp-page>
  <view:sp-head-part>
    <%
      String channelId = "" ;
    %>

    <script type="text/javascript">
      function addNotif(){
        var options = '<%=personalizationScc.buildOptions(personalizationScc.getNotifChannels(), channelId, null)%>';
        $("#channelManager #txtNotifName").val("");
        $("#channelManager #channelId").html(options);

        $("#channelManager #txtAddress").val("");
        $("#channelManager #txtNotifName").focus();

        document.channelManagerForm.Action.value = "Add";

        var title = "<%=resource.getString("browseBar_Path2")%>";
        showDialog(title);
      }

      function editNotif(id){
        var options = '<%=personalizationScc.buildOptions(personalizationScc.getNotifChannels(), channelId, null)%>';
        $("#channelManager #channelId").html(options);

        var txtNotifName = $("#personalization #"+id+" td:nth-child(2)").html();
        var txtAddress = $("#personalization #"+id+" td:nth-child(3)").html();

        $("#channelManager #txtNotifName").val(txtNotifName);
        $("#channelManager #txtAddress").val(txtAddress);
        $("#channelManager #txtNotifName").focus();

        document.channelManagerForm.Action.value = "Update";
        document.channelManagerForm.id.value = id;

        var title = "<%=resource.getString("browseBar_Path4")%>";
        showDialog(title);
      }

      function showDialog(title) {
        $("#channelManager").popup({
          title: title,
          callback: function() {
            return sendData();
          }
        });
      }

      function sendData() {
        var errorMsg = "";
        var errorNb = 0;
        var name = stripInitialWhitespace($("#channelManager #txtNotifName").val());

        if (!checkemail($("#channelManager #txtAddress").val())) {
          errorMsg += "  - '<%=resource.getString("adresse")%>'  <%=resource.getString("GML.MustContainsEmail")%>\n";
          errorNb++;
        }

        if (name == "") {
          errorMsg+="  - '<%=resource.getString("GML.name")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
          errorNb++;
        }

        switch(errorNb) {
          case 0 :
            $("#channelManager #channelId").prop("disabled", false);
            document.channelManagerForm.submit();
            break;
          case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            jQuery.popup.error(errorMsg);
            break;
          default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
        }
        return false;
      }


      function paramNotif(){
        SP_openWindow("paramNotif.jsp","paramNotif","750","400","scrollbars=yes");
      }
      function deleteCanal(id){
        if (window.confirm("<fmt:message key='MessageSuppressionCanal'/>")) {
          document.channelForm.action = "ParametrizeNotification";
          document.channelForm.Action.value = "Delete";
          document.channelForm.id.value = id;
          document.channelForm.submit();
        }
      }

      function setDefault(id) {
        document.channelForm.action = "ParametrizeNotification";
        document.channelForm.Action.value = "SetDefault";
        document.channelForm.id.value = id;
        document.channelForm.submit();
      }

      function testNotification(id) {
        document.channelForm.action = "ParametrizeNotification";
        document.channelForm.Action.value = "Test";
        document.channelForm.id.value = id;
        document.channelForm.submit();
      }

      function sendChoiceChannel() {
        var selectedChannels = getChannels();
        if (StringUtil.isNotDefined(selectedChannels)) {
          notyWarning("${noChannelWarning}");
        } else {
          document.channelForm.SelectedChannels.value = selectedChannels;
          document.channelForm.SelectedFrequency.value = getFrequency();
          document.channelForm.action = "SaveChannels";
          document.channelForm.submit();
        }
      }

      function getChannels() {
        var  items = "";
        try {
          var boxItems = document.channelForm.SelectChannel;
          if (boxItems != null){
            // au moins une checkbox exist
            var nbBox = boxItems.length;
            if ( (nbBox == null) && (boxItems.checked == true) ){
              items += boxItems.value+",";
            } else{
              for (i=0;i<boxItems.length ;i++ ){
                if (boxItems[i].checked == true){
                  items += boxItems[i].value+",";
                }
              }
            }
          }
        } catch (e) {
          //Checkboxes are not displayed
        }
        return items;
      }

      function getFrequency() {
        return $("#SelectFrequency").val();
      }

      function onChangeFrequency() {
        document.channelForm.action = "ParametrizeNotification";
        document.channelForm.Action.value = "SetFrequency";
        document.channelForm.id.value = getFrequency();
        document.channelForm.submit();
      }
    </script>
  </view:sp-head-part>
  <view:includePlugin name="popup"/>
  <view:sp-body-part>
    <view:browseBar componentId="${myNotifications}" path="${parametrize}"/>
    <view:operationPane>
      <view:operation icon="${addProtocol}" altText="${addAddress}" action="javascript:addNotif();"/>
      <view:operationSeparator/>
      <view:operation icon="${param.notif}" altText="${op_parametrizeNotif}" action="javascript:paramNotif();"/>
    </view:operationPane>
    <view:window>
      <view:tabs>
        <view:tab label="${viewNotif}" action="${silvermailURL}" selected="false"/>
        <view:tab label="${sentNotif}" action="${sentNotificationsURL}" selected="false"/>
        <view:tab label="${parametrizeNotif}" action="ParametrizeNotification" selected="true"/>
      </view:tabs>

      <view:frame>

        <c:if test="${not empty validationMessage}">
          <div class="inlineMessage-ok">
            <c:out value="${validationMessage}" />
          </div>
        </c:if>
        <c:if test="${not empty testExplanation}">
          <div class="inlineMessage">${testExplanation}</div>
        </c:if>

        <p><b><fmt:message key="channelChoiceLabel" /></b></p>
        <form name="channelForm" action="" method="POST">
          <input type="hidden" name="Action"/>
          <input type="hidden" name="id"/>
          <input type="hidden" name="SelectedChannels"/>
          <input type="hidden" name="SelectedFrequency"/>

          <view:arrayPane var="personalization" routingAddress="ParametrizeNotification">
            <view:arrayColumn title="${defaultColumn}" sortable="false"/>
            <view:arrayColumn title="${nameColumn}" sortable="true"/>
            <view:arrayColumn title="${addressColumn}" sortable="true"/>
            <view:arrayColumn title="${operationsColumn}" sortable="false"/>
            <c:forEach var="props" items="${notifAddresses}">
              <c:set var="name" value="${fn:escapeXml(props['name'])}"/>
              <c:set var="address" value="${fn:escapeXml(props['address'])}"/>
              <view:arrayLine id="${props.id}">
                <%
                  IconPane actions = gef.getIconPane();
                  Properties props = (Properties) pageContext.getAttribute("props");
                  ArrayLine arrayLine = (ArrayLine) pageContext.getAttribute(ArrayLineTag.ARRAY_LINE_PAGE_ATT);
                  String id = Encode.forHtml(props.getProperty("id"));
                %>
                <c:choose>
                  <c:when test="${!isMultiChannelNotif}">
                    <%
                      Icon anAction = actions.addIcon();
                      if (props.getProperty("isDefault").equalsIgnoreCase("true")) {
                        anAction.setProperties(on_default, "", "");
                      } else {
                        anAction.setProperties(off_default, resource.getString("iconPane_Default"),
                            "javascript:setDefault('" + id + "');");
                      }
                      arrayLine.addArrayCellIconPane(actions);
                    %>
                  </c:when>
                  <c:otherwise>
                    <%
                      // print the choice cases for a multiple selection
                      String usedCheck = "";
                      if (props.getProperty("isDefault").equalsIgnoreCase("true")) {
                        usedCheck = "checked=\"checked\"";
                      }
                      pageContext.setAttribute("cellText",
                          "<input type='checkbox' name='SelectChannel' value='" + id + "' " + usedCheck + "/>");
                    %>
                    <view:arrayCellText text="${cellText}"/>
                  </c:otherwise>
                </c:choose>
                <view:arrayCellText text="${name}"/>
                <view:arrayCellText text="${address}"/>
                <%
                  // add the icons related to the suppression and to the modification
                  actions = gef.getIconPane();
                  if (props.getProperty("canEdit").equalsIgnoreCase("true")) {
                    Icon modifier = actions.addIcon();
                    modifier.setProperties(modif, resource
                        .getString("GML.modify"), "javascript:editNotif(" + id + ");");
                  } else {
                    Icon modifier = actions.addIcon();
                    modifier.setProperties(ArrayPnoColorPix, "", "");
                  }
                  if (props.getProperty("canDelete").equalsIgnoreCase("true")) {
                    Icon del = actions.addIcon();
                    del.setProperties(delete, resource.getString("GML.delete"),
                        "javascript:deleteCanal('" + id + "');");
                  } else {
                    Icon del = actions.addIcon();
                    del.setProperties(ArrayPnoColorPix, "", "");
                  }

                  if (props.getProperty("canTest").equalsIgnoreCase("true")) {
                    Icon tst = actions.addIcon();
                    tst.setProperties(test,
                        resource.getString("iconPane_Test"),
                        "javascript:testNotification('" + id + "');");
                  } else {
                    Icon tst = actions.addIcon();
                    tst.setProperties(ArrayPnoColorPix, "", "");
                  }

                  arrayLine.addArrayCellIconPane(actions);
                %>

              </view:arrayLine>
            </c:forEach>
          </view:arrayPane>

          <br/>
          <p><b><fmt:message key="frequencyChoiceLabel" /></b>
            <fmt:message key="frequency${requestScope.delayedNotification.defaultFrequency.name}" var="defaultFrequencyLabel" />
            <c:set var="currentUserFrequencyCode" value="${requestScope.delayedNotification.currentUserFrequencyCode}" />
            <c:set var="frequencyOnChange" value="" />
            <c:if test="${!isMultiChannelNotif}">
              <c:set var="frequencyOnChange" value="javascript:onChangeFrequency();" />
            </c:if>
            <select id="SelectFrequency" name="SelectFrequency" onchange="${frequencyOnChange}">
              <c:set var="currentUserFrequencyCode" value="${requestScope.delayedNotification.currentUserFrequencyCode}" />
              <c:choose>
                <c:when test="${empty currentUserFrequencyCode or empty requestScope.delayedNotification.frequencies}">
                  <option value="" selected="selected"><fmt:message key="frequencyDefault"><fmt:param value="${defaultFrequencyLabel}"/></fmt:message></option>
                </c:when>
                <c:otherwise>
                  <option value=""><fmt:message key="frequencyDefault"><fmt:param value="${defaultFrequencyLabel}"/></fmt:message></option>
                </c:otherwise>
              </c:choose>
              <c:forEach items="${requestScope.delayedNotification.frequencies}" var="frequency">
                <c:choose>
                  <c:when test="${frequency.code eq currentUserFrequencyCode}">
                    <option value="${frequency.code}" selected="selected"><fmt:message key="frequency${frequency.name}" /></option>
                  </c:when>
                  <c:otherwise>
                    <option value="${frequency.code}"><fmt:message key="frequency${frequency.name}" /></option>
                  </c:otherwise>
                </c:choose>
              </c:forEach>
            </select>
          </p>

          <c:if test="${isMultiChannelNotif}">
            <view:buttonPane verticalPosition="center">
              <view:button label="${buttonValidationLabel}" action="javascript:onClick=sendChoiceChannel();" disabled="false"/>
            </view:buttonPane>
          </c:if>
        </form>
      </view:frame>
    </view:window>

    <div id="channelManager" style="display: none;">
      <form name="channelManagerForm" action="ParametrizeNotification" method="POST">
        <input type="hidden" name="id"/>
        <input type="hidden" name="Action"/>
        <table>
          <caption></caption>
          <th id="channelFormHeader"></th>
          <tr>
            <td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
            <td><input type="text" name="txtNotifName" id="txtNotifName" size="20" maxlength="20"/></td>
            <td><img src="<%=mandatoryField%>" width="5" height="5" alt="<%=resource.getString("GML.mandatory")%>"/></td>
          </tr>
          <tr>
            <td class="txtlibform"><%=resource.getString("type")%> :</td>
            <td><select disabled id="channelId" name="channelId"></select>
            </td>
          </tr>
          <tr>
            <td class="txtlibform"><%=resource.getString("adresse")%> :</td>
            <td><input type="text" name="txtAddress" id="txtAddress" size="50" maxlength="250"/></td>
            <td><img src="<%=mandatoryField%>" width="5" height="5" alt="<%=resource.getString("GML.mandatory")%>"/></td>
          </tr>
          </tr>
          <tr><td colspan="2"><img src="<%=mandatoryField%>" width="5" height="5" alt="<%=resource.getString("GML.mandatory")%>"/> : <%=resource.getString("GML.requiredField")%></td></tr>
        </table>
      </form>
    </div>
  </view:sp-body-part>
</view:sp-page>