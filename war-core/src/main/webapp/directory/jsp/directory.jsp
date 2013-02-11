<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@page import="com.silverpeas.directory.control.DirectorySessionController"%>
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
    List<UserFragmentVO> fragments = (List<UserFragmentVO>) request.getAttribute("UserFragments");
    Pagination pagination = (Pagination) request.getAttribute("pagination");
    String breadcrumb = (String) request.getAttribute("BreadCrumb");
    String query = (String) request.getAttribute("Query");
    if (!StringUtil.isDefined(query)) {
      query = "";
    } else {
      query = EncodeHelper.javaStringToHtmlString(query);
    }
    String sort = (String) request.getAttribute("Sort");
    int scope = (Integer) request.getAttribute("Scope");
    String para = (String) request.getAttribute("View");
	if (!StringUtil.isDefined(para)) {
	  para = "tous";
	}
	boolean showHelp = (Boolean) request.getAttribute("ShowHelp");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  	<title></title>
    <view:looknfeel />
    <view:includePlugin name="invitme"/>
    <view:includePlugin name="messageme"/>
    <view:includePlugin name="popup"/>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.cookie.js"></script>
    <script type="text/javascript">
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
      
      function sort(val) {
    	  $.progressMessage();
    	  location.href="Sort?Type="+val;
      }
      
      function showAutoHelp() {
      	var helpCookieName = "Silverpeas_Directory_Help";
      	var helpCookieValue = $.cookie(helpCookieName);
      	if ("IKnowIt" != helpCookieValue) {
      		$( "#help-message" ).dialog({
      			modal: true,
      			resizable: false,
      			width: 570,
      			dialogClass: 'help-modal-message',
      			buttons: {
      				"<fmt:message key="GML.help.cookie.buttons.ok" />": function() {
      					$.cookie(helpCookieName, "IKnowIt", { expires: 3650, path: '/' });
      					$( this ).dialog( "close" );
      				},
      				"<fmt:message key="GML.help.cookie.buttons.remind" />": function() {
      					$( this ).dialog( "close" );
      				}
      			}
      		});
      	}
      }
      
      function showHelp() {
    	  $('#help-message').popup('help', {
    		  title: "<fmt:message key="directory.help.title"/>"
    	  });
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
  		
  		<% if (showHelp) { %>
  			showAutoHelp();
  		<% } %>
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
            <span id="help"><a href="javascript:onclick=showHelp()"><fmt:message key="GML.help" /></a></span>
          </div>
          <div id="index">
            <%
                // afficher la bande d'index alphabetique
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
            <% if ((scope == DirectorySessionController.DIRECTORY_DEFAULT || scope == DirectorySessionController.DIRECTORY_DOMAIN) && !DirectorySessionController.SORT_PERTINENCE.equals(sort)) { %>
            <div id="sort">
                <fmt:message key="directory.sort" />
                <%
                	indexCSS = "";
                	if (DirectorySessionController.SORT_ALPHA.equals(sort)) {
                	  indexCSS = "class=\"active\"";
                	}
                %>
                <a <%=indexCSS%> href="javascript:sort('<%=DirectorySessionController.SORT_ALPHA%>')"><fmt:message key="directory.sort.alpha" /></a> -  
                <%
                	indexCSS = "";
                	if (DirectorySessionController.SORT_NEWEST.equals(sort)) {
                	  indexCSS = "class=\"active\"";
                	}
                %>
                <a <%=indexCSS%> href="javascript:sort('<%=DirectorySessionController.SORT_NEWEST%>')"><fmt:message key="directory.sort.newest" /></a>
        	</div>
        	<% } %>
        </div>
        <div id="users">
          <ol class="message_list aff_colonnes">
            <% for (UserFragmentVO fragment : fragments) { %>
	            <li class="intfdcolor" id="user-<%=fragment.getUserId() %>">
	                 <%=fragment.getFragment() %>
	                 <br clear="all" />
	            </li>
            <% } %>
          </ol>
          <div id="pagination">
            <%=pagination.printIndex("doPagination")%>
          </div>
        </div>
      </view:frame>

    </view:window>
	
	<view:progressMessage/>
	
	<div id="dialog-message" title="<fmt:message key="directory.query.error.title"/>">
		<fmt:message key="directory.query.error.msg"/>
	</div>
	
	<div id="help-message" title="<fmt:message key="directory.help.title"/>" style="display: none;" class="help-modal-message">
		<view:applyTemplate locationBase="core:directory" name="help" />
	</div>
  </body>
</html>