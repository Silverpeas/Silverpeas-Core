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
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.silverpeas.directory.model.Member"%>
<%@page import="java.util.List"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="java.io.File"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"
	var="LML" />
<view:setBundle
	basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML" />
<c:set var="browseContext" value="${requestScope.browseContext}" />



<%
  String m_context = GeneralPropertiesManager
					.getGeneralResourceLocator().getString("ApplicationURL");
			List members = (List) request.getAttribute("Members");
			String myid = (String) request.getAttribute("MyId");
%>

<html>
<head>
<view:looknfeel />



<style type="text/css">
* {
	margin: 0;
	padding: 0;
}

/* message display page */
.message_list {
	list-style: none;
	margin: 0;
	padding: 0;
	width: 100%;
}

.message_list li {
	padding: 0px;
	margin: 3px;
}

.message_table rd {
	padding: 0px;
	margin: 3px;
}

#recherche {
	background-image:
		url(<%=m_context%>/admin/jsp/icons/silverpeasV5/recherche.jpg);
	background-repeat: no-repeat;
}

.index {
	color: black;
	font-family: sans-serif;
	font-size: 12px;
	font-weight: bold;
	text-decoration: none;
}

.active {
	color: blue;
	font-family: sans-serif;
	font-size: 12px;
	font-weight: bold;
	text-decoration: none;
}

.mail {
	text-decoration: underline;
	color: blue;
}

.link {
	color: blue;
}

.NameLink {
	font-size: 14px;
	color: blue;
}

#indexandsearch {
	background: none repeat scroll 0 0 pink;
	height: 35px;
	width: 100%;
}

#index {
	float: left;
	padding: 10px;
	text-align: center;
	width: 500px;
}

#search {
	float: right;
	margin: 2px;
	padding: 5px;
	text-align: center;
	width: 300px;
}

#principale {
	background-color: purple;
}

#photo {
	float: left;
	margin: 2px;
	padding: 5px;
}

#information {
	border: medium none;
	float: left;
	padding: 10px;
	text-align: left;
	width: 500px;
}

#connect {
}

#disconnect{
}
.size{
width : 10px;
height : 10px;
}

#invitandmessage {
	float: right;
	margin: 10px;
	padding: 14px;
	text-align: center;
	width: 300px;
}
</style>
<script type="text/javascript"
	src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript"
	src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">


    function OpenPopup(usersId,name ){
              
      usersId=usersId+'&Name='+name
          options="location=no, menubar=no,toolbar=no,scrollbars=yes, resizable        , alwaysRaised"
      SP_openWindow('<%=m_context + "/Rdirectory/jsp/NotificationView"%>?Recipient='+usersId , 'strWindowName', '500', '200',options );

    }
    function OpenPopupInvitaion(usersId,name){
      usersId=usersId+'&Name='+name
          options="directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no        , alwaysRaised"
      SP_openWindow('<%=m_context + "/Rinvitation/jsp/invite"%>?Recipient='+usersId, 'strWindowName', '350', '200',options);
    }




  </script>

</head>




<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">
<%--  <a href="${GroupUrl}" >Annuiare</a>--%>
<c:url value="/RprofilPublic/ProfilPublic" var="profilPublic" />

<%--<view:browseBar extraInformations="Annuaire Interne"></view:browseBar>--%>

<view:window>

	<view:frame>

		<view:board>
			<ol class="message_list">
				<div id="indexandsearch">
				<div id="index">
				<%
				  // afficher la bande d'index alphabetique
										String para = (String) request.getAttribute("Index");

										for (char i = 'A'; i <= 'Z'; ++i) {

											if (para != null && para.equals(String.valueOf(i))) {
												out.println("<a class=\"active\" href=\"" + i
														+ "\">" + i + "</a>");
											} else {
												out.println("<a class=\"index\" href=\"" + i
														+ "\">" + i + "</a>");
											}
										}
										if (para != null && para.equals("tous")) {
											out
													.println("<a class=\"active\" href=\"tous\">Tous</a>");
										} else {
											out
													.println("<a class=\"index\" href=\"tous\">Tous</a>");
										}
				%>
				</div>
				<div id="search">
				<form name="search" action="searchByKey" method="post"><input
					type="text" name="key" size="40" maxlength="60"
					style="height: 20px" /> <img
					src="<%=m_context%>/directory/jsp/icons/connected.jpg" width="10"
					height="10" alt="advsearch" /> <%--</td>--%> <%--<td><a href="searchByKey"></a></td>--%>
				</form>
				</div>

				</div>
				<%
				  for (int i = 0; i < members.size(); i++) {
											Member member = (Member) members.get(i);
				%>

				<li><view:board>

					<div id="photo"><a href="createPhoto"><img
						src="<%=m_context + member.getProfilPhoto()%>" width="60"
						height="70" border="0" alt="viewUser" /> </a></div>
					<div id="information"><b> <a
						href="viewUser?UserId=<%=member.getId()%>" class="link"><%=member.getLastName() + " "
										+ member.getFirstName()%></a></b> </br>

					<a class="link" href="#" class="link"
						onclick="OpenPopup(<%=member.getId()%>,'<%=member.getLastName() + " "
										+ member.getFirstName()%>')"><%=member.geteMail()%>
					</a> </br>
					<fmt:message key="<%=member.getAccessLevel()%>" bundle="${LML}"
						var="carAccessLevel" /> <fmt:message key="${carAccessLevel}"
						bundle="${GML}" /> </br>

					<%
					  if (member.isConnected()) {
					%>
					<div id="connect"><img
						src="<%=m_context%>/directory/jsp/icons/connected.jpg" 
						class="size" alt="connected" /> <fmt:message
						key="directory.connected" bundle="${LML}" /><%=" " + member.getDuration()%>
					</div>


					<%
					  } else {
					%>
					<div id="disconnect">
					  <img src="<%=m_context%>/directory/jsp/icons/deconnected.jpg"
						class="size" alt="deconnected" /> <fmt:message
						key="directory.connected" bundle="${LML}" /><%=" " + member.getDuration()%>
					</div>
					 
					<%
					  }
					%>
					</div>

					<div id="invitandmessage">
					<%
					  if (!request.getAttribute("MyId").equals(
															member.getId())) {
														if (!member.isRelationOrInvitation(request
																.getAttribute("MyId").toString())) {
					%> <a href="#" class="link"
						onclick="OpenPopupInvitaion(<%=member.getId()%>,'<%=member.getLastName() + " "
												+ member.getFirstName()%>');">
					Envoyer une invitation</a><br />
					<br />
					<%
					  }
					%> <a href="#" class="link"
						onclick="OpenPopup(<%=member.getId()%>,'<%=member.getLastName() + " "
											+ member.getFirstName()%>')">
					Envoyer un message</a> <%
   }
 %>
					</div>
					<br />
				</view:board></li>

				<%
				  }
				%>

				<%
				  int nbPages = 1;
										nbPages = Integer.parseInt(request.getAttribute(
												"nbPages").toString());
										if (nbPages > 1) {
				%>

				<div id="pagination"><view:pagination
					currentPage="${requestScope.currentPage}"
					nbPages="${requestScope.nbPages}" action="pagination"
					pageParam="currentPage" /></div>
				<%
				  }
				%>


			</ol>

		</view:board>
	</view:frame>

</view:window>

</body>
</html>