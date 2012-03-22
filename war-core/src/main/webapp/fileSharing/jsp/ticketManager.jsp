<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/legal/licensing"

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

<c:set var="language" value="${sessionScope[sessionController].language}"/>
<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="mandatoryIcon"><fmt:message key='fileSharing.obligatoire' bundle='${icons}'/></c:set>
<c:set var="ticket" value="${requestScope.Ticket}"/>
<c:set var="ticketURL" value="${requestScope.Url}"/>
<c:set var="action" value="${requestScope.Action}"/>
<c:set var="validation" value="javascript:onClick=sendDataCreate();"/>
<c:set var="endDate" value="${ticket.endDate}"/>
<c:set var="maxAccessNb" value="${ticket.nbAccessMax}"/>
<fmt:message var="currentOp" key="fileSharing.createTicket"/>
<c:if test="${action eq 'UpdateTicket'}">
  <c:set var="validation" value="javascript:onClick=sendData();"/>
  <fmt:message var="currentOp" key="fileSharing.updateTicket"/>
</c:if>
<c:set var="fileName" value="${ticket.attachmentDetail.logicalName}"/>
<c:if test="${fileName eq null}">
  <c:set var="fileName" value="${ticket.document.name}"/>
</c:if>
<c:set var="continuousChecked" value=""/>
<c:if test="${ticket.continuous}">
  <c:set var="continuousChecked" value="checked='checked'"/>
  <c:set var="endDate" value="<%= new java.util.Date() %>"/>
  <c:set var="maxAccessNb" value="1"/>
  <c:if test="${fn:length(ticket.downloads) gt 1}">
    <c:set var="maxAccessNb" value="${fn:length(ticket.downloads)}"/>
  </c:if>
</c:if>

<html>
  <head>

    <view:looknfeel/>
    <view:includePlugin name="datepicker"/>
    
    <script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript">
      var continuousTicket = !<c:out value="${ticket.continuous}"/>;
      $(window).load(function () {
        toggleContinuous();
      });

      function sendData()
      {
        if (isCorrectForm())
        {
          window.opener.document.ticketForm.action = "<c:out value='${action}'/>";
          window.opener.document.ticketForm.KeyFile.value = document.ticketForm.KeyFile.value;
          window.opener.document.ticketForm.EndDate.value = document.ticketForm.EndDate.value;
          window.opener.document.ticketForm.NbAccessMax.value = document.ticketForm.NbAccessMax.value;
          if (continuousTicket)
          {
            window.opener.document.ticketForm.Continuous.value = document.ticketForm.Continuous.value;
          }
          window.opener.document.ticketForm.submit();
          window.close();
        }
      }

      function sendDataCreate()
      {
        if (isCorrectForm())
        {
          document.ticketForm.action = "<c:out value='${action}'/>";
          document.ticketForm.submit();
        }
      }

      function isCorrectForm() {

        var errorMsg 			= "";
        var errorNb 			= 0;
        var nb 				= document.ticketForm.NbAccessMax.value;
        var nbMin = <c:out value="${maxAccessNb}"/>;
        var endDate 			= document.ticketForm.EndDate.value;

        if (nb > 100 || nb < nbMin) {
          errorMsg+="  - <fmt:message key='GML.theField'/> '<fmt:message key='fileSharing.nbAccessMax'/>' <fmt:message key='fileSharing.maxValue'/> " +
            nbMin + " <fmt:message key='GML.and'/> 100\n";
          errorNb++;
        }
        if (isWhitespace(endDate)) {
          errorMsg +="  - <fmt:message key='GML.theField'/> '<fmt:message key='fileSharing.endDate'/>' <fmt:message key='GML.MustBeFilled'/>\n";
          errorNb++;
        } else {
          if (!isDateOK(endDate, '<c:out value="${language}"/>'))
          {
            errorMsg+="  - <fmt:message key='GML.theField'/> '<fmt:message key='fileSharing.endDate'/>' <fmt:message key='GML.MustContainsCorrectDate'/>\n";
            errorNb++;
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
          if (continuousTicket) {
            $('.threshold').hide(effect);
          } else {
            $('.threshold').show(effect);
          }
        }
    </script>
  </head>
  <body>
    <fmt:message var="componentId" key="fileSharing.tickets"/>
    <view:browseBar componentId="${ticket.componentId}">
      <view:browseBarElt id="${currentOp}" label="${currentOp}" link=""/>
    </view:browseBar>

    <view:window>
      <view:frame>
        <view:board>

          <FORM Name="ticketForm" method="post" action="">
            <table CELLPADDING=5 WIDTH="100%">
              <tr>
                <td class="txtlibform"><fmt:message key="fileSharing.nameFile"/> :</td>
                <td><c:out value="${fileName}"/></td>
              </tr>
              <tr>
                <c:if test="${action eq 'UpdateTicket'}">
                  <td class="txtlibform"><fmt:message key="fileSharing.keyFile"/> :</td>
                  <td><a href="<c:out value='${ticketURL}'/>"><c:out value="${ticket.keyFile}"/></a></td>
                </c:if>
              </tr>
              <input type="hidden" name="ComponentId" value="<c:out value='${ticket.componentId}'/>">
              <input type="hidden" name="KeyFile" value="<c:out value='${ticket.keyFile}'/>">
              <input type="hidden" name="FileId" size="60" maxlength="150" value="<c:out value='${ticket.fileId}'/>">
              <input type="hidden" name="Versioning" value="<c:out value='${ticket.versioned}'/>">

              <tr>
                <td class="txtlibform"><fmt:message key="fileSharing.creationDate"/> :</td>
                <TD><view:formatDate value="${ticket.creationDate}" language="${language}"/>&nbsp;<span class="txtlibform"><fmt:message key="fileSharing.by"/></span>&nbsp;<c:out value="${ticket.creator.displayedName}"/></TD>
              </tr>
              <c:if test="${ticket.modified}">
                <tr>
                  <td class="txtlibform"><fmt:message key="fileSharing.updateDate"/> :</td>
                  <TD><view:formatDate value="${ticket.updateDate}" language="${language}"/>&nbsp;<span class="txtlibform"><fmt:message key="fileSharing.by"/></span>&nbsp;<c:out value="${ticket.lastModifier.displayedName}"/></TD>
                </tr>
              </c:if>
              <tr>
                <td class="txtlibform"><fmt:message key="fileSharing.continuous"/> :</td>
                <td><input type="checkbox" name="Continuous" <c:out value="${continuousChecked}"/> onClick="javascript:toggleContinuous('slow');"/></td>
              </tr>
              <tr class="threshold">
                <td class="txtlibform"><fmt:message key="fileSharing.endDate"/> :</td>
                <TD><input type="text" class="dateToPick" name="EndDate" size="12" maxlength="10" value="<view:formatDate value='${endDate}' language='${language}'/>"/><span class="txtnote">(<fmt:message key="GML.dateFormatExemple"/>)
                    <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
                </TD>
              </tr>
              <tr class="threshold">
                <td class="txtlibform"><fmt:message key="fileSharing.nbAccessMax"/> :</td>
                <TD>
                  <input type="text" name="NbAccessMax" size="3" maxlength="3" value="<c:out value='${maxAccessNb}'/>" >
                  <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
                </TD>
              </tr>
              <tr>
                <td colspan="2">( <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/> : <fmt:message key="GML.mandatory"/> )</td>
              </tr>

              <c:if test="${action eq 'UpdateTicket'}">
                <tr><td colspan="2">
                    <fmt:message var="downloadDate" key="fileSharing.downloadDate"/>
                    <fmt:message var="downloadIP" key="fileSharing.IP"/>
                    <view:arrayPane var="downloadList" routingAddress="EditTicket?KeyFile=${ticket.keyFile}">
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
                  </td></tr>
                </c:if>
            </table>
          </form>

        </view:board>
      <BR/><center>
        <fmt:message var="cancel" key="GML.cancel"/>
        <fmt:message var="validate" key="GML.validate"/>
        <view:buttonPane>
          <view:button action="${validation}" label="${validate}" disabled="false"/>
          <view:button action="javascript:window.close();" label="${cancel}" disabled="false"/>
        </view:buttonPane>
      </center><BR/>
    </view:frame>
  </view:window>
</body>
</html>