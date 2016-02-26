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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${pageContext.request.locale.language}" />
<%@ include file="../../headLog.jsp" %>

<view:setBundle basename="org.silverpeas.social.multilang.registration" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<link rel="SHORTCUT ICON" href='<c:url value="/util/icons/favicon.ico" />'/>
<link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />

<!--[if lt IE 8]>
<style type="text/css">
input{
	background-color:#FAFAFA;
	border:1px solid #DAD9D9;
	width:448px;
	text-align:left;
    margin-left:-10px;
    height:26px;
    line-height:24px;
    display:block;
    padding:0;
}
</style>
<![endif]-->

<style>
.titre {
    left: 375px;
    top: 15px;
}
</style>

<view:includePlugin name="jquery"/>

<script type="text/javascript">

//whitespace characters
var whitespace = " \t\n\r";


// Check whether string s is empty.
function isEmpty(s) {
	return ((s == null) || (s.length == 0))
}

//Returns true if string s is empty or
//whitespace characters only.
function isWhitespace (s) {

	var i;

 // Is s empty?
 if (isEmpty(s)) return true;

 // Search through string's characters one by one
 // until we find a non-whitespace character.
 // When we do, return false; if we don't, return true.
 for (i = 0; i < s.length; i++) {
     // Check that current character isn't whitespace.
     var c = s.charAt(i);
     if (whitespace.indexOf(c) == -1) return false;
 }

 // All characters are whitespace.
 return true;
}

//WORKAROUND FUNCTION FOR NAVIGATOR 2.0.2 COMPATIBILITY.
//
// The below function *should* be unnecessary.  In general,
// avoid using it.  Use the standard method indexOf instead.
//
// However, because of an apparent bug in indexOf on
// Navigator 2.0.2, the below loop does not work as the
// body of stripInitialWhitespace:
//
// while ((i < s.length) && (whitespace.indexOf(s.charAt(i)) != -1))
//   i++;
//
// ... so we provide this workaround function charInString
// instead.
//
// charInString (CHARACTER c, STRING s)
//
// Returns true if single character c (actually a string)
// is contained within string s.
function charInString (c, s) {
	for (i = 0; i < s.length; i++) {
		if (s.charAt(i) == c) return true;
    }
    return false
}

function checkEmail(src)
{
 if (src==null || src=='')
   return true;
 else
 {
  var regex = /^[a-z0-9A-Z\_\.\-]{1,}[\@@]{1}[a-z0-9A-Z\_\.\-]*[a-z0-9A-Z]{1}[\.]{1}[a-zA-Z]{2,6}$/;
  return !(src.match(regex)==null);
 }
}

// Removes initial (leading) whitespace characters from s.
// Global variable whitespace (see above)
// defines which characters are considered whitespace.
function stripInitialWhitespace (s) {
	var i = 0;
    while ((i < s.length) && charInString (s.charAt(i), whitespace))
       i++;

    return s.substring (i, s.length);
}

function emailExists(email) {
	var exists = false;
	$.ajax({
		  url: "<c:url value="/MailExists"/>?email="+escape(email),
		  async: false
		}).done(function(data) {
			if (data.indexOf('MailExists') != -1) {
				exists = true;
			}
			else {
				exists = false;
			}
		});
	return exists;
}

function checkForm()
{
	var form = document.getElementById("EDform");
	var errorMsg = "";
    var errorNb = 0;

	var lastName = stripInitialWhitespace(form.elements["lastName"].value);
	var firstName = stripInitialWhitespace(form.elements["firstName"].value);
    var email = stripInitialWhitespace(form.elements["email"].value);

    if (isWhitespace(firstName)) {
		errorMsg+=" - <fmt:message key="registration.firstNameRequired"/>\n";
		errorNb++;
     }

    if (isWhitespace(lastName)) {
		errorMsg+=" - <fmt:message key="registration.lastNameRequired"/>\n";
		errorNb++;
     }

    if (isWhitespace(email)) {
		errorMsg+=" - <fmt:message key="registration.emailRequired"/>\n";
		errorNb++;
    }

    else if (!checkEmail(email)) {
		errorMsg+=" - <fmt:message key="registration.emailBadSyntax"/>\n";
        errorNb++;
    }

    else if (emailExists(email)) {
		errorMsg+=" - <fmt:message key="registration.alreadyRegistered"/>\n<fmt:message key="registration.pleaseConnectWithSvpAccount"/>";
        errorNb++;
    }

    if (errorNb>0) {
        window.alert(errorMsg);
        return false;
    }

    form.action='<c:url value="/SocialNetworkLogin" />';
	form.submit();
}

function checkSubmit(ev)
{
	var touche = ev.keyCode;
	if (touche == 13) {
      checkForm();
    }
}
-->
</script>

</head>
<body>
      <form id="EDform" action="javascript:checkForm();" method="post" accept-charset="UTF-8">
	<input type="hidden" name="command" value="register"/>
	<input type="hidden" name="networkId" value="${networkId}"/>
        <div id="top"></div> <!-- Backgroud fonce -->
        <div class="page"> <!-- Centrage horizontal des elements (960px) -->
          <div class="titre"><fmt:message key="registration.title"/></div>
            <div id="background"> <!-- image de fond du formulaire -->
                <div class="cadre">
                    <p style="text-align: center">
			<span>
				<fmt:message key="registration.noSilverpeasAccount"/><br>
				<fmt:message key="registration.completeProfile"/>
                        </span><br><br>
					</p>

                    <p><label for="firstName"><span><fmt:message key="registration.firstname" /></span></label><input type="text" name="firstName" id="firstName" value="${userProfile.firstName}"/></p>
                    <p><label for="lastName"><span><fmt:message key="registration.lastname" /></span></label><input type="text" name="lastName" id="lastName" value="${userProfile.lastName}"/></p>
                    <p><label for="email"><span><fmt:message key="registration.email" /></span></label><input type="text" name="email" id="email" value="${userProfile.email}"/></p>

				<p>&nbsp;</p>
                    <p>&nbsp;</p>
                    <p>&nbsp;</p>

                    <p><a href="#" class="submit" onclick="checkForm();"><img src='<c:url value="/images/bt-ok.png" />' alt="register"/></a></p>

                </div>
            </div>
            <div id="copyright"><fmt:message key="GML.trademark" /></div>
        </div>
        </form><!-- Fin class="page" -->

		<script type="javascript">
			document.getElementById("EDform").email.focus();
		</script>

</body>
</html>
