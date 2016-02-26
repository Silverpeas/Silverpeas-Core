<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="mandatoryIcon"><fmt:message key='sharing.obligatoire' bundle='${icons}'/></c:set>
<c:set var="usersIcon"><fmt:message key='sharing.users' bundle='${icons}'/></c:set>
<c:set var="ticket" value="${requestScope.Ticket}"/>
<c:set var="ticketURL" value="${requestScope.Url}"/>
<c:set var="action" value="${requestScope.Action}"/>
<c:set var="cancellation" value="javascript:window.close();"/>
<c:set var="endDate" value="${ticket.endDate}"/>
<c:set var="sharedObjectType" value="${ticket.sharedObjectType}"/>
<c:set var="maxAccessNb" value="${ticket.nbAccessMax}"/>
<fmt:message var="currentOp" key="sharing.createTicket"/>
<c:if test="${action eq 'UpdateTicket'}">
  <c:set var="cancellation" value="ViewTickets"/>
  <fmt:message var="currentOp" key="sharing.updateTicket"/>
</c:if>
<c:set var="fileName" value="${ticket.resource.name}"/>
<c:set var="continuousChecked" value=""/>
<c:if test="${ticket.continuous}">
  <c:set var="continuousChecked" value="checked='checked'"/>
  <c:set var="endDate" value="<%= new java.util.Date() %>"/>
  <c:set var="maxAccessNb" value="0"/>
  <!--
  <c:if test="${fn:length(ticket.downloads) gt 1}">
    <c:set var="maxAccessNb" value="${fn:length(ticket.downloads)}"/>
  </c:if>
   -->
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	<title></title>
    <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
    <link href="<c:url value='/sharing/jsp/styleSheets/tickets.css'/>" type="text/css" rel="stylesheet"/>
    <view:includePlugin name="datepicker"/>
    <view:includePlugin name="tags"/>
    <script type="text/javascript">
var continuousTicket = !<c:out value="${ticket.continuous}" />;
$(window).load(function () {
  toggleContinuous();
});

$(function () {
  var tagTriggerKeys = ['enter', 'comma', 'tab', 'semicolon', 'space'];
  $('#ticket-email').tagit({triggerKeys:tagTriggerKeys});
});

function sendData() {
  if (isCorrectForm()) {
    document.ticketForm.action = "<c:out value='${action}'/>";
    document.ticketForm.submit();
  }
}

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;
  var nb  = document.ticketForm.nbAccessMax.value;
  var nbMin = <c:out value="${ticket.nbAccess}"/>;
  var endDate = document.ticketForm.endDate.value;

  if(!continuousTicket)
  {
    if (nb > 10000 || nb < nbMin) {
      errorMsg+="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.nbAccessMax'/>' <fmt:message key='sharing.maxValue'/> " +
        nbMin + " <fmt:message key='GML.and'/> 10000\n";
      errorNb++;
    }
    if (isWhitespace(endDate)) {
      errorMsg +="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.endDate'/>' <fmt:message key='GML.MustBeFilled'/>\n";
      errorNb++;
    } else {
      if (!isDateOK(endDate, '<c:out value="${language}"/>')) {
        errorMsg+="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.endDate'/>' <fmt:message key='GML.MustContainsCorrectDate'/>\n";
        errorNb++;
      }
    }
  }
  switch(errorNb) {
  case 0 :
    result = true;
    break;
  case 1 :
    errorMsg = "<fmt:message key='GML.ThisFormContains'/> 1 <fmt:message key='GML.error'/> : \n" + errorMsg;
    window.alert(errorMsg);
    result = false;
    break    ;
  default :
    errorMsg = "<fmt:message key='GML.ThisFormContains'/> " + errorNb + " <fmt:message key='GML.errors'/> :\n" + errorMsg;
    window.alert(errorMsg);
    result = false;
    break;
  }
  return result;
}

function toggleContinuous(effect) {
  continuousTicket = !continuousTicket;
  var isNodeSharing = <c:out value="${sharedObjectType eq 'Node'}" />;
  if (continuousTicket) {
    $('.threshold').hide(effect);
  } else {
    $('.threshold').show(effect);
    if (isNodeSharing) {
	$("#nbAccessMaxArea").hide();
    }
  }
}

    </script>
  </head>
  <body class="tickets">
    <fmt:message var="componentId" key="sharing.tickets"/>
    <view:browseBar componentId="${ticket.componentId}" clickable="false">
      <view:browseBarElt id="${currentOp}" label="${currentOp}" link=""/>
    </view:browseBar>

    <view:window>
      <view:frame>
        <view:board>

          <form name="ticketForm" method="post" action="">
            <input type="hidden" name="componentId" value="<c:out value='${ticket.componentId}'/>"/>
            <input type="hidden" name="token" value="<c:out value='${ticket.token}'/>"/>
            <input type="hidden" name="objectId" size="60" maxlength="150" value="<c:out value='${ticket.sharedObjectId}'/>"/>
            <input type="hidden" name="type" value="<c:out value='${ticket.sharedObjectType}'/>"/>

            <fieldset class="skinFieldset" id="ticketInformation">
              <legend><fmt:message key="GML.bloc.information.principal"/></legend>
              <div class="fields">
                <div id="ticketNameArea" class="field">
                  <label class="txtlibform" for="ticketName">
                    <c:choose>
                      <c:when test="${sharedObjectType eq 'Node'}"><fmt:message key="GML.theme"/> </c:when>
                      <c:when test="${sharedObjectType eq 'Publication'}"><fmt:message key="GML.publication"/> </c:when>
                      <c:otherwise><fmt:message key="GML.file"/> </c:otherwise>
                    </c:choose>
                  </label>
                  <div class="champs"><c:out value="${fileName}"/></div>
                </div>
                <div id="ticketClefArea" class="field">
                  <c:if test="${action eq 'UpdateTicket'}">
                    <label class="txtlibform" for="ticketClef"><fmt:message key="sharing.token"/></label>
                    <div class="champs"><a href="<c:out value='${ticketURL}'/>"><c:out value="${ticket.token}"/></a></div>
                  </c:if>
                </div>
                <div id="ticketCreationArea" class="field">
                  <label class="txtlibform" for="ticketCreation"><fmt:message key="sharing.creationDate"/></label>
                  <div class="champs"><view:formatDate value="${ticket.creationDate}" language="${language}"/>&nbsp;<span class="txtlibform"><fmt:message key="sharing.by"/></span>&nbsp;<c:out value="${requestScope.Creator}"/> </div>
                </div>
              <c:if test="${ticket.modified}">
                <div id="ticketModificationArea" class="field">
                  <label class="txtlibform" for="ticketCreation"><fmt:message key="sharing.updateDate"/></label>
                  <div class="champs"><view:formatDate value="${ticket.updateDate}" language="${language}"/>&nbsp;<span class="txtlibform"><fmt:message key="sharing.by"/></span>&nbsp;<c:out value="${requestScope.Updater}"/> </div>
                </div>
              </c:if>

              </div>
            </fieldset>
            <div class="table">
              <div class="cell">
                <fieldset class="skinFieldset" id="ticketValidity">
                  <legend><fmt:message key="sharing.validity"/></legend>
                  <div class="fields">
                    <div id="ticketValidityTypeArea" class="field">
                      <label class="txtlibform" for="validity"><fmt:message key="sharing.validity"/></label>
                      <div class="champs">
                        <select id="validity" name="validity" onchange="javascript:toggleContinuous();">
                          <option label="<fmt:message key="sharing.validity.continuous"/>" value="0" ${ticket.continuous ? 'selected="selected"' : ''}><fmt:message key="sharing.validity.continuous"/></option>
                          <option label="<fmt:message key="sharing.validity.limited"/>" value="1" ${ticket.continuous ? '' : 'selected="selected"'}><fmt:message key="sharing.validity.limited"/></option>
                        </select>
                      </div>
                      <div id="ticketValidityEndArea" class="field threshold">
                        <label class="txtlibform" for="endDate"><fmt:message key="sharing.endDate"/></label>
                        <div class="champs">
                          <input type="text" class="dateToPick" name="endDate" size="12" maxlength="10" value="<view:formatDate value='${endDate}' language='${language}'/>"/><span class="txtsublibform">(<fmt:message key="GML.dateFormatExemple"/>)</span>
                          <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
                        </div>
                      </div>
                      <div id="nbAccessMaxArea" class="field threshold">
                        <label class="txtlibform" for="nbAccessMax"><fmt:message key="sharing.nbAccessMax"/></label>
                        <div class="champs">
                          <input id="nbAccessMax" type="text" value="${maxAccessNb}" maxlength="5" size="5" name="nbAccessMax"/> <fmt:message key="sharing.nbAccessMax.info" /> <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
                        </div>
                      </div>
                    </div>
                  </div>
                </fieldset>
              </div>
              <div class="cell">
                <fieldset class="skinFieldset" id="ticketNotification">
                  <legend><fmt:message key="GML.notification"/></legend>
                  <div class="fields">
                    <div id="ticketNotificationUserArea" class="field">
                      <label class="txtlibform" for="users_name"><fmt:message key="GML.users"/></label>
                      <div class="champs">
                        <input id="users" name="users" value="" type="hidden" />
                        <textarea id="users_name"  name="users$$name" cols="30" rows="3" ></textarea>&nbsp;
                        <a href="#" onclick="javascript:SP_openWindow( webContext + '/RselectionPeasWrapper/jsp/open?formName=ticketForm&amp;elementId=users&amp;elementName=users$$name&amp;selectedUsers=' + $('#users').val() + '&amp;selectionMultiple=true','selectUser',800,600,'');">
                          <img src="<c:url value='${usersIcon}' />" alt="<fmt:message key='GML.users'/>" title="<fmt:message key='GML.users'/>" align="absmiddle" border="0" height="15" width="15" />
                        </a>
                      </div>
                    </div>
                     <div id="ticketNotificationEmailArea" class="field">
                      <label class="txtlibform" for="externalEmails"><fmt:message key="GML.external.emails"/></label>
                      <div class="champs">
                        <!-- <ul id="ticket-email"></ul>
                        <input type="hidden" id="ticketEmail" name="externEmails" size="60" maxlength="150" /> -->
                        <input type="email" pattern="^([\w+-.%]+@[\w-.]+\.[A-Za-z]{2,4},*[\W]*)+$" value="" name="externalEmails" size="60" maxlength="150" multiple />
                      </div>
                    </div>
                    <div id="ticketNotificationMessageArea" class="field">
                      <label class="txtlibform" for="additionalMessage"><fmt:message key="GML.additional.message"/></label>
                      <div class="champs">
                        <textarea name="additionalMessage" rows="5" cols="30" ></textarea>
                      </div>
                    </div>
                  </div>
                </fieldset>
              </div>
            </div>

            <c:if test="${action == 'UpdateTicket' && sharedObjectType != 'Node'}">
              <fieldset class="skinFieldset" id="ticketAccessControl">
                <legend><fmt:message key="sharing.accessControl" /></legend>
                <div class="fields">
                  <fmt:message var="downloadDate" key="sharing.downloadDate"/>
                  <fmt:message var="downloadIP" key="sharing.IP"/>
                  <view:arrayPane var="downloadList" routingAddress="EditTicket?token=${ticket.token}">
                    <view:arrayColumn title="${downloadDate}" />
                    <view:arrayColumn title="${downloadIP}" />
                    <c:forEach items="${ticket.downloads}" var="download">
                      <c:set var="downloadDate"><view:formatDateTime value="${download.downloadDate}" language="${language}"/></c:set>
                      <view:arrayLine>
                        <view:arrayCellText text="${downloadDate}"/>
                        <view:arrayCellText text="${download.userIP}"/>
                      </view:arrayLine>
                    </c:forEach>
                  </view:arrayPane>

                </div>
              </fieldset>
            </c:if>
            <div class="legend"> <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5" alt="<fmt:message key="GML.mandatory"/>"/> : <fmt:message key="GML.mandatory"/> </div>
          </form>

        </view:board>
      <br/><center>
        <fmt:message var="cancel" key="GML.cancel"/>
        <fmt:message var="validate" key="GML.validate"/>
        <view:buttonPane>
          <view:button action="javascript:onclick=sendData();" label="${validate}" disabled="false"/>
          <view:button action="${cancellation}" label="${cancel}" disabled="false"/>
        </view:buttonPane>
      </center>
    </view:frame>
  </view:window>
  </body>
</html>
