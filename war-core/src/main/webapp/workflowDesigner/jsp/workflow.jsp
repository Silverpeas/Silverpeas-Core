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
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%
    String             strProcessFileName = (String) request.getAttribute( "ProcessFileName" ),
                       strComponentDescriptor = (String)request.getAttribute( "componentDescriptor" ),
                       strCurrentTab = "ModifyWorkflow";
    ArrayPane          processPane = gef.getArrayPane( "processPane", strCurrentTab, request, session );
    ProcessModel       processModel = (ProcessModel) request.getAttribute("ProcessModel");
    boolean            fExistingProcess = !( (Boolean)request.getAttribute( "IsANewProcess" ) ).booleanValue();
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script language="javascript">
	
	function sendData() 
	{
        // If saving a process model that is referenced in component descriptor
        // give a warning.
        //
		if (isCorrectlyFilled()
            && ( <%=Boolean.toString( strComponentDescriptor == null )%>  
                 || confirm( '<%=resource.getString("workflowDesigner.confirmProcessReferencedJS")%>' ) ) ) 
		{
			document.workflowHeaderForm.submit();
		}
	}

	function isCorrectlyFilled() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var title = stripInitialWhitespace(document.workflowHeaderForm.name.value);
        
     	if (title == "") 
     	{
           	errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           	errorNb++;
     	}
        if ( isWhitespace(document.workflowHeaderForm.ProcessFileName.value) ) 
        {
            errorMsg+="  - '<%=resource.getString("GML.path")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }
         
     	switch(errorNb) 
     	{
        	case 0 :
            	result = true;
            	break;
        	case 1 :
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error").toLowerCase()%> : \n" + errorMsg;
            	window.alert(errorMsg);
            	result = false;
            	break;
        	default :
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            	window.alert(errorMsg);
            	result = false;
            	break;
     	} 
     	return result;
	}

	/**
	 * If there is a component descriptor, confirm that you really want to overwrite it.
	 */
	function editComponentDescription()
	{
	    if ( <%=Boolean.toString( strComponentDescriptor != null )%> 
	         && confirm( '<%=resource.getString("workflowDesigner.confirmProcessReferencedJS")%>' ) )
	    	location.href = "EditComponentDescription";
	}
</script>

</HEAD>
<body>
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
    browseBar.setComponentName(resource.getString("workflowDesigner.workflowHeader"), strCurrentTab );
    
    processPane.setTitle( resource.getString("workflowDesigner.workflowHeader") );
    
    // add an operation:  Generate the Component descriptor xml file.
    // Only if the description has not yet been defined but the workflow has aleready been saved
    //
    if ( fExistingProcess )
        operationPane.addOperation( resource.getIcon("workflowDesigner.generate.componentDescriptor"),
                                    resource.getString("workflowDesigner.generate.componentDescriptor"),
                                    "javascript:editComponentDescription();" );
    
    addContextualDesignation( operationPane, resource, "labels", "workflowDesigner.add.label", strCurrentTab );
    addContextualDesignation( operationPane, resource, "descriptions", "workflowDesigner.add.description", strCurrentTab );
    
    row = processPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "name", processModel.getName() );
    cellInput.setSize( "50" );
    
    row = processPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.path") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "ProcessFileName", strProcessFileName );
    cellInput.setReadOnly( fExistingProcess );
    cellInput.setSize( "80" );
    
    out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ModifyWorkflow"/>
<%
    out.println(frame.printBefore());
    out.println(board.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.workflowHeader")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");
%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="POST">
<%
    out.println( processPane.print() );
    
    //Labels
    //
%>
<br>
<designer:contextualDesignationList
    designations="<%=processModel.getLabels()%>" 
    context="labels" 
    parentScreen="<%=strCurrentTab%>"
    columnLabelKey="GML.label"
    paneTitleKey="workflowDesigner.list.label"/>
<%
// Descriptions
//
%>
<br>
<designer:contextualDesignationList
    designations="<%=processModel.getDescriptions()%>" 
    context="descriptions" 
    parentScreen="<%=strCurrentTab%>"
    columnLabelKey="GML.description"
    paneTitleKey="workflowDesigner.list.description"/>

</form>
<%
    out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="Main" />
<%    
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>
