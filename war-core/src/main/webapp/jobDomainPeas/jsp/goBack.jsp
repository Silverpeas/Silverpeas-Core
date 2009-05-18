<%@ include file="check.jsp" %>

<HTML>
<HEAD>
<script language='Javascript'>
function viewDomainBar()
{
    <% 
        String URLForContent = (String)request.getAttribute("URLForContent");

        if ((URLForContent != null) && (URLForContent.length() > 0))
        {
            out.println("parent.domainBar.location = \"" + URLForContent + "\"");
        }
    %>
}
</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onload="javascript:viewDomainBar()">
</BODY>
</HTML>