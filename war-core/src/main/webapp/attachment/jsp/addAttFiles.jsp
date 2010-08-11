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
<%@ include file="checkAttachment.jsp"%>
<%@ page import="com.stratelia.silverpeas.util.*"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>

<%
      //initialisation des variables
      String id = request.getParameter("Id");
      String componentId = request.getParameter("ComponentId");
      String context = request.getParameter("Context");
      String url = request.getParameter("Url");
      String indexIt = request.getParameter("IndexIt");

      //création du path
      String path = AttachmentController.createPath(componentId, context);

      Window window = gef.getWindow();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><%=attResources.getString("GML.popupTitle")%></title>
    <% out.println(gef.getLookStyleSheet());%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <script type="text/javascript" src="<%=m_Context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript">

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

      function isEmptyField(){

        var isEmpty = false;
        var field = trim(document.addForm.file_upload.value);
        if ( field == "" ){
          isEmpty = true;
        }
        return isEmpty;
      }

      function Attachment(action)
      {
        // verifie que la valeur du champ texte contenant le chemin du fichier n'est pas vide
        if (!isEmptyField()){
          if(action == "add"){
            var obj = document.getElementById("InProgress");
            if (obj != null)
              obj.style.visibility = "visible";
            document.addForm.submit();
          }
          if(action == "link"){
            document.linkForm.Path.value=document.addForm.file_upload.value;
            document.linkForm.Title.value=document.addForm.Title.value;
            document.linkForm.Description.value=document.addForm.Description.value;
            document.linkForm.submit();
          }
        }
        else {
      document.addForm.file_upload.value = ''    ;
          alert("<%=messages.getString("nomVide")%>");
        }
      }
    </script>
  </head>
  <body>
    <%
          Button toAdd = (Button) gef.getFormButton(attResources.getString("GML.add"), "javascript:Attachment('add')", false);

          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(toAdd);

          if (!runOnUnix()) {
            Button toLink = (Button) gef.getFormButton(messages.getString("lier"), "javascript:Attachment('link')", false);
            buttonPane.addButton(toLink);
          }

          Frame frame = gef.getFrame();
          Board board = gef.getBoard();

          out.println(frame.printBefore());
          out.println("<center>");
          out.println(board.printBefore());
    %>

	<form name="addForm" action="<%=m_Context%>/attachment/jsp/saveFile.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
    <table border="0" cellspacing="0" cellpadding="5" width="100%">
        <%=I18NHelper.getFormLine(attResources)%>
        <tr align="justify">
          <td class="txtlibform" nowrap="nowrap" align="left"><%=messages.getString("fichierJoint")%> :</td>
          <td><input type="file" name="file_upload" size="60" class="INPUT"/></td>
        </tr>
        <tr>
          <td class="txtlibform" nowrap="nowrap" align="left"><%=messages.getString("Title")%> :</td>
          <td><input type="text" name="Title" size="60"/></td>
        </tr>
        <tr>
          <td class="txtlibform" nowrap="nowrap" align="left" valign="top"><%=attResources.getString("GML.description")%> :</td>
          <td><textarea name="Description" cols="60" rows="3"></textarea></td>
        </tr>
        <tr>
          <td align="center" colspan="4" class="txtlibform">
            <div id="InProgress" style="visibility:hidden">
              <%=messages.getString("downloadInProgress")%><br />
              <img alt="upload" src="<%=m_Context%>/util/icons/attachment_to_upload.gif" height="20" width="83" />
            </div>
          </td>
        </tr>
    </table>
    </form>
    <%
          out.println(board.printAfter());
          out.println("<br/>" + buttonPane.print());
          out.println("</center>");
          out.println(frame.printAfter());
    %>
    <form name="linkForm" action="<%=m_Context%>/attachment/jsp/saveLink.jsp" method="post">
      <input type="hidden" name="Id" value="<%=id%>"/>
      <input type="hidden" name="ComponentId" value="<%=componentId%>"/>
      <input type="hidden" name="Context" value="<%=context%>"/>
      <input type="hidden" name="Url" value="<%=url%>"/>
      <input type="hidden" name="Title"/>
      <input type="hidden" name="Description"/>
    </form>
  </body>
</html>