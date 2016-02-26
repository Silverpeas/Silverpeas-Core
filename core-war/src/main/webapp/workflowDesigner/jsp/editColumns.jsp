<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%
Columns         columns = (Columns)request.getAttribute( "Columns" );

ArrayPane       columnPane = gef.getArrayPane( "columnList", "ModifyColumns?columns="+columns.getRoleName(), request, session),
                rolePane = gef.getArrayPane( "roleName", "ModifyColumns", request, session );
                columnPane.setVisibleLineNumber(wfdsc.getSettings().getInteger("NbElementsParPage",20));
String[]        astrFolderItemNames = (String[])request.getAttribute( "FolderItemNames" ),
                astrRoleNames = (String[])request.getAttribute( "RoleNames" ),
                astrRoleValues = (String[])astrRoleNames.clone();
String          strCancelAction = "ViewPresentation";

%>
<HTML>
<HEAD>
<view:looknfeel/>
<script type="text/javascript">

    function sendData()
    {
        if ( isCorrectlyFilled() )
        {
            document.columnsForm.submit();
        }
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;
        var fChecked = false;
        var i = 0;

        for ( i = 0; i < document.columnsForm.column.length; i++ )
            fChecked = fChecked || document.columnsForm.column[i].checked;

        var totalElementsChecked = <%=columns.getColumnList().size()%>;
        if ( document.columnsForm.column != null )
        {
          for ( i = 0 ; i < document.columnsForm.elements.length ; i++ ) {
            oElement = document.columnsForm.elements[i] ;
            if ( oElement.tagName.toLowerCase( ) == "input" ) {
              if ( oElement.type.toLowerCase( ) == "checkbox" )
                   fChecked = fChecked || oElement.checked;
            }
          }
          if (totalElementsChecked>i && !fChecked)
            fChecked = true;
        }


        if ( !fChecked )
        {
            errorMsg+="  - '<%=resource.getString("workflowDesigner.list.column")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
                errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors").toLowerCase()%> :\n" + errorMsg;
                window.alert(errorMsg);
                result = false;
                break;
        }
        return result;
    }
</script>
</HEAD>
<body>
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.presentationTab"), strCancelAction );
browseBar.setExtraInformation( resource.getString("workflowDesigner.editor.columns") );

rolePane.setTitle(resource.getString("workflowDesigner.role"));
columnPane.setTitle(resource.getString("workflowDesigner.list.column"));

//Fill the 'role' section
//
row = rolePane.addArrayLine();
cellText = row.addArrayCellText( resource.getString("workflowDesigner.role") );
cellText.setStyleSheet( "txtlibform" );
astrRoleNames[0] = resource.getString("workflowDesigner.default");
cellSelect = row.addArrayCellSelect( "role", astrRoleNames, astrRoleValues );
cellSelect.setSelectedValues( new String[] { columns.getRoleName() } );
cellSelect.setSize( "1" );

//Fill the 'columns' section
// Prepare a list of column names, based on the dataFolder
//
for ( int i = 0; i < astrFolderItemNames.length; i++ )
{
    row = columnPane.addArrayLine();
    row.addArrayCellCheckbox( "column",
                              astrFolderItemNames[i],
                              columns.getColumn( astrFolderItemNames[i] ) != null );
    row.addArrayCellText( astrFolderItemNames[i] );
}

out.println(window.printBefore());
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.columns")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());
%>
<form name="columnsForm" action="UpdateColumns" method="POST">
<!-- A hidden input with the original role name -->
<input name="role_original" value="<%=columns.getRoleName()%>" type="hidden"/>
<%
out.println( rolePane.print() );
out.println( columnPane.print() );
%>
</form>
<%
out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="<%=strCancelAction%>" />
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
