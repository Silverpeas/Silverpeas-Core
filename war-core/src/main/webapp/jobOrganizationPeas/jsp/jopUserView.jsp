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

<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();
    String userId = (String)request.getAttribute("userid");
%>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
function showPDCSubscription() {
        chemin = '<%=(m_context + URLManager.getURL(URLManager.CMP_PDCSUBSCRIPTION))%>showUserSubscriptions.jsp?userId=<%=userId%>';
                largeur = "600";
                hauteur = "440";
                SP_openWindow(chemin,"",largeur,hauteur,"resizable=yes,scrollbars=yes");
}


MM_reloadPage(true);
//-->
</script>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<BODY>

<%
operationPane.addOperation(resource.getIcon("JOP.userPanelAccess"),resource.getString("JOP.select"),"Main");
if (userId != null && userId != "") {
    operationPane.addOperation(resource.getIcon("PDCSubscription.subscriptions"),resource.getString("PDCSubscription.show"),"javascript:showPDCSubscription()");
}
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
<%
        // User or group infos
        String[][] infos = (String[][])request.getAttribute("infos");
        if (infos != null && infos.length>0) {

                StringBuffer outBuffer = new StringBuffer();
                for (int iInfo=0 ; iInfo<infos.length ; iInfo++) {
                        if (infos[iInfo][0]!=null) {
                                outBuffer.append("<tr>");
                                outBuffer.append("      <td class='textePetitBold'>");
                                if (infos[iInfo][0].startsWith("GML.") || infos[iInfo][0].startsWith("JOP."))
                                        outBuffer.append(Encode.javaStringToHtmlString(resource.getString(infos[iInfo][0])));
                                else
                                        outBuffer.append(Encode.javaStringToHtmlString(infos[iInfo][0]));
                                outBuffer.append(" :");
                                outBuffer.append("      </td>");
                                outBuffer.append("      <td align=left valign='baseline'>");
                                if (infos[iInfo][1]!=null
                                        && (infos[iInfo][1].startsWith("GML.")
                                                || infos[iInfo][1].startsWith("JOP.")))
                                        outBuffer.append(Encode.javaStringToHtmlString(resource.getString(infos[iInfo][1])));
                                else
                                        outBuffer.append(Encode.javaStringToHtmlString(infos[iInfo][1]));
                                outBuffer.append("      </td>");
                                outBuffer.append("</tr>");
                        }
                }
                out.println(outBuffer);
        } else {
                out.println("<tr><td class='textePetitBold'>");
                out.println(resource.getString("JOP.noSelection"));
                out.println("</td></tr>");
        }
%>
</table>
<%
out.println(board.printAfter());

  // Groups (only for user and not for group view)
  String[][] groups = (String[][])request.getAttribute("groups");
  if (groups != null && groups.length>0)
  {
        out.println("<br>");
    ArrayPane arrayPane = gef.getArrayPane("groups", "ViewUserOrGroup", request, session);

    arrayPane.setVisibleLineNumber(-1);
    arrayPane.setTitle(resource.getString("JOP.groups"));

    arrayPane.addArrayColumn(resource.getString("GML.name"));
    arrayPane.addArrayColumn(resource.getString("GML.users"));
    arrayPane.addArrayColumn(resource.getString("GML.description"));

    for(int i=0; i<groups.length; i++){
      //cr�ation des ligne de l'arrayPane
      ArrayLine arrayLine = arrayPane.addArrayLine();
      arrayLine.addArrayCellText(groups[i][1]);
      arrayLine.addArrayCellText(groups[i][2]);
      arrayLine.addArrayCellText(groups[i][3]);
    }
    if (arrayPane.getColumnToSort() == 0)
      arrayPane.setColumnToSort(1);
    out.println(arrayPane.print());
  }

  // Manageable Spaces
  String[] spaces = (String[])request.getAttribute("spaces");
  if (spaces != null && spaces.length>0)
  {
        out.println("<br>");
    ArrayPane arrayPane = gef.getArrayPane("spaces", "ViewUserOrGroup", request, session);

    arrayPane.setVisibleLineNumber(-1);
    arrayPane.setTitle(resource.getString("JOP.spaces"));

    arrayPane.addArrayColumn(resource.getString("GML.name"));

    for(int i=0; i<spaces.length; i++){
      //cr�ation des ligne de l'arrayPane
      ArrayLine arrayLine = arrayPane.addArrayLine();
      arrayLine.addArrayCellText(spaces[i]);
    }
    if (arrayPane.getColumnToSort() == 0)
      arrayPane.setColumnToSort(1);
    out.println(arrayPane.print());
  }

  // Instances and roles sorted by spaces
  List profiles = (List)request.getAttribute("profiles");
  if (profiles != null && profiles.size()>0)
  {
    out.println("<br/>");
    ArrayPane arrayPane = gef.getArrayPane("profiles", "ViewUserOrGroup", request, session);

    arrayPane.setVisibleLineNumber(-1);
    arrayPane.setTitle(resource.getString("JOP.profiles"));

    arrayPane.addArrayColumn(resource.getString("GML.domains"));
    arrayPane.addArrayColumn(resource.getString("JOP.instance"));
    arrayPane.addArrayColumn(resource.getString("GML.jobPeas"));
    arrayPane.addArrayColumn(resource.getString("JOP.profile"));

    String[] profile = null;
    for(int i=0; i<profiles.size(); i++)
    {
    	profile = (String[]) profiles.get(i);
      
    	//cr�ation des ligne de l'arrayPane
      	ArrayLine arrayLine = arrayPane.addArrayLine();
      	arrayLine.addArrayCellText(profile[0]);
      	arrayLine.addArrayCellText(profile[1]);
      	arrayLine.addArrayCellText(profile[2]);
      	arrayLine.addArrayCellText(profile[3]);
    }
    if (arrayPane.getColumnToSort() == 0)
      arrayPane.setColumnToSort(1);
    out.println(arrayPane.print());
  }


%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>