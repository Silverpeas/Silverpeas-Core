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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.crypto.multilang.cryptoBundle" />

<fmt:message var="title"       key="crypto.cipherKeyImport"/>
<fmt:message var="description" key="crypto.importDescription"/>
<fmt:message var="label"       key="crypto.cipherKey"/>
<fmt:message var="buttonLabel" key="crypto.import"/>
<fmt:message var="hexaError"   key="crypto.keyNotInHexadecimal"/>
<fmt:message var="sizeError"   key="crypto.invalidKeySize"/>
<html>
<head>
  <view:looknfeel/>
  <style type="text/css">
    #help {
      margin: 2em;
    }
    #action {
      margin: 2em;
      text-align: center;
    }
  </style>
  <script type="text/javascript">
    function isInHexadecimal(key) {
      var hexa = /^[0-9A-F]+$/gi;
      return key.match(hexa) !== null;
    }

    function isIn256Bits(key) {
      /* 256 bits = 64 characters in hexadecimal */
      return key.length === 64;
    }

    function printError(msg) {
      var status = $("#status");
      status.children().remove();
      $("<p>").addClass("inlineMessage-nok").html(msg.replace("\n", "<br/>")).appendTo(status);
    }

    function printMessage(msg) {
      var status = $("#status");
      status.children().remove();
      $("<p>").addClass("inlineMessage-ok").html(msg.replace("\n", "<br/>")).appendTo(status);
    }

    function importCipherKey() {
      var key = $("#cipherKey").val();
      if (!isInHexadecimal(key)) {
        printError("${hexaError}");
      }
      else if (!isIn256Bits(key)) {
        printError("${sizeError}");
      }
      else {
        $.progressMessage();
        $.ajax(webContext + "/services/security/cipherkey", {
          type: 'PUT',
          data: key,
          contentType:"text/plain",
          dataType: "text",
          processData: false,
          success: function(status) {
            $.closeProgressMessage();
            printMessage(status);
          },
          error: function(jqXHR, textStatus, errorThrown) {
            $.closeProgressMessage();
            if (jqXHR.responseText)
              printError(jqXHR.responseText);
            else
              printError(errorThrown);
          }
        });
      }
    }

    $(document).ready(function() {
      var enterKey = 13;
      $("#cipherKey").keypress(function(event) {
        if ( event.which === enterKey ) {
          event.preventDefault();
          importCipherKey();
        }
      });
    });
  </script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<view:browseBar ignoreComponentLink="true" componentId="${title}"/>
<view:window>
  <view:frame>
    <view:board>

      <div id="help" class="inlineMessage">
        <p>${description}</p>
      </div>

      <form>
        <label for="cipherKey">${label}</label>
        <input type="text" size="64" maxlength="64" name="cipherKey" id="cipherKey"/>

        <div id="status">
        </div>

        <div id="action">
          <view:buttonPane>
            <view:button label="${buttonLabel}" action="javascript:importCipherKey();"/>
          </view:buttonPane>
        </div>
      </form>

    </view:board>
  </view:frame>
</view:window>

<view:progressMessage/>
</body>
</html>