<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
	String sURI = request.getRequestURI();
	String sRequestURL = request.getRequestURL().toString();
   	String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

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
		String from_action = request.getParameter("from_action");

    if (id==null)
        id = request.getParameter("DocId");

    String profile = (String) request.getAttribute("Profile");

    String name = request.getParameter("name");
    String description = request.getParameter("description");
    String comments = request.getParameter("comments");

%>
<html>
<TITLE><%=messages.getString("popupTitle")%></TITLE>
<%
		Document document = null;
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
%>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
  <link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/modal-message.css" />
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/ajax-dynamic-content.js"></script>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/ajax.js"></script>

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

	function openFile(fileURL, fileName, attachmentId, classicalFileURL)
	{
		if (navigator.appName == 'Microsoft Internet Explorer')
		{
			ucPass.Download(fileName, '<%=m_sAbsolute%>'+fileURL, '<%=onlineEditingFolder%>', '<%=m_sAbsolute+m_context%>/FileUploader/upload', 'oui', '<%=user_id%>', attachmentId, '<%=m_MainSessionCtrl.getFavoriteLanguage()%>','<%=m_MainSessionCtrl.getFavoriteLanguage()%>');
			document.forms[0].submit();
		}
		else
		{
			SP_openWindow(classicalFileURL, "test", "600", "240", "scrollbars, resizable, alwaysRaised");
		}
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
<script language="Javascript">
var activex = true;
var action_bis;
var filename_bis;

function handleError() {
	activex = false;
    window.onerror = null;
    perfAction(action_bis, filename_bis);
}

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
        if (action == "checkin" && navigator.appName == 'Microsoft Internet Explorer'  && <%=isOffice%> && <%=onlineEditingEnable%> && activex)	{
    		window.onerror = handleError;
    		var yesno = ucPass.CheckIn('<%=Encode.javaStringToJsString(last_version.getLogicalName())%>', '<%=onlineEditingFolder%>', '<%=m_sAbsolute+m_context%>/FileUploader/upload', 'oui', '<%=user_id%>', '<%=id%>', '<%=m_MainSessionCtrl.getFavoriteLanguage()%>','<%=m_MainSessionCtrl.getFavoriteLanguage()%>');
    		window.onerror = null;
    		toSubmit = true;
    	} else if (action == "checkin" &&  <%=webdavEditingEnable%> && <%=isOpenOffice%>) {
            messageObj = new DHTML_modalMessage();  // We only create one object of this class
            messageObj.setShadowOffset(5);  // Large shadow
            messageObj.setSize(550,300);
            messageObj.setCssClassMessageBox(false);
            messageObj.setSource('<%=m_context%>/versioningPeas/jsp/newOnlineVersion.jsp?Id=<%=id%>&ComponentId=<%=document.getPk().getInstanceId()%>&SpaceId=<%=document.getPk().getSpaceId()%>&documentId=<%=document.getPk().getId()%>');
            messageObj.setShadowDivVisible(false);  // Disable shadow for these boxes
            messageObj.display();
        } else if (action == "checkin") {
        	document.forms[0].action.value="checkin";
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
</script>
<% if(onlineEditingEnable) { %>
<OBJECT ID="ucPass" CLASSID="CLSID:60FFD28D-9C2B-41ED-9928-05ABDA287AEC" CODEBASE="<%=m_sAbsolute%>/weblib/onlineEditing/SilverpeasOnlineEdition.CAB#version=4,1,0,0"></OBJECT>
<% } %>
</body>
</html>