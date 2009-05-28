<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%@ include file="checkVersion.jsp" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

    Document 	document 	= (Document) request.getAttribute("Document");
    List 		vVersions 	= (List) request.getAttribute("PublicVersions");
    String		fromAlias	= (String) request.getAttribute("Alias");

    String componentId = document.getPk().getInstanceId();
    String id = document.getPk().getId();
%>
<html>
<head>
<TITLE><%=messages.getString("popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
ArrayPane arrayPane = gef.getArrayPane("List", "ListPublicVersionsOfDocument?DocId="+id+"&Alias=null&ComponentId="+componentId, request,session);// declare an array

// header of the array
ArrayColumn arrayColumn_mimeType = arrayPane.addArrayColumn(messages.getString("type"));
arrayColumn_mimeType.setSortable(false);
ArrayColumn arrayColumn_version = arrayPane.addArrayColumn(messages.getString("version"));
arrayColumn_version.setSortable(false);
ArrayColumn arrayColumn_creatorLabel = arrayPane.addArrayColumn(messages.getString("creator"));
arrayColumn_creatorLabel.setSortable(false);
ArrayColumn arrayColumn_date = arrayPane.addArrayColumn(messages.getString("date"));
arrayColumn_date.setSortable(false);
ArrayColumn arrayColumn_status = arrayPane.addArrayColumn(messages.getString("comments"));
arrayColumn_status.setSortable(false);

ArrayLine arrayLine = null; // declare line object of the array

browseBar.setExtraInformation(document.getName());

out.println(window.printBefore());
out.println(frame.printBefore());

boolean spinfireViewerEnable = attachmentSettings.getBoolean("SpinfireViewerEnable", false);

DocumentVersion publicVersion = null;
String url = null;
for (int i=0;i<vVersions.size();i++) {
	publicVersion = (DocumentVersion) vVersions.get(i);
    arrayLine = arrayPane.addArrayLine(); // set a new line

    url = versioningSC.getDocumentVersionURL(publicVersion.getLogicalName(),publicVersion.getDocumentPK().getId(), publicVersion.getPk().getId());

    if ("1".equals(fromAlias))
	{
		String contextFileServer = m_context+"/FileServer/";
		int index = url.indexOf(contextFileServer);
		url = m_context+"/AliasFileServer/"+url.substring(index+contextFileServer.length());
	}

    String spinFire = "";
    if (publicVersion.isSpinfireDocument() && spinfireViewerEnable)
    {
		spinFire = "<br><div id=\"switchView\" name=\"switchView\" style=\"display: none\">";
		spinFire += "<a title=\"Viewer SpinFire 3D\"href=\"#\" onClick=\"changeView3d(" + publicVersion.getPk().getId() + ")\"><img name= \"iconeView\" border=0 src=\"/util/icons/masque.gif\"></a>";
		spinFire += "</div>";
		spinFire += "<div id=\"" + publicVersion.getPk().getId() + "\" style=\"display: none\">";
		spinFire += "<OBJECT classid=\"CLSID:A31CCCB0-46A8-11D3-A726-005004B35102\"";
		spinFire += "width=\"300\" height=\"200\" id=\"XV\">";
		spinFire += "<PARAM NAME=\"ModelName\" VALUE=\"" + url + "\">";
		spinFire += "</OBJECT>";
		spinFire += "</div>";
    }
    arrayLine.addArrayCellText("<a href=\""+url+"\" target=\"_blank\"><img src=\""+versioningSC.getDocumentVersionIconPath(publicVersion.getPhysicalName())+"\" border=\"0\"/></a>");
	arrayLine.addArrayCellText("<a href=\""+url+"\" target=\"_blank\">"+publicVersion.getMajorNumber()+"."+publicVersion.getMinorNumber()+"</a>" + spinFire);
    arrayLine.addArrayCellText(versioningSC.getUserNameByID(publicVersion.getAuthorId()));

   	ArrayCellText cell = arrayLine.addArrayCellText(resources.getOutputDate(publicVersion.getCreationDate()));
   	cell.setNoWrap(true);

    arrayLine.addArrayCellText(publicVersion.getComments());

}

    out.println(arrayPane.print());
	out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>
<% if (spinfireViewerEnable) { %>
<script language="javascript">
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
				iconeView.src = '/util/icons/visible.gif';
				}
			else
			{
				document.getElementById(objectId).style.display = 'none';
				iconeView.src = '/util/icons/masque.gif';
			}
	}
</script>
<% } %>
