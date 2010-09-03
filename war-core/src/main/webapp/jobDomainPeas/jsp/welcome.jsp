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

<%@ include file="check.jsp"%>
<%
  Boolean displayOperations = (Boolean) request.getAttribute("DisplayOperations");
  String content = (String) request.getAttribute("Content");

  Board board = gef.getBoard();
%>




<%@page import="org.antlr.stringtemplate.*"%>

<%@page
	import="org.antlr.stringtemplate.language.AngleBracketTemplateLexer"%>
<%@page import="com.silverpeas.util.template.SilverpeasTemplate"%>
<%@page import="com.silverpeas.util.template.SilverpeasTemplateFactory"%><HTML>
<HEAD>
<%
  out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5"
	bgcolor="#FFFFFF">
<%
  if (displayOperations.booleanValue()) {
    operationPane.addOperation(resource.getIcon("JDP.domainAdd"), resource
        .getString("JDP.domainAdd"), "displayDomainCreate");
    operationPane.addOperation(resource.getIcon("JDP.domainSqlAdd"), resource
        .getString("JDP.domainSQLAdd"), "displayDomainSQLCreate");
  }

  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<center>
<%
  out.println(board.printBefore());

  out.println(content);

  out.println(board.printAfter());
%>
</center>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</BODY>
</HTML>