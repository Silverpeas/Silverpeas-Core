<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@ page import="org.silverpeas.core.web.http.HttpRequest"%>
<%@ page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button "%>
<%@ page import="java.io.File" %>
<%@ page import="org.silverpeas.core.util.file.FileUploadUtil" %>

<%
  GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    String m_context = URLUtil.getApplicationURL();
	String language = request.getParameter("Language");
	String thePath = request.getParameter("Path");
    LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.wysiwyg.multilang.wysiwygBundle",
        language);

    //Le cadre
    Board board = gef.getBoard();

    //Icons
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
    HttpRequest httpRequest = HttpRequest.decorate(request);
    if (httpRequest.isContentInMultipart())
    {
	    FileItem fileItem = httpRequest.getSingleFile();
	    if (fileItem != null)
	    {
			String fichierName = FileUploadUtil.getFileName(fileItem);
			File fichier = new File(thePath, fichierName);
			FileUploadUtil.saveToFile(fichier, fileItem);

			String urlPath = thePath.substring(thePath.indexOf("/website"));

			%>
			<script language="javascript">
				window.opener.document.getElementById('txtUrl').value='<%=EncodeHelper.javaStringToJsString(urlPath+'/'+fichierName)%>';
				window.close();
			</script>
			<%
	    }
    }
%>
<view:script src="/util/javaScript/checkForm.js"/>
	<script type="text/javascript">
		function isCorrect(nom) {
		if (nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
		        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || nom.indexOf("'")>-1 ||
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 ||
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("^")>-1 ||
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || /*nom.indexOf("�")>-1 ||*/
		        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 ||
		        nom.indexOf("�")>-1) {
		    }
		    return true;
	     }
	    function B_VALIDER_ONCLICK() {
	        if (checkString(document.descriptionFile.fichier, "<%=message.getString("ErreurmPrefix")+message.getString("FichierUpload")+message.getString("ErreurmSuffix")%>")) {
	            var file = document.descriptionFile.fichier.value;
	            var indexPoint = file.lastIndexOf(".");
	            var ext = file.substring(indexPoint + 1);
	            if (ext.toLowerCase() != "gif" && ext.toLowerCase()!= "jpg" &&
				ext.toLowerCase() != "bmp" && ext.toLowerCase() != "png" &&
				ext.toLowerCase() != "pcd" && ext.toLowerCase() != "tga" &&
				ext.toLowerCase() != "tif" && ext.toLowerCase() != "swf")
	            {
			alert("<%=message.getString("ErreurFichierUpload")%>");
	            }
	            else if (!isCorrect(file))
	            {
				// verif caract�res speciaux contenus dans le nom du fichier
			alert("<%=message.getString("NameFile")%> <%=message.getString("MustNotContainSpecialChar")%>\n<%=EncodeHelper.javaStringToJsString(message.getString("Char7"))%>\n");
	            }
	            else
		        {
			document.descriptionFile.submit();
	            }
	       }
	    }
	</script>

<% out.println(board.printBefore()); %>

	  <form name="descriptionFile" action="uploadFile.jsp" method="post" enctype="multipart/form-data">
		<table>
	    <tr>
	        <td class="txtlibform"><%=message.getString("FichierUpload")%> : </td>
	        <td valign="top">&nbsp;<input type="file" name="fichier" size="50"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5" alt=""/></td>
	    </tr>
        <tr>
            <td colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5" alt=""/> : <%=message.getString("RequiredField")%>)</td>
        </tr>
        </table>
	</form>
  <%
  out.println(board.printAfter());

	//fin du code
	ButtonPane buttonPane = gef.getButtonPane();
	Button validerButton = (Button) gef.getFormButton(message.getString("Valider"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
	buttonPane.addButton(validerButton);

  String bodyPart ="<center><br/>";
  bodyPart += buttonPane.print();
  bodyPart +="</center>";
	out.println(bodyPart);

 %>