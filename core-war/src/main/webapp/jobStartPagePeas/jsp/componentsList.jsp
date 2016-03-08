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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ include file="check.jsp" %>
<%
  String spaceId = (String) request.getAttribute("CurrentSpaceId");

  browseBar.setSpaceId(spaceId);
  browseBar.setExtraInformation(resource.getString("JSPP.creationInstance"));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
  $(document).ready(function()
  {

		$(".linkSee").click(function() {
			$(".applicationsType ul").hide();
			$(".linkSee").removeClass("select");
			$(this).addClass("select");
			$(this).parents('.applicationsType').children("ul").show();
		});

		$(".application a").hover(
			function() {
			$(this).children("p").show();
			$(this).children("h3").hide();
			},
			function(){
			$(this).children("h3").show();
			$(this).children("p").hide();
			}
		);

		$(".linkSee span").html(function() {
			id = $.trim($(this).text()).substring(0,2);
			$(this).html($.trim($(this).text()).substring(3));
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
<body>
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