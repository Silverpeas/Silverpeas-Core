<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ include file="import.jsp" %>

<HTML>
<HEAD>
<TITLE></TITLE>
<script language='Javascript'>
function forwardToComponent()
{
    <% 
        String component = (String)request.getParameter("component");
        String space = (String)request.getParameter("space");

        if ((component != null) && (component.length() > 0))
        {
            out.println("window.location = \"" + m_context + URLManager.getURL(space, component) + "Main\"");
        }
    %>
}
</script>
</HEAD>
<BODY onload="javascript:forwardToComponent()">
</BODY>
</HTML>
