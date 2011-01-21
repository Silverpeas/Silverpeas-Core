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

String monthBegin = "";
String yearBegin = "";
String monthEnd = "";
String yearEnd = "";
String spaceId = (String) request.getAttribute("SpaceId");
String axisId = (String) request.getAttribute("AxisId");
String axisValue = (String) request.getAttribute("AxisValue");
Vector vPath = (Vector) request.getAttribute("Path");
CrossStatisticVO crossAxis = (CrossStatisticVO) request.getAttribute("StatsData");

browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
browseBar.setComponentName(resources.getString("silverStatisticsPeas.access.pdc"));
browseBar.setPath(resources.getString("silverStatisticsPeas.pdc.axis"));
%>


<html>
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<view:looknfeel />

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-ui-1.7.3.custom.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/progressMessage.js"></script>
<link type="text/css" href="<%=m_context%>/util/styleSheets/jquery/ui-lightness/jquery-ui-1.7.3.custom.css" rel="stylesheet">

<script language="javascript">
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
		pdcAccessForm.FilterLibGroup.value = "";
		pdcAccessForm.FilterIdGroup.value = "";
	}
	
	function clearFilterUser(){
		pdcAccessForm.FilterLibUser.value = "";
		pdcAccessForm.FilterIdUser.value = "";
	}
	
	
	function validerForm(){
		if (checkForm()) {
			$.progressMessage();
			document.pdcAccessForm.submit();
		}
	}

	function checkForm() {
		if (document.pdcAccessForm.FirstAxis.value == 0 || document.pdcAccessForm.SecondAxis.value == 0) {
			alert(document.pdcAccessForm.selectAxisError.value);
			return false;
		}
		if (document.pdcAccessForm.FirstAxis.value != 0 &&
			document.pdcAccessForm.FirstAxis.value == document.pdcAccessForm.SecondAxis.value) {
			alert(document.pdcAccessForm.sameAxisError.value);
			return false;
		}
		return true;
	}

	function openAxisStats(axisId) {
		document.pdcAccessForm.AxisId.value = axisId;
		document.pdcAccessForm.AxisValue.value = "/0/";
		document.pdcAccessForm.submit();
	}

</script>
</head>
<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" onLoad="">
<view:window>
  <c:if test="${fn:contains(userProfile, 'A')}">
<view:tabs>
	<fmt:message var="axisTabLabel" key="silverStatisticsPeas.pdc.axis" />
	<view:tab label="${axisTabLabel}" selected="false" action="${ctxPath}/RsilverStatisticsPeas/jsp/ViewPDCAccess"></view:tab>
	<fmt:message var="crossAxisTabLabel" key="silverStatisticsPeas.pdc.cross" />
	<view:tab label="${crossAxisTabLabel}" selected="true" action="${ctxPath}/RsilverStatisticsPeas/jsp/ViewCrossPDCAccess"></view:tab>
</view:tabs>
  </c:if>
  <view:frame>
    <view:board>

<center>
  <form name="pdcAccessForm" action="ValidateViewCrossPDCAccess" method="post">
    <input type="hidden" name="SpaceId" value="<%=(spaceId==null) ? "" : spaceId%>" />
    <input type="hidden" name="AxisId" id="hiddenAxisId" value="<%=(axisId==null) ? "" : axisId%>" />
    <input type="hidden" name="AxisValue" id="hiddenAxisValue" value="<%=(axisValue==null) ? "" : axisValue%>" />
    <input type="hidden" name="selectAxisError" id="hiddenSelectAxisError" value="<fmt:message key='silverStatisticsPeas.pdc.error.select' />" />
    <input type="hidden" name="sameAxisError" id="hiddenSameAxisError" value="<fmt:message key='silverStatisticsPeas.pdc.error.same' />" />
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
        <td nowrap class=txtlibform><fmt:message key="silverStatisticsPeas.pdc.cross.axis.first" />&nbsp;:</td>
        <td nowrap> 
          <select name="FirstAxis" size="1">
          	<option value="0"><fmt:message key="silverStatisticsPeas.pdc.cross.select" />&nbsp;&nbsp;</option>
			<c:forEach items="${requestScope['PrimaryAxis']}" var="mAxis" varStatus="status">
				<c:set var="curValue" value="${requestScope['FirstAxis']}" />
				<c:choose>
					<c:when test="${mAxis.id == curValue}">
						<option value="${mAxis.id}" selected><c:out value="${mAxis.name}" /> </option>
					</c:when>
					<c:otherwise>
						<option value="${mAxis.id}"><c:out value="${mAxis.name}" /> </option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
          </select>
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><fmt:message key="silverStatisticsPeas.pdc.cross.axis.second" />&nbsp;:</td>
        <td nowrap> 
          <select name="SecondAxis" size="1">
          	<option value="0"><fmt:message key="silverStatisticsPeas.pdc.cross.select" />&nbsp;&nbsp;</option>
			<c:forEach items="${requestScope['PrimaryAxis']}" var="mAxis" varStatus="status">
				<c:set var="curValue" value="${requestScope['SecondAxis']}" />
				<c:choose>
					<c:when test="${mAxis.id == curValue}">
						<option value="${mAxis.id}" selected><c:out value="${mAxis.name}" /> </option>
					</c:when>
					<c:otherwise>
						<option value="${mAxis.id}"><c:out value="${mAxis.name}" /> </option>
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
		  	<fmt:message key="GML.cancel" var="labelCancel" />
		    <view:button label="${labelValidate}" action="javascript:validerForm()" ></view:button>
		    <view:button label="${labelCancel}" action="javascript:document.cancelAccessForm.submit()"></view:button>
	  	</view:buttonPane>
	  </center>
  </div>
  <%
  
   //Graphiques
   /*
   if (vStatsData != null)
   {
  %>
		   	<div align="center">
				<img src="<%=m_context%>/ChartServlet/?chart=USER_VENTIL_CHART&random=<%=(new Date()).getTime()%>">
			</div>
  <%
  }*/
  %>
  <br>
  
  <%

	if (crossAxis != null) {
 		List columns = (List) crossAxis.getColumnHeader();
 		List rows = (List) crossAxis.getFirstRow();
		List statsArray = (List) crossAxis.getStatsArray();
  	    ArrayPane arrayPane = gef.getArrayPane("List", "ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"MonthEnd="+monthEnd+"&YearEnd="+yearEnd, request,session);
		//arrayPane.setVisibleLineNumber(50);
        arrayPane.setExportData(true);
		
		ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("");
		arrayColumn1.setSortable(false);
		for (int nbCol=0; nbCol < columns.size(); nbCol++) {
		  ArrayColumn arrayCol = arrayPane.addArrayColumn((String) columns.get(nbCol));
		  arrayCol.setSortable(false);
		}
		
		for (int nbRow = 0; nbRow < rows.size(); nbRow++) {
		  arrayLine = arrayPane.addArrayLine();
		  List rowData = (List) statsArray.get(nbRow);
		  arrayLine.addArrayCellText((String) rows.get(nbRow));
		  for (int nbCol = 0; nbCol < rowData.size(); nbCol++) {
		    CrossAxisAccessVO curAxisVO = (CrossAxisAccessVO) rowData.get(nbCol);
		    arrayLine.addArrayCellText(curAxisVO.getNbAccess());
		  }
		}
       	        	
        out.println(arrayPane.print());
        out.println("");
    }
    %>
</center>
  </view:frame>
</view:window>
<form name="cancelAccessForm" action="ViewCrossPDCAccess" method="post">
</form>
<view:progressMessage/>
</body>
</html>