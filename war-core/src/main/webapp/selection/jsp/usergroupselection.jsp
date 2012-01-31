<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" />
<fmt:message var="selectLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <title><fmt:message key="GML.selection"/></title>
    <script type="text/javascript" >
      var callbackUrl = '<c:out value="${requestScope.url}"/>';
      
      function validateSelection() {
        var usersSelection = '', groupsSelection = '';
        $('#users :checkbox:checked').each(function() {
          usersSelection += $(this).val() + ' ';
        });
        if(callbackUrl) {
          $("input#user-selection").val($.trim(usersSelection));
          $("#selection").submit();
        }
      }
      
      $(document).ready(function() {
        $.ajax({
          url: webContext + '/services/profile/users',
          type: 'GET',
          dataType: 'json',
          cache: false,
          success: function(users) {
            var style = 'even';
            $.each(users, function() {
              $('<li>').addClass(style).
                append($('<input>', {type: 'checkbox', value: this.id})).
                append($('<img>', {src: webContext + this.avatar, alt: this.lastName + ' ' + this.firstName})).
                append($('<span>').addClass('lastname').text(this.lastName)).
                append($('<span>').addClass('fistname').text(this.firtstName)).
                append($('<span>').addClass('email').text('(' + this.eMail + ')')).
                append($('<span>').addClass('domain').text(this.domainName))
              .appendTo('#user_list');
              if (style == 'even')
                style = 'odd';
              else
                style = 'even';
            });
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });
      });
    </script>
  </head>
  <body>
    <form action="<c:out value='${requestScope.url}'/>" id="selection" method="POST">
      <input id="user-selection" type="hidden" value=""/>
      <input id="group-selection" type="hidden" value=""/>
      <div id="users">
        <ul id="user_list">
        </ul>
      </div>
    </form>
    <div id="validate">
      <view:buttonPane>
        <view:button label="${selectLabel}" action="javascript: validateSelection();"/>
        <view:button label="${cancelLabel}" action="javascript: window.close();"/>
      </view:buttonPane>
    </div>
  </body>
</html>
