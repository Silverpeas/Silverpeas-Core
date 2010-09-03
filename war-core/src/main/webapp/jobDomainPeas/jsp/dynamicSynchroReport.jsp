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

<%@ include file="check.jsp" %>
<html>
<head>
<title> Synchro Dynamic Report - Mode <%=SynchroReport.getTraceLevelStr()%></title>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<NOBR>
<%
    String toDisp = SynchroReport.getMessage();
    boolean isActive = true;

    SynchroReport.setState(SynchroReport.STATE_WAITSTART);
    while (isActive || (toDisp != null))
    {
        if (toDisp != null)
        {
            if (toDisp.length() > 0) 
            {
                if (toDisp.startsWith("F"))
                {
                    out.print("<FONT SIZE=3 COLOR=#FF0000>");
                }
                else if (toDisp.startsWith("E"))
                {
                    out.print("<FONT SIZE=3 COLOR=#FF6666>");
                }
                else if (toDisp.startsWith("W"))
                {
                    out.print("<FONT COLOR=#000000>");
                }
                else if (toDisp.startsWith("I"))
                {
                    out.print("<FONT COLOR=#0000FF>");
                }
                else if (toDisp.startsWith("D"))
                {
                    out.print("<FONT COLOR=#B6B6B6>");
                }
                else
                {
                    out.print("<FONT COLOR=#00FF00>");
                }
            }
            out.print(EncodeHelper.javaStringToHtmlString(toDisp.substring(2)) + "<BR>");
            out.print("</FONT>");
            out.flush();
        }
        // Attention a l'ordre !!! Il faut absolument d'abord tester l'etat avant de faire l'appel a getMessage
        if ((SynchroReport.isSynchroActive()) || (SynchroReport.getState() == SynchroReport.STATE_WAITSTART))
            isActive = true;
        else
            isActive = false;
        toDisp = SynchroReport.getMessage();
    }
    SynchroReport.setState(SynchroReport.STATE_NOSYNC);
%>
</NOBR>
</body>
</html>