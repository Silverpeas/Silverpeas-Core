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
<view:setBundle basename="org.silverpeas.sharing.multilang.fileSharingBundle" var="fsBundle"/>
<view:setBundle basename="org.silverpeas.sharing.settings.fileSharingIcons" var="fsIcons"/>

<c:set var="usersIcon"><fmt:message key='sharing.users' bundle='${fsIcons}'/></c:set>
<c:set var="mandatoryIcon"><fmt:message key='sharing.obligatoire' bundle='${fsIcons}'/></c:set>

<view:includePlugin name="datepicker"/>
<view:link href="/util/styleSheets/fieldset.css"/>
<view:script src="/util/javaScript/checkForm.js"/>

<script type="text/javascript">
function createSharingTicketPopup(sharingParam) {
  cleanForm();
  $("#hiddenType").val(sharingParam.type);
  $("#hiddenComponentId").val(sharingParam.componentId);
  $("#displayNameId").text(sharingParam.name);
  $("#hiddenObjectId").val(sharingParam.id);

  toggleContinuous();

  if (sharingParam.type == 'Node') {
    $("#objectName").text("<fmt:message key="GML.theme" />")
  } else if (sharingParam.type == 'Publication') {
    $("#objectName").text("<fmt:message key="GML.publication" />")
  } else {
    //Attachment
    $("#objectName").text("<fmt:message key="GML.file" />")
  }

  $('#sharingticket-popup-content').popup('validation', {
      title : "<fmt:message key="sharing.createTicket" bundle="${fsBundle}" />",
      width : "700px",
    callback : function() {
      if ( window.console && window.console.log ) {
        console.log("User create new share ticket");
      }
      if (isCorrectForm()) {
        createTicket();
        return true;
      } else {
        return false;
      }
    }
  });
}

function toggleContinuous(effect) {
  var continuousTicket = $("#validity").val() == "0";
  var nbAccessShown = $("#hiddenType").val() === 'Attachment';
  if (continuousTicket) {
    $('.threshold').hide(effect);
  } else {
    $('.threshold').show(effect);
    if (nbAccessShown) {
      $("#nbAccessMaxArea").show();
    } else {
      $("#nbAccessMaxArea").hide();
    }
  }
}


function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;
  var nb  = $("#nbAccessMax").val();
  var nbMin = 0;
  var endDate = $("#endDate").val();
  var nbAccessShown = $("#hiddenType").val() === 'Attachment';

  if($("#validity").val() == "1")
  {
    if (nbAccessShown && isWhitespace(nb)) {
      errorMsg +="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.nbAccessMax' bundle='${fsBundle}'/>' <fmt:message key='GML.MustBeFilled'/>\n";
      errorNb++;
    }
    if (nbAccessShown && !isInteger(nb)) {
      errorMsg +="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.nbAccessMax' bundle='${fsBundle}'/>' <fmt:message key='GML.MustContainsNumber'/>\n";
      errorNb++;
    }
    if (nb > 10000 || nb < 0) {
      errorMsg+="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.nbAccessMax' bundle='${fsBundle}'/>' <fmt:message key='sharing.maxValue'/> " +
        nbMin + " <fmt:message key='GML.and'/> 10000\n";
      errorNb++;
    }
    if (isWhitespace(endDate)) {
      errorMsg +="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.endDate' bundle='${fsBundle}'/>' <fmt:message key='GML.MustBeFilled'/>\n";
      errorNb++;
    } else {
      if (!isDateOK(endDate, '<c:out value="${language}"/>')) {
        errorMsg+="  - <fmt:message key='GML.theField'/> '<fmt:message key='sharing.endDate' bundle='${fsBundle}'/>' <fmt:message key='GML.MustContainsCorrectDate'/>\n";
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


function createTicket() {
  var ajaxUrl = webContext + '/services/mytickets/' + $("#hiddenComponentId").val();

  var newTicket = {
      "sharedObjectId": $("#hiddenObjectId").val(),
      "sharedObjectType": $("#hiddenType").val(),
      "componentId": $("#hiddenComponentId").val(),
      "validity": $("#validity").val(),
      "uri": '',
      "endDateStr": $("#endDate").val(),
      "endDateFormat": $("#endDateFormat").val(),
      "nbAccessMax" : $("#nbAccessMax").val(),
      "users": $("#users").val(),
      "externalEmails" : $("#externalEmails").val(),
      "additionalMessage" : $("#additionalMessage").val()
  };

  jQuery.ajax({
    url: ajaxUrl,
    type: 'POST',
    data: $.toJSON(newTicket),
    contentType: "application/json",
    cache: false,
    dataType: "json",
    async: true,
    success: function(result) {
      $("#message-content-url").html($('<a>',
      { text: result.url,
        //title: 'some title',
        href: result.url
      }));
      showInformation();
      cleanForm();
    },
    error: function (xhr, ajaxOptions, thrownError) {
      var data = /<body.*?>([\s\S]*)<\/body>/.exec(xhr.responseText)[1];
      $("#sharingticket-error-content").html(data);
      $('#sharingticket-error-content').popup('basic', {
        title : "Error"
      });
      cleanForm();
    }
  });
}


function cleanForm() {
  $("#hiddenObjectId").val("");
  $("#hiddenType").val("");
  $("#hiddenComponentId").val("");
  $("#validity").val("0");
  $("#endDate").val("");
  $("#endDateFormat").val("");
  $("#nbAccessMax").val("");
  $("#users").val("");
  $("#users_name").val("");
  $("#externalEmails").val("");
  $("#additionalMessage").val("");
}

function showInformation() {
  $('#sharingticket-message-content').popup('information', {
    title : "<fmt:message key="sharing.tickets" bundle="${fsBundle}"/> > <fmt:message key="sharing.confirmTicket" bundle="${fsBundle}"/>",
    callback : function() {
      return true;
    }
  });
}

</script>

<div id="sharingticket-popup-content" style="display:none; " class="ui-dialog-content ui-widget-content">

  <form name="ticketForm" method="post" action="">
    <input type="hidden" name="componentId" value="" id="hiddenComponentId"/>
    <input type="hidden" name="token" value=""/>
    <input type="hidden" name="objectId" size="60" maxlength="150" value="" id="hiddenObjectId"/>
    <input type="hidden" name="type" value="" id="hiddenType"/>
    <label class="label-ui-dialog" for="langCreate" id="objectName"> </label>
    <span class="champ-ui-dialog" id="displayNameId"></span>

    <fieldset class="filedset-ui-dialog validity" id="ticketValidity">
      <legend><fmt:message key="sharing.validity" bundle="${fsBundle}"/></legend>

      <label class="label-ui-dialog" for="validity" id="validity_label"><fmt:message key="sharing.validity" bundle="${fsBundle}"/></label>
      <span class="champ-ui-dialog">
        <select  id="validity" name="validity" onchange="javascript:toggleContinuous();">
          <option label="<fmt:message key="sharing.validity.continuous" bundle="${fsBundle}"/>" value="0"><fmt:message key="sharing.validity.continuous" bundle="${fsBundle}"/></option>
          <option label="<fmt:message key="sharing.validity.limited" bundle="${fsBundle}"/>" value="1"><fmt:message key="sharing.validity.limited" bundle="${fsBundle}"/></option>
        </select>
      </span>

      <label class="label-ui-dialog threshold" for="endDate"><fmt:message key="sharing.endDate" bundle="${fsBundle}"/></label>
      <span class="champ-ui-dialog threshold">
        <input type="text" class="dateToPick" name="endDate" size="12" maxlength="10" value="" id="endDate" /><span class="txtsublibform">(<fmt:message key="GML.dateFormatExemple"/>)</span>
        <input type="hidden" name="endDateFormat" id="endDateFormat" value="<fmt:message key='GML.dateFormat'/>"/>
        <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
      </span>
      <div id="nbAccessMaxArea">
        <label class="label-ui-dialog threshold" for="nbAccessMax"><fmt:message key="sharing.nbAccessMax" bundle="${fsBundle}"/></label>
        <span class="champ-ui-dialog threshold">
          <input id="nbAccessMax" type="text" value="" maxlength="5" size="5" name="nbAccessMax" class="int-5"/> <fmt:message key="sharing.nbAccessMax.info" bundle="${fsBundle}" /> <img border="0" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
        </span>
      </div>
    </fieldset>

    <fieldset class="filedset-ui-dialog notification">
      <legend><fmt:message key="GML.notification" /></legend>
      <label class="label-ui-dialog" for="users_name"><fmt:message key="GML.users"/></label>
      <span class="champ-ui-dialog">
        <input id="users" name="users" value="" type="hidden" />
        <textarea id="users_name"  name="users$$name" cols="30" rows="2" readonly></textarea>&nbsp;
        <a href="#" onclick="javascript:SP_openWindow( webContext + '/RselectionPeasWrapper/jsp/open?formName=ticketForm&amp;elementId=users&amp;elementName=users$$name&amp;selectedUsers=' + $('#users').val() + '&amp;selectionMultiple=true','selectUser',800,600,'');">
          <img src="<c:url value='${usersIcon}' />" alt="<fmt:message key='GML.users' />" title="<fmt:message key='GML.users' />" align="absmiddle" border="0" height="15" width="15" />
        </a>
      </span>
      <label class="label-ui-dialog" for="externalEmails"><fmt:message key="GML.external.emails" /></label>
      <span class="champ-ui-dialog">
        <input type="text" pattern="^([\w+-.%]+@[\w-.]+\.[A-Za-z]{2,4},*[\W]*)+$" value="" name="externalEmails" id="externalEmails" size="60" maxlength="" />
      </span>
      <label class="label-ui-dialog" for="additionalMessage"><fmt:message key="GML.additional.message" /></label>
      <span class="champ-ui-dialog">
        <textarea name="additionalMessage" id="additionalMessage" rows="5" cols="30" ></textarea>
      </span>
    </fieldset>
  </form>
</div>
<div id="sharingticket-message-content" style="display: none">
  <label class="label-ui-dialog" for=""><fmt:message key="sharing.url" bundle="${fsBundle}"/></label>
  <span class="champ-ui-dialog" id="message-content-url"></span>
</div>
<div id="sharingticket-error-content" style="display: none">
</div>
<script type="application/javascript">
  applyTokenSecurity('#sharingticket-popup-content');
</script>
