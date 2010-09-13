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

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
   	ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
    ResourceLocator messages_attached = new ResourceLocator("com.stratelia.webactiv.util.attachment.multilang.attachment", m_MainSessionCtrl.getFavoriteLanguage());
    String pleaseFill = messages.getString("pleaseFill");

    int user_id = Integer.parseInt(m_MainSessionCtrl.getUserId());
    String spaceLabel = "";
    String componentLabel = "";
    String save = null;
    String type_of_version = null;
    String type = null;
		String flag = (String)request.getAttribute("Profile");

    String spaceId = request.getParameter("SpaceId");
    String componentId = request.getParameter("ComponentId");
    String url = request.getParameter("Url");
    String id = request.getParameter("DocId");
    String foreignId = request.getParameter("ForeignId");
	String from_action = request.getParameter("from_action");

    String profile = (String) request.getAttribute("Profile");

    String name = request.getParameter("name");
    String description = request.getParameter("description");
    String comments = request.getParameter("comments");
    
    Document document = (Document) request.getAttribute("Document");

%>
<html>
<title><%=messages.getString("popupTitle")%></title>
<%
    if (document == null)
    {
        DocumentPK documentPK = new DocumentPK(Integer.parseInt(id), spaceId, componentId);
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
    }
    else
    {
        //document = versioningSC.getEditingDocument();
        DocumentPK document_pk = document.getPk();
        spaceId = document_pk.getSpace();
        componentId = document_pk.getComponentName();
        id = document_pk.getId();
    }
%>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script language="Javascript">
	function rtrim(texte){
		while (texte.substring(0,1) == ' '){
	    	texte = texte.substring(1, texte.length);
	    }
        return texte;
	}

	function ltrim(texte){
        while (texte.substring(texte.length-1,texte.length) == ' ') {
	        texte = texte.substring(0, texte.length-1);
	    }
	    return texte;
	}

	function trim(texte){
        var len = texte.length;
        if (len == 0){
        	texte = "";
        }
        else {
            texte = rtrim(texte);
            texte = ltrim(texte);
        }
        return texte;
	}

	function onRadioClick(i) {
		document.forms[0].radio.value = i;
	}

	function isFormFilled() {
		var isEmpty = false;
	        if (( trim(document.forms[0].file_upload.value) == "")
	            || ( trim(document.forms[0].name.value) == "")
	            || ( trim(document.forms[0].radio.value) == "")){
	      isEmpty = true;
	    }
	    return isEmpty;
	}

	function addFile(){
		if (!isFormFilled()){
	        document.forms[0].save.value="true";
	        document.forms[0].submit();
	    } else {
	    	document.forms[0].file_upload.value = '';
	        alert("<%=pleaseFill%>");
	    }
	}

	function submitForm( action )
	{
	    document.forms[0].submit();
	}

	function showDnD()
	{
		<%
		ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
		String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
		String language = versioningSC.getLanguage();
	  	String indexIt = "0";
	  	if (versioningSC.isIndexable())
	  		indexIt = "1";
		String baseURL = httpServerBase+m_context+"/VersioningDragAndDrop/jsp/Drop?UserId="+user_id+"&ComponentId="+componentId+"&Id="+foreignId+"&IndexIt="+indexIt+"&DocumentId="+document.getPk().getId();
		String publicURL 	= baseURL+"&Type="+DocumentVersion.TYPE_PUBLIC_VERSION;
		String workURL 		= baseURL+"&Type="+DocumentVersion.TYPE_DEFAULT_VERSION;
		%>
		showHideDragDrop('<%=publicURL%>','<%=httpServerBase + m_context%>/upload/VersioningPublic_<%=language%>.html','<%=workURL%>','<%=httpServerBase + m_context%>/upload/VersioningWork_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	}

	function uploadCompleted(s)
	{
		location.href="<%=m_context%>/RVersioningPeas/jsp/ViewVersions";
	}
	</script>
	</head>
	<body>

<%
    if ( id != null )
    {
        DocumentPK documentPK = new DocumentPK(Integer.parseInt(id), spaceId, componentId);
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
    }
    else
    {
        document = versioningSC.getEditingDocument();
        DocumentPK document_pk = document.getPk();
        spaceId = document_pk.getSpace();
        componentId = document_pk.getComponentName();
        id = document_pk.getId();
    }
    
    if (versioningSC.getEditingDocument() != null)
        versioningSC.setEditingDocument( versioningSC.getDocument(versioningSC.getEditingDocument().getPk()) );

    List versions = versioningSC.getDocumentVersions( document.getPk() );

    DocumentVersion last_version = null;
    DocumentVersion first_version = null;
    if ( versions != null && versions.size() != 0 )
    {
        last_version = (DocumentVersion)versions.get(versions.size()-1);
        first_version = (DocumentVersion)versions.get(0);
    }
    out.flush();
		getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/editDocument.jsp?DocId="+id+"&Url="+url+"&profile="+flag).include(request, response);
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="Javascript">
function goToUserPanel(action)
{
	url = action;
	windowName = "userPanelWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	userPanelWindow = SP_openUserPanel(url, windowName, windowParams);
}    
       
<%
  DocumentPK lastVersionPk = new DocumentPK(Integer.parseInt(id), spaceId, componentId );
  boolean isOpenOffice= false;
  boolean isOffice = false;
  if(versioningSC.getLastVersion(lastVersionPk) != null) {
    isOpenOffice = versioningSC.getLastVersion(lastVersionPk).isOpenOfficeCompatibleDocument();
    isOffice = versioningSC.getLastVersion(lastVersionPk).isOfficeDocument();
  }
%>

function perfAction(action, fileName)
{
	var toSubmit = false;
    if ( action == "validate" || action == "refuse" )
    {
        window.open("comment.jsp?action="+action, "WindowAction", "height=150,width=500");
    }
    else
    {
        if (action == "checkin" &&  <%=webdavEditingEnable%> && <%=isOpenOffice%>) {
            var url = "<%=m_context%>/RVersioningPeas/jsp/AddNewOnlineVersion?Id=<%=id%>&ComponentId=<%=document.getPk().getInstanceId()%>&SpaceId=<%=document.getPk().getSpaceId()%>&documentId=<%=document.getPk().getId()%>";
            $("#attachmentModalDialog").dialog("open").load(url);
        } else if (action == "checkin") {
        	document.forms[0].Action.value="checkin";
        	toSubmit = true;
    	} else {
        	toSubmit = true;
		}
    }
    if (action != "checkin" || toSubmit)
		document.forms[0].submit();
}
	function update()
	{
		document.forms[0].action="Update";
		document.forms[0].submit();
	}

$(document).ready(function(){
	$("#attachmentModalDialog").dialog({
  	  	autoOpen: false,
        modal: true,
        height: 'auto',
        width: 400});
});
</script>
<div id="attachmentModalDialog" style="display: none"/>
</body>
</html>