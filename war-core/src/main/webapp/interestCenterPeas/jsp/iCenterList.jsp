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

<%@ page import="java.util.ArrayList,
                 com.silverpeas.interestCenter.model.InterestCenter,
                 java.util.Vector,
                 java.util.Iterator,
                 com.stratelia.silverpeas.peasCore.URLManager,
                 java.net.URLEncoder"%>
<%@ include file="checkICenter.jsp" %>
<%

    String iconEdit		= m_context+"/util/icons/update.gif";
    String iconAdd		= resource.getIcon("icoAddNew");
    String iconDelete	= resource.getIcon("icoDelete");
    String path			= resource.getString("Path");

    ArrayList iCentersList = (ArrayList)request.getAttribute("icList");

    if ( iCentersList == null ){
       iCentersList = new ArrayList();
    }

%>
<html>
<head>
<%
    out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script>
function editICenter(id) {
        chemin = '<%=m_context%>/RpdcSearch/jsp/LoadAdvancedSearch?showNotOnlyPertinentAxisAndValues=true&iCenterId='+id;
		largeur = "600";
		hauteur = "440";
		SP_openWindow(chemin,"",largeur,hauteur,"resizable=yes,scrollbars=yes");
}

function newICenter() {
        chemin = '<%=m_context%>/RpdcSearch/jsp/GlobalView?showNotOnlyPertinentAxisAndValues=true&mode=clear';
		largeur = "600";
		hauteur = "440";
		SP_openWindow(chemin,"",largeur,hauteur,"resizable=yes,scrollbars=yes");
}

function deleteICenter() {
  document.readForm.mode.value = 'delete';
  document.readForm.submit();
}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" class="txtlist">
<form name="readForm" action="iCenterList" method="POST">
<input type="hidden" name="mode">
 <%
      browseBar.setComponentName(path);

      operationPane.addOperation(iconAdd , resource.getString("AddICenter"),"javascript:newICenter()");
      operationPane.addOperation(iconDelete , resource.getString("DeleteICenter"),"javascript:deleteICenter()");

      out.println(window.printBefore());
      out.println(frame.printBefore());
      ArrayPane arrayPane = gef.getArrayPane("tableau1", "iCenterList.jsp", request, session);

      arrayPane.addArrayColumn(resource.getString("ICName"));
      ArrayColumn arrayColumn0 = arrayPane.addArrayColumn(resource.getString("Operations"));
	  arrayColumn0.setSortable(false);

      Iterator i = iCentersList.iterator();
      ArrayLine ligne;
      IconPane iconPane;
      Icon updateIcon;
      String link = m_context+"/RpdcSearch/jsp/AdvancedSearch?urlToRedirect=" +
              URLEncoder.encode(m_context + URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS)+ "iCenterList.jsp") + "&icId=";

	  InterestCenter ic = null;
      while (i.hasNext()) {
         ic		= (InterestCenter)  i.next();
         ligne	= arrayPane.addArrayLine();
         
         iconPane	= gef.getIconPane();
         updateIcon = iconPane.addIcon();
         updateIcon.setProperties(iconEdit, resource.getString("EditICenter") , "javascript:onClick=editICenter('" + ic.getId() + "')");

		 ligne.addArrayCellLink(ic.getName(), link + ic.getId());
         ligne.addArrayCellText(updateIcon.print()+"&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"icCheck\" value=\""+ic.getId()+"\">");
      }

  out.println(arrayPane.print());
  out.println(frame.printAfter());
  out.println(window.printAfter());
 %>

</form>
</body>
</html>