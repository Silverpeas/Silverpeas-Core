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

<%@ page import="org.silverpeas.core.pdc.interests.model.Interests,
                 java.net.URLEncoder,
                 java.util.ArrayList,
                 java.util.List"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkICenter.jsp" %>
<%

    String iconEdit		= m_context+"/util/icons/update.gif";
    String iconAdd		= resource.getIcon("icoAddNew");
    String iconDelete	= resource.getIcon("icoDelete");
    String path			= resource.getString("Path");

    List<Interests> iCentersList = (ArrayList<Interests>)request.getAttribute("icList");

    if ( iCentersList == null ){
       iCentersList = new ArrayList<Interests>();
    }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript">
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
<body class="txtlist">
<form name="readForm" action="iCenterList" method="post">
<input type="hidden" name="mode"/>
 <%
      browseBar.setComponentName(path);

      operationPane.addOperationOfCreation(iconAdd , resource.getString("AddICenter"),"javascript:newICenter()");
      operationPane.addOperation(iconDelete , resource.getString("DeleteICenter"),"javascript:deleteICenter()");

      out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
      ArrayPane arrayPane = gef.getArrayPane("tableau1", "iCenterList.jsp", request, session);

      arrayPane.addArrayColumn(resource.getString("ICName"));
      ArrayColumn arrayColumn0 = arrayPane.addArrayColumn(resource.getString("Operations"));
	  arrayColumn0.setSortable(false);

      ArrayLine ligne;
      IconPane iconPane;
      Icon updateIcon;
      String link = m_context+"/RpdcSearch/jsp/AdvancedSearch?urlToRedirect=" +
              URLEncoder.encode(m_context + URLUtil.getURL(URLUtil.CMP_INTERESTCENTERPEAS)+ "iCenterList.jsp") + "&icId=";

	  for (Interests ic : iCentersList) {
         ligne	= arrayPane.addArrayLine();

         iconPane	= gef.getIconPane();
         updateIcon = iconPane.addIcon();
         updateIcon.setProperties(iconEdit, resource.getString("EditICenter") , "javascript:onClick=editICenter('" + ic.getId() + "')");

		 ligne.addArrayCellLink(ic.getName(), link + ic.getId());
         ligne.addArrayCellText(updateIcon.print()+"&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"icCheck\" value=\""+ic.getId()+"\"/>");
      }

  out.println(arrayPane.print());
%>
</view:frame>
<%
  out.println(window.printAfter());
%>
</form>
</body>
</html>