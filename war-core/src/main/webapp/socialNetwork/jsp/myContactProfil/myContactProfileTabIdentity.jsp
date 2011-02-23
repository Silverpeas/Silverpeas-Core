<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.StringUtil"%>
    
<%
ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
%>

<!-- sousNav  --> 
<div class="sousNavBulle">
	<p><fmt:message key="profil.subnav.display" /> : <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a> <!-- <a href="#">Personnelles</a> <a href="#">Personnelles</a> --></p>
</div><!-- /sousNav  --> 

<div class="tab-content">
<table width="100%" cellspacing="0" cellpadding="5" border="0">
<%
	if (userFull != null) {
       	//  récupérer toutes les propriétés de ce User
        String[] properties = userFull.getPropertiesNames();

        String property = null;
        for (int p = 0; p < properties.length; p++) {
         property = properties[p];
         if (StringUtil.isDefined(userFull.getValue(property)) && resource.getSetting(property, true)) {
        %>
            <tr>
              <td class="txtlibform"><%= userFull.getSpecificLabel(resource.getLanguage(), property)%> :</td>
              <td >
                <%=userFull.getValue(property)%>
              </td>
            </tr>
		<%
            }
        }
	}
%>
</table>
</div>