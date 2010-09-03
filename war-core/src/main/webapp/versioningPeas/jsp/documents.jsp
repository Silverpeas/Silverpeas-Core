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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion,com.stratelia.silverpeas.versioning.model.DocumentPK"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp"%>

<%
  String id = request.getParameter("Id");
  String spaceId = request.getParameter("SpaceId");
  String componentId = request.getParameter("ComponentId");
  String context = request.getParameter("Context");
  String url = request.getParameter("Url");
  String spaceLabel = request.getParameter("SL");
  String componentLabel = request.getParameter("CL");
  String flag = request.getParameter("profile");
  String indexIt = request.getParameter("IndexIt"); //indexIt can be 0 or 1 or notdefined (used only by kmelia actually)
	String nodeId = request.getParameter("NodeId");
	String versionningFileRightsMode = request.getParameter("VersionningFileRightsMode");
	String s_topicRightsEnabled = request.getParameter("TopicRightsEnabled");
	String xmlForm = request.getParameter("XMLFormName");
	boolean topicRightsEnabled = false;
	if (StringUtil.isDefined(s_topicRightsEnabled))
  {
		topicRightsEnabled = new Boolean(s_topicRightsEnabled).booleanValue();
  }
	boolean bIndexIt = true;
	if (indexIt != null && !"null".equals(indexIt) && indexIt.length()!=0 && "0".equals(indexIt))
	{
	  bIndexIt = false;
	}
	if (versioningSC == null) 
	{
	  versioningSC = setComponentSessionController(session, m_MainSessionCtrl, componentId);
	}
  versioningSC.setAttributesContext(spaceId,componentId,spaceLabel,componentLabel, nodeId, topicRightsEnabled);
  ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
  ForeignPK foreignKey = new ForeignPK(id, componentId);
  String userId = versioningSC.getUserId();
  versioningSC.setIndexable(bIndexIt);
  versioningSC.setProfile(flag);
  versioningSC.setFileRightsMode(versionningFileRightsMode);
  versioningSC.setXmlForm(xmlForm);
  List documents = versioningSC.getDocuments(foreignKey);
  Iterator documents_iterator = documents.iterator();
%>

<%@page import="com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException"%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language='Javascript'>
function open_pu_window(url,docid)
{
  SP_openWindow(url + "?DocId="+docid+"&Id=<%=id%>&SpaceId=<%=spaceId%>&ComponentId=<%=componentId%>&Context=<%=context%>&IndexIt=<%=indexIt%>&Url=<%=url%>&profile=<%=flag%>", "", "750", "400","scrollbars=yes", "resizable", "alwaysRaised");
}
function addNewDocument(pubId)
{
  url = "<%=m_context%>/RVersioningPeas/jsp/AddNewDocument?PubId="+pubId+"&Url=<%=URLEncoder.encode(m_context+url)%>";
  width = "750";
  <% if (StringUtil.isDefined(xmlForm)) { %>
  	SP_openWindow(url,"AddNewDocument",width,"600","scrollbars=yes, resizable, alwaysRaised");
  <% } else { %>
  	SP_openWindow(url,"AddNewDocument",width,"400","");
  <% } %>
}

function deleteDoc(docid)
{
  url = "<%=m_context%>/RVersioningPeas/jsp/DeleteDocumentRequest?DocId="+docid+"&Url=<%=URLEncoder.encode(m_context+url)%>";
  width = "400";
  height = "130";
  SP_openWindow(url,"DeleteDocument",width,height,"");
}

function showDnD()
{
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize 		= uploadSettings.getString("MaximumFileSize", "10000000");
	String language = versioningSC.getLanguage();
	String baseURL = httpServerBase+m_context+"/VersioningDragAndDrop/jsp/Drop?UserId="+userId+"&ComponentId="+componentId+"&Id="+id+"&IndexIt="+indexIt;
	String publicURL 	= baseURL+"&Type="+DocumentVersion.TYPE_PUBLIC_VERSION;
	String workURL 		= baseURL+"&Type="+DocumentVersion.TYPE_DEFAULT_VERSION;
	%>
	showHideDragDrop('<%=publicURL%>','<%=httpServerBase + m_context%>/upload/VersioningPublic_<%=language%>.html','<%=workURL%>','<%=httpServerBase + m_context%>/upload/VersioningWork_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
}

function uploadCompleted(s)
{
	location.href="<%=m_context%><%=url%>";
}

function ShareAttachment(id)
{
	var url = "<%=m_context%>/RfileSharing/jsp/NewTicket?FileId="+id+"&ComponentId=<%=componentId%>&Type=Version";
	SP_openWindow(url, "NewTicket", "700", "300","scrollbars=no, resizable, alwaysRaised");
}
</script>
<script type="text/javascript" src="<%=m_context%>/versioningPeas/jsp/javaScript/dragAndDrop.js"></script>
<script src="<%=m_context%>/util/javaScript/upload_applet.js" type="text/javascript"></script>
<CENTER>
<% if (dragAndDropEnable) { %>
	<table width="98%" border="0" id="DropZone">
		<tr><td colspan="3" align="right">
			<a href="javascript:showDnD()" id="dNdActionLabel"><%=resources.getString("GML.DragNDropExpand")%></a>
		</td></tr>
		<tr>
			<td>
				<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
			</td>
			<td width="5%">&nbsp;</td>
			<td>
				<div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
			</td>
		</tr>
	</table>
<% } //end if dragAndDropEnable %>
<%
	ArrayPane arrayPane = gef.getArrayPane("docsList", m_context+url, request, session);
	arrayPane.addArrayColumn(messages.getString("type"));
	arrayPane.addArrayColumn(messages.getString("name"));
	arrayPane.addArrayColumn(messages.getString("version"));
	arrayPane.addArrayColumn(messages.getString("date"));
	arrayPane.addArrayColumn(messages.getString("status"));
	arrayPane.addArrayColumn(messages.getString(""));
  String alt_message = messages.getString("alt");
  while(documents_iterator.hasNext())
  {
    Document document = (Document) (documents_iterator.next());
    if (versioningSC.hasAccess(document, userId))
    {
      List versions = versioningSC.getDocumentFilteredVersions(document.getPk(), new Integer(versioningSC.getUserId()).intValue());
      if ( versions.size() > 0 )
      {
        DocumentVersion document_version = (DocumentVersion)(versions.get(versions.size()-1));
        ArrayLine arrayLine = arrayPane.addArrayLine();
        if ( document_version.getSize() != 0 || !"dummy".equals(document_version.getLogicalName()) )
        {
          String lockedBy = "";
          String share = "";
          if (document.getStatus() == Document.STATUS_CHECKOUTED)
          {
            String until = "";
            if (StringUtil.isDefined(resources.getOutputDate(document.getExpiryDate()))) 
            {
              until = "&nbsp;" + messages.getString("until") + "&nbsp;" + resources.getOutputDate(document.getExpiryDate());
            }
            lockedBy = "<br><font size=1>(" + messages.getString("lockedBy") + versioningSC.getUserNameByID(document.getOwnerId()) + "&nbsp;" + messages.getString("at") + "&nbsp;" + resources.getOutputDate(document.getLastCheckOutDate()) + until + ")</font>";
          }
          else
          {
        	  if (isFileSharingEnable(m_MainSessionCtrl, componentId) && "admin".equalsIgnoreCase(flag))
              {
        		  share = "<a href=\"javascript:onClick=ShareAttachment('"+ document.getPk().getId() +"')\"><img src=\""+m_context+"/util/icons/webLink.gif\" border=\"0\" alt=\""+messages.getString("attachment.share")+"\"/></a> ";
              }
          }
          
          String permalink = " <a href=\""+URLManager.getSimpleURL(URLManager.URL_DOCUMENT, document.getPk().getId())+"\"><img src=\""+m_context+"/util/icons/link.gif\" border=\"0\" valign=\"absmiddle\" alt=\""+messages.getString("versioning.CopyLink")+"\" title=\""+messages.getString("versioning.CopyLink")+"\" target=\"_blank\"></a> ";
          
					arrayLine.addArrayCellLink("<img src='"+versioningSC.getDocumentVersionIconPath( document_version.getPhysicalName())+"' border=0>", "javascript:open_pu_window('"+versioningSC.getDocumentVersionShowVersionsURL()+"',"+document.getPk().getId()+");");
					arrayLine.addArrayCellText("<a href=\"#\" onClick=\"open_pu_window('"+versioningSC.getDocumentVersionShowVersionsURL()+"',"+document.getPk().getId()+");\">"+document.getName()+"</a>"+permalink+lockedBy);
					arrayLine.addArrayCellText(document_version.getMajorNumber() + "." + document_version.getMinorNumber());
					arrayLine.addArrayCellText(resources.getOutputDate(document_version.getCreationDate()));
					arrayLine.addArrayCellText("<img src='"+versioningSC.getDocumentVersionStatusIconPath( document.getStatus() )+"'/>");
					arrayLine.addArrayCellText(share+"<a href=\"#\"><img border=0 src=\""+m_context+"/util/icons/delete.gif\" onclick = \"deleteDoc(" + document.getPk().getId() + ")\" style=\"cursor:hand\"></a>");
        }
        else
        {
					arrayLine.addArrayCellText("");
					arrayLine.addArrayCellLink(document.getName(), "javascript:open_pu_window('"+versioningSC.getDocumentVersionShowVersionsURL()+"',"+document.getPk().getId()+");");
					arrayLine.addArrayCellText("");
					arrayLine.addArrayCellText("");
					arrayLine.addArrayCellText("");
					arrayLine.addArrayCellText("<a href=\"#\"><img border=0 src=\""+m_context+"/util/icons/delete.gif\" onclick = \"deleteDoc(" + document.getPk().getId() + ")\" style=\"cursor:hand\"></a>");
        }
			}
    }
  }
  out.println(arrayPane.print());
	%> <br>
<div align="center">
<%
  ButtonPane buttonPane2 = gef.getButtonPane();
  buttonPane2.addButton(gef.getFormButton(messages.getString("new_document"), "javascript:addNewDocument("+id+")", false));
  out.println(buttonPane2.print());
%>
</div>
</CENTER>