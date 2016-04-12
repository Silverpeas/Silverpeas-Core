<%@ page import="java.util.Map" %>
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
    Input           input = (Input)request.getAttribute("Input");
    String          strCancelAction = (String)request.getAttribute("parentScreen"),
                    strContext = (String)request.getAttribute("context"),
                    strCurrentScreen = "ModifyInput?context=" + URLEncoder.encode( strContext, UTF8 ),
                    strLabelContext = strContext + "/labels";
    ArrayPane       inputPane = gef.getArrayPane( "inputPane", strCurrentScreen, request, session );
    boolean         fExistingInput = ( (Boolean)request.getAttribute( "IsExisitingInput" ) ).booleanValue();
    List<Item>      folderItems = (List<Item>) request.getAttribute("FolderItems");
    List<String>    folderItemNames = new ArrayList<>();
    folderItemNames.add(resource.getString("GML.none"));
    for (Item folderItem : folderItems) {
      folderItemNames.add(folderItem.getName());
    }
    Map<String, List<String>> typesAndDisplayers = (Map<String, List<String>>) request.getAttribute("TypesAndDisplayers");
%>
<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script language="javaScript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
               document.inputForm.submit();
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        if ( document.inputForm.item.options.selectedIndex == 0
             && isWhitespace(document.inputForm.value.value) )
        {
            errorMsg+="  - '<%=resource.getString("workflowDesigner.folderItem")%>'"
                      + " <%=resource.getString("workflowDesigner.or")%>"
                      + " '<%=resource.getString("workflowDesigner.value")%>'"
                      + " <%=resource.getString("GML.MustBeFilled")%>\n";
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

    var items = [];
    <% for (Item item : folderItems) {%>
    items["<%=item.getName()%>"] = "<%=item.getType()%>";
    <% } %>

    var displayers = [];
    <% for (String type : typesAndDisplayers.keySet()) {
       String displayers = "";
       for (String displayer : typesAndDisplayers.get(type)) {
        if (!displayers.isEmpty()) {
          displayers += ",";
        }
        displayers += "\""+displayer+"\"";
       }
       %>
      displayers["<%=type%>"] = [<%=displayers%>];
    <% } %>

    function changeDisplayersList() {
      var itemName = $("select[name='item']").val();
      var itemType = items[itemName];
      var itemDisplayers = displayers[itemType];

      var displayerSelect = $("select[name='displayerName']");
      displayerSelect.empty();
      var options = '';
      for (var j = 0; j < itemDisplayers.length; j++){
        options += "<option value='" +itemDisplayers[j]+ "'>" +itemDisplayers[j]+ "</option>";
      }
      displayerSelect.html(options);
    }

    $(function() {

      // change displayers list according to selected type
      $("select[name='item']").change(function() {
        changeDisplayersList();
      });

      // load displayers list for current input
      changeDisplayersList();
      <% if (fExistingInput) { %>
        $("select[name='displayerName']").val("<%=input.getDisplayerName()%>");
      <% } %>

    });

</script>
</HEAD>
<BODY>
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
    browseBar.setComponentName(resource.getString("workflowDesigner.editor.form"), strCancelAction );
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.input"));

    inputPane.setTitle(resource.getString("workflowDesigner.input"));

    if ( fExistingInput )
        addContextualDesignation( operationPane, resource, strLabelContext, "workflowDesigner.add.label", strCurrentScreen );

    // Input
    //
    row = inputPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.folderItem") );
    cellText.setStyleSheet( "txtlibform" );
    cellSelect = row.addArrayCellSelect("item", folderItemNames);
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { input.getItem() == null ? "" : input.getItem().getName() } );

    // Value
    //
    row = inputPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.value") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "value", EncodeHelper.javaStringToHtmlString( input.getValue() ) );
    cellInput.setSize( "80" );

    // Displayer
    //
    row = inputPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.displayerName") );
    cellText.setStyleSheet( "txtlibform" );
    //astrTypeNames[0] = resource.getString( "GML.none" );
    cellSelect = row.addArrayCellSelect( "displayerName", new ArrayList<>());
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { input.getDisplayerName() == null ? "" : input.getDisplayerName() } );

    // Mandatory
    //
    row = inputPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.requiredField") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellCheckbox( "mandatory", "true", input.isMandatory() );

    // Readonly
    //
    row = inputPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.readonly") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellCheckbox( "readonly", "true", input.isReadonly() );

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.input")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="inputForm" METHOD="POST" ACTION="UpdateInput">
    <input type="hidden" name="context" value="<%=EncodeHelper.javaStringToHtmlString(strContext)%>" />
<%
    out.println( inputPane.print() );
%>
</FORM>
<%
	out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="<%=strCancelAction%>" />
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>