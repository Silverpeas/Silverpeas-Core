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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination"%>
<%@page import="java.util.List"%>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    List fragments = (List) request.getAttribute("UserFragments");
    Pagination pagination = (Pagination) request.getAttribute("pagination");
%>


<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  	<title></title>
    <view:looknfeel />
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript">
      function OpenPopup(userId, name){
    	initNotification(userId, name);
      }
      
      function OpenPopupInvitaion(userId,name){
		initInvitation(userId,name);
      }

      function viewIndex(index) {
          $.progressMessage();
          location.href=index;
      }

      function search() {
    	  $.progressMessage();
    	  document.search.submit();
      }
    </script>

  </head>
  <body id="directory">
    <view:window>
    	<view:browseBar extraInformations="Annuaire"/>
      <view:frame>
        <div id="indexAndSearch">
          <div id="search">
            <form name="search" action="searchByKey" method="post" onsubmit="$.progressMessage()">
            	<input type="text" name="key" size="40" maxlength="60" />
            	<!-- Ajout d'un bouton rechercher -->
                   <table cellspacing="0" cellpadding="0" border="0">
                        <tbody><tr>
                            <td align="left" class="gaucheBoutonV5"><img alt="" src="/silverpeas/util/viewGenerator/icons/px.gif"/></td>
                            <td nowrap="nowrap" class="milieuBoutonV5"><a href="javascript:search()"><fmt:message key="GML.search" /></a></td>
                            <td align="right" class="droiteBoutonV5"><img alt="" src="/silverpeas/util/viewGenerator/icons/px.gif"/></td>
                         </tr></tbody>
                    </table>
            </form>
          </div>
          <div id="index">
            <%
                // afficher la bande d'index alphabetique
                String para = (String) request.getAttribute("View");
				if (!StringUtil.isDefined(para)) {
				  para = "tous";
				}
				String indexCSS = "";
                for (char i = 'A'; i <= 'Z'; ++i) {
                  if (String.valueOf(i).equals(para)) {
                    indexCSS = "class=\"active\"";
                  }
                  %>
                  <a <%=indexCSS%> href="javascript:viewIndex('<%=i%>')"><%=i%></a>
                  <%
                  indexCSS = "";
                }
                out.println(" - ");
                if ("tous".equals(para)) {
                  indexCSS = "class=\"active\"";
                }
                %>
                <a <%=indexCSS%> href="javascript:viewIndex('tous')"><fmt:message key="directory.scope.all" /></a>
                <%
                out.println(" - ");
                indexCSS = "";
                if ("connected".equals(para)) {
                  indexCSS = "class=\"active\"";
                }
                %>
                <a <%=indexCSS%> href="javascript:viewIndex('connected')"><fmt:message key="directory.scope.connected" /></a>
         </div>          
        </div>
        <div id="users">
          <ol class="message_list aff_colonnes">
            <%
                for (int i = 0; i < fragments.size(); i++) {
                  String fragment = (String) fragments.get(i);
            %>
            <li class="intfdcolor">
                 <%=fragment %>
                 <br clear="all" />
            </li>
            <%
                }
            %>
          </ol>
          <div id="pagination">
            <%=pagination.printIndex()%>
          </div>
        </div>
      </view:frame>

    </view:window>

	<%@include file="../../socialNetwork/jsp/notificationDialog.jsp" %>
	<%@include file="../../socialNetwork/jsp/invitationDialog.jsp" %>
	
	<view:progressMessage/>
  </body>
</html>