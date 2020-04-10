<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="import.jsp" %>

<c:set var="isPopup" value="${silfn:booleanValue(param['isPopup'])}"/>
<c:set var="action" value="${param['action']}"/>

<c:set var="closeLabel" value='<%=generalMessage.getString("GML.close")%>'/>
<c:set var="detailLabel" value='<%=generalMessage.getString("GML.detail")%>'/>
<c:set var="minimizeLabel" value='<%=generalMessage.getString("GML.minimize")%>'/>
<c:if test="${empty formNameErrorDataProvider}">
  <c:set var="formNameErrorDataProvider" value="${param['formNameErrorDataProvider']}"/>
</c:if>
<c:if test="${empty formNameErrorDataProvider}">
  <c:set var="formNameErrorDataProvider" value='errorDataProviderForm'/>
</c:if>
<c:if test="${empty formNameErrorDataProviderSubmitButtonLabel}">
  <c:set var="formNameErrorDataProviderSubmitButtonLabel" value="${param['formNameErrorDataProviderSubmitButtonLabel']}"/>
</c:if>

<div class="error-container">
  <div class="intfdcolor">
    <div class="intfdcolor4">
      <div class="error-message-container"><span class="txtnav message-slot"></span></div>
      <div class="error-message-extra-container hide"><span class="txtnav message-extra-slot"></span></div>
      <div class="error-detail-container hide"><span class="txtnav detail-slot"></span></div>
      <div class="error-stack-container hide"><textarea rows="12" cols="90" name="stack" class="stack-slot"></textarea></div>
    </div>
  </div>
  <view:buttonPane>
    <c:if test="${isPopup}">
    <view:button classes="close" label="${closeLabel}" action="javascript:window.close();"/>
    </c:if>
    <c:if test="${not empty formNameErrorDataProviderSubmitButtonLabel}">
    <view:button classes="submit" label="${formNameErrorDataProviderSubmitButtonLabel}" action="javascript:document.${formNameErrorDataProvider}.submit();"/>
    </c:if>
    <view:button classes="action" label="${detailLabel}" action="javascript:errorManager.switchDisplay();"/>
  </view:buttonPane>
  <script type="text/javascript">
    (function() {
      const isPopin = window.opener == null;
      const $parentWindow = isPopin ? window : window.opener;
      const formName = '${formNameErrorDataProvider}';
      window.errorManager = new function() {
        let __detailed = 'start';
        let $msg = window.document.querySelector('.error-container .message-slot');
        let $msgExtra = window.document.querySelector('.error-container .message-extra-slot');
        let $detailedMsg = window.document.querySelector('.error-container .detail-slot');
        let $stack = window.document.querySelector('.error-container .stack-slot');
        let $action = window.document.querySelector('.error-container .action');
        $msg.textContent = $parentWindow.document[formName].message.value;
        let $parentExtraMessageInput = $parentWindow.document[formName].messageExtra;
        if ($parentExtraMessageInput && StringUtil.isDefined($parentExtraMessageInput.value)) {
          $msgExtra.textContent = $parentExtraMessageInput.value;
          $msgExtra.parentElement.classList.remove('hide');
        } else {
          $msgExtra = undefined;
        }
        $detailedMsg.textContent = $parentWindow.document[formName].detailedMessage.value;
        $stack.value = $parentWindow.document[formName].stack.value;
        this.switchDisplay = function() {
          if (__detailed === 'start') {
            __detailed = false;
          } else if (__detailed) {
            $msg.parentElement.classList.remove('hide');
            if ($msgExtra) {
              $msgExtra.parentElement.classList.remove('hide');
            }
            $detailedMsg.parentElement.classList.add('hide');
            $stack.parentElement.classList.add('hide');
            $action.innerHTML = '${detailLabel}';
            __detailed = false;
          } else {
            $msg.parentElement.classList.add('hide');
            if ($msgExtra) {
              $msgExtra.parentElement.classList.add('hide');
            }
            $detailedMsg.parentElement.classList.remove('hide');
            $stack.parentElement.classList.remove('hide');
            $action.innerHTML = '${minimizeLabel}';
            __detailed = true;
          }
          if (!isPopin) {
            setTimeout(function() {
              currentPopupResize();
              window.focus();
            }, 0);
          }
        };
        this.switchDisplay();
      };
      <c:if test="${not (action eq 'NOBack')}">
      whenSilverpeasEntirelyLoaded().then(function() {
        if (!isPopin) {
          setTimeout(function() {
            $parentWindow.document[formName].submit();
          }, 0);
        }
      });
      </c:if>
    })();
  </script>
</div>