<%--

    Copyright (C) 2000 - 2012 Silverpeas

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


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="java.util.Locale"%>
<%@ page import="xtrim.data.Domain"%>
<%@ include file="checkAdvancedSearch.jsp"%>
<%
//recuperation des donnees pour l'espace de recherche   
   Vector searchDomains = (Vector) request.getAttribute("searchDomains");
   String currentSearchDomainId = (String) request.getAttribute("currentSearchDomainId");
   currentSearchDomainId = (currentSearchDomainId==null) ? "SILVERPEAS" : currentSearchDomainId;

%>
<html>
<head>
  <title><%=resource.getString("GML.popupTitle")%></title>
  <view:looknfeel/>
  <script type="text/javascript" src='<c:url value="/util/javaScript/animation.js" />'></script>
  <script Language="Javascript">
    function calculateAction() {
      var index = document.queryForm.searchDomainId.selectedIndex;
      var value = document.queryForm.searchDomainId.options[index].value;
      if (value!="SILVERPEAS") {
        document.queryForm.action = "SpecificDomainView";
      } else {
        document.queryForm.action = "GlobalView";
      }
      document.queryForm.submit();
    }
  </script>
</head>

<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));
	out.println(window.printBefore());
	out.println(frame.printBefore());
	// Retrieve domains available in Ask'Once
	Vector domains = (Vector) request.getAttribute("domains");
%>
<center>


<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<tr> 
	<td> 
		<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
		<tr> 
			<td align="center"><!--TABLE SAISIE-->
				<table border="0" cellspacing="0" cellpadding="5" class="intfdcolor4" width="100%">
				<form name="queryForm" action="askOnceResultsForm" method="POST">
				<tr>
          <td class="txtlibform" nowrap><%=resource.getString("pdcPeas.searchDomain")%> :&nbsp;</td>
          <td align="left"> 
            <span class="selectNS"> 
							<select name="searchDomainId" onChange="calculateAction()">
							   <% for (int i=0; searchDomains!=null && i<searchDomains.size() ; i++)  {
									  String[] domain = (String[]) searchDomains.get(i);%>
								   <option <%=currentSearchDomainId.equals(domain[2])?"selected":""%> 	value="<%=domain[2]%>"><%=domain[0]%></option>
								<% } %>
							</select>
            </span>
          </td>
				</tr>
        <tr>
					<td valign="top" nowrap align="left">
						<span class="txtlibform"><%=resource.getString("pdcPeas.SearchFind")%></span>
					</td>
					<td align="left"> 
						<input type="text" name="query" size="36">
					</td>
        </tr>
        <tr>
					<td valign="top" nowrap align="left">
						<span class="txtlibform"><%=resource.getString("pdcPeas.askOnce.subDomainSelect")%></span> 
					</td>
					<td align="left"> 
            <table cellpadding="5">
              <%
                int i = 0;
                for (i = 0; domains!= null && i<domains.size(); i++) {
                  Domain domain = (Domain) domains.get(i);
                  if ((i%4)==0) {
                    out.println("<tr>");
                  }
              %>
                  <td><input type="checkbox" name="domains" value="<%=domain.getInternalName()%>"><%=domain.getDisplayName(new Locale("", ""))%></td>
              <%
                  if ((i%4)==3) {
                    out.println("</tr>");
                  }
                }
                if ((i%4)!=0) {
                  out.println("</tr>");
                }
              %>
            </table>
          </td>
        </tr>
        </form>
      </table>
			</td>
		</tr>
		</table>	
	</td>
</tr>
</table>                
</center>
<%
  out.println(frame.printMiddle());
  out.println("<br><CENTER>");

  ButtonPane buttonPane = gef.getButtonPane();
  Button validateButton = gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:document.queryForm.submit()", false);
  buttonPane.addButton(validateButton);
  buttonPane.setHorizontalPosition();
  out.println(buttonPane.print());

  out.println("</CENTER><br>");

  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>