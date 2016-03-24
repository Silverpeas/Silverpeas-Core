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
<%@ page import="org.silverpeas.core.util.DateUtil" %>

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
	ArrayLine  arrayLine = null;
	Iterator   iter = null;
	String monthBegin = "";
	String yearBegin = "";
	String monthEnd = "";
	String yearEnd = "";
	Collection cActorDetail = (Collection)request.getAttribute("ActorDetail");
	String actorDetail = "";
	String filterType = (String)request.getAttribute("FilterType");
	String filterLib = (String)request.getAttribute("FilterLib");
	String filterId = (String)request.getAttribute("FilterId");
  Collection cResultData = (Collection)request.getAttribute("ResultData");
  UserAccessLevel userProfile = (UserAccessLevel)request.getAttribute("UserProfile");

  String[] item = null;
  String theValue = null;
  int indexOfSelected;

%>

<c:set var="distincUsersPeriodChart" value="${requestScope.DistinctUsersChart}"/>
<c:set var="connectionsPeriodChart" value="${requestScope.ConnectionsChart}"/>

<%
  browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
  browseBar.setComponentName(resources.getString("silverStatisticsPeas.Connections"));
  browseBar.setPath(resources.getString("silverStatisticsPeas.LoginNumber"));

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><fmt:message key="silverStatisticsPeas.LoginNumber" /></title>
<view:looknfeel />
<script type="text/javascript">

	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		fonction = fonction + "?MonthBegin=" + connexionFormulaire.MonthBegin.value;
		fonction = fonction + "&YearBegin=" + connexionFormulaire.YearBegin.value;
		fonction = fonction + "&MonthEnd=" + connexionFormulaire.MonthEnd.value;
		fonction = fonction + "&YearEnd=" + connexionFormulaire.YearEnd.value;
		fonction = fonction + "&ActorDetail=" + connexionFormulaire.ActorDetail.value;
		fonction = fonction + "&FilterLib=" + connexionFormulaire.FilterLib.value;
		fonction = fonction + "&FilterType=" + connexionFormulaire.FilterType.value;
		fonction = fonction + "&FilterId=" + connexionFormulaire.FilterId.value;
		SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, resizable, alwaysRaised');
	}

	function changeDetail() {
		if(connexionFormulaire.ActorDetail.value == "0") {
			clearFilter();
		}
	}

	function clearFilter(){
		connexionFormulaire.FilterLib.value = "";
		connexionFormulaire.FilterType.value = "";
		connexionFormulaire.FilterId.value = "";
	}

	function validateForm(){
		connexionFormulaire.FilterLib.disabled = false;
		$.progressMessage();
		document.connexionFormulaire.submit();
	}
</script>
</head>
<body class="admin stats">
<view:window>
<%
          if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {
%>
<view:tabs>
	<fmt:message var="userTabLabel" key="silverStatisticsPeas.usersWithSession" />
	<view:tab label="${userTabLabel}" selected="false" action="${ctxPath}/RsilverStatisticsPeas/jsp/Main"></view:tab>
	<fmt:message var="connectionLabel" key="silverStatisticsPeas.connectionNumber" />
	<view:tab label="${connectionLabel}" selected="true" action="${ctxPath}/RsilverStatisticsPeas/jsp/ViewConnections"></view:tab>
	<fmt:message var="frequenceLabel" key="silverStatisticsPeas.connectionFrequence" />
	<view:tab label="${frequenceLabel}" selected="false" action="${ctxPath}/RsilverStatisticsPeas/jsp/ViewFrequence"></view:tab>
</view:tabs>

<%      } %>

  <view:frame>
    <view:board>
<center>
<form name="connexionFormulaire" action="ValidateViewConnection" method="post">
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
      <tr>
        <td nowrap class=txtlibform><fmt:message key="GML.detail" />&nbsp;:</td>
        <td nowrap>
          <select name="ActorDetail" size="1" onChange="changeDetail()">
		    <%
		iter = cActorDetail.iterator();
		while (iter.hasNext())
		{
		item = (String[]) iter.next();
		theValue = item[0];
			out.print("<option value="+ theValue +">"+resources.getString(item[1])+"</option>");
			indexOfSelected = theValue.indexOf("selected");
			if(indexOfSelected != -1)
				actorDetail = theValue.substring(0, indexOfSelected - 1);
		}
		%>
          </select>
        </td>
      </tr>
      <tr>
        <td nowrap class=txtlibform><fmt:message key="silverStatisticsPeas.filter" />&nbsp;(<fmt:message key="silverStatisticsPeas.group" />&nbsp;-&nbsp;<fmt:message key="GML.user" />)&nbsp;:</td>
        <td nowrap>
          <input type="text" name="FilterLib" value="<%=( filterLib == null ? "" : filterLib )%>" size="25" disabled>
		  <input type="hidden" name="FilterType" value="<%=filterType%>">
		  <input type="hidden" name="FilterId" value="<%=filterId%>">
          <a href="javascript:openSPWindow('CallUserPanel','')"><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessGroupPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border="0" title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a>
          <a href="javascript:clearFilter()"><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border="0" title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a>
        </td>
      </tr>
  </table>
</form>
</view:board>

  <div id="stats_viewConnectionButton">
    <view:buttonPane>
      <fmt:message key="GML.validate" var="labelValidate" />
      <fmt:message key="GML.reset" var="labelReset" />
      <view:button label="${labelValidate}" action="javascript:validateForm()" ></view:button>
      <view:button label="${labelReset}" action="javascript:document.resetConnectionForm.submit()"></view:button>
    </view:buttonPane>
  </div>
  <br/>


  <%

		// Tableau
          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
          arrayPane.setExportData(true);
          //arrayPane.setExportDataURL(m_context + "/RsilverStatisticsPeas/jsp/ExportViewConnection");

          arrayPane.addArrayColumn(resources.getString("GML.name"));
          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.connectionNumber"));
          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.durationAvg"));

          if(("1".equals(actorDetail) || "2".equals(actorDetail)) &&
			(filterId == null || "".equals(filterId)) ) {//ajoute une colonne dans le tableau

				ArrayColumn arrayColumn = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Actions"));
				arrayColumn.setSortable(false);
		  }

          ArrayCellText cellTextCount;
		  ArrayCellText cellTextDuration;

        if (cResultData != null)
        {
      %>

    <div class="flex-container">
      <viewTags:displayChart chart="${distincUsersPeriodChart}" colors="#1c94d4" displayAsBars="true"/>
      <viewTags:displayChart chart="${connectionsPeriodChart}" displayAsBars="true"/>
	  </div>
    <br/>

      <%
		iter = cResultData.iterator();
		if(iter.hasNext()) {
			String title;
			while (iter.hasNext())
			{
			item = (String[]) iter.next();

				arrayLine = arrayPane.addArrayLine();

				arrayLine.addArrayCellText(item[0]);

			cellTextCount = arrayLine.addArrayCellText(item[1]);
			cellTextCount.setCompareOn(new Integer(item[1]));

					long duration	= Long.valueOf(item[2]).longValue();

					String formattedDuration = DateUtil.formatDuration(duration);
					cellTextDuration = arrayLine.addArrayCellText(formattedDuration);
					cellTextDuration.setCompareOn(new Long(duration));

					if(("1".equals(actorDetail) || "2".equals(actorDetail)) &&
						(filterId == null || "".equals(filterId)) )
					{
						title = resources.getString("silverStatisticsPeas.GraphConnections")+" ";
						if("1".equals(actorDetail)) {
							title += resources.getString("GML.groupe")+" ";
						}
						else if("2".equals(actorDetail)) {
							title += resources.getString("GML.user")+" ";
						}
						title += item[0];

						arrayLine.addArrayCellText("<div align=left><a href=\"ValidateViewConnection?EntiteId="+item[3]+"&MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&MonthEnd="+monthEnd+"&YearEnd="+yearEnd+"&ActorDetail="+actorDetail+"&FilterType="+filterType+"&FilterLib="+filterLib+"&FilterId="+filterId+"\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoHistoAccess")+"\" align=absmiddle alt=\""+title+"\" border=0 title=\""+title+"\"></a></div>");
					}
			}
	        } else {
				arrayLine = arrayPane.addArrayLine();

				arrayLine.addArrayCellText(filterLib == null ? "*" : filterLib);
				arrayLine.addArrayCellText("0");
				arrayLine.addArrayCellText("0");
	        }

		out.println(arrayPane.print());

%>
<%--
   <view:arrayPane var="connectionArrayPane" >
	<fmt:message var="gmlName" key="GML.name" />
	<view:arrayColumn title="${gmlName}" />
	<fmt:message var="sspConNumberLabel" key="silverStatisticsPeas.connectionNumber" />
	<view:arrayColumn title="${sspConNumberLabel}" />
	<fmt:message var="sspDurationAVG" key="silverStatisticsPeas.durationAvg" />
	<view:arrayColumn title="${sspDurationAVG}" />
	<c:forEach items="${requestScope['ResultData']}" var="lineResult">
		<view:arrayLine>
			<view:arrayCellText text="${lineResult[0]"></view:arrayCellText>
			<view:arrayCellText text="Coucou2"></view:arrayCellText>
			<view:arrayCellText text="Coucou3"></view:arrayCellText>
		</view:arrayLine>
	</c:forEach>
	<view:arrayLine>
		<view:arrayCellText text="Coucou1"></view:arrayCellText>
		<view:arrayCellText text="Coucou2"></view:arrayCellText>
		<view:arrayCellText text="Coucou3"></view:arrayCellText>
	</view:arrayLine>
  </view:arrayPane>
--%>

<%
		}
%>
</center>
  </view:frame>
</view:window>
<form name="resetConnectionForm" action="ViewConnections" method="post">
</form>
<view:progressMessage/>
</body>
</html>