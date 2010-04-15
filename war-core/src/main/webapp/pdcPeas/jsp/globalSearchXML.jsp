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

<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@ page import="com.silverpeas.form.DataRecord"%>
<%@ page import="com.silverpeas.form.PagesContext"%>
<%@ page import="com.silverpeas.form.Form"%>

<%@ include file="checkAdvancedSearch.jsp"%>
<%
List 				xmlForms 	= (List) request.getAttribute("XMLForms");
PublicationTemplate template 	= (PublicationTemplate) request.getAttribute("Template");
DataRecord			emptyData	= (DataRecord) request.getAttribute("Data");
PagesContext		context		= (PagesContext) request.getAttribute("context"); 
List				webTabs		= (List) request.getAttribute("WebTabs");

Form form = null;
String selectedTemplate = null;
if (template != null) 
{
	selectedTemplate 	= template.getFileName();
	form 				= template.getSearchForm();
}

List			allComponents		= (List) request.getAttribute("ComponentList");
List			allSpaces			= (List) request.getAttribute("SpaceList");
QueryParameters query				= (QueryParameters) request.getAttribute("QueryParameters");
String			spaceSelected		= null;
String			componentSelected	= null;
String			title				= ""; 
if (query != null)
{
	spaceSelected		= query.getSpaceId();
	componentSelected	= query.getInstanceId();
	title				= query.getXmlTitle();
	if (title == null || "null".equals(title))
		title = "";
}
%>
<html>
<HEAD>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<script language="JavaScript">
function sendXMLRequest()
{
	if(document.XMLSearchForm != null) {
		document.XMLSearchForm.submit();
	} else {
		alert("<%=resource.getString("pdcPeas.choiceForm")%>");
	}
}
function chooseTemplate()
{
	var valuePath = document.XMLTemplatesForm.xmlSearchSelectedForm.value;
	if (valuePath.length > 0)
	{
		document.XMLTemplatesForm.submit();
	}
}
function viewXmlSearch(){
	document.XMLRestrictForm.submit();
}
</script>
</HEAD>
<BODY class="yui-skin-sam">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));

	Board board = gef.getBoard();
	ButtonPane buttonPane = gef.getButtonPane();
	
	out.println(window.printBefore());

	tabs = gef.getTabbedPane();
	tabs.addTab(resource.getString("pdcPeas.SearchResult"), "LastResults", false);
	if (webTabs != null)
	{
		for (int i=0; i<webTabs.size(); i++)
		{
			GoogleTab webTab = (GoogleTab) webTabs.get(i);
			tabs.addTab(webTab.getLabel(), "ViewWebTab?Id="+i, false);
		}
	}
	tabs.addTab(resource.getString("pdcPeas.SearchSimple"), "ChangeSearchTypeToAdvanced", false);
	tabs.addTab(resource.getString("pdcPeas.SearchAdvanced"), "ChangeSearchTypeToExpert", false);
	tabs.addTab(resource.getString("pdcPeas.SearchXml"), "#", true);	

	out.println(tabs.print());
	out.println(frame.printBefore());
	out.println("<center>");
	
	out.println(board.printBefore());
	%>
    <table border="0" cellspacing="0" cellpadding="5" width="100%">
    <form name="XMLTemplatesForm" action="XMLSearchViewTemplate" method="post">
	    <tr align="center">
		    <td class="txtlibform" nowrap align="left" width="200"><%=resource.getString("pdcPeas.Template")%> :</td>
		    <td align="left">
		    	<select name="xmlSearchSelectedForm" size="1" onChange="chooseTemplate();return;">
		    	<option value=""><%=resource.getString("GML.select")%></option>
		    	<%
		    	String selected = "";
		    	PublicationTemplate oneTemplate = null;
				for (int i=0;i<xmlForms.size();i++) {
					selected	= "";
					oneTemplate	= (PublicationTemplate) xmlForms.get(i);

					if (oneTemplate.getFileName().equals(selectedTemplate))
						selected = " selected";

					out.println("<option value=\""+oneTemplate.getFileName()+"\""+selected+">"+oneTemplate.getName()+"</option>");
				}
		    	%>
		    	</select>
		    </td>
	    </tr>
	</form>
	</table>
	<%
		out.println(board.printAfter());
		out.println("<br>");
		out.println(board.printBefore());
	%>
        <table border="0" cellspacing="0" cellpadding="5" width="100%">
        <form name="XMLRestrictForm" action="XMLRestrictSearch" method="post">
        <tr align="center">
          <td valign="top" nowrap align="left" class="txtlibform" width="200"><%=resource.getString("pdcPeas.DomainSelect")%></td>
          <td align="left" class="selectNS"><select name="spaces" size=1 onChange="javascript:viewXmlSearch()">
            <%
				out.println("<option value=\"\">"+resource.getString("pdcPeas.AllAuthors")+"</option>");
				String			incr	= "";
				SpaceInstLight 	space	= null;
				for (int i=0;i<allSpaces.size();i++) {
						selected	= "";
						incr		= "";
						space		= (SpaceInstLight) allSpaces.get(i);
						if (space.getLevel() == 1)
							incr = "&nbsp;&nbsp;";

						if (space.getFullId().equals(spaceSelected))
							selected = " selected";

						out.println("<option value=\""+space.getFullId()+"\""+selected+">"+incr+space.getName(language)+"</option>");
				}
             %>
             </select></td>
	    </tr>
		<tr align="center">
			<td valign="top" nowrap align="left" class="txtlibform"><%=resource.getString("pdcPeas.ComponentSelect")%></td>
			<td align="left" class="selectNS">
			<select name="componentSearch" size=1 onChange="javascript:viewXmlSearch()">
			<option value=""><%=resource.getString("pdcPeas.AllAuthors")%></option>
			<%
				ComponentInstLight component = null;
				for(int nI = 0; allComponents!=null && nI < allComponents.size(); nI++) {
						selected	= "";
						component	= (ComponentInstLight) allComponents.get(nI);
						if (component.getId().equals(componentSelected)){
							selected = " selected";
						}
						out.println("<option value=\""+component.getId()+"\""+selected+">"+component.getLabel(language)+"</option>");
				}
			%>
			</select>
			</td>
		</tr>
		</form>
        </table>
		<%
		out.println(board.printAfter());
		out.println("<br>");
		%>
	<% if (form != null) {
		out.println(board.printBefore());
		out.println("<FORM NAME=\"XMLSearchForm\" METHOD=\"POST\" ACTION=\"XMLSearch\" ENCTYPE=\"multipart/form-data\">");
		out.println("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\"><tr>");
		out.println("<td width=\"200\" class=\"txtlibform\">Titre :</td>");
		out.println("<td><input type=\"text\" name=\"TitleNotInXMLForm\" size=\"36\" value=\""+title+"\"/></td>");
		out.println("</tr></table>");
		form.display(out, context, emptyData);
		out.println("</FORM>");
		out.println(board.printAfter());
		out.println("<br>");
	} %>   
<%
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:sendXMLRequest();", false));
	out.println(buttonPane.print());
	out.println("</center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>