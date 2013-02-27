<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory" %>
<%@ include file="headLog.jsp" %>

<%
  ResourceLocator authenticationBundle =
      new ResourceLocator("com.silverpeas.authentication.multilang.authentication", "");
  GraphicElementFactory gef =
      (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=generalMultilang.getString("GML.popupTitle")%>
  </title>
  <link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/util/icons/favicon.ico"/>
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/silverpeas-password.css"/>
  <script src="<%=m_context%>/util/javaScript/jquery/jquery-1.7.1.min.js" type="text/javascript"></script>
  <script src="<%=m_context%>/util/javaScript/jquery/jquery.json-2.3.min.js" type="text/javascript"></script>
  <script src="<%=m_context%>/util/javaScript/jquery/jquery.i18n.properties-min-1.0.9.js" type="text/javascript"></script>
  <script src="<%=m_context%>/password.js" type="text/javascript"></script>
  <!--[if lt IE 8]>
  <style type="text/css">
    input {
      background-color: #FAFAFA;
      border: 1px solid #DAD9D9;
      width: 448px;
      text-align: left;
      margin-left: -10px;
      height: 26px;
      line-height: 24px;
      display: block;
      padding: 0;
    }
  </style>
  <![endif]-->
  <script type="text/javascript">
    var webContext = '<%=m_context%>';
    $(document).ready(function() {
      handlePasswordForm({
        passwordFormId : 'changePwdForm',
        passwordFormAction : '<c:url value="/CredentialsServlet/EffectiveChangePasswordBeforeExpiration"/>',
        passwordInputId : 'newPassword'
      })
    });
  </script>
  <script src="<%=m_context%>/util/javaScript/silverpeas-password.js" type="text/javascript"></script>
</head>

<body>
<form id="changePwdForm" action="#" method="post">
  <div class="page">
    <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %></div>
    <div id="backgroundBig">
      <div class="cadre">
        <div id="header">
          <img src="<%=logo%>" class="logo"/>

          <p class="information"><%=authenticationBundle
              .getString("authentication.password.aboutToExpire") %><br/>
            <%
              String message = (String) request.getAttribute("message");
              if (message != null) {
            %>
            <span><%=message%></span><br/>
            <%
              }
            %></p>

          <div class="clear"></div>
        </div>
        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.old") %></span><input type="password" name="oldPassword" id="oldPassword"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.new") %></span><input type="password" name="newPassword" id="newPassword"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.confirm") %></span><input type="password" name="confirmPassword" id="confirmPassword"/></label>
        </p>
        <br/>

        <p>
        <table cellspacing="0" width="100%">
          <tbody>
          <tr>
            <td style="background-image: url('<%=m_context%>/images/bt-left.png'); width: 31px; height: 31px">
              &nbsp;</td>
            <td style="background: url('<%=m_context%>/images/bt-bg.png') repeat-x;">
              <input type="submit" style="width:0; height:0; border:0; padding:0"/>
              <a href="#" class="submit" onclick="$('#changePwdForm').submit()"><%=authenticationBundle
                  .getString("authentication.password.change") %>
              </a></td>
            <td style="background-image: url('<%=m_context%>/images/bt-right.png');width: 16px; height: 31px">
              &nbsp;</td>
            <td style="width: 16px; ">&nbsp;</td>
            <td style="background-image: url('<%=m_context%>/images/bt-left.png'); width: 31px; height: 31px">
              &nbsp;</td>
            <td style="background: url('<%=m_context%>/images/bt-bg.png') repeat-x;">
              <a href="<%=m_context%>/Main/<%=gef.getLookFrame() %>" class="submit"><%=authenticationBundle
                  .getString("authentication.password.remindMeLater") %>
              </a></td>
            <td style="background-image: url('<%=m_context%>/images/bt-right.png');width: 16px; height: 31px">
              &nbsp;</td>
          </tr>
          </tbody>
        </table>
        </p>

        <p>
          <span class="passwordRules"><a href="#" onclick="$('#newPassword').focus()">
            <%=authenticationBundle.getString("authentication.password.showRules") %>
          </a></span>
        </p>
      </div>
    </div>
  </div>
</form>
</body>
</html>