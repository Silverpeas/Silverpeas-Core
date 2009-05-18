<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkAttachment.jsp"%>

<%
	//initialisation des variables
	String id 			= request.getParameter("Id");
	String componentId 	= request.getParameter("ComponentId");
	String context 		= request.getParameter("Context");
	String fromAlias	= request.getParameter("Alias");
	
	ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");

	boolean displayUniversalLinks = URLManager.displayUniversalLinks();

	String attachmentPosition = "right";
	boolean showTitle = true;
	boolean showFileSize = true;
	boolean showDownloadEstimation = true;
	boolean showInfo = true;
	boolean showIcon = true;
	
	if (request.getParameter("AttachmentPosition") != null)
		attachmentPosition = request.getParameter("AttachmentPosition");
	if (request.getParameter("ShowTitle") != null)
		showTitle = (new Boolean(request.getParameter("ShowTitle"))).booleanValue();
	if (request.getParameter("ShowFileSize") != null)
		showFileSize = (new Boolean(request.getParameter("ShowFileSize"))).booleanValue();
	if (request.getParameter("ShowDownloadEstimation") != null)
		showDownloadEstimation = (new Boolean(request.getParameter("ShowDownloadEstimation"))).booleanValue();
	if (request.getParameter("ShowInfo") != null)
		showInfo = (new Boolean(request.getParameter("ShowInfo"))).booleanValue();
	if (request.getParameter("ShowIcon") != null)
		showIcon = (new Boolean(request.getParameter("ShowIcon"))).booleanValue();
	
	String contentLanguage	= request.getParameter("Language");
	if (!StringUtil.isDefined(contentLanguage))
		contentLanguage = null;
  
   	boolean spinfireViewerEnable = settings.getBoolean("SpinfireViewerEnable", false);

  	//récupération des fichiers attachés à un événement
  	//create foreignKey with componentId and customer id
  	//use AttachmentPK to build the foreign key of customer object.
	AttachmentPK foreignKey =  new AttachmentPK(id, componentId);

	Vector 		attachments 	= AttachmentController.searchAttachmentByPKAndContext(foreignKey, context);
  	Iterator 	itAttachments 	= attachments.iterator();

	if (itAttachments.hasNext())
  	{
		Board board	= gef.getBoard();
  		out.println(board.printBefore());
  		
  		int nbAttachmentPerLine = 3;
  		
  		if (attachmentPosition != null && "right".equals(attachmentPosition))
  		{
	  		out.println("<TABLE width=\"150\">");
	  		out.println("<TR><TD align=\"center\"><img src=\""+m_Context+"/util/icons/attachedFiles.gif\"></td></TR>");
	  	}
	  	else
	  	{
	  		out.println("<TABLE border=\"0\">");
	  		out.println("<TR><TD align=\"center\" colspan=\""+(2*nbAttachmentPerLine-1)+"\"><img src=\""+m_Context+"/util/icons/attachedFiles.gif\"></td></TR>");
	  	}      
		
		AttachmentDetail attachmentDetail = null;
		String 	author 	= "";
		String 	title	= "";
		String 	info	= "";
		String	url		= "";
		int a = 1; 
		while (itAttachments.hasNext()) {
			attachmentDetail = (AttachmentDetail) itAttachments.next();
			title	= attachmentDetail.getTitle(contentLanguage);
			if (!StringUtil.isDefined(title) || !showTitle)
				title = attachmentDetail.getLogicalName(contentLanguage);
			info = attachmentDetail.getInfo(contentLanguage);
			if (StringUtil.isDefined(attachmentDetail.getAuthor(contentLanguage)))
				author = "<BR/><i>"+attachmentDetail.getAuthor(contentLanguage)+"</i>";
			
			if ("bottom".equals(attachmentPosition) && a==1)
				out.println("<TR>");
			else if ("right".equals(attachmentPosition))
				out.println("<TR>");
				
		    out.println("<TD valign=\"top\">");
		    out.println("<NOBR>");
		    if (showIcon)
		    	out.println("<img src=\""+attachmentDetail.getAttachmentIcon(contentLanguage)+"\" width=\"20\" valign=\"absmiddle\">");
		    
		    url = attachmentDetail.getAttachmentURL(contentLanguage);
		    if ("1".equals(fromAlias))
		    	url = attachmentDetail.getAliasURL(contentLanguage);
		    			
		    out.println("<A href=\""+url+"\" target=_blank>"+title+"</A>");
		    out.println("</NOBR>");
		    
		    if (displayUniversalLinks)
			{
				String link = URLManager.getSimpleURL(URLManager.URL_FILE, attachmentDetail.getPK().getId());
				String linkIcon = m_Context+"/util/icons/link.gif";
				out.print(" <a href=\""+link+"\"><img src=\""+linkIcon+"\" border=\"0\" valign=\"absmiddle\" alt=\""+messages.getString("CopyLink")+"\" title=\""+messages.getString("CopyLink")+"\" target=_blank></a>");
			}
		    
		    out.println("<BR>");
		    		    
			if (showFileSize)
				out.println(attachmentDetail.getAttachmentFileSize(contentLanguage));
			if (showFileSize && showDownloadEstimation)
				out.println(" / ");
			if (showDownloadEstimation)
				out.println(attachmentDetail.getAttachmentDownloadEstimation(contentLanguage));
			if (showDownloadEstimation || showFileSize)
				out.println("<br>");
			title = attachmentDetail.getTitle(contentLanguage);
		    if (StringUtil.isDefined(title) && showTitle)
			    out.println(attachmentDetail.getLogicalName(contentLanguage)+"<BR>");
			if (StringUtil.isDefined(info) && showInfo)
				out.println("<i>"+Encode.javaStringToHtmlParagraphe(info)+"</i>");

			if (attachmentDetail.isSpinfireDocument(contentLanguage) && spinfireViewerEnable)
		    {
			    %>
			    
		    	<div id="switchView" name="switchView" style="display: none">
    				<a href="#" onClick="changeView3d(<%=attachmentDetail.getPK().getId()%>)"><img name="iconeView<%=attachmentDetail.getPK().getId()%>" valign="top" border="0" src="<%=URLManager.getApplicationURL()%>/util/icons/masque3D.gif"></a>
    			</div>
			    <div id="<%=attachmentDetail.getPK().getId()%>" style="display: none">
					<OBJECT classid="CLSID:A31CCCB0-46A8-11D3-A726-005004B35102"
					width="300" height="200" id="XV" >
					<PARAM NAME="ModelName" VALUE="<%=url%>">
					<PARAM NAME="BorderWidth" VALUE="1">
					<PARAM NAME="ReferenceFrame" VALUE="1">
					<PARAM NAME="ViewportActiveBorder" VALUE="FALSE">
					<PARAM NAME="DisplayMessages" VALUE="TRUE">
					<PARAM NAME="DisplayInfo" VALUE="TRUE">
					<PARAM NAME="SpinX" VALUE="0">
					<PARAM NAME="SpinY" VALUE="0">
					<PARAM NAME="SpinZ" VALUE="0">
					<PARAM NAME="AnimateTransitions" VALUE="0">
					<PARAM NAME="ZoomFit" VALUE="1">
					</OBJECT>
				</div>
				<br>
				<%
		    }
			out.println("</TD>");
			
			if ("bottom".equals(attachmentPosition) && a<nbAttachmentPerLine)
				out.println("<TD width=\"30\">&nbsp;</TD>");
			
			if ("bottom".equals(attachmentPosition) && a==nbAttachmentPerLine)
			{
				out.println("</TR>");
				if (itAttachments.hasNext())
					out.println("<TR><TD colspan=\""+(2*nbAttachmentPerLine-1)+"\">&nbsp;</TD></TR>");
			}
			else if ("right".equals(attachmentPosition))
			{
				out.println("</TR>");
				if (itAttachments.hasNext())
					out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			author = "";
			if (a==3)
				a = 1;
			else
				a++;
		}
    	out.println("</TABLE>");
    	out.println(board.printAfter());
   }
%>

<% if (spinfireViewerEnable) { %>
	<script language="javascript">
		if (navigator.appName=='Microsoft Internet Explorer')
		{
			for (i=0; i<document.getElementsByName("switchView").length; i++)
				document.getElementsByName("switchView")[i].style.display = '';
		}
		function changeView3d(objectId)
		{
			if (document.getElementById(objectId).style.display == 'none')
			{
				document.getElementById(objectId).style.display = '';
				eval("iconeView"+objectId).src = '<%=URLManager.getApplicationURL()%>/util/icons/visible3D.gif';
			}
			else
			{
				document.getElementById(objectId).style.display = 'none';
				eval("iconeView"+objectId).src = '<%=URLManager.getApplicationURL()%>/util/icons/masque3D.gif';
			}
		}
	</script>
<% } %>
