<%@ page import="org.silverpeas.web.silverstatistics.vo.StatisticVO" %><%--

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

<%@ include file="checkSilverStatistics.jsp" %>

<%-- Include tag library --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>
<c:set var="userProfile" value="${requestScope['UserProfile']}"/>

<%
//Recuperation des parametres
ArrayLine arrayLine = null;
Iterator   iter1 = null;

String spaceId = (String) request.getAttribute("SpaceId");
String axisId = (String) request.getAttribute("AxisId");
String axisValue = (String) request.getAttribute("AxisValue");
List vStatsData = (List)request.getAttribute("StatsData");

%>


<html>
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">
	function validerForm(){
		openAxisStats(0);
	}

	function openAxisStats(axisId) {
		document.pdcAccessForm.AxisId.value = axisId;
		document.pdcAccessForm.AxisValue.value = "/0/";
		$.progressMessage();
		document.pdcAccessForm.submit();
	}

	function openSubAxisStats(axisId, axisValue) {
		document.pdcAccessForm.AxisId.value = axisId;
		document.pdcAccessForm.AxisValue.value = axisValue;
		$.progressMessage();
		document.pdcAccessForm.submit();
	}
</script>
</head>
<body>
<view:window>
  <c:if test="${fn:startsWith(userProfile.name, 'ADMINISTRATOR')}">
<view:tabs>
	<fmt:message var="axisTabLabel" key="silverStatisticsPeas.pdc.axis" />
	<view:tab label="${axisTabLabel}" selected="true" action="${ctxPath}/RsilverStatisticsPeas/jsp/ViewPDCAccess"></view:tab>
	<fmt:message var="crossAxisTabLabel" key="silverStatisticsPeas.pdc.cross" />
	<view:tab label="${crossAxisTabLabel}" selected="false" action="${ctxPath}/RsilverStatisticsPeas/jsp/ViewCrossPDCAccess"></view:tab>
</view:tabs>
  </c:if>
  <view:frame>
    <view:board>

<center>
  <form name="pdcAccessForm" action="ValidateViewPDCAccess" method="post">
    <input type="hidden" name="SpaceId" value="<%=(spaceId==null) ? "" : spaceId%>" />
    <input type="hidden" name="AxisId" id="hiddenAxisId" value="<%=(axisId==null) ? "" : axisId%>" />
    <input type="hidden" name="AxisValue" id="hiddenAxisValue" value="<%=(axisValue==null) ? "" : axisValue%>" />
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
      <tr>
        <td width="300" nowrap class=txtlibform><fmt:message key="silverStatisticsPeas.since" />&nbsp;:</td>
        <td nowrap>
          <select name="MonthBegin" size="1">
			<c:forEach items="${requestScope['MonthBegin']}" var="mBegin" varStatus="status">
				<c:set var="curValue" value="${mBegin[0]}" />
				<c:set var="curSel" value="" />
				<c:choose>
					<c:when test="${fn:contains(curValue, 'selected')}">
						<option value="${fn:substringBefore(curValue, ' selected')}" selected><fmt:message key="${mBegin[1]}" /></option>
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
						<option value="${fn:substringBefore(curValue, ' selected')}" selected><c:out value="${yBegin[1]}" /></option>
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
        <td nowrap class=txtlibform><fmt:message key="silverStatisticsPeas.to" />&nbsp;:</td>
        <td nowrap>
          <select name="MonthEnd" size="1">
			<c:forEach items="${requestScope['MonthEnd']}" var="mEnd" varStatus="status">
				<c:set var="curValue" value="${mEnd[0]}" />
				<c:set var="curSel" value="" />
				<c:choose>
					<c:when test="${fn:contains(curValue, 'selected')}">
						<option value="${fn:substringBefore(curValue, ' selected')}" selected><fmt:message key="${mEnd[1]}" /></option>
					</c:when>
					<c:otherwise>
						<option value="${curValue}"><fmt:message key="${mEnd[1]}" /></option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
          </select>
		&nbsp;&nbsp;
          <select name="YearEnd" size="1">
			<c:forEach items="${requestScope['YearEnd']}" var="yEnd" varStatus="status">
				<c:set var="curValue" value="${yEnd[0]}" />
				<c:set var="curSel" value="" />
				<c:choose>
					<c:when test="${fn:contains(curValue, 'selected')}">
						<option value="${fn:substringBefore(curValue, ' selected')}" selected><c:out value="${yEnd[1]}" /></option>
					</c:when>
					<c:otherwise>
						<option value="${curValue}"><c:out value="${yEnd[1]}" /></option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
          </select>
        </td>
      </tr>
    </table>
  </form>
    </view:board>
  <div id="stats_viewConnectionButton">
	  <center>
		<view:buttonPane>
			<fmt:message key="GML.validate" var="labelValidate" />
			<fmt:message key="GML.reset" var="labelReset" />
		    <view:button label="${labelValidate}" action="javascript:validerForm()" ></view:button>
		    <view:button label="${labelReset}" action="javascript:document.resetlAccessForm.submit()"></view:button>
		</view:buttonPane>
	  </center>
  </div>

  <br/>

  <%
	// Tableau
	ArrayPane arrayPane = gef.getArrayPane("List", "ValidateViewPDCAccess?AxisId="+axisId+"&AxisValue="+axisValue, request,session);
	arrayPane.setExportData(true);

	//arrayPane.setExportDataURL("ExportPDCAccess");
    arrayPane.setVisibleLineNumber(50);

    ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.pdc.th.axis"));
    arrayColumn1.setSortable(false);
    ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.pdc.th.access"));
    arrayColumn2.setSortable(false);

    if (vStatsData != null)
    {
	iter1 = vStatsData.iterator();
	String title;

	while (iter1.hasNext())
	{
				arrayLine = arrayPane.addArrayLine();
				StatisticVO statsVO = (StatisticVO) iter1.next();
				if (statsVO.getAxisLevel() > 0) {
					arrayLine.addArrayCellLink(statsVO.getAxisName(), "javascript:openSubAxisStats(" + statsVO.getAxisId() + ",'" + statsVO.getAxisValue() + "');");
				} else {
					arrayLine.addArrayCellLink(statsVO.getAxisName(), "javascript:openAxisStats(" + statsVO.getAxisId() + ");");
				}
				//arrayLine.addArrayCellText(statsVO.getAxisName() + " - " + statsVO.getAxisId());
				arrayLine.addArrayCellText(statsVO.getNbAccess());
		}
        out.println(arrayPane.print());
        out.println("");
    }
    %>
</center>
  </view:frame>
</view:window>
<form name="resetlAccessForm" action="ViewPDCAccess" method="post">
</form>
<view:progressMessage/>
</body>
</html>
