<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp" %>
<%
  String keys 		= "";
  String values 		= "";
  String cols 		= "";
  String defaultValue = "";

  if (field != null)
  {
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }

    if (parameters.containsKey("values")) {
      values = parameters.get("values");
    }

    if (parameters.containsKey("cols")) {
      cols = parameters.get("cols");
    }

    if (parameters.containsKey("default")) {
      defaultValue = parameters.get("default");
    }
  }
%>
<script type="application/javascript">
  function isCorrectForm() {
    checkFieldName();

    let keys = "";
    let values = "";

    // Add all available options.
    for ( var i = 0 ; i < oListText.options.length ; i++ )
    {
      let sText	= oListText.options[i].value ;
      let sValue	= oListValue.options[i].value ;
      if ( sValue.length == 0 ) sValue = sText ;

      keys += "##"+sValue;
      values += "##"+sText;
    }

    document.getElementById("Param_keys").value = keys.substring(2);
    document.getElementById("Param_values").value = values.substring(2);

    return checkErrors();
  }

  function Select( combo )
  {
    let iIndex = combo.selectedIndex ;

    oListText.selectedIndex		= iIndex ;
    oListValue.selectedIndex	= iIndex ;

    let oTxtText	= document.getElementById( "txtText" ) ;
    let oTxtValue	= document.getElementById( "txtValue" ) ;

    oTxtText.value	= oListText.value ;
    oTxtValue.value	= oListValue.value ;
  }

  function Add()
  {
    let oTxtText	= document.getElementById( "txtText" ) ;
    let oTxtValue	= document.getElementById( "txtValue" ) ;

    AddComboOption( oListText, oTxtText.value, oTxtText.value ) ;
    AddComboOption( oListValue, oTxtValue.value, oTxtValue.value ) ;

    oListText.selectedIndex = oListText.options.length - 1 ;
    oListValue.selectedIndex = oListValue.options.length - 1 ;

    oTxtText.value	= '' ;
    oTxtValue.value	= '' ;

    oTxtText.focus() ;
  }

  function Modify()
  {
    let iIndex = oListText.selectedIndex ;

    if ( iIndex < 0 ) return ;

    let oTxtText	= document.getElementById( "txtText" ) ;
    let oTxtValue	= document.getElementById( "txtValue" ) ;

    oListText.options[ iIndex ].innerHTML	= oTxtText.value ;
    oListText.options[ iIndex ].value		= oTxtText.value ;

    oListValue.options[ iIndex ].innerHTML	= oTxtValue.value ;
    oListValue.options[ iIndex ].value		= oTxtValue.value ;

    oTxtText.value	= '' ;
    oTxtValue.value	= '' ;

    oTxtText.focus() ;
  }

  function Move( steps )
  {
    ChangeOptionPosition( oListText, steps ) ;
    ChangeOptionPosition( oListValue, steps ) ;
  }

  function Delete()
  {
    RemoveSelectedOptions( oListText ) ;
    RemoveSelectedOptions( oListValue ) ;
  }

  function SetSelectedValue()
  {
    let iIndex = oListValue.selectedIndex ;
    if ( iIndex < 0 ) return ;

    let oTxtValue = document.getElementById("Param_default");

    oTxtValue.value = oListValue.options[ iIndex ].value ;
  }

  //	 Moves the selected option by a number of steps (also negative)
  function ChangeOptionPosition( combo, steps )
  {
    let iActualIndex = combo.selectedIndex ;

    if ( iActualIndex < 0 )
      return ;

    let iFinalIndex = iActualIndex + steps ;

    if ( iFinalIndex < 0 )
      iFinalIndex = 0 ;

    if ( iFinalIndex > ( combo.options.lenght - 1 ) )
      iFinalIndex = combo.options.lenght - 1 ;

    if ( iActualIndex == iFinalIndex )
      return ;

    let oOption = combo.options[ iActualIndex ] ;
    let sText	= oOption.innerHTML ;
    let sValue	= oOption.value ;

    combo.remove( iActualIndex ) ;

    oOption = AddComboOption( combo, sText, sValue, null, iFinalIndex ) ;

    oOption.selected = true ;
  }

  //	 Remove all selected options from a SELECT object
  function RemoveSelectedOptions(combo)
  {
    // Save the selected index
    let iSelectedIndex = combo.selectedIndex ;

    let oOptions = combo.options ;

    // Remove all selected options
    for ( var i = oOptions.length - 1 ; i >= 0 ; i-- )
    {
      if (oOptions[i].selected) combo.remove(i) ;
    }

    // Reset the selection based on the original selected index
    if ( combo.options.length > 0 )
    {
      if ( iSelectedIndex >= combo.options.length ) iSelectedIndex = combo.options.length - 1 ;
      combo.selectedIndex = iSelectedIndex ;
    }
  }

  //	 Add a new option to a SELECT object (combo or list)
  function AddComboOption( combo, optionText, optionValue, documentObject, index )
  {
    let oOption ;

    if ( documentObject )
      oOption = documentObject.createElement("OPTION") ;
    else
      oOption = document.createElement("OPTION") ;

    if ( index != null )
      combo.options.add( oOption, index ) ;
    else
      combo.options.add( oOption ) ;

    oOption.innerHTML = optionText.length > 0 ? optionText : '&nbsp;' ;
    oOption.value     = optionValue ;

    return oOption ;
  }

  //Gets a element by its Id. Used for shorter coding.
  function GetE( elementId )
  {
    return document.getElementById(elementId);
  }

  let oListText ;
  let oListValue ;

  $(document).ready(function(){
    oListText	= document.getElementById( 'cmbText' ) ;
    oListValue	= document.getElementById( 'cmbValue' ) ;

    <% if (keys.length() > 0 || values.length() > 0) { %>
    let keys = "<%=keys%>";
    let values = "<%=values%>";

    let tKeys=keys.split('##');
    let tValues=values.split('##');

    // Load the actual options
    for (var i=0; i<tKeys.length; i++) {
      AddComboOption( oListText, tValues[i], tValues[i]);
      AddComboOption( oListValue, tKeys[i], tKeys[i]);
    }
    <% } %>
  });
</script>
<%@ include file="includeTopField.jsp" %>
<% if (!displayer.equals("listbox")) { %>
<tr>
  <td class="txtlibform"><%=resource.getString("templateDesigner.cols")%> :</td><td><input type="text" name="Param_cols" value="<%=cols%>" size="5" maxLength="3"/></td>
</tr>
<% } %>
<% if (displayer.equals("radio") || displayer.equals("checkbox") || displayer.equals("listbox")) { %>
<tr>
  <td class="txtlibform"><%=resource.getString("templateDesigner.default")%> :</td><td><input type="text" id="Param_default" name="Param_default" value="<%=defaultValue%>"/></td>
</tr>
<% } %>
<input type="hidden" id="Param_keys" name="Param_keys" value=""/><input type="hidden" id="Param_values" name="Param_values" value=""/>
<tr>
  <td colspan="2">

    <table>
      <tr>
        <td class="txtlibform"><%=resource.getString("templateDesigner.optionText")%><br>
          <input id="txtText" style="WIDTH: 100%" type="text" name="txtText" onKeyPress="if (event.keyCode==13) Add();">
        </td>
        <td class="txtlibform"><%=resource.getString("templateDesigner.optionValue")%><br>
          <input id="txtValue" style="WIDTH: 100%" type="text" name="txtValue" onKeyPress="if (event.keyCode==13) Add();">
        </td>
        <td><a href="javascript:Add();"><img src="<%=resource.getIcon("templateDesigner.smallAdd")%>" alt="<%=resource.getString("templateDesigner.addOption")%>" title="<%=resource.getString("templateDesigner.addOption")%>"/></a></td>
        <td><a href="javascript:Modify();"><img src="<%=resource.getIcon("templateDesigner.smallUpdate")%>" alt="<%=resource.getString("templateDesigner.updateOption")%>" title="<%=resource.getString("templateDesigner.updateOption")%>"/></a></td>
        <td><a href="javascript:Delete();"><img src="<%=resource.getIcon("templateDesigner.smallDelete")%>" alt="<%=resource.getString("templateDesigner.deleteOption")%>" title="<%=resource.getString("templateDesigner.deleteOption")%>"/></a></td>
        <% if (displayer.equals("radio")) { %>
        <td><a href="javascript:SetSelectedValue();"><img src="<%=resource.getIcon("templateDesigner.default")%>" alt="<%=resource.getString("templateDesigner.defaultOption")%>" title="<%=resource.getString("templateDesigner.defaultOption")%>"/></a></td>
        <% } %>
      </tr>
      <tr>
        <td rowSpan="2"><select id="cmbText" style="WIDTH: 100%" onchange="GetE('cmbValue').selectedIndex = this.selectedIndex;Select(this);"
                                size="5" name="cmbText"></select>
        </td>
        <td rowSpan="2"><select id="cmbValue" style="WIDTH: 100%" onchange="GetE('cmbText').selectedIndex = this.selectedIndex;Select(this);"
                                size="5" name="cmbValue"></select>
        </td>
        <td colSpan="4">
        </td>
      </tr>
      <tr>
        <td colSpan="2"><a href="javascript:Move(-1);"><img src="<%=resource.getIcon("templateDesigner.arrowUp")%>" border="0"/></a>
          <br>
          <a href="javascript:Move(1);"><img src="<%=resource.getIcon("templateDesigner.arrowDown")%>" border="0"/></a>
        </td>
      </tr>
    </table>

  </td>
</tr>
<%@ include file="includeBottomField.jsp" %>