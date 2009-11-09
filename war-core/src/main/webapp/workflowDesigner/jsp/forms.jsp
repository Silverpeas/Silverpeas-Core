<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
String     strContextEncoded,
           strRoleName,
           strEditForm,
           strCurrentTab = "ViewForms";
Forms      forms = (Forms)request.getAttribute( "Forms" );
ArrayPane  formsPane = gef.getArrayPane("formsList", strCurrentTab, request, session);
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
browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
browseBar.setComponentName(resource.getString("workflowDesigner.forms") );

operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.add.form"),
        "AddForm");

formsPane.setVisibleLineNumber(20);
formsPane.setTitle(resource.getString("workflowDesigner.list.form"));
formsPane.addArrayColumn(resource.getString("GML.name"));
formsPane.addArrayColumn(resource.getString("workflowDesigner.role"));
column = formsPane.addArrayColumn(resource.getString("workflowDesigner.HTMLFileName"));
column.setSortable( false );
column = formsPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable( false );

if ( forms != null )
{
    Form       form;
    Iterator   iterForm = forms.iterateForm();

    while ( iterForm.hasNext() )
    {
        form = (Form)iterForm.next();
        strRoleName = form.getRole() == null ? "" : form.getRole();
        strContextEncoded = URLEncoder.encode( "forms[" + form.getName() + "," + strRoleName + "]", UTF8);
        strEditForm = "ModifyForm?context=" + strContextEncoded;
        row    = formsPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveForm?context=" 
                              + strContextEncoded + "', '"
                              + resource.getString("workflowDesigner.confirmRemoveJS") + " " 
                              + Encode.javaStringToJsString( form.getName() + " " + strRoleName ) 
                              + " ?');" );
        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                 resource.getString("GML.modify"),
                                 strEditForm );
        
        
        row.addArrayCellLink( form.getName(), strEditForm );
        row.addArrayCellLink( strRoleName, strEditForm );
        row.addArrayCellLink( form.getHTMLFileName() == null ? "" : form.getHTMLFileName(),
                              strEditForm );
        row.addArrayCellIconPane(iconPane);
    }
}

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ViewForms"/>
<%
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.forms")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());
out.println( formsPane.print() );
out.println(board.printAfter());

%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="POST">
<designer:buttonPane cancelAction="Main" />
</form>
<%    

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
