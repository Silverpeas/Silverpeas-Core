<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion,
         com.stratelia.silverpeas.versioning.model.DocumentPK,
         com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException"%>
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
	  versioningSC = setComponentSessionController(session, m_MainSessionCtrl);
	}
  versioningSC.setAttributesContext(spaceId,componentId,spaceLabel,componentLabel, nodeId, topicRightsEnabled);
  ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
  ForeignPK foreignKey = new ForeignPK(id, componentId);
  String userId = versioningSC.getUserId();
  versioningSC.setIndexable(bIndexIt);
  versioningSC.setProfile(flag);
  versioningSC.setFileRightsMode(versionningFileRightsMode);
  List documents = versioningSC.getDocuments(foreignKey);
  Iterator documents_iterator = documents.iterator();
%>
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
  height = "400";
  SP_openWindow(url,"AddNewDocument",width,height,"");
}

function deleteDoc(docid)
{
  url = "<%=m_context%>/RVersioningPeas/jsp/DeleteDocumentRequest?DocId="+docid+"&Url=<%=URLEncoder.encode(m_context+url)%>";
  width = "400";
  height = "130";
  SP_openWindow(url,"DeleteDocument",width,height,"");
}
</script>
<CENTER>
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
          if (document.getStatus() == Document.STATUS_CHECKOUTED)
          {
            String until = "";
            if (StringUtil.isDefined(resources.getOutputDate(document.getExpiryDate()))) 
            {
              until = "&nbsp;" + messages.getString("until") + "&nbsp;" + resources.getOutputDate(document.getExpiryDate());
            }
            lockedBy = "<br><font size=1>(" + messages.getString("lockedBy") + versioningSC.getUserNameByID(document.getOwnerId()) + "&nbsp;" + messages.getString("at") + "&nbsp;" + resources.getOutputDate(document.getLastCheckOutDate()) + until + ")</font>";
          }
					arrayLine.addArrayCellLink("<img src='"+versioningSC.getDocumentVersionIconPath( document_version.getPhysicalName())+"' border=0>", "javascript:open_pu_window('"+versioningSC.getDocumentVersionShowVersionsURL()+"',"+document.getPk().getId()+");");
					arrayLine.addArrayCellText("<a href=\"#\" onClick=\"open_pu_window('"+versioningSC.getDocumentVersionShowVersionsURL()+"',"+document.getPk().getId()+");\">"+document.getName()+"</a>"+lockedBy);
					arrayLine.addArrayCellText(document_version.getMajorNumber() + "." + document_version.getMinorNumber());
					arrayLine.addArrayCellText(resources.getOutputDate(document_version.getCreationDate()));
					arrayLine.addArrayCellText("<img src='"+versioningSC.getDocumentVersionStatusIconPath( document.getStatus() )+"'/>");
					arrayLine.addArrayCellText("<a href=\"#\"><img border=0 src=\""+m_context+"/util/icons/delete.gif\" onclick = \"deleteDoc(" + document.getPk().getId() + ")\" style=\"cursor:hand\"></a>");
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