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
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="com.silverpeas.jobDomainPeas.JobDomainSettings"%>
<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ include file="headLog.jsp"%>

<%
int minLengthPassword = JobDomainSettings.m_MinLengthPwd;
ResourceLocator authenticationBundle = new ResourceLocator("com.silverpeas.authentication.multilang.authentication", "");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title><%=generalMultilang.getString("GML.popupTitle")%></title>
		<link REL="SHORTCUT ICON" HREF="<%=request.getContextPath()%>/util/icons/favicon.ico">
		<link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />
		<!--[if lt IE 8]>
			<style>
			input{
				background-color:#FAFAFA;
				border:1px solid #DAD9D9;
				width:448px;
				text-align:left;
			    margin-left:-10px;
			    height:26px;
			    line-height:24px;
			    padding:0px 60px;
			    display:block;
			    padding:0px;
			}
			</style>
		<![endif]-->
		<script type="text/javascript" src="<%=m_context%>/passwordValidator.js"></script>
	    <script type="text/javascript">

		function checkPassword() {
			var form = document.getElementById("changePwdForm");
			var newPassword = form.newPassword.value;
			var passed = validatePassword(newPassword, {
				length:   [4, Infinity],
				combined: 0
			});
	    	if (newPassword != form.confirmPassword.value) {
	    		alert("<%=authenticationBundle.getString("authentication.password.different") %>");
		    	return false;
	    	}
	    	else if (passed == false) {
		    	alert("Votre mot de passe doit comporter au moins huit caractères et être composé d'une combinaison de trois types de caractères (à choisir entre minuscules, majuscules, chiffres et signes spéciaux). ");
		    	return false;
	    	}
	    	else {
			    form.submit();
	    	}
	    }

	    </script>
	</head>

<body>
	<form id="changePwdForm" action="<%=m_context%>/CredentialsServlet/ChangeExpiredPassword" method="post">
        <div id="top"></div> <!-- Background foncé -->
        <div class="page"> <!-- Centrage horizontal des éléments (960px) -->
            <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %></div>
            <div id="backgroundBig"> <!-- image de fond du formulaire -->
                <div class="cadre">
                    <div id="header">
                        <img src="<%=logo%>" class="logo" />
                        <p class="information"><%=authenticationBundle.getString("authentication.password.expired") %><br/>
								<%
									String message = (String) request.getAttribute("message");
									if (message != null) {
								%>
									( <%=message%> )<br/>
								<%
									}
								%></p>
                        <div class="clear"></div>
                    </div>
					<p><label><span><%=authenticationBundle.getString("authentication.password.old") %></span><input type="password" name="oldPassword" id="oldPassword"/></label></p>
					<p><label><span><%=authenticationBundle.getString("authentication.password.new") %> <%=authenticationBundle.getStringWithParam("authentication.password.length", Integer.toString(minLengthPassword)) %></span><input type="password" name="newPassword" id="newPassword"/></label></p>
					<p><label><span><%=authenticationBundle.getString("authentication.password.confirm") %></span><input type="password" name="confirmPassword" id="confirmPassword"/></label></p>
                    <input type="hidden" name="login" value="${param.login}" />
                    <input type="hidden" name="domainId" value="${param.domainId}" />
					<br/>
					<p>
						<table cellspacing="0" width="100%">
						<tbody>
						<tr>
							<td style="background-image: url('images/bt-left.png'); width: 31px; height: 31px">&nbsp;</td>
							<td style="background-image: url('images/bt-bg.png'); background-repeat: repeat-x;"><a href="#" class="submit" onclick="checkPassword();"><%=authenticationBundle.getString("authentication.password.change") %></a></td>
							<td style="background-image: url('images/bt-right.png');width: 16px; height: 31px">&nbsp;</td>
						</tr>
						</tbody>
						</table>
					</p>
                </div>
            </div>
        </div>
      </form>

	</body>
</html>