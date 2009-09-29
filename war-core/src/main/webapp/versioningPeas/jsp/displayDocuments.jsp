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

<script src="<%=m_context %>/attachment/jsp/jquery-1.3.2.min.js" type="text/javascript"></script>
<script src="<%=m_context %>/attachment/jsp/jquery.qtip-1.0.0-rc3.min.js" type="text/javascript"></script>
<%
	String id 			= request.getParameter("Id");
	String componentId 	= request.getParameter("ComponentId");
	String context 		= request.getParameter("Context");
	String fromAlias	= request.getParameter("Alias");
	String profile		= request.getParameter("Profile");
	String nodeId		= request.getParameter("NodeId");
	String versionningFileRightsMode = request.getParameter("VersionningFileRightsMode");
	String s_topicRightsEnabled = request.getParameter("TopicRightsEnabled");

	boolean topicRightsEnabled = false;
	if (StringUtil.isDefined(s_topicRightsEnabled))
		topicRightsEnabled = new Boolean(s_topicRightsEnabled).booleanValue();
	if (versioningSC == null)
	{	
		versioningSC = setComponentSessionController(session, m_MainSessionCtrl);
 	 	versioningSC.setComponentId(componentId);
		 versioningSC.setFileRightsMode(versionningFileRightsMode);
		 versioningSC.setAttributesContext(nodeId, topicRightsEnabled);
	}

	versioningSC.setProfile(profile);
	ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

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

  ForeignPK foreignKey =  new ForeignPK(id, componentId);
  VersioningUtil versioning_util = new VersioningUtil();

	List documents = versioning_util.getDocuments(foreignKey);
  	Iterator iterator = documents.iterator();
    Document document;
    DocumentVersion document_version;
	boolean attachmentsDisplayed = false;
    if (iterator.hasNext()) {
        Board		board	= gef.getBoard();
        while (iterator.hasNext())
        {
        	document = (Document) iterator.next();
        	versioningSC.setEditingDocumentWithDefaultLists(document);
        	if (versioningSC.hasAccess(document, versioningSC.getUserId()))
	        { 
	        	if (!attachmentsDisplayed)
	        	{
					out.println(board.printBefore());
					out.println("<TABLE width=\"250\">");
			        out.println("<TR><TD align=\"center\"><img src=\""+m_context+"/util/icons/attachedFiles.gif\"></td></TR>");
			        attachmentsDisplayed = true;
			    }
	        	  document_version = versioning_util.getLastPublicVersion(document.getPk());
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
	                 <TR>
		                 <TD>
					             <IMG alt="" src="<%=versioning_util.getDocumentVersionIconPath(document_version.getPhysicalName())%>" width="20">&nbsp;
				                 <A href="<%=documentVersionUrl%>" target="_blank"><%=document.getName()%></A>
				                 &nbsp;(v<%=document_version.getMajorNumber()%>.<%=document_version.getMinorNumber()%>)
										<br>
										<%
										if (showFileSize && showDownloadEstimation)
										{
											out.println(" (" + FileRepositoryManager.formatFileSize(document_version.getSize()));
											out.println(" / " + versioning_util.getDownloadEstimation(document_version.getSize()) + ")");
	
										}
										else
										{
											if (showFileSize)
												out.println(" (" + FileRepositoryManager.formatFileSize(document_version.getSize()) + ")");
											if (showDownloadEstimation)
												out.println("(" + versioning_util.getDownloadEstimation(document_version.getSize()) + ")");
										}
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
										<br/><a rel="<%=xmlURL%>" href="#" title="<%=document.getName()%>"><%=messages.getString("versioning.xmlForm.View")%></a>
										<%
									}
									if (document_version.getMajorNumber() > 1)
									{ %>
				            	 <br/>>> <a href="javaScript:viewPublicVersions('<%=document.getPk().getId()%>')"><%=messages.getString("allVersions")%></a><br>
					       <% } %>
				            &nbsp;<br></TD>
				         </TR> <%
				        }
             }
          }
	     	  if (attachmentsDisplayed)
	     	  {
	          out.println("</TABLE>");
	          out.println(board.printAfter());
	         }
     }
%>
<script language="JavaScript">
	var publicVersionsWindow = window;
	function viewPublicVersions(docId) {
		url = "<%=m_context+URLManager.getURL("VersioningPeas", "useless", componentId)%>ListPublicVersionsOfDocument?DocId="+docId+"&Alias=<%=fromAlias%>&ComponentId=<%=componentId%>";
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
               text: '<%=messages.getString("versioning.xmlForm.ToolTip")%> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
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
</script>