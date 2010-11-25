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

<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%@ include file="checkVersion.jsp" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

    Document 	document 	= (Document) request.getAttribute("Document");
    List 		vVersions 	= (List) request.getAttribute("Versions");
    boolean		fromAlias	= StringUtil.getBooleanValue((String) request.getAttribute("Alias"));

    String componentId = document.getPk().getInstanceId();
    String id = document.getPk().getId();
%>

<%@page import="com.stratelia.webactiv.util.FileServerUtils"%><html>
<head>
<TITLE><%=messages.getString("popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
ArrayPane arrayPane = gef.getArrayPane("List", "ViewAllVersions?DocId="+id+"&Alias=null&ComponentId="+componentId, request,session);// declare an array

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

    if (fromAlias) {
		url = FileServerUtils.getAliasURL(componentId, publicVersion.getLogicalName(),publicVersion.getDocumentPK().getId(), publicVersion.getPk().getId());
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
    
    String permalink = " <a href=\""+URLManager.getSimpleURL(URLManager.URL_VERSION, publicVersion.getPk().getId())+"\"><img src=\""+m_context+"/util/icons/link.gif\" border=\"0\" valign=\"absmiddle\" alt=\""+messages.getString("versioning.CopyLink")+"\" title=\""+messages.getString("versioning.CopyLink")+"\" target=\"_blank\"></a> ";
    arrayLine.addArrayCellText("<a href=\""+url+"\" target=\"_blank\"><img src=\""+versioningSC.getDocumentVersionIconPath(publicVersion.getPhysicalName())+"\" border=\"0\"/></a>");
	arrayLine.addArrayCellText("<a href=\""+url+"\" target=\"_blank\">"+publicVersion.getMajorNumber()+"."+publicVersion.getMinorNumber()+"</a>" + permalink + spinFire);
    arrayLine.addArrayCellText(versioningSC.getUserNameByID(publicVersion.getAuthorId()));

   	ArrayCellText cell = arrayLine.addArrayCellText(resources.getOutputDate(publicVersion.getCreationDate()));
   	cell.setNoWrap(true);
   	
   	String xtraData = "";
  	if (StringUtil.isDefined(publicVersion.getXmlForm()))
  	{
		String xmlURL = m_context+"/RformTemplate/jsp/View?ObjectId="+publicVersion.getPk().getId()+"&ComponentId="+componentId+"&ObjectType=Versioning&XMLFormName="+URLEncoder.encode(publicVersion.getXmlForm());
		xtraData = "<a rel=\""+xmlURL+"\" href=\"#\" title=\""+document.getName()+" "+publicVersion.getMajorNumber()+"."+publicVersion.getMinorNumber()+"\"><img src=\""+m_context+"/util/icons/info.gif\" border=\"0\"></a> ";
	}

    arrayLine.addArrayCellText(xtraData+publicVersion.getComments());

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
            width: 350 // Set the tooltip width
         }
      })
   });
});
</script>