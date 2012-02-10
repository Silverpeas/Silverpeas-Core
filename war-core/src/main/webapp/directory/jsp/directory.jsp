<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination"%>
<%@page import="java.util.List"%>
<%@page import="com.silverpeas.directory.model.UserFragmentVO"%>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    List fragments = (List) request.getAttribute("UserFragments");
    Pagination pagination = (Pagination) request.getAttribute("pagination");
    String breadcrumb = (String) request.getAttribute("BreadCrumb");
    String query = (String) request.getAttribute("Query");
    if (!StringUtil.isDefined(query)) {
      query = "";
    } else {
      query = EncodeHelper.javaStringToHtmlString(query);
    }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  	<title></title>
    <view:looknfeel />
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
      
      function doPagination(page) {
    	  $.progressMessage();
    	  location.href="Pagination?Index="+page;
      }
      
      function isTermOK(term) {
    	  var firstCharacter = term.substring(0,1);
    	  // Lucene cannot parse a query starting with '*' or '?'. This characters are not allowed as first character in WildcardQuery.
    	  return (firstCharacter != "*" && firstCharacter != "?");
      }

      function search() {
    	  var query = $("#searchField").val();
    	  var terms = query.split(" ");
    	  var queryOK = true;
    	  for (i=0; queryOK && i<terms.length; i++) {
    		  queryOK = isTermOK(terms[i]);
    	  }
    	  if (!queryOK) {
    		  $( "#dialog-message" ).dialog( "open" );
    	  } else {
    	  	$.progressMessage();
    	  	document.search.submit();
    	  }
      }
      
      $(function() {
  		$("#dialog-message" ).dialog({
  			modal: true,
  			autoOpen: false,
  			width: 350,
  			resizable: false,
  			buttons: {
  				Ok: function() {
  					$( this ).dialog( "close" );
  				}
  			}
  		});
  		
  		$("#searchField").keypress(function(e){
			if(e.which == 13){
				e.preventDefault();
  	    		search();
  	    		return false;
  	      	}
  	     });

  	});
    </script>

  </head>
  <body id="directory">
  	<view:browseBar extraInformations="<%=breadcrumb %>"/>
    <view:window>
      <view:frame>
        <div id="indexAndSearch">
          <div id="search">
            <form name="search" action="searchByKey" method="post">
            	<input type="text" name="key" id="searchField" size="40" maxlength="60" value="<%=query%>"/> 
            	<fmt:message key="GML.search" var="buttonLabel"/>
            	<view:button label="${buttonLabel}" action="javascript:search()"/>
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
                  UserFragmentVO fragment = (UserFragmentVO) fragments.get(i);
            %>
            <li class="intfdcolor" id="user-<%=fragment.getUserId() %>">
                 <%=fragment.getFragment() %>
                 <br clear="all" />
            </li>
            <%
                }
            %>
          </ol>
          <div id="pagination">
            <%=pagination.printIndex("doPagination")%>
          </div>
        </div>
      </view:frame>

    </view:window>

	<%@include file="../../socialNetwork/jsp/notificationDialog.jsp" %>
	<%@include file="../../socialNetwork/jsp/invitationDialog.jsp" %>
	
	<view:progressMessage/>
	
	<div id="dialog-message" title="<fmt:message key="directory.query.error.title"/>">
		<fmt:message key="directory.query.error.msg"/>
	</div>
  </body>
</html>