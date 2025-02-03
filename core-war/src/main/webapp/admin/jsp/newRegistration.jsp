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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ include file="../../headLog.jsp" %>

<fmt:setLocale value="<%=userLanguage%>" />
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication" />

<fmt:message key="authentication.logon.newRegistration.qaptcha.lock" var="qaptchaLockMsg"/>
<fmt:message key="authentication.logon.newRegistration.qaptcha.unlock" var="qaptchaUnlockMsg"/>
<fmt:message key="authentication.logon.newRegistration.qaptcha.error" var="qaptchaErrorMsg"/>

<view:sp-page>
<view:sp-head-part minimalSilverpeasScriptEnv="true">
  <link rel="icon" href="<%=favicon%>" />
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />
  <view:includePlugin name="virtualkeyboard"/>
  <view:includePlugin name="popup"/>

  <link rel="stylesheet" type="text/css" href="<c:url value="/util/javaScript/jquery/slidercaptcha/slidercaptcha.min.css"/>" media="screen" />
  <link rel="stylesheet" type="text/css" href="<c:url value="/util/javaScript/jquery/slidercaptcha/all.min.css"/>" media="screen" />
  <style type="text/css">
  
    .card {
    	border: none;
    }
    
  </style>
  <!-- jQuery files -->
  <view:script src="/util/javaScript/jquery/slidercaptcha/longbow.slidercaptcha.min.js"/>
  <view:script src="/util/javaScript/checkForm.js"/>
  <script type="text/javascript">
  function checkIsNotEmpty(text) {
    return new Promise(function(resolve, reject) {
      resolve(!isWhitespace(text));
    });
  }
  function checkEmailIsCorrectlyFormatted(src) {
    return new Promise(function(resolve, reject) {
      const regex = /^[a-z0-9A-Z_.\-]+@[a-z0-9A-Z_.\-]*[a-z0-9A-Z][.][a-zA-Z]{2,6}$/;
      resolve(!isEmpty(src) && src.match(regex) != null);
    });
  }
  function checkEmailDoesNotExist(email) {
    return new Promise(function(resolve, reject) {
      $.get("<c:url value="/MailExists"/>?email=" + escape(email),
          function(data) {
            console.info("Check if " + email + " exists: " + data);
            resolve(data.indexOf('MailExists') === -1);
        });
    });
  }
  function checkAvatar() {
    return new Promise(function(resolve, reject) {
      const image = $("#avatar").val();
      resolve(isWhitespace(image) || isAnImageExtension(image));
    });
  }

  function isAnImageExtension(filename) {
    const indexPoint = filename.lastIndexOf(".");
    // check the filename includes the file extension
    if (indexPoint !== -1) {
      // in this case, the file extension is fetched
      const ext = filename.substring(indexPoint + 1).toLowerCase();
      return (ext === "gif") || (ext === "jpeg") || (ext === "jpg") || (ext === "png");
    }
    return false;
  }

  function get$form() {
    return jQuery(document.getElementById("EDform"));
  }

  function saveNewUser() {
    if ($("#identity-template").length) {
      checkForm(function() {
        ifCorrectFormExecute(function() {
          get$form().submit();
        });
      });
    } else {
      checkForm(function() {
        get$form().submit();
      });
    }
  }

  function checkForm(callback) {
    const form = document.getElementById("EDform");
    const lastName = stripInitialWhitespace(form.elements["lastName"].value);
    const firstName = stripInitialWhitespace(form.elements["firstName"].value);
    const email = stripInitialWhitespace(form.elements["email"].value);

    function checkResult(result, errorMessage) {
      if (!result) {
        SilverpeasError.add(errorMessage);
      }
    }
    checkIsNotEmpty(firstName)
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.firstNameRequired'/>\n");
          return checkIsNotEmpty(lastName);
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.lastNameRequired'/>\n");
          return checkIsNotEmpty(email);
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.emailRequired'/>\n");
          return checkAvatar();
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.avatar.bad'/>\n");
          return checkEmailIsCorrectlyFormatted(email);
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.emailBadSyntax'/>\n");
          return checkEmailDoesNotExist(email);
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.alreadyRegistered'/>\n");
          if (!SilverpeasError.show()) {
            form.action='<c:url value="/CredentialsServlet/Register" />';
            callback.call(this);
          }
        });
  }

  function checkSubmit(ev) {
    const keyCode = ev.keyCode;
    if (keyCode === 13) {
      saveNewUser();
    }
  }

  $(document).ready(function(){
  	var captcha = sliderCaptcha({
    	id:'captcha',
    	width: 600,
    	height: 450,
    	failedText:'${silfn:escapeJs(qaptchaLockMsg)}',
    	barText:'${silfn:escapeJs(qaptchaLockMsg)}',
    	onSuccess:function () {
      		$('#btn-register').attr('onclick', 'saveNewUser()');
    	}
   	});
  });
  </script>

</view:sp-head-part>
<view:sp-body-part id="self-registration">
  <form id="EDform" action="javascript:saveNewUser();" method="post" accept-charset="UTF-8" enctype="multipart/form-data">
    <div id="top"></div>
    <div class="page">
      <div class="titre"><fmt:message key="registration.title"/></div>
        <div id="background">
            <div class="cadre">
                <div class="registrationText">
        <p class="noSilverpeasAccount"><fmt:message key="registration.noSilverpeasAccount"/></p>
        <p class="noSilverpeasAccount"><fmt:message key="registration.completeProfile"/></p>
      </div>

      <div class="form-registration">
        <div class="form-registration-firstName">
          <label for="firstName"><fmt:message key="registration.firstname" /></label>
          <input type="text" name="firstName" id="firstName" value="${userProfile.firstName}"/>
        </div>
        <div class="form-registration-lastName">
          <label for="lastName"><fmt:message key="registration.lastname" /></label>
          <input type="text" name="lastName" id="lastName" value="${userProfile.lastName}"/>
        </div>
        <div class="form-registration-email">
          <label for="email"><fmt:message key="registration.email" /></label>
          <input type="text" name="email" id="email" value="${userProfile.email}"/>
        </div>
        <div class="form-registration-avatar">
          <label for="avatar"><fmt:message key="registration.avatar" /></label>
          <input type="file" name="avatar" id="avatar" />
        </div>
      </div>

      <view:directoryExtraForm userId="unknown" edition="true"/>

      
      
      <div class="slidercaptcha card">
  	
        <div class="card-body">
    		<div id="captcha"></div>
  	</div>
      </div>

      
      
      <a href="#" id="btn-register" class="btn-registrer submit" onclick="">
          <span><span><fmt:message key="registration.title" /></span></span>
      </a>
            </div>
        </div>
        <div id="copyright"><fmt:message key="GML.trademark" /></div>
    </div>
  </form>

  <script type="text/javascript">
    document.querySelector("input").focus();
  </script>

</view:sp-body-part>
</view:sp-page>
