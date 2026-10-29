<%--

    Copyright (C) 2000 - 2026 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ include file="check.jsp" %>
<%
  String spaceId = (String) request.getAttribute("CurrentSpaceId");

  browseBar.setSpaceId(spaceId);
  browseBar.setExtraInformation(resource.getString("JSPP.creationInstance"));
%>

<!DOCTYPE html>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<adminTags:refreshSpaceNavigation/>
<script type="text/javascript">
  $(function()
  {

		$(".linkSee").on('click', function() {
			$(".applicationsType ul").hide();
			$(".linkSee").removeClass("select");
			$(this).addClass("select");
			$(this).parents('.applicationsType').children("ul").show();
		});

		$(".application a").on("mouseenter", function() {
			$(this).children("p").show();
			$(this).children("h3").hide();
			}).on("mouseleave", function(){
			$(this).children("h3").show();
			$(this).children("p").hide();
			}
		);

		$(".linkSee span").html(function() {
			id = $(this).text().trim().substring(0,2);
			$(this).html($(this).text().trim().substring(3));
			$(this).parents('.applicationsType').addClass("type_"+id);
		});



		$(".type_01 ul").show();
		$(".type_01 .linkSee").addClass("select");

  });
</script>
<style>
<!--
/* ne peut �tre ajouter � la global html heit � 100% a trop d'impacte*/
body , html {
height:100%;
}
-->
</style>

</head>
<body class="page_content_admin">
	<% out.print(window.printBefore()); %>
		<ul class="applicationsTypeList">
				<c:set var="currentSuite" value="null" scope="page"/>
				<c:forEach items="${requestScope.ListComponents}" var="component" varStatus="loop">
				  <c:if test="${component.visible}">
					<c:if test="${component.suite != null && component.suite != currentSuite  && currentSuite != 'null'}">
						</ul></li>
					</c:if>
					<c:if test="${component.suite != null && component.suite != currentSuite}">
					  <c:set var="currentSuite" value="${component.suite}" scope="page"/>
						<li class="applicationsType"><a class="linkSee" href="#"><span><c:out value="${currentSuite}"/></span></a>
						<ul class="applicationList">
					</c:if>
							<li id="<c:out value="${component.name}" />" class="application">
								<a href="CreateInstance?ComponentName=<c:out value="${component.name}" />" title="<%=resource.getString("JSPP.applications.add")%>">
									<c:if test="${component.suite == '05 Workflow'}">
										<img src="<%=iconsPath%>/util/icons/component/processManagerBig.png" class="component-icon" alt=""/>
									</c:if>
									<c:if test="${component.suite != '05 Workflow'}">
										<img src="<%=iconsPath%>/util/icons/component/<c:out value="${component.name}" />Big.png" class="component-icon" alt=""/>
									</c:if>

									<h3 class="applicationName"><c:out value="${component.label}" /></h3>
									<p class="applicationDescription"><c:out value="${component.description}"/></p>
								</a>
							</li>
				  </c:if>
				</c:forEach>
		</ul>

        <br />
     <% out.print(window.printAfter()); %>
</body>
</html>