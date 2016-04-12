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
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %>
<%@ page import="org.silverpeas.core.chart.pie.PieChart" %>
<%@ page import="org.silverpeas.core.chart.pie.PieChartItem" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSilverStatistics.jsp" %>

<%-- Include tag library --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>

<%
	//Recuperation des parametres
	ArrayLine arrayLine = null;
	Iterator   iter1 = null;
	String filterLibGroup = (String)request.getAttribute("FilterLibGroup");
	String filterIdGroup = (String) request.getAttribute("FilterIdGroup");
	String filterLibUser = (String)request.getAttribute("FilterLibUser");
	String filterIdUser = (String) request.getAttribute("FilterIdUser");
	String spaceId = (String) request.getAttribute("SpaceId");
	Vector vPath = (Vector) request.getAttribute("Path");
  Vector vStatsData = (Vector)request.getAttribute("StatsData");
  UserAccessLevel userProfile = (UserAccessLevel) request.getAttribute("UserProfile");
  PieChart chart = (PieChart) request.getAttribute("Chart");
%>

<c:set var="pieChart" value="${requestScope.Chart}"/>

<html>
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<view:looknfeel/>
<script type="text/javascript">
	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		fonction = fonction + "?MonthBegin=" + accessFormulaire.MonthBegin.value;
		fonction = fonction + "&YearBegin=" + accessFormulaire.YearBegin.value;
		fonction = fonction + "&FilterLibGroup=" + accessFormulaire.FilterLibGroup.value;
		fonction = fonction + "&FilterIdGroup=" + accessFormulaire.FilterIdGroup.value;
		fonction = fonction + "&FilterLibUser=" + accessFormulaire.FilterLibUser.value;
		fonction = fonction + "&FilterIdUser=" + accessFormulaire.FilterIdUser.value;
		fonction = fonction + "&SpaceId=";
		SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
	}

	function clearFilterGroup(){
		accessFormulaire.FilterLibGroup.value = "";
		accessFormulaire.FilterIdGroup.value = "";
	}

	function clearFilterUser(){
		accessFormulaire.FilterLibUser.value = "";
		accessFormulaire.FilterIdUser.value = "";
	}


	function validerForm(){
		accessFormulaire.FilterLibGroup.disabled = false;
		accessFormulaire.FilterLibUser.disabled = false;
		$.progressMessage();
		document.accessFormulaire.submit();
	}

  function onItemClickHelp(item) {
    return item.srcData && item.srcData.extra;
  }

  function onItemClick(item) {
    if (onItemClickHelp(item)) {
      displaySubSpaceStatistics(item.srcData.extra.spaceStatisticUrl);
    }
  }

  function displaySubSpaceStatistics(url) {
    $.progressMessage();
    document.location.href = url;
  }

</script>
</head>
<body class="admin stats">
<c:forEach items="${requestScope['MonthBegin']}" var="mBegin" varStatus="status">
	<c:set var="curValue" value="${mBegin[0]}" />
	<c:if test="${fn:contains(curValue, 'selected')}">
		<c:set var="monthBegin" value="${fn:substringBefore(curValue, ' selected')}" scope="page" />
	</c:if>
</c:forEach>
<c:forEach items="${requestScope['YearBegin']}" var="yBegin" varStatus="status">
	<c:set var="curValue" value="${yBegin[0]}" />
	<c:if test="${fn:contains(curValue, 'selected')}">
		<c:set var="yearBegin" value="${fn:substringBefore(curValue, ' selected')}" scope="page"/>
	</c:if>
</c:forEach>


<%
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Access"), "ValidateViewAccess?MonthBegin="+pageContext.getAttribute("monthBegin")+"&YearBegin="+pageContext.getAttribute("yearBegin")+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId=");

	if (spaceId != null && ! "".equals(spaceId))
	{
		String path = "";
		String separator = "";
		Iterator i = vPath.iterator();
		while ( i.hasNext() )
		{
			String[] pathItem = (String[]) i.next();
			if(UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {//Administrateur
				path += separator + "<a href=\"ValidateViewAccess?MonthBegin="+pageContext.getAttribute("monthBegin")+"&YearBegin="+pageContext.getAttribute("yearBegin")+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+( (pathItem[0]==null) ? "" : ("&SpaceId="+pathItem[0]) )+"\">"+pathItem[1]+ "</a>";
			} else {//manager d'espaces
				path += separator + pathItem[1];
			}
			separator = " > ";
		}

		browseBar.setPath(path);
	}

	operationPane.addOperation(resources.getIcon("silverStatisticsPeas.icoGenExcel"),resources.getString("silverStatisticsPeas.export"),"javascript:openSPWindow('ExportAccess.txt','')");

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<center>
  <form name="accessFormulaire" action="ValidateViewAccess" method="post">
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
      <tr>
        <td width="300" nowrap class=txtlibform><fmt:message key="GML.date" />&nbsp;:</td>
        <td nowrap>
          <select name="MonthBegin" size="1">
			<c:forEach items="${requestScope['MonthBegin']}" var="mBegin" varStatus="status">
				<c:set var="curValue" value="${mBegin[0]}" />
				<c:set var="curSel" value="" />
				<c:choose>
					<c:when test="${fn:contains(curValue, 'selected')}">
						<c:set var="monthBegin" value="${fn:substringBefore(curValue, ' selected')}" scope="page" />
						<option value="${monthBegin}" selected><fmt:message key="${mBegin[1]}" /></option>
					</c:when>
					<c:otherwise>
						<option value="${curValue}"><fmt:message key="${mBegin[1]}" /></option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
          </select>
          &nbsp;&nbsp;
          <select name="YearBegin" size="1">
			<c:forEach items="${requestScope['YearBegin']}" var="yBegin" varStatus="status">
				<c:set var="curValue" value="${yBegin[0]}" />
				<c:set var="curSel" value="" />
				<c:choose>
					<c:when test="${fn:contains(curValue, 'selected')}">
						<c:set var="yearBegin" value="${fn:substringBefore(curValue, ' selected')}" scope="page"/>
						<option value="${yearBegin}" selected><c:out value="${yBegin[1]}" /></option>
					</c:when>
					<c:otherwise>
						<option value="${curValue}"><c:out value="${yBegin[1]}" /></option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td nowrap class=txtlibform><fmt:message key="silverStatisticsPeas.group" />&nbsp;:</td>
        <td nowrap colspan="2">
          <input type="text" name="FilterLibGroup" value="<%=filterLibGroup%>" size="25" disabled>
		  <input type="hidden" name="FilterIdGroup" value="<%=filterIdGroup%>">
          <a href="javascript:openSPWindow('AccessCallUserPanelGroup','')"><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessGroupPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a>
          <a href="javascript:clearFilterGroup()"><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a>
        </td>
      </tr>
      <tr>
        <td nowrap class=txtlibform><%=resources.getString("GML.user")%>&nbsp;:</td>
        <td nowrap colspan="2">
          <input type="text" name="FilterLibUser" value="<%=filterLibUser%>" size="25" disabled>
		  <input type="hidden" name="FilterIdUser" value="<%=filterIdUser%>">
          <a href="javascript:openSPWindow('AccessCallUserPanelUser','')"><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessUserPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a>
          <a href="javascript:clearFilterUser()"><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a>
        </td>
      </tr>
    </table>
      <input type="hidden" name="SpaceId" value="<%=(spaceId==null) ? "" : spaceId%>" />
  </form>
  <%

	out.println(board.printAfter());
  %>
  <div id="stats_viewConnectionButton">
		<view:buttonPane>
			<fmt:message key="GML.validate" var="labelValidate" />
			<fmt:message key="GML.reset" var="labelReset"/>
		    <view:button label="${labelValidate}" action="javascript:validerForm()" ></view:button>
		    <view:button label="${labelReset}" action="javascript:document.resetAccessForm.submit()"></view:button>
		</view:buttonPane>
  </div>
  <br/>

  <%

	// Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "ValidateViewAccess?MonthBegin="+ pageContext.getAttribute("monthBegin") +"&YearBegin="+pageContext.getAttribute("yearBegin")+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId="+spaceId, request,session);
          arrayPane.setVisibleLineNumber(50);

          ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.organisation"));
          ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.allMP"));
          ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.group"));
          if(filterIdGroup == null || "".equals(filterIdGroup)) {
			  arrayColumn3.setSortable(false);
		  }
          ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.user"));
          if(filterIdUser == null || "".equals(filterIdUser)) {
			  arrayColumn4.setSortable(false);
		  }

		  ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Actions")+"</A>");
          arrayColumn5.setSortable(false);

        if (vStatsData != null)
        {
          Iterator<PieChartItem> itChartItems = chart.getItems().iterator();
		iter1 = vStatsData.iterator();
		String title;

		while (iter1.hasNext())
		{
				arrayLine = arrayPane.addArrayLine();

		String[] item = (String[]) iter1.next();

		title = resources.getString("silverStatisticsPeas.Historique");
		if ( "SPACE".equals(item[0]) ) {
                String url = "ValidateViewAccess?MonthBegin="+pageContext.getAttribute("monthBegin")+"&YearBegin="+pageContext.getAttribute("yearBegin")+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId="+item[1];
                itChartItems.next().addExtra("spaceStatisticUrl", url);
				arrayLine.addArrayCellLink("<B>"+item[2]+"</B>", "javascript:displaySubSpaceStatistics('" + url + "')");
				title += " ["+item[2]+"]";
			} else {
				arrayLine.addArrayCellText(item[2]);
				title += " "+item[2];
			}

			ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[3]);
			cellTextCount.setCompareOn(new Integer(item[3]));

			if(filterIdGroup != null && ! "".equals(filterIdGroup)) {
				cellTextCount = arrayLine.addArrayCellText(item[4]);
			cellTextCount.setCompareOn(new Integer(item[4]));
			if(filterIdUser == null || "".equals(filterIdUser)) {
				title += " "+resources.getString("silverStatisticsPeas.For")+ " "+filterLibGroup;
			}
		} else {
			arrayLine.addArrayCellText("");
		}

		if(filterIdUser != null && ! "".equals(filterIdUser)) {
				cellTextCount = arrayLine.addArrayCellText(item[5]);
			cellTextCount.setCompareOn(new Integer(item[5]));
			title += " "+resources.getString("silverStatisticsPeas.For")+ " "+filterLibUser;
		} else {
			arrayLine.addArrayCellText("");
		}

		arrayLine.addArrayCellText("<div align=left><a href=\"ViewEvolutionAccess?Entite="+item[0]+"&Id="+item[1]+"\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoComponent")+"\" align=absmiddle alt=\""+title+"\" border=0 title=\""+title+"\"></a></div>");
		}
    }
    %>

  <div class="flex-container">
    <viewTags:displayChart chart="${pieChart}" onItemClick="onItemClick" onItemClickHelp="onItemClickHelp"/>
  </div>

  <br>
  <%
    out.println(arrayPane.print());
    out.println("");
  %>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<form name="resetAccessForm" action="ViewAccess" method="post">
</form>
<view:progressMessage/>
</body>
</html>