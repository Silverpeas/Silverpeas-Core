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
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<BODY topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" SCROLL=YES>

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
            outBuffer.append(Encode.javaStringToHtmlString(infos[iInfo][0]));
            outBuffer.append(" :");
            outBuffer.append("	</td>");
            outBuffer.append("	<td align=left valign='baseline'>");
            outBuffer.append(Encode.javaStringToHtmlString(infos[iInfo][1]));
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

    // Définition des entêtes de colonnes
    for (int c = 0; c < contentColumns.length; c++)
    {
        ArrayColumn arrayColumn = arrayPane.addArrayColumn(contentColumns[c]);
        arrayColumn.setSortable(true);
    }

    for(int i=0; i<content.length; i++){
      //création des ligne de l'arrayPane
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