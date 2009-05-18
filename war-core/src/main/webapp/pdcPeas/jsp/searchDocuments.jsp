<%@ include file="checkAdvancedSearch.jsp"%>

<%!
void displayItemsListHeader(int nbPubs, Value value, Pagination pagination, ResourcesWrapper resource, JspWriter out) throws IOException {
	String valueName = null;
	if (value != null)
		valueName = value.getName(resource.getLanguage());
	out.println("<tr valign=\"middle\" class=intfdcolor>");
	out.println("<td align=\"center\" class=\"ArrayNavigation\" colspan=\"3\">");
	out.println("<img align=\"absmiddle\" src=\""+resource.getIcon("pdcPeas.1px")+"\" height=\"20\">");
	out.println(pagination.printCounter());
	if (nbPubs > 1)
		out.println(resource.getString("pdcPeas.Documents") );
	else
		out.println(resource.getString("pdcPeas.Document") );
	if (value != null)
		out.println(resource.getString("pdcPeas.In")+"\""+valueName+"\"");
	out.println("</td>");
	out.println("</tr>");
	out.println("<tr class=intfdcolor4><td colspan=\"2\">&nbsp;</td></tr>");
}
%>

<%
    List	resultList			= (List) request.getAttribute("ResultList");

	int		firstItemIndex		= ((Integer) request.getAttribute("FirstItemIndex")).intValue();
	int		nbItemsPerPage		= ((Integer) request.getAttribute("NbItemsPerPage")).intValue();

	Value	selectedValue		= (Value) request.getAttribute("SelectedValue");

    String	name				= "";
	String	description			= "";
    String	url					= "";
	String  creationDate		= "";
	String	creatorFirstName	= "";
	String	creatorLastName		= "";
	String	creatorName			= "";
	String	componentId			= "";

	Board	board				= gef.getBoard();

	Pagination pagination = gef.getPagination(resultList.size(), nbItemsPerPage, firstItemIndex);
	pagination.setAltPreviousPage(resource.getString("multipage.PreviousPage"));
	pagination.setAltNextPage(resource.getString("multipage.NextPage"));
	pagination.setActionSuffix("PDC");
	List alSilverContents = resultList.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());
%>
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="javascript">
function submitContent(cUrl, componentId) {
	document.searchDocForm.contentURL.value		= cUrl;
	document.searchDocForm.componentId.value	= componentId;
	document.searchDocForm.submit();
}
</script>
</head>
<body>
<form name="searchDocForm" action="GlobalContentForward" method="POST">
	<input type="hidden" name="contentURL" value="">
	<input type="hidden" name="componentId" value="">
</form>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());

    out.println("<table border=0 cellspacing=0 cellpadding=0 width=\"100%\">");
	displayItemsListHeader(alSilverContents.size(), selectedValue, pagination, resource, out);
	GlobalSilverContent gsc = null;
	String thumbnailURL = null;
	String thumbnailWidth = null;
	String thumbNailHeight = null;
	for (int i=0; i < alSilverContents.size(); i++) {
		gsc					= (GlobalSilverContent) alSilverContents.get(i);
		name				= gsc.getName();
		description			= gsc.getDescription();
		url					= gsc.getURL();
		creatorFirstName	= gsc.getCreatorFirstName();
		creatorLastName		= gsc.getCreatorLastName();
		componentId			= gsc.getInstanceId();
		try
		{
			creationDate	= resource.getOutputDate(gsc.getDate());
		}
		catch (Exception e)
		{
			creationDate	= "Unknown";
		}

		creatorName = "Unknown";
		if (creatorFirstName != null)
			creatorName = creatorFirstName+" "+creatorLastName;		

		out.print("<tr>");
		out.println("<td width=\"3%\" valign=\"top\"><img src=\""+m_context +"/util/icons/component/"+((GlobalSilverContent)alSilverContents.get(i)).getIconUrl()+"\" onClick=\"javascript:;\"></td>");
		out.println("<td>");
		out.println("<table cellspacing=\"0\" cellpadding=\"0\"><tr>");
		
		String link = "javascript:submitContent('"+url+"','"+componentId+"');";
		
		if (gsc.getThumbnailURL() != null && gsc.getThumbnailURL().length()>0)
			out.println("<td><a href=\""+link+"\"><img src=\""+gsc.getThumbnailURL()+"\" border=\"0\" width=\""+gsc.getThumbnailWidth()+"\" height=\""+gsc.getThumbnailHeight()+"\"></a>&nbsp;</td>");
		
		out.println("<td valign=\"top\"><a href=\""+link+"\"><span class=textePetitBold>"+Encode.javaStringToHtmlString(name)+"</span></a>");//</td></tr>");
		
		if (!creationDate.equals("Unknown") || !creatorName.equals("Unknown")) {
			out.println("<br/>"+creatorName+" - "+creationDate);
		}
		
		if (description != null && description.length() > 0) {
			
			// LBN : patch for whitepages
			if ( gsc.getURL().startsWith("consultIdentity?userCardId") ) 
			{
				out.println("<br/>"+description);
			}
			else {
				out.println("<br/>"+Encode.javaStringToHtmlParagraphe(description));
			}
		}
		out.print("</td>");
		out.println("</tr></table>");
		out.print("</td>");
		out.println("</tr>");
		
		out.print("<tr>");
		out.println("<td width=\"3%\">&nbsp;</td>");
		out.println("<td>&nbsp;</td>");
		out.println("</tr>");
     }
	 if (resultList.size() > nbItemsPerPage)
	 {
		out.println("<tr class=intfdcolor4><td colspan=\"2\">&nbsp;</td></tr>");
		out.println("<tr valign=\"middle\" class=intfdcolor>");
		out.println("<td colspan=\"5\" align=\"center\">");
		out.println(pagination.printIndex());
		out.println("</td>");
		out.println("</tr>");
	 }
	  
	 out.println("</table>");
    
	 out.println(board.printAfter());
     out.println(frame.printAfter());
     out.println(window.printAfter());
%>
</body>
</html>