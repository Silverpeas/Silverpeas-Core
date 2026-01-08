<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%@ page import="org.silverpeas.core.contribution.content.form.DataRecord" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.contribution.template.publication.PublicationTemplate" %>

<%@ include file="checkAdvancedSearch.jsp" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<fmt:message var="componentName" key="pdcPeas.SearchPage"/>
<fmt:message var="resultsTab" key="pdcPeas.SearchResult"/>
<fmt:message var="simpleSearchTab" key="pdcPeas.SearchSimple"/>
<fmt:message var="advancedSearchTab" key="pdcPeas.SearchAdvanced"/>
<fmt:message var="searchByFormTab" key="pdcPeas.SearchXml"/>
<fmt:message var="searchPageTab" key="pdcPeas.SearchPage"/>
<fmt:message var="searchAction" key="pdcPeas.search"/>

<c:set var="language" value="${sessionScope[sessionController].language}"/>
<c:set var="templates" value="${requestScope.XMLForms}"/>
<c:set var="actualTemplate" value="${requestScope.Template}"/>
<c:set var="selectedTemplate"/>
<c:set var="form" value="${null}"/>
<c:if test="${actualTemplate != null}">
  <c:set var="selectedTemplate" value="${actualTemplate.fileName}"/>
  <c:set var="form" value="${actualTemplate.searchForm}"/>
</c:if>
<c:set var="spaces" value="${requestScope.SpaceList}"/>
<c:set var="selectedSpace"/>
<c:set var="selectedComponent"/>
<c:set var="title" value=""/>
<c:set var="query" value="${requestScope.QueryParameters}"/>
<c:if test="${query != null}">
  <c:set var="selectedSpace" value="${query.spaceId}"/>
  <c:set var="selectedComponent" value="${query.instanceId}"/>
  <c:set var="title" value="${silfn:defaultEmptyString(query.keywords)}"/>
</c:if>
<c:set var="components" value="${requestScope.ComponentList}"/>
<c:set var="context" value="${requestScope.context}"/>
<c:set var="data" value="${requestScope.Data}"/>

<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="wysiwyg"/>
    <script type="text/javascript">
      function sendXMLRequest() {
        if (document.XMLSearchForm != null) {
          $.progressMessage();
          applyPlainTextSearch();
          document.XMLSearchForm.submit();
        } else {
          jQuery.popup.error('<fmt:message key="pdcPeas.choiceForm"/>');
        }
      }

      function chooseTemplate() {
        const valuePath = document.XMLRestrictForm.xmlSearchSelectedForm.value;
        if (valuePath.length > 0) {
          $.progressMessage();
          applyPlainTextSearch();
          document.XMLRestrictForm.action = "XMLSearchViewTemplate";
          document.XMLRestrictForm.submit();
        }
      }

      function applyPlainTextSearch() {
        $("input[name='TitleNotInXMLForm']").val($("#plainText").val());
      }

      function viewXmlSearch() {
        $.progressMessage();
        document.XMLRestrictForm.submit();
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="yui-skin-sam" id="globalSearchXML">
    <view:browseBar componentId="componentName"/>
    <view:window>
      <view:tabs>
        <view:tab label="${resultsTab}" action="LastResults" selected="false"/>
        <c:choose>
          <c:when test="${requestScope.ExpertSearchVisible}">
            <view:tab label="${simpleSearchTab}" action="ChangeSearchTypeToAdvanced"
                      selected="false"/>
            <view:tab label="${advancedSearchTab}" action="ChangeSearchTypeToExpert"
                      selected="false"/>
          </c:when>
          <c:otherwise>
            <view:tab label="${searchPageTab}" action="ChangeSearchTypeToAdvanced"
                      selected="false"/>
          </c:otherwise>
        </c:choose>
        <view:tab label="${searchByFormTab}" action="#" selected="true"/>
      </view:tabs>
      <view:frame>
        <div id="scope">
          <view:board>
            <form name="XMLRestrictForm" action="XMLRestrictSearch" method="post">
              <table>
                <tr>
                  <th scope="row" class="txtlibform" style="width: 200px">
                    <label for="searchSelection"><fmt:message key="pdcPeas.Template"/></label>
                  </th>
                  <td>
                    <select id="searchSelection" name="xmlSearchSelectedForm" size="1"
                            onchange="chooseTemplate();return;">
                      <option value=""><fmt:message key="GML.select"/></option>
                      <c:forEach var="template" items="${templates}">
                        <c:set var="selected" value=""/>
                        <c:if test="${template.fileName == selectedTemplate}">
                          <c:set var="selected" value="selected"/>
                        </c:if>
                        <option value="${template.fileName}" ${selected}>${template.name}</option>
                      </c:forEach>
                    </select>
                  </td>
                </tr>
                <tr></tr>
                <tr id="spaceList">
                  <th scope="row" class="txtlibform" style="width: 200px">
                    <label for="spaces"><fmt:message key="pdcPeas.DomainSelect"/></label>
                  </th>
                  <td>
                    <select id="spaces" name="spaces" size="1" onchange="viewXmlSearch()">
                      <option value=""><fmt:message key="pdcPeas.AllAuthors"/></option>
                      <c:forEach var="space" items="${spaces}">
                        <c:set var="selected" value=""/>
                        <c:if test="${space.id == selectedSpace}">
                          <c:set var="selected" value="selected"/>
                        </c:if>
                        <c:set var="incr" value=""/>
                        <c:if test="${space.level == 1}">
                          <c:set var="incr" value="&nbsp;&nbsp;"/>
                        </c:if>
                        <option value="${space.id}"
                          ${selected}>${incr}${silfn:escapeHtml(space.getName(language))}</option>
                      </c:forEach>
                    </select>
                  </td>
                </tr>
                <tr></tr>
                <c:if test="${components != null}">
                  <tr>
                    <th scope="row" class="txtlibform" style="width: 200px">
                      <label for="components"><fmt:message key="pdcPeas.ComponentSelect"/></label>
                    </th>
                    <td>
                      <select id="components" name="componentSearch" size="1"
                              onchange="viewXmlSearch()">
                        <option value=""><fmt:message key="pdcPeas.AllAuthors"/></option>
                        <c:forEach var="component" items="${components}">
                          <c:set var="selected" value=""/>
                          <c:if test="${component.id == selectedComponent}">
                            <c:set var="selected" value="selected"/>
                          </c:if>
                          <option
                              value="${component.id}" ${selected}>${silfn:escapeHtml(component.getName(language))}</option>
                        </c:forEach>
                      </select>
                    </td>
                  </tr>
                  <tr></tr>
                </c:if>
                <tr>
                  <th scope="row" style="width: 200px" class="txtlibform">
                    <label for="plainText"><fmt:message key="GML.search"/></label>
                  </th>
                  <td><input type="text" id="plainText" size="50" value="${title}"/></td>
                </tr>
              </table>
              <input type="hidden" name="sortOrder" value="<c:out value="${param.sortOrder}"/>"/>
              <input type="hidden" name="TitleNotInXMLForm" value="${title}"/>
            </form>
          </view:board>
        </div>

        <c:if test="${form != null}">
          <div id="template">
            <form name="XMLSearchForm" method="post" action="XMLSearch"
                  enctype="multipart/form-data">
              <input type="hidden" name="TitleNotInXMLForm" value="${title}"/>
              <%
                PublicationTemplate template = (PublicationTemplate) request.getAttribute("Template");
                DataRecord emptyData = (DataRecord) request.getAttribute("Data");
                PagesContext context = (PagesContext) request.getAttribute("context");
                Form form = template.getSearchForm();
                form.display(out, context, emptyData);
              %>
            </form>
          </div>
          <br/>
        </c:if>
        <view:buttonPane>
          <view:button label="${searchAction}" action="javascript:sendXMLRequest();"
                       disabled="false"/>
        </view:buttonPane>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>
