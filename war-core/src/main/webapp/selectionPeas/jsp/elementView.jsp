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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
	String[][] infos = (String[][])request.getAttribute("infos");
    String     contentText = (String)request.getAttribute("contentText");
    String[]   contentColumns = (String[])request.getAttribute("contentColumns");
    String[][] content = (String[][])request.getAttribute("content");
    String     action = (String)request.getAttribute("action");

    Board board = gef.getBoard();
%>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>
<BODY topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
<%
    StringBuffer outBuffer = new StringBuffer();
    for (int iInfo=0 ; iInfo<infos.length ; iInfo++) {
        if (infos[iInfo][0]!=null) {
            outBuffer.append("<tr>");
            outBuffer.append("	<td class='textePetitBold'>");
            outBuffer.append(EncodeHelper.javaStringToHtmlString(infos[iInfo][0]));
            outBuffer.append(" :");
            outBuffer.append("	</td>");
            outBuffer.append("	<td align=left valign='baseline'>");
            outBuffer.append(EncodeHelper.javaStringToHtmlString(infos[iInfo][1]));
            outBuffer.append("	</td>");
            outBuffer.append("</tr>");
        }
    }
    out.println(outBuffer);
%>
</table>
<%
    out.println(board.printAfter());

  if (content.length>0)
  {
	out.println("<br>");
    ArrayPane arrayPane = gef.getArrayPane("content", action, request, session);

    arrayPane.setVisibleLineNumber(15);
    arrayPane.setTitle(contentText);

    // D�finition des ent�tes de colonnes
    for (int c = 0; c < contentColumns.length; c++)
    {
        ArrayColumn arrayColumn = arrayPane.addArrayColumn(contentColumns[c]);
        arrayColumn.setSortable(true);
    }

    for(int i=0; i<content.length; i++){
      //cr�ation des ligne de l'arrayPane
      ArrayLine arrayLine = arrayPane.addArrayLine();
      for (int c = 0; c < contentColumns.length; c++)
      {
          arrayLine.addArrayCellText(content[i][c]);
      }
    }
    if (arrayPane.getColumnToSort() == 0)
      arrayPane.setColumnToSort(1);
    out.println(arrayPane.print());
  }
%>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close();", false));
	out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>