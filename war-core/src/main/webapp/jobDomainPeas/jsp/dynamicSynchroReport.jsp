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
            out.print(Encode.javaStringToHtmlString(toDisp.substring(2)) + "<BR>");
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