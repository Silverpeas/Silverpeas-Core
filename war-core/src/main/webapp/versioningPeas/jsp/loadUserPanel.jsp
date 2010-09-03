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

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ include file="checkVersion.jsp" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
String pfile = request.getParameter("pfile");
String statusDoc = request.getParameter("statusDoc");
String fromUp = request.getParameter("fromUp");
String urlUP;
if (fromUp != null &&  "fromUp".equals(fromUp)) {
    if ("update".equals(statusDoc)) {
        urlUP =  m_context+ "/RVersioningPeas/jsp/versions.jsp?";
    } else {
        urlUP =  m_context+ "/RVersioningPeas/jsp/newDocument.jsp?";
    }
    if (pfile != null && !"".equals(pfile)) {
        if ("Reader".equals(pfile)) {
            urlUP += "tab_number=2&from_action=2";
        } else {
            String style_type = String.valueOf(versioningSC.getEditingDocument().getTypeWorkList());
            urlUP += "tab_number=3&from_action=3&style_type="+style_type + "&lines=" + versioningSC.getEditingDocument().getWorkList().size();
        }
    }

    urlUP += "&profile=admin&reset=true";
} else {
    urlUP = m_context+"/RVersioningPeas/jsp/fromVersioning?pfile="+pfile+"&statusDoc="+statusDoc;
}
 
%>
<html>
<head>
<script language="Javascript">
location.replace("<%=urlUP%>");
</script>
</head>
<body></body>
</html>