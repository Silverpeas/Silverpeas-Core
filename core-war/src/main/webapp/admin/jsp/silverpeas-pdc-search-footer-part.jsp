<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@page import="org.silverpeas.web.pdc.QueryParameters"%>
<%@page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.pdc.control.PdcSearchSessionController"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Retrieve user menu display mode --%>
<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<%
MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

//recuperation des parametres pour le PDC
QueryParameters	 parameters			= (QueryParameters) request.getAttribute("QueryParameters");
%>
<c:set var="searchLabel" value='<%=resource.getString("pdcPeas.search")%>'/>
<view:includePlugin name="pdcdynamically" />
<script type="text/javascript">
  function sendQuery() {
    var values = $('#used_pdc').pdc('selectedValues');
    if (values.length > 0) {
      document.PdcWidgetAdvancedSearch.AxisValueCouples.value = values.flatten();
      executePdcActionToBodyPartTarget("<c:url value="/RpdcSearch/jsp/AdvancedSearch"/>")
    }
  }

  function handleSearchAccess() {
    var $button = $('.sp_button', $('.pdcFrameGroup'));
    var values = $('#used_pdc').pdc('selectedValues');
    if (values.length > 0) {
      $button.removeClass('disabled');
    } else {
      $button.addClass('disabled');
    }
  }

  function raz() {
    var settings = getPDCSettings();
    settings.values = [];
    $('#used_pdc').pdc('used', settings);
  }

  function executePdcActionToBodyPartTarget(baseUrl) {
    var url = sp.url.format(baseUrl, jQuery(document.PdcWidgetAdvancedSearch).serializeFormJSON());
    spWindow.loadContent(url);
  }

  function getPDCSettings() {
    var settings = {
      <% if (StringUtil.isDefined(parameters.getInstanceId())) { %>
      component : '<%=parameters.getInstanceId()%>',
      <% } %>
      <% if (StringUtil.isDefined(parameters.getSpaceId())) { %>
      workspace : '<%=parameters.getSpaceId()%>',
      <% } %>
      withSecondaryAxis : false,
      onLoaded : function(loadedPdC) {
        handleSearchAccess();
        if (loadedPdC && loadedPdC.axis.length) {
          spLayout.getFooter().showPdc();
        } else {
          spLayout.getFooter().hidePdc();
        }
      },
      onAxisChanged : function() {
        handleSearchAccess()
      }
    };
    return settings;
  }

  whenSilverpeasReady(function() {
    window.PDC_PROMISE.then(function() {
      $('#used_pdc').pdc('used', getPDCSettings());
    });
  });
</script>
<div id="pdcFrame">
<form name="PdcWidgetAdvancedSearch" action="#" method="get">
  <input type="hidden" name="mode"/>
  <input type="hidden" name="FromPDCFrame" value="true"/>
  <input type="hidden" name="ShowResults" value="<%=PdcSearchSessionController.SHOWRESULTS_ONLY_PDC %>"/>
  <input type="hidden" name="ResultPage" value=""/>
  <input type="hidden" name="SearchPage" value="/admin/jsp/silverpeas-pdc-search-footer-part.jsp"/>
  <input type="hidden" name="spaces" value="<%=parameters.getSpaceId()%>"/>
  <input type="hidden" name="componentSearch" value="<%=parameters.getInstanceId()%>"/>
  <input type="hidden" name="AxisValueCouples" />
</form>

  <div class="pdcFrameGroup">
    <div id="used_pdc"></div>
    <view:button label="${searchLabel}" action="javascript:onClick=sendQuery()"/>
    <a id="razButton" href="javaScript:raz()" title="<%=resource.getString("GML.reset")%>"><img src="<%=m_context%>/util/icons/arrow/refresh.png" alt="<%=resource.getString("GML.reset")%>"/></a>
  </div>

</div>