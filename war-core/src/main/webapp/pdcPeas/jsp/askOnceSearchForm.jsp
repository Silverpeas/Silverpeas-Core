<%@ page import="java.util.Locale"%>
<%@ page import="xtrim.data.Domain"%>
<%@ include file="checkAdvancedSearch.jsp"%>
<%
//récupération des données pour l'espace de recherche   
   Vector searchDomains = (Vector) request.getAttribute("searchDomains");
   String currentSearchDomainId = (String) request.getAttribute("currentSearchDomainId");
   currentSearchDomainId = (currentSearchDomainId==null) ? "SILVERPEAS" : currentSearchDomainId;

%>
<html>
<head>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<SCRIPT Language="Javascript">
	function calculateAction()
	{
		var index = document.queryForm.searchDomainId.selectedIndex;
		var value = document.queryForm.searchDomainId.options[index].value;
		if (value!="SILVERPEAS")
		{
			document.queryForm.action = "SpecificDomainView";
		}
		else
		{
			document.queryForm.action = "GlobalView";
		}
		document.queryForm.submit();
	}
</SCRIPT>
</head>

<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));

	out.println(window.printBefore());
//	out.flush();
//	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/searchDomainSelection.jsp").include(request, response);
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
				<FORM name="queryForm" action="askOnceResultsForm" method="POST">
				<tr>
					<!--<form name="searchDomainChoice" method="POST" action="..." onSubmit="calculateAction()">-->
		<TD class="txtlibform" nowrap><%=resource.getString("pdcPeas.searchDomain")%> :&nbsp;</td>

				<td align="left"> 
							<span class="selectNS"> 
							<select name="searchDomainId" onChange="calculateAction()">
							   <% for (int i=0; searchDomains!=null && i<searchDomains.size() ; i++) 
								  {
									  String[] domain = (String[]) searchDomains.get(i);%>
								   <option <%=currentSearchDomainId.equals(domain[2])?"selected":""%> 	value="<%=domain[2]%>"><%=domain[0]%></option>
								<% } %>
							</select>
							</span>
						</td>
					<!--</form>-->
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
	int i=0;
	for (i=0; domains!= null && i<domains.size(); i++)
	{
		Domain domain = (Domain) domains.get(i);
		if ((i%4)==0)
			out.println("<tr>");
%>
		<td><input type="checkbox" name="domains" value="<%=domain.getInternalName()%>"><%=domain.getDisplayName(new Locale("", ""))%></td>
<%
		if ((i%4)==3)
			out.println("</tr>");
	}
		if ((i%4)!=0)
			out.println("</tr>");
%>
	</table>
					</td>
                </tr>
				</FORM>
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
  Button validateButton = (Button) gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:document.queryForm.submit()", false);
  buttonPane.addButton(validateButton);
  buttonPane.setHorizontalPosition();
  out.println(buttonPane.print());

  out.println("</CENTER><br>");

  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>
