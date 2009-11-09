<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
String        strCurrentTab   = "ViewUserInfos",
              strContext = (String)request.getAttribute("context");
DataFolder    items = (DataFolder)request.getAttribute("Items");
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
function sendData() 
{
    document.workflowHeaderForm.submit();
}
</script>
</HEAD>
<body>
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.userInfos"));

addItem( operationPane, resource, strContext, "workflowDesigner.add.item" );

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="<%=strCurrentTab%>"/>
<%
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.userInfos")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());
%>
<designer:itemList currentScreen="<%=strCurrentTab%>" context="<%=strContext%>" 
                   items="<%=items%>" paneTitleKey="workflowDesigner.list.userInfos" />
<%
out.println(board.printAfter());
%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="POST">
</form>
<designer:buttonPane cancelAction="Main" />
<%    
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
