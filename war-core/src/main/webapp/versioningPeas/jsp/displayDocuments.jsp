<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="java.util.Vector"%>
<%@ page import="com.stratelia.silverpeas.versioning.util.VersioningUtil"%>

<%@ include file="checkVersion.jsp"%>

<%@page import="com.stratelia.webactiv.util.DateUtil"%>
<script src="<%=m_context %>/attachment/jsp/jquery-1.3.2.min.js" type="text/javascript"></script>
<script src="<%=m_context%>/util/javaScript/jquery/ui.core.js" type="text/javascript"></script>
<script src="<%=m_context%>/util/javaScript/jquery/ui.sortable.js" type="text/javascript"></script>
<script src="<%=m_context %>/attachment/jsp/jquery.qtip-1.0.0-rc3.min.js" type="text/javascript"></script>

<script type="text/javascript" src="<%=m_context%>/util/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/container/container_core-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/animation/animation-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/menu/menu-min.js"></script>

<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/modal-message.css">

<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/ajax-dynamic-content.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/ajax.js"></script>

<link rel="stylesheet" type="text/css" href="<%=m_context %>/util/yui/menu/assets/menu.css"/>
<script src="<%=m_context%>/versioningPeas/jsp/javaScript/dragAndDrop.js" type="text/javascript"></script>

<style type="text/css">
<!--
a.yuimenuitemlabel-disabled:hover {
	color: #B9B9B9;
} 

ul#attachmentList.ui-sortable {
	cursor:default;
}

ul#attachmentList {
	list-style-type: none;
	margin-left: 0px;
	padding-left: 0px;
}

ul#attachmentList li.attachmentListItem {
	margin:0;
  	padding:10px;
}

ul#attachmentList li.attachmentListItem .lineSize {
	white-space: nowrap;
}

ul#attachmentList li.attachmentListItem .lineMain {
	white-space: nowrap;
}

-->
</style>

<%!

boolean isDocumentCheckinable(Document document, DocumentVersion version, String flag, int user_Id, boolean is_user_writer)
{
	if ("admin".equals(flag))
	  return true;
	
	int userId = user_Id;
	if (document.getStatus() == Document.STATUS_CHECKOUTED)
	{
		if (document.getTypeWorkList() == 0)
		{
		   return (document.getOwnerId() == userId);
		}
		else if (document.getTypeWorkList() == 1)
		{
		    return (document.getOwnerId() == userId && is_user_writer && version.getStatus() != DocumentVersion.STATUS_VALIDATION_REQUIRED);
		}
		else if (document.getTypeWorkList() == 2)
		{
			Worker user = (Worker)document.getWorkList().get(document.getCurrentWorkListOrder());
            return (userId == user.getUserId() && user.isWriter());
		}
	}
	return false;
}

boolean isDocumentCheckoutable(Document document, String flag, int user_Id, boolean is_user_writer)
{
  int userId = user_Id;
  if (document.getStatus()==Document.STATUS_CHECKINED)
  {
    if (document.getTypeWorkList() == 0)
    {
    	return is_user_writer || "publisher".equals(flag) || "admin".equals(flag);
    }
    else if (document.getTypeWorkList() == 1)
    {
        return is_user_writer;
    }
    else if (document.getTypeWorkList() == 2)
    {
        Worker user = (Worker)document.getWorkList().get(document.getCurrentWorkListOrder());
        return (user.isWriter() && user.getUserId() == userId /*|| "admin".equals(flag)*/ );
    }  
  }
  return false;
}

void displayActions(Document document, DocumentVersion version, String profile, boolean useXMLForm, boolean useFileSharing, boolean useWebDAV, int userId, ResourcesWrapper resources, String httpServerBase, VersioningSessionController versioningSC, JspWriter out) throws IOException
{
	String documentId = document.getPk().getId();
	String webDavOK = "false";
	if (useWebDAV && version.isOpenOfficeCompatibleDocument())
		webDavOK = "true";
    
	out.println("<div id=\"basicmenu"+documentId+"\" class=\"yuimenu\">");
	out.println("<div class=\"bd\">");
		out.println("<ul class=\"first-of-type\">");
			out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkout("+documentId+","+webDavOK+")\">"+resources.getString("checkOut")+"</a></li>");
		    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkoutAndDownload("+documentId+","+webDavOK+")\">"+resources.getString("versioning.checkOutAndDownload")+"</a></li>");
		    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkoutAndEdit("+documentId+")\">"+resources.getString("versioning.checkOutAndEditOnline")+"</a></li>");
		    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:addVersion("+documentId+")\">"+resources.getString("addNewVersion")+"</a></li>");
		    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkin("+documentId+",false)\">"+resources.getString("checkIn")+"</a></li>");
		    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:validateFile("+documentId+")\">"+resources.getString("operation.validate")+"</a></li>");
		    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:refuseFile("+documentId+")\">"+resources.getString("operation.refuse")+"</a></li>");
		out.println("</ul>");
		out.println("<ul>");
			out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:updateAttachment('"+documentId+"')\">"+resources.getString("GML.modify")+"</a></li>");
			out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:deleteAttachment('"+document.getName()+"',"+documentId+")\">"+resources.getString("GML.delete")+"</a></li>");
		out.println("</ul>");
		if (useFileSharing) 
		{
			out.println("<ul>");
				out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:ShareAttachment('"+documentId+"')\">"+resources.getString("versioning.share")+"</a></li>");
			out.println("</ul>");
		}
	out.println("</div>");
	out.println("</div>");

	out.println("<script type=\"text/javascript\">");
	    
			out.println("var oMenu"+documentId+";");
			out.println("var webDav"+documentId+" = \""+URLEncoder.encode(httpServerBase+version.getWebdavUrl())+"\";");
			out.println("YAHOO.util.Event.onContentReady(\"basicmenu"+documentId+"\", function () {");			    
				out.println("oMenu"+documentId+" = new YAHOO.widget.ContextMenu(\"basicmenu"+documentId+"\", { trigger: \"img_"+documentId+"\", hidedelay: 100, effect: { effect: YAHOO.widget.ContainerEffect.FADE, duration: 0.30}});");
				out.println("oMenu"+documentId+".render();");
				boolean is_user_writer = versioningSC.isWriter(document, userId);
				
				out.println("oMenu"+documentId+".getItem(5).cfg.setProperty(\"disabled\", true);"); //validate
		  		out.println("oMenu"+documentId+".getItem(6).cfg.setProperty(\"disabled\", true);"); //refuse
				if (document.getStatus()==Document.STATUS_CHECKOUTED)
				{
				  	//réservé
				  	if (useFileSharing) 
					{
						out.println("oMenu"+documentId+".getItem(0, 2).cfg.setProperty(\"disabled\", true);"); //share
					}
					out.println("oMenu"+documentId+".getItem(0).cfg.setProperty(\"disabled\", true);"); //checkout
					out.println("oMenu"+documentId+".getItem(1).cfg.setProperty(\"disabled\", true);"); //checkout and download
					
					if (document.getOwnerId() != userId && "admin".equals(profile))
					{
					  	out.println("oMenu"+documentId+".getItem(2).cfg.setProperty(\"disabled\", true);"); //edit online
						out.println("oMenu"+documentId+".getItem(3).cfg.setProperty(\"disabled\", true);"); //add version
					}
										
					if (!isDocumentCheckinable(document, version, profile, userId, is_user_writer))
					{
						out.println("oMenu"+documentId+".getItem(4).cfg.setProperty(\"disabled\", true);"); //checkin
					}
					
					if (document.getTypeWorkList() == 1)
					{
					  	if (isValidator(document.getWorkList(), userId) && version.getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED)
            			{
					  	  	out.println("oMenu"+documentId+".getItem(5).cfg.setProperty(\"disabled\", false);"); //validate
					  		out.println("oMenu"+documentId+".getItem(6).cfg.setProperty(\"disabled\", false);"); //refuse
            			}
					}
					else if (document.getTypeWorkList() == 2)
					{
					  	Worker user = (Worker)document.getWorkList().get(document.getCurrentWorkListOrder());
					  	if (userId == user.getUserId() && user.isApproval())
            			{
					  	  	out.println("oMenu"+documentId+".getItem(5).cfg.setProperty(\"disabled\", false);"); //validate
					  		out.println("oMenu"+documentId+".getItem(6).cfg.setProperty(\"disabled\", false);"); //refuse
            			}
					}
				}
				else
				{
				    //libre
				  	out.println("oMenu"+documentId+".getItem(4).cfg.setProperty(\"disabled\", true);"); //checkin
				  	
				  	if (!isDocumentCheckoutable(document, profile, userId, is_user_writer))
				  	{
				  	  	out.println("oMenu"+documentId+".getItem(0).cfg.setProperty(\"disabled\", true);"); //checkout
						out.println("oMenu"+documentId+".getItem(1).cfg.setProperty(\"disabled\", true);"); //checkout and download
						out.println("oMenu"+documentId+".getItem(3).cfg.setProperty(\"disabled\", true);"); //edit online
						out.println("oMenu"+documentId+".getItem(4).cfg.setProperty(\"disabled\", true);"); //new version
				  	}
				}
				
				if (!useWebDAV || !version.isOpenOfficeCompatibleDocument())
					out.println("oMenu"+documentId+".getItem(2).cfg.setProperty(\"disabled\", true);"); //edit online
				  
				out.println("YAHOO.util.Event.addListener(\"basicmenu"+documentId+"\", \"mouseover\", oMenu"+documentId+".show);");
				out.println("YAHOO.util.Event.addListener(\"basicmenu"+documentId+"\", \"mouseout\", oMenu"+documentId+".hide);");
			out.println("});");
	    
	out.println("</script>");
}
%>

<%
	String id 			= request.getParameter("Id");
	String componentId 	= request.getParameter("ComponentId");
	String context 		= request.getParameter("Context");
	String fromAlias	= request.getParameter("Alias");
	String profile		= request.getParameter("Profile");
	String nodeId		= request.getParameter("NodeId");
	String versionningFileRightsMode = request.getParameter("VersionningFileRightsMode");
	String s_topicRightsEnabled = request.getParameter("TopicRightsEnabled");
	String sIndexIt		= request.getParameter("IndexIt");
	String callbackURL  = request.getParameter("CallbackUrl");
	String xmlForm 		= m_MainSessionCtrl.getOrganizationController().getComponentParameterValue(componentId, "XmlFormForFiles");
	String sHideAllVersionsLink = m_MainSessionCtrl.getOrganizationController().getComponentParameterValue(componentId, "hideAllVersionsLink");
	boolean hideAllVersionsLink = StringUtil.getBooleanValue(sHideAllVersionsLink) && profile.equals("user");
	
	boolean indexIt = "1".equals(sIndexIt);

	boolean topicRightsEnabled = false;
	if (StringUtil.isDefined(s_topicRightsEnabled))
		topicRightsEnabled = new Boolean(s_topicRightsEnabled).booleanValue();
	if (versioningSC == null)
	{	
		versioningSC = setComponentSessionController(session, m_MainSessionCtrl);
 	 	versioningSC.setComponentId(componentId);
		 versioningSC.setFileRightsMode(versionningFileRightsMode);
		 versioningSC.setAttributesContext(nodeId, topicRightsEnabled);
		request.setAttribute(URLManager.CMP_VERSIONINGPEAS, versioningSC);
	}

	versioningSC.setProfile(profile);
	versioningSC.setIndexable(indexIt);
  	versioningSC.setFileRightsMode(versionningFileRightsMode);
  	versioningSC.setXmlForm(xmlForm);

	boolean spinfireViewerEnable = attachmentSettings.getBoolean("SpinfireViewerEnable", false);
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
	boolean useFileSharing = (isFileSharingEnable(m_MainSessionCtrl, componentId) && "admin".equalsIgnoreCase(profile));
	boolean contextualMenuEnabled = ("admin".equalsIgnoreCase(profile) || "publisher".equalsIgnoreCase(profile) || "writer".equalsIgnoreCase(profile));
	String iconStyle = "";
    if (contextualMenuEnabled)
    	iconStyle = "style=\"cursor:move\"";
    String language = versioningSC.getLanguage();
    boolean useXMLForm 	= StringUtil.isDefined(xmlForm);
    
    

  ForeignPK foreignKey =  new ForeignPK(id, componentId);
  VersioningUtil versioning_util = new VersioningUtil();

	List documents = versioning_util.getDocuments(foreignKey);
  	Iterator iterator = documents.iterator();
    Document document;
    DocumentVersion document_version;
	boolean attachmentsDisplayed = false;
    if (iterator.hasNext() || (StringUtil.isDefined(profile) && !profile.equals("user"))) {
        Board		board	= gef.getBoard();
        out.println(board.printBefore());
        out.println("<TABLE width=\"150\">");
	  	out.println("<TR><TD align=\"center\"><img src=\""+m_context+"/util/icons/attachedFiles.gif\"/></td></TR>");
	  	out.println("<tr><td>");
        out.println("<ul id=\"attachmentList\">");
        while (iterator.hasNext())
        {
        	document = (Document) iterator.next();
        	versioningSC.setEditingDocumentWithDefaultLists(document);
        	if (versioningSC.hasAccess(document, versioningSC.getUserId()))
	        { 
        		if (profile.equals("user"))
        	  	{
	        		document_version = versioning_util.getLastPublicVersion(document.getPk());
        	  	}
        		else
        		{
	        	  	document_version = versioning_util.getLastVersion(document.getPk());
        		}
	        	  if (document_version != null)
	        	  {
	             		String documentVersionUrl = versioning_util.getDocumentVersionURL(componentId, document_version.getLogicalName(), document.getPk().getId(), document_version.getPk().getId());
	
	             		if ("1".equals(fromAlias))
	             		{
							String contextFileServer = m_context+"/FileServer/";
							int index = documentVersionUrl.indexOf(contextFileServer);
							documentVersionUrl = m_context+"/AliasFileServer/"+documentVersionUrl.substring(index+contextFileServer.length());
	             		}
	             	%>
					<li id="attachment_<%=document.getPk().getId()%>" class="attachmentListItem">
								 <span class="lineMain">
	                             <img id="img_<%=document.getPk().getId() %>" alt="" src="<%=versioning_util.getDocumentVersionIconPath(document_version.getPhysicalName())%>" width="20" <%=iconStyle%>/>&nbsp;
				                 <A id="url<%=document.getPk().getId() %>" href="<%=documentVersionUrl%>" target="_blank"><%=document.getName()%></A>
				                 &nbsp;(<span id="version_<%=document.getPk().getId() %>">v<%=document_version.getMajorNumber()%>.<%=document_version.getMinorNumber()%></span>)

								 <a href="<%=URLManager.getSimpleURL(URLManager.URL_DOCUMENT, document.getPk().getId())%>"><img src="<%=m_context%>/util/icons/link.gif" border="0" valign="absmiddle" alt="<%=attResources.getString("versioning.CopyLink") %>" title="<%=attResources.getString("versioning.CopyLink") %>" target="_blank"></a>

								<%
							 	if (contextualMenuEnabled)
							    {
							 	 	displayActions(document, versioning_util.getLastVersion(document.getPk()), profile, false, useFileSharing, webdavEditingEnable, Integer.parseInt(versioningSC.getUserId()), attResources, httpServerBase, versioningSC, out);
								    out.println("<br/>");
								    if (document.getStatus() == Document.STATUS_CHECKOUTED)
								    {
								    	out.println("<div id=\"worker"+document.getPk().getId()+"\" style=\"visibility:visible\">"+attResources.getString("lockedBy")+" "+m_MainSessionCtrl.getOrganizationController().getUserDetail(Integer.toString(document.getOwnerId())).getDisplayedName()+" "+attResources.getString("at")+" "+resources.getOutputDate(document.getLastCheckOutDate())+"</div>");
								    }
								    else
								    {
								    	out.println("<div id=\"worker"+document.getPk().getId()+"\" style=\"visibility:hidden\"></div>");
								    }
							    }
							    else
							    {
							    	out.println("<br/>");
							    }
								out.print("</span>");
							
								out.println("<span class=\"lineSize\">");
								if (showFileSize && showDownloadEstimation)
								{
									out.println(FileRepositoryManager.formatFileSize(document_version.getSize()));
									out.println(" / " + versioning_util.getDownloadEstimation(document_version.getSize()));
								}
								else
								{
									if (showFileSize)
										out.println(FileRepositoryManager.formatFileSize(document_version.getSize()));
									if (showDownloadEstimation)
										out.println(versioning_util.getDownloadEstimation(document_version.getSize()));
								}
								out.println(" - " + resources.getOutputDate(document_version.getCreationDate()));
								out.println("</span>");
								if (StringUtil.isDefined(document.getDescription()) && showInfo)
									out.println("<br/><i>"+Encode.javaStringToHtmlParagraphe(document.getDescription())+"</i>");
									
								if (document_version.isSpinfireDocument() && spinfireViewerEnable)
							    {
									    %>
                                        <div id="switchView" name="switchView" style="display: none">
                                          <a href="#" onClick="changeView3d(<%=document_version.getPk().getId()%>)"><img name="iconeView" valign="bottom" border="0" src="/util/icons/masque3D.gif"></a>
                                        </div>
                                        <div id="<%=document_version.getPk().getId()%>" style="display: none">
                                          <OBJECT classid="CLSID:A31CCCB0-46A8-11D3-A726-005004B35102"
                                                  width="300" height="200" id="XV" >
                                            <PARAM NAME="ModelName" VALUE="<%=documentVersionUrl%>">
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
										<%
							    }							   
								if (StringUtil.isDefined(document_version.getXmlForm()))
								{
									String xmlURL = m_context+"/RformTemplate/jsp/View?width=400&ObjectId="+document_version.getPk().getId()+"&ComponentId="+componentId+"&ObjectType=Versioning&XMLFormName="+URLEncoder.encode(document_version.getXmlForm());
									%>
									<br/><a rel="<%=xmlURL%>" href="#" title="<%=document.getName()%>"><%=attMessages.getString("versioning.xmlForm.View")%></a>
									<%
								}
								boolean displayAllVersionsLink = false;
								if ("user".equals(profile) && document_version.getMajorNumber() > 1)
								{
									displayAllVersionsLink = true;
								}
								else if (!profile.equals("user") && versioning_util.getDocumentVersions(document.getPk()).size()>1)
								{
								  	displayAllVersionsLink = true;
								}
								%>
								<% if (displayAllVersionsLink && !hideAllVersionsLink) { %>
									<br/>>> <a href="javaScript:viewPublicVersions('<%=document.getPk().getId()%>')"><%=attMessages.getString("allVersions")%></a><br/>
								<% } %>
							</li>
			<%
				        }
             }
          }
        	  out.println("</ul>");
	     	  out.println("</TD></TR>");
	     	  if (contextualMenuEnabled && dragAndDropEnable) { %>
              <tr>
                <td align="right">
                  <table width="100%">
                    <tr>
                      <td colspan="3" align="right"><a href="javascript:showDnD()" id="dNdActionLabel">Déposer rapidement un fichier...</a></td>
                    </tr>
                    <tr>
                      <td><div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div></td>
                      <td width="5%">&nbsp;</td>
                      <td><div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div></td>
                    </tr>
                  </table>
                </td>
              </tr>
	 		  <% } %>
	 		  <% if (contextualMenuEnabled && !dragAndDropEnable) { %>
	 				<tr><td align="right"><br/><a href="javascript:AddAttachment();"><%=attResources.getString("GML.add") %>...</a></td></tr>
	 		  <% }
	          out.println("</TABLE>");
	          out.println(board.printAfter());
     }
%>
<script language="JavaScript">
	var publicVersionsWindow = window;
	function viewPublicVersions(docId) {
		url = "<%=m_context+URLManager.getURL("VersioningPeas", "useless", componentId)%>ViewAllVersions?DocId="+docId+"&Alias=<%=fromAlias%>&ComponentId=<%=componentId%>";
		windowName = "publicVersionsWindow";
		larg = "800";
		haut = "475";
		windowParams = "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised";
		if (!publicVersionsWindow.closed && publicVersionsWindow.name== "publicVersionsWindow")
						publicVersionsWindow.close();
		publicVersionsWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
<% if (spinfireViewerEnable) { %>
		if (navigator.appName=='Microsoft Internet Explorer')
		{
			for (i=0; document.getElementsByName("switchView")[i].style.display=='none'; i++)
				document.getElementsByName("switchView")[i].style.display = '';
		}
		function changeView3d(objectId)
		{
			if (document.getElementById(objectId).style.display == 'none')
			{
				document.getElementById(objectId).style.display = '';
				iconeView.src = '/util/icons/visible3D.gif';
				}
			else
			{
				document.getElementById(objectId).style.display = 'none';
				iconeView.src = '/util/icons/masque3D.gif';
			}
		}
<% } %>
</script>
<script type="text/javascript">
function showDnD()
{
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize 		= uploadSettings.getString("MaximumFileSize", "10000000");
	String maxFileSizeForApplet = maximumFileSize.substring(0, maximumFileSize.length()-3);
	String pathInstallerJre = GeneralPropertiesManager.getGeneralResourceLocator().getString("pathInstallerJre");
  	if (pathInstallerJre != null && !pathInstallerJre.startsWith("http"))
    	pathInstallerJre = m_sAbsolute + pathInstallerJre;
	String baseURL = httpServerBase+m_context+"/VersioningDragAndDrop/jsp/Drop?UserId="+versioningSC.getUserId()+"&ComponentId="+componentId+"&Id="+id+"&IndexIt="+indexIt;
	String publicURL 	= baseURL+"&Type="+DocumentVersion.TYPE_PUBLIC_VERSION;
	String workURL 		= baseURL+"&Type="+DocumentVersion.TYPE_DEFAULT_VERSION;
	%>
	showHideDragDrop('<%=publicURL%>','<%=httpServerBase%>/weblib/dragAnddrop/VersioningPublic_<%=language%>.html','<%=httpServerBase%>/weblib/dragAnddrop/radupload.properties','<%=workURL%>','<%=httpServerBase%>/weblib/dragAnddrop/VersioningWork_<%=language%>.html','<%=maxFileSizeForApplet%>','<%=pathInstallerJre%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
}
// Create the tooltips only on document load
$(document).ready(function() 
{
   // Use the each() method to gain access to each elements attributes
   $('a[rel]').each(function()
   {
      $(this).qtip(
      {
         content: {
            // Set the text to an image HTML string with the correct src URL to the loading image you want to use
            text: '<img class="throbber" src="<%=m_context%>/util/icons/inProgress.gif" alt="Loading..." />',
            url: $(this).attr('rel'), // Use the rel attribute of each element for the url to load
            title: {
               text: '<%=attResources.getString("versioning.xmlForm.ToolTip")%> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
               button: '<%=resources.getString("GML.close")%>' // Show a close link in the title
            }
         },
         position: {
            corner: {
               target: 'leftMiddle', // Position the tooltip above the link
               tooltip: 'rightMiddle'
            },
            adjust: {
               screen: true // Keep the tooltip on-screen at all times
            }
         },
         show: { 
            when: 'click', 
            solo: true // Only show one tooltip at a time
         },
         hide: 'unfocus',
         style: {
            tip: true, // Apply a speech bubble tip to the tooltip at the designated tooltip corner
            border: {
               width: 0,
               radius: 4
            },
            name: 'light', // Use the default light style
            width: 570 // Set the tooltip width
         }
      })
   });
});

<% if (contextualMenuEnabled) { %>

function checkout(id, webdav)
{
	if (id > 0) {
		$.get('<%=m_context%>/AjaxVersioning', {DocId:id,Action:'Checkout'}, 
		function(data){
			if (data == "ok")
			{
				var oMenu = eval("oMenu"+id);
				oMenu.getItem(0).cfg.setProperty("disabled", true); //checkout
				oMenu.getItem(1).cfg.setProperty("disabled", true); //checkout and download
				if (!webdav)
				{
					oMenu.getItem(2).cfg.setProperty("disabled", true); //edit online
				}
				oMenu.getItem(3).cfg.setProperty("disabled", false); //add new version
				oMenu.getItem(4).cfg.setProperty("disabled", false); //checkin
				
				$('#worker'+id).html("<%=attResources.getString("lockedBy")%> <%=m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName()%> <%=attResources.getString("at")%> <%=DateUtil.getOutputDate(new Date(), language)%>");
				$('#worker'+id).css({'visibility':'visible'});
			}
			else if (data == "alreadyCheckouted")
			{
				//do nothing
			}
			else
			{
				alert(data);
			}
		});
	}
}

function checkoutAndDownload(id, webdav)
{
	checkout(id, webdav);

	var url = $('#url'+id).attr('href');
	window.open(url);
}

function checkoutAndEdit(id)
{
	checkout(id, true);

	var url = "<%=httpServerBase+m_context%>/attachment/jsp/launch.jsp?documentUrl="+eval("webDav"+id);
	window.open(url);
}

function checkin(id, force) {
  if (id > 0) {
    //release the file without changing it
    $.get('<%=m_context%>/AjaxVersioning', {DocId:id,Action:'IsLocked',force_release:force},
    function(data) {
      data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
      if (data == "locked") {
        displayWarning();
      }
      else {
        if (data == "ok") {
          SP_openWindow('<%=m_context%>/RVersioningPeas/jsp/AddNewOnlineVersion?Id='+id+'&ComponentId=<%=componentId%>&documentId='+id+'&force_release='+force+'&Callback=newVersionAdded', "test", "600", "400","scrollbars=no, resizable, alwaysRaised");
        }
      }
    }, "html");
  }
  else
  {
    SP_openWindow('<%=m_context%>/RVersioningPeas/jsp/AddNewOnlineVersion?Id='+id+'&ComponentId=<%=componentId%>&documentId='+id+'&force_release='+force+'&Callback=newVersionAdded', "test", "600", "400","scrollbars=no, resizable, alwaysRaised");
  }
}

function newVersionAdded(documentId, majorNumber, minorNumber) {
	menuCheckin(documentId);
	$('#version_'+documentId).html("v"+majorNumber+"."+minorNumber);
}

function menuCheckin(id)
{
	var oMenu = eval("oMenu"+id);
	oMenu.getItem(0).cfg.setProperty("disabled", false); 
	oMenu.getItem(1).cfg.setProperty("disabled", false);
	oMenu.getItem(2).cfg.setProperty("disabled", false);
	oMenu.getItem(3).cfg.setProperty("disabled", false);
	oMenu.getItem(4).cfg.setProperty("disabled", true);
	
	$('#worker'+id).html("");
	$('#worker'+id).css({'visibility':'hidden'});
}

function addVersion(id, webdav) {
	checkout(id, webdav);
	var url = "<%=httpServerBase+m_context%>/RVersioningPeas/jsp/AddNewVersion?documentId="+id+"&Id=<%=id%>&ComponentId=<%=componentId%>&Context=<%=context%>&IndexIt=<%=indexIt%>&ReturnURL=<%=URLEncoder.encode(m_context+callbackURL)%>";
	SP_openWindow(url, "test", "700", "400","scrollbars=no, resizable, alwaysRaised");
}

function AddAttachment()
{
	url = "<%=m_context%>/RVersioningPeas/jsp/AddNewDocument?PubId=<%=id%>&Url=<%=URLEncoder.encode(m_context+callbackURL)%>";
	width = "750";
	<% if (useXMLForm) { %>
		SP_openWindow(url,"AddNewDocument",width,"600","scrollbars=yes, resizable, alwaysRaised");
	<% } else { %>
		SP_openWindow(url,"AddNewDocument",width,"400","");
	<% } %>
}

function deleteAttachment(attachmentName, attachmentId)
{
	if (window.confirm("<%=attResources.getString("confirmDelete")%>"))
	{
		$.get('<%=m_context%>/AjaxVersioning', {Id:attachmentId,Action:'Delete'}, 
				function(data){
					data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
					if (data == "ok")
					{
						$('#attachment_'+attachmentId).remove();
					}
					else
					{
						alert(data);
					}
				});
	}
}

function reloadIncludingPage()
{
	<% if (!StringUtil.isDefined(callbackURL)) { %>
		document.location.reload();
	<% } else { %>
		document.location.href = "<%=m_sAbsolute+m_context+callbackURL%>";
	<% } %>
}

function updateAttachment(attachmentId)
{
	var url = "<%=m_context%>/RVersioningPeas/jsp/versions.jsp";
	SP_openWindow(url + "?DocId="+attachmentId+"&Id=<%=id%>&ComponentId=<%=componentId%>&Context=Images&IndexIt=1&profile=<%=profile%>", "", "750", "400","scrollbars=yes", "resizable", "alwaysRaised");
}

// Suppression du fichier
messageObj = new DHTML_modalMessage();	// We only create one object of this class
messageObj.setShadowOffset(5);	// Large shadow

var forceRelease = "false";
function closeMessage(force)
{
	forceRelease = force;
	messageObj.close(); 
}

function displayWarning()
{
	messageObj.setSize(300,80);
	messageObj.setCssClassMessageBox(false);   
	messageObj.setSource('<%=m_context%>/attachment/jsp/warning_locked.jsp?profile=<%=profile%>' );
	messageObj.setShadowDivVisible(false);  // Disable shadow for these boxes
	messageObj.display();
}

$(document).ready(function(){
	$("#attachmentList").sortable({opacity: 0.4, axis: 'y', cursor: 'hand', handle: 'img'}); 
});

$('#attachmentList').bind('sortupdate', function(event, ui) {
	var reg=new RegExp("attachment", "g");
	
	var data = $('#attachmentList').sortable('serialize');
	data += "#";
	var tableau=data.split(reg);
	var param = "";
	for (var i=0; i<tableau.length; i++)
	{
		if (i != 0)
			param += ","
				
		param += tableau[i].substring(3, tableau[i].length-1);
	}
	  sortAttachments(param);
	});

function sortAttachments(orderedList)
{
	//alert(orderedList);
	$.get('<%=m_context%>/AjaxVersioning', {orderedList:orderedList,Action:'Sort',ComponentId:'<%=componentId%>'}, 
			function(data){
				data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
				if (data != "ok")
				{
					alert(data);
				}
			});
}

function uploadCompleted(s)
{
	reloadIncludingPage();
}

function ShareAttachment(id)
{
	var url = "<%=m_context%>/RfileSharing/jsp/NewTicket?FileId="+id+"&ComponentId=<%=componentId%>&Type=Version";
	SP_openWindow(url, "NewTicket", "700", "300","scrollbars=no, resizable, alwaysRaised");
}
<% } %>
</script>