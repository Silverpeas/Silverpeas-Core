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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ include file="checkVersion.jsp" %>

<%
// declaration of labels !!!
      ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

      String documentId = request.getParameter("documentId");
      String returnURL = request.getParameter("ReturnURL");

      String documentPathLabel = messages.getString("newDocumentVersion");
      String versionTypeLabel = messages.getString("typeOfVersion");
      String commentsLabel = messages.getString("comments");
      String okLabel = messages.getString("ok");
      String nokLabel = messages.getString("cancel");
      String[] radioButtonLabel = {messages.getString("public"), messages.getString("archive")};
      String requiredFieldLabel = messages.getString("required");
      String pleaseFill = messages.getString("pleaseFill");
      String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

      String spaceId = request.getParameter("SpaceId");
      String componentId = request.getParameter("ComponentId");
      String publicationId = request.getParameter("Id");
      String hide_radio = request.getParameter("hide_radio");

      Form formUpdate = (Form) request.getAttribute("XMLForm");
      DataRecord data = (DataRecord) request.getAttribute("XMLData");
      String xmlFormName = (String) request.getAttribute("XMLFormName");
      PagesContext context = (PagesContext) request.getAttribute("PagesContext");
      if (context != null) {
        context.setBorderPrinted(false);
        context.setFormIndex("0");
        context.setCurrentFieldIndex("7");
      }
%>

<html>
  <title></title>
  <%
        out.println(gef.getLookStyleSheet());
  %>
  <head>
  	<script src="<%=m_context%>/versioningPeas/jsp/javaScript/dragAndDrop.js" type="text/javascript"></script>
    <script src="<%=m_context%>/util/javaScript/upload_applet.js" type="text/javascript"></script>
    <script type="text/javascript"  language="Javascript">
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
        document.addForm.radio.value = i;
      }


      function isFormFilled(){

        var isEmpty = false;
      <%
          if (!"true".equals(hide_radio)) {
      %>
          if ( (trim(document.addForm.file_upload.value) == "") ||
            (trim(document.addForm.radio.value) == "") )
      <%    } else {
      %>
          document.addForm.radio.value = "0";
        if ( (trim(document.addForm.file_upload.value) == "") )
      <%      }
      %>
        {
          isEmpty = true;
        }

      return isEmpty;
    }

    function addFile(){
      if (!isFormFilled() && isCorrectForm()){
        document.addForm.submit();
      } else {
        document.addForm.file_upload.value = '';
        alert("<%=pleaseFill%>");
      }
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
		String baseURL = httpServerBase+m_context+"/VersioningDragAndDrop/jsp/Drop?UserId="+m_MainSessionCtrl.getUserId()+"&ComponentId="+componentId+"&Id="+publicationId+"&IndexIt="+indexIt+"&DocumentId="+documentId;
		String publicURL 	= baseURL+"&Type="+DocumentVersion.TYPE_PUBLIC_VERSION;
		String workURL 		= baseURL+"&Type="+DocumentVersion.TYPE_DEFAULT_VERSION;
		%>
		showHideDragDrop('<%=publicURL%>','<%=httpServerBase + m_context%>/upload/VersioningPublic_<%=language%>.html','<%=workURL%>','<%=httpServerBase + m_context%>/upload/VersioningWork_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	}

	function uploadCompleted(s)
	{
		self.opener.location = '<%=returnURL%>';
		self.close();
	}
    </script>
    <% if (formUpdate != null) {%>
    <% formUpdate.displayScripts(out, context);%>
    <% } else {%>
    <script type="text/javascript">
  function isCorrectForm()
  {
    return true;
  }
    </script>
    <% }%>
  </head>
  <body class="yui-skin-sam">
    <%
          Board board = gef.getBoard();

          out.println(window.printBefore());
          out.println(frame.printBefore());
          
          if (dragAndDropEnable) { %>
          <table width="100%" border="0" id="DropZone">
      		<tr>
      			<td colspan="3" align="right">
          			<a href="javascript:showDnD()" id="dNdActionLabel"><%=resources.getString("GML.DragNDropExpand")%></a>
        		</td>
      		</tr>
      		<tr>
        		<td>
          			<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%" valign="top"><img alt=""border" src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
        		</td>
        		<td width="2%"><img alt="border"  src="<%=m_context%>/util/icons/colorPix/1px.gif" width="10px"/></td>
        		<td>
          			<div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding: 0px; width: 100%" valign="top"><img alt="border" src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
        		</td>
      		</tr>
    	  </table>
    	  <br/>
    	  <% } //end if dragAndDropEnable
          
          out.println(board.printBefore());
    %>
    <form name="addForm" action="<%=m_context%>/RVersioningPeas/jsp/SaveNewVersion" method="POST" enctype="multipart/form-data" accept-charset="UTF-8">
      <input type="hidden" name="documentId" value="<%=documentId%>"/>
      <input type="hidden" name="publicationId" value="<%=publicationId%>"/>
      <input type="hidden" name="ReturnURL" value="<%=returnURL%>"/>
      <input type="hidden" name="radio">

      <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="txtlibform">
            <%=documentPathLabel%> :
          </td>
          <td align=left valign="baseline">
            <input type="file" name="file_upload"> &nbsp;<img alt="<%=requiredFieldLabel%>"  border="0" src="<%=mandatoryField%>" width="5" height="5">
          </td>
        </tr>
        <%
              if (!"true".equals(hide_radio)) {
        %>
        <tr>
          <td class="txtlibform">
            <%=versionTypeLabel%> :
          </td>
          <td align=left valign="baseline">
            <%
                  // display radio button
                  for (int i = 0; i < radioButtonLabel.length; i++) {
                    out.println(" <input type=\"radio\" name=\"versionType\" onClick=\"onRadioClick(" + i + ")\"> " + radioButtonLabel[i]);
                  }
            %>
            &nbsp;<img alt="<%=requiredFieldLabel%>" border="0" src="<%=mandatoryField%>" width="5" height="5">
          </td>
        </tr>
        <%
              }
        %>
        <tr>
          <td class="txtlibform" valign="top"><%=commentsLabel%> :</td>
          <td align="left" valign="baseline">
            <textarea name="comments" rows="3" cols="70"></textarea>
          </td>
        </tr>
        <tr>
          <td colspan="2">(<img alt="<%=requiredFieldLabel%>" border="0" src="<%=mandatoryField%>" width="5" height="5">
            : <%=requiredFieldLabel%>)
          </td>
        </tr>
      </table>
      
      <%
            if (formUpdate != null) {
      %><br/>
      <table cellpadding="2" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="intfdcolor6outline"><span class="txtlibform"><%=messages.getString("versioning.xmlForm.XtraData")%></span></td>
        </tr>
      </table>
      <%formUpdate.display(out, context, data);
            }
      %>
    </form>
    <%
          out.println(board.printAfter());
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(gef.getFormButton(okLabel, "javascript:addFile()", false));
          buttonPane.addButton(gef.getFormButton(nokLabel, "javascript:window.close()", false));
          out.println("<br /><center>");
          out.println(buttonPane.print());
          out.println("</center>");
          out.println(frame.printAfter());
          out.println(window.printAfter());
    %>
  </body>
</html>