<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ include file="headLog.jsp"%>
<%
    ResourceLocator reinitPasswordBundle = new ResourceLocator("com.silverpeas.authentication.multilang.forgottenPasswordMail", "");
	ResourceLocator authenticationBundle = new ResourceLocator("com.silverpeas.authentication.multilang.authentication", "");

	String action = request.getParameter("Action");
	String actionLabel = "";
	String actionTitle = reinitPasswordBundle.getString("screen.title.reinitRequested");
	    
	if ("InvalidLogin".equalsIgnoreCase(action))
	{
	  actionLabel = reinitPasswordBundle.getString("screen.invalidLogin");
	}
	else if ("FirstMailSended".equalsIgnoreCase(action))
	{
	  actionLabel = reinitPasswordBundle.getString("screen.reinitRequested");
	}
	else if ("ChangeNotAllowed".equalsIgnoreCase(action))
	{
	  actionLabel = reinitPasswordBundle.getString("screen.reinitNotAllowed");
	}
	else if ("NewPasswordSended".equalsIgnoreCase(action))
	{
	  actionLabel = reinitPasswordBundle.getString("screen.reinitDone");
	  actionTitle = reinitPasswordBundle.getString("screen.title.reinitDone");
	}
	else if ("NewPasswordError".equalsIgnoreCase(action))
	{
	  actionLabel = reinitPasswordBundle.getString("screen.reinitError");
	}
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
</head>
<body>
        <div id="top"></div> <!-- Backgroud fonc� -->
        <div class="page"> <!-- Centrage horizontal des �l�ments (960px) -->
            <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %></div>
            <div id="background"> <!-- image de fond du formulaire -->    	
                <div class="cadre">   
                    <div id="header">
                        <img src="<%=logo%>" class="logo" />
                        <p class="information"><%=actionTitle %></p>
                        <div class="clear"></div>
                    </div>
                    <p><label><%=actionLabel %></label></p>
					<br/>
					<p><a href="#" class="submit" onclick="location.href='<%=request.getContextPath()%>/Login.jsp'"><img src="../images/bt-ok.png" /></a></p>
                </div>  
            </div>
        </div>
</body>

</html>