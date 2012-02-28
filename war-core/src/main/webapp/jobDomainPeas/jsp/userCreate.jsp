<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle"/>


<%
    Board board = gef.getBoard();

	Domain 		domObject 	= (Domain)request.getAttribute("domainObject");
    UserFull 	userObject 	= (UserFull)request.getAttribute("userObject");
    String 		action 		= (String)request.getAttribute("action");
    String 		groupsPath 	= (String)request.getAttribute("groupsPath");
    Boolean 	userRW 		= (Boolean) request.getAttribute("isUserRW");
    Integer 	minLengthLogin = (Integer) request.getAttribute("minLengthLogin");
    Integer     minLengthPwd = (Integer) request.getAttribute("minLengthPwd");
    Boolean		blanksAllowedInPwd = (Boolean) request.getAttribute("blanksAllowedInPwd");
    UserDetail	currentUser = (UserDetail) request.getAttribute("CurrentUser");
    List		groups		= (List) request.getAttribute("GroupsManagedByCurrentUser");
    
    boolean bUserRW = false;
    if (userRW != null) {
    	bUserRW = userRW.booleanValue();
    }
    
    String currentUserAccessLevel = currentUser.getAccessLevel();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath(groupsPath);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function SubmitWithVerif()
{
  var namefld = $("#userLastName");
  var errorMsg = "";
  
  if (namefld.length > 0 && isWhitespace(namefld.val())) {
    errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.lastName")+resource.getString("JDP.missingFieldEnd")%>\n";
  }
      
  <% if ("userCreate".equals(action)) { %>
  var loginfld = stripInitialWhitespace(document.userForm.userLogin.value);
  if (isWhitespace(loginfld)) {
    errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.missingFieldEnd")%>\n";
  } else if(loginfld.length < <%=minLengthLogin.intValue()%>) {
    errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.minLength")+" "+minLengthLogin.toString()+" "+resource.getString("JDP.caracteres")%>\n";
  }
  <% } %>
   
  <% if (userObject.isPasswordAvailable()) { %>
  if ($('#userPasswordValid:checked').val() == 'true') {	       
   	var passwordfld = stripInitialWhitespace($('#userPasswordId').val());
    var passwordFldAgain = stripInitialWhitespace($('#userPasswordAgainId').val());
    <% if ("userModify".equals(action)) { %>
    if (passwordfld != "" && passwordFldAgain != "") {
      if (<%=! blanksAllowedInPwd.booleanValue()%> && passwordfld.indexOf(" ") != -1) { 
        errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.password")+resource.getString("JDP.noSpaces")%>\n";
      }
    
      if(passwordfld.length < <%=minLengthPwd.intValue()%>) { 
        errorMsg += "- <fmt:message key="JDP.missingFieldStart"/><%=resource.getString("GML.password")+resource.getString("JDP.minLength")+" "+minLengthPwd.toString()+" "+resource.getString("JDP.caracteres")%>\n";
      }
  
      if (passwordfld != passwordFldAgain) {
        errorMsg += "- <fmt:message key="JDP.confirmPwdError"/> \n";
      }
    }
    
    <% } %>
    <% if ("userCreate".equals(action)) { %>
    
   	if (<%=! blanksAllowedInPwd.booleanValue()%> && passwordfld.indexOf(" ") != -1) { 
   	  errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.password")+resource.getString("JDP.noSpaces")%>\n";
   	}
  
    if(passwordfld.length < <%=minLengthPwd.intValue()%>) { 
      errorMsg += "- <fmt:message key="JDP.missingFieldStart"/><%=resource.getString("GML.password")+resource.getString("JDP.minLength")+" "+minLengthPwd.toString()+" "+resource.getString("JDP.caracteres")%>\n";
    }

    if (passwordfld != passwordFldAgain) {
      errorMsg += "- <fmt:message key="JDP.confirmPwdError"/> \n";
    }
    <% } %>
  }
  <% } %>
   
  if (errorMsg == "")
  {
  	<% if ("userCreate".equals(action) && groups != null && groups.size() > 0) { %>
  		var firstName = $("#userFirstName").attr("value");
  		var lastName = $("#userLastName").attr("value");
  		var email = $("#userEMail").attr("value");
    	$.post('<%=m_context%>/JobDomainPeasAJAXServlet', {FirstName:firstName,LastName:lastName,Email:email,Action:'CheckUser'},
  			function(data){
  				if (data == "ok")
  				{
  					document.userForm.submit();
  				}
  				else
  				{
  					alert("Création impossible...\nUn utilisateur de même nom, même prénom et même email existe déjà !");
  				}
  			}, 'text');
  <% } else { %>
      	document.userForm.submit();
      <% } %>
  }
  else
  {
      window.alert(errorMsg);
  }
}

function selectUnselect()
{
<% if (userObject.isPasswordAvailable()) { %>
  var bSelected;
  
  bSelected = document.userForm.userPasswordValid.checked;
  if (bSelected){
      document.userForm.userPassword.disabled = false;
      $("#sendEmailTRid").show();
  } else {
      document.userForm.userPassword.disabled = true;
      $("#sendEmailTRid").hide();
  }
<% } %>
}

</script>
</head>
<body onload="javascript:selectUnselect()">

<view:window>
<view:frame>
<view:board>
  <form name="userForm" action="<%=action%>" method="post">
    <input type="hidden" name="Iduser" value="<% if (userObject.getId() != null) out.print(userObject.getId()); %>"/>
    <table cellpadding="5" cellspacing="0" border="0" width="100%">
    	<tr>			
        	<td class="txtlibform" width="40%"><fmt:message key="GML.lastName"/> :</td>
            <td width="60%">
            	<% if (action.equals("userMS")) { %>
            		<%= userObject.getLastName()%>
            	<% } else { %>
            		<input type="text" name="userLastName" id="userLastName" size="50" maxlength="99" value="<%=EncodeHelper.javaStringToHtmlString(userObject.getLastName())%>" />&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/>
            	<% } %> 
            </td>
        </tr>
        <tr>			
        	<td class="txtlibform"><fmt:message key="GML.surname"/> :</td>
            <td>
            	<% if (action.equals("userMS")) { %>
	        		<%= userObject.getFirstName()%>
	        	<% } else { %>
	        		<input type="text" name="userFirstName" id="userFirstName" size="50" maxlength="99" value="<%=EncodeHelper.javaStringToHtmlString(userObject.getFirstName())%>" />
	        	<% } %> 
            </td>
        </tr>
        <tr>			
        	<td class="txtlibform"><fmt:message key="GML.login"/> :</td>
            <td>
            	<% if (action.equals("userCreate")) { %>
                	<input type="text" name="userLogin" size="50" maxlength="50" value="<%=EncodeHelper.javaStringToHtmlString(userObject.getLogin())%>"/>&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/>
                <% } else { %>
                	<%=EncodeHelper.javaStringToHtmlString(userObject.getLogin())%>
                <% } %>
            </td>
        </tr>
        <tr>			
            <td class="txtlibform"><fmt:message key="GML.eMail"/> :</td>
            <td>
            	<% if (action.equals("userMS")) { %>
	        		<%= userObject.geteMail()%>
	        	<% } else { %>
                	<input type="text" name="userEMail" id="userEMail" size="50" maxlength="99" value="<%=EncodeHelper.javaStringToHtmlString(userObject.geteMail())%>" />
                <% } %> 
            </td>
        </tr>
        <tr>
            <td class="txtlibform"><fmt:message key="JDP.userRights"/> :</td>
            <td>
            	<% if (StringUtil.isDefined(currentUserAccessLevel) && "A".equals(currentUserAccessLevel)) { %>
                	<input type="radio" name="userAccessLevel" value="A" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("A"))) out.print("checked"); %>/>&nbsp;<%=resource.getString("GML.administrateur") %><br/>
                	<input type="radio" name="userAccessLevel" value="K" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("K"))) out.print("checked"); %>/>&nbsp;<%=resource.getString("GML.kmmanager") %><br/>
                <% } %>
                <% if (StringUtil.isDefined(currentUserAccessLevel) && ("A".equals(currentUserAccessLevel) || "D".equals(currentUserAccessLevel))) { %>
                	<input type="radio" name="userAccessLevel" value="D" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("D"))) out.print("checked"); %>/>&nbsp;<%=resource.getString("GML.domainManager") %><br/>
                	<input type="radio" name="userAccessLevel" value="U" <% if ((userObject.getAccessLevel() == null) || (userObject.getAccessLevel().length() <= 0) || (userObject.getAccessLevel().equalsIgnoreCase("U"))) out.print("checked"); %>/>&nbsp;<%=resource.getString("GML.user") %><br/>
                	<input type="radio" name="userAccessLevel" value="G" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("G"))) out.print("checked"); %>/>&nbsp;<%=resource.getString("GML.guest") %>
                <% } else { %>
					<input type="hidden" name="userAccessLevel" value="U"/><fmt:message key="GML.user" />
                <% } %>
            </td>
        </tr>
        <% if (userObject.isPasswordAvailable()) { %>
            <tr>
                <td class="txtlibform"><fmt:message key="JDP.silverPassword"/> :</td>
                <td>
                    <input type="checkbox" name="userPasswordValid" id="userPasswordValid" value="true" <% if (userObject.isPasswordValid()) out.print("checked"); %> onclick="javascript:selectUnselect()"/>&nbsp;<fmt:message key="GML.yes" /><br/>
                </td>
            </tr>
            <tr>
                <td class="txtlibform"><fmt:message key="GML.password"/> :</td>
                <td>
                    <input type="password" name="userPassword" id="userPasswordId" size="50" maxlength="32" value=""/> 
                </td>
            </tr>
            <tr>
                <td class="txtlibform"><fmt:message key="GML.passwordAgain"/> :</td>
                <td>
                    <input type="password" name="userPasswordAgain" id="userPasswordAgainId" size="50" maxlength="32" value=""/> 
                </td>
            </tr>
            <tr id="sendEmailTRid" style="display:none;">
                <td class="txtlibform"><fmt:message key="JDP.sendEmail" /></td>
                <td>
                    <input type="checkbox" name="sendEmail" id="sendEmailId" value="true" />&nbsp;<fmt:message key="GML.yes" /> <br/>
                </td>
            </tr>        
 
        <% } %>
 
        <% 
        		String[] properties = userObject.getPropertiesNames();
				String property = null;
				for (int p=0; p<properties.length; p++) {
					property = (String) properties[p];
					if (!property.startsWith("password")) {
		%>
					<tr>
						<td class="txtlibform"><%=userObject.getSpecificLabel(resource.getLanguage(), property) %> :</td>
						<td>
						<% if (bUserRW || userObject.isPropertyUpdatableByAdmin(property)) { %>
							<% if("STRING".equals(userObject.getPropertyType(property)) || "USERID".equals(userObject.getPropertyType(property))) { %>
								<input type="text" name="prop_<%=property%>" size="50" maxlength="100" value="<%=EncodeHelper.javaStringToHtmlString(userObject.getValue(property))%>"/>
							<% } else if("BOOLEAN".equals(userObject.getPropertyType(property))) { %>
								<input type="radio" name="prop_<%=property%>" value="1" <% if (userObject.getValue(property) != null && "1".equals(userObject.getValue(property))) out.print("checked"); %>/><fmt:message key="GML.yes"/>
								<input type="radio" name="prop_<%=property%>" value="0" <% if (userObject.getValue(property) == null || "".equals(userObject.getValue(property)) || "0".equals(userObject.getValue(property))) out.print("checked"); %>/><fmt:message key="GML.no"/>
							<% } %>
						<% } else { %>
							<% if("STRING".equals(userObject.getPropertyType(property)) || "USERID".equals(userObject.getPropertyType(property))) { %>
								<%=EncodeHelper.javaStringToHtmlString(userObject.getValue(property))%>
							<% } else if("BOOLEAN".equals(userObject.getPropertyType(property))) { 
								if (StringUtil.getBooleanValue(userObject.getValue(property))) {
					 				out.print(resource.getString("GML.yes"));
					 			} else {
					 				out.print(resource.getString("GML.no")); 
					 			}
							 } %>
						<% } %>
						</td>							
					</tr>
		<%			}
				}
        %>
        
        <%
        	//in case of group manager, the added user must be set to one group
        	//if user manages only once group, user will be added to it
        	//else if he manages several groups, manager chooses one group 
        	if (groups != null && groups.size() > 0) {
		%>
				<tr>
                	<td class="txtlibform"><fmt:message key="GML.groupe"/> :</td>
                	<td valign="baseline">
                		<% if (groups.size() == 1) {
                		  	Group group = (Group) groups.get(0); 
                		%>
                			<%=group.getName() %> <input type="hidden" name="GroupId" id="GroupId" value="<%=group.getId()%>"/>
                		<% } else { %>
                			<select id="GroupId" name="GroupId">
                				<% for (int g=0; g<groups.size(); g++) {
                				  	Group group = (Group) groups.get(g); 
                				%>
                					<option value="<%=group.getId()%>"><%=group.getName()%></option>
                				<% } %>
                			</select>&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/>
                		<% } %>
                	</td>
            	</tr>
		<% } %>
        
		<tr> 
        	<td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>)</td>
        </tr>
    </table>
  </form>
</view:board>
<br/>
<%
  ButtonPane bouton = gef.getButtonPane();
  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif()", false));
  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
  out.println("<center>"+bouton.print()+"</center>");
%>
</view:frame>
</view:window>
</body>
</html>