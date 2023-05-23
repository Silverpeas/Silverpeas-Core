<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="../../headLog.jsp" %>

<fmt:setLocale value="<%=userLanguage%>" />
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication" />
<view:sp-page>
<view:sp-head-part minimalSilverpeasScriptEnv="true">
<link rel="icon" href="<%=favicon%>" />
<link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />
<view:includePlugin name="virtualkeyboard"/>
<view:includePlugin name="popup"/>
<style>
.titre {
    left: 375px;
}

.submit {
	width: 95px;
    background-color: transparent;
    border: 0;
}
</style>

<link rel="stylesheet" type="text/css" href="<c:url value="/util/javaScript/jquery/qaptcha/jquery/QapTcha.jquery.css"/>" media="screen" />

<!-- jQuery files -->
<script src="<c:url value="/util/javaScript/jquery/qaptcha/jquery/jquery.ui.touch.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<c:url value="/util/javaScript/jquery/qaptcha/jquery/QapTcha.jquery.js"/>" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">

//whitespace characters
const whitespace = " \t\n\r";


// Check whether string s is empty.
function isEmpty(s) {
	return ((s === null) || (s === undefined) || (s.length === 0));
}

//Returns true if string s is empty or
//whitespace characters only.
function isWhitespace (s) {
 // Is s empty?
 if (isEmpty(s)) return true;

 // Search through string's characters one by one
 // until we find a non-whitespace character.
 // When we do, return false; if we don't, return true.
 for (let i = 0; i < s.length; i++) {
     // Check that current character isn't whitespace.
     const c = s.charAt(i);
     if (whitespace.indexOf(c) === -1) return false;
 }

 // All characters are whitespace.
 return true;
}


// Removes initial (leading) whitespace characters from s.
// Global variable whitespace (see above)
// defines which characters are considered whitespace.
function stripInitialWhitespace (s) {
    let i = 0;
    while ((i < s.length) && whitespace.includes(s.charAt(i)))
       i++;

    return s.substring (i, s.length);
}

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

function checkForm()
{
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
          return checkIsNotEmpty(email)
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.emailRequired'/>\n");
          return checkEmailIsCorrectlyFormatted(email);
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.emailBadSyntax'/>\n");
          return checkEmailDoesNotExist(email)
        })
        .then(function(result) {
          checkResult(result, "<fmt:message key='registration.alreadyRegistered'/>\n");
          if (!SilverpeasError.show()) {
            form.action='<c:url value="/CredentialsServlet/Register" />';
            form.submit();
          }
        });
}

function checkSubmit(ev)
{
    const touche = ev.keyCode;
    if (touche === 13) {
      checkForm();
    }
}

$(document).ready(function(){
		// More complex call
		$('#QapTcha').QapTcha({
			autoSubmit : false,
			autoRevert : true,
			PHPfile : '<c:url value="/Qaptcha"/>'
		});
});
</script>

</view:sp-head-part>
<view:sp-body-part>
      <form id="EDform" action="javascript:checkForm();" method="post" accept-charset="UTF-8">
        <div id="top"></div> <!-- Backgroud fonce -->
        <div class="page"> <!-- Centrage horizontal des elements (960px) -->
          <div class="titre"><fmt:message key="registration.title"/></div>
            <div id="background"> <!-- image de fond du formulaire -->
                <div class="cadre">
                    <p style="text-align: center">
			<span>
				<fmt:message key="registration.noSilverpeasAccount"/><br/>
				<fmt:message key="registration.completeProfile"/>
                        </span><br/><br/>
					</p>

                    <p><label for="firstName"><span><fmt:message key="registration.firstname" /></span><input type="text" name="firstName" id="firstName" value="${userProfile.firstName}"/></label></p>
                    <p><label for="lastName"><span><fmt:message key="registration.lastname" /></span><input type="text" name="lastName" id="lastName" value="${userProfile.lastName}"/></label></p>
                    <p><label for="email"><span><fmt:message key="registration.email" /></span><input type="text" name="email" id="email" value="${userProfile.email}"/></label></p>

				<p>&nbsp;</p>
                    <p>&nbsp;</p>
                    <p>&nbsp;</p>
					<div id="QapTcha"></div>
                    <p><input type="image" class="submit" src='<c:url value="/images/bt-ok.png" />' alt="register"/></p>

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
