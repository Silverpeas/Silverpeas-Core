<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.silverpeas.socialNetwork.myProfil.servlets.MyProfileRoutes" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@page import="com.stratelia.webactiv.beans.admin.SpaceInstLight" %>
<%@page import="java.util.List" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<c:set var="preferences" value="${request.preferences}"/>
<%
  List availableLooks = gef.getAvailableLooks();
  pageContext.setAttribute("availableLooks", availableLooks);
  List spaceTreeview = (List) request.getAttribute("SpaceTreeview");

%>
<form name="UserForm" action="<%=MyProfileRoutes.UpdateMySettings %>" method="post">
  <table border="0" cellspacing="0" cellpadding="5" width="100%">
    <!-- Language -->
    <tr>
      <td class="txtlibform"><fmt:message key="myProfile.preferences.FavoriteLanguage"/> :</td>
      <td>
        <select name="SelectedLanguage" size="1">
          <c:forEach items="${request.AllLanguages}" var="language">
            <c:choose>
              <c:when test="${request.preferences.language eq language}">
                <option value="<c:out value="${language}"/>" selected="selected"><fmt:message
                    key="myProfile.preferences.language_+ ${language}"/></option>
              </c:when>
              <c:otherwise>
                <option value="<c:out value="${language}"/>"><fmt:message
                    key="myProfile.preferences.language_+ ${language}"/></option>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </select>
      </td>
    </tr>
    <c:choose>
      <!-- Look -->
      <c:when test="{not empty availableLooks}">
        <tr>
          <td class="txtlibform"><fmt:message key="myProfile.preferences.FavoriteLook"/> :</td>
          <td><select name="SelectedLook" size="1">
            <c:forEach items="${availableLooks}" var="look">
              <c:choose>
                <c:when test="${request.preferences.language eq look}">
                  <option value="<c:out value="${look}"/>" selected="selected"><c:out
                      value="${look}"/></option>
                </c:when>
                <c:otherwise>
                  <option value="<c:out value="${look}"/>"><c:out value="${look}"/></option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
          </td>
        </tr>
      </c:when>
      <c:otherwise>
        <input type="hidden" name="SelectedLook" value="<c:out value="${preferences.look}" />"/>
      </c:otherwise>
    </c:choose>
    <!-- defaultWorkSpace -->
    <tr>
      <td class="txtlibform"><fmt:message key="myProfile.preferences.DefaultWorkSpace"/> :</td>
      <td>
        <select name="SelectedWorkSpace" size="1">
          <c:if
              test="${empty preferences.personalWorkSpaceId || 'null' eq  preferences.personalWorkSpaceId}">
            <option value="null" selected="selected"><fmt:message
                key="UndefinedFavoriteSpace"/></option>
          </c:if>
          <c:set var="indentation" value=''/>
          <c:forEach items="${request.SpaceTreeview}" var="space">
            <c:forEach begin="0" end="${space.level}">
              <c:set var="indentation">"&nbsp;&nbsp;"<c:out value="${idndentation}"/></c:set>
            </c:forEach>
            <option value="<c:out value="${space.fullId}"/>" <c:if
                test="${space.fullid eq preferences.personalWorkSpaceId}">selected="selected"</c:if>>
              <c:out value="${indentation}"/> <c:out value="${space.name}"/></option>
          </c:forEach>
        </select>
      </td>
    </tr>
    <!-- ThesaurusState -->
    <tr>
      <td class="txtlibform"><fmt:message key="myProfile.preferences.Thesaurus"/> :</td>
      <td>
        <input name="opt_thesaurusStatus" type="checkbox"
               value="true" <c:if test="${preferences.}"<%=checkedThesaurus_activate%>/>
      </td>
    </tr>
    <!-- Drag&Drop -->
    <tr>
      <td class="txtlibform"><fmt:message key="myProfile.preferences.DragDrop"/> :</td>
      <td>
        <input name="opt_dragDropStatus" type="checkbox"
               value="true" <%=checkedDragDrop_activate%>/>
      </td>
    </tr>
    <!-- Webdav Editing -->
    <tr>
      <td class="txtlibform"><fmt:message key="myProfile.preferences.WebdavEditing"/> :</td>
      <td>
        <input name="opt_webdavEditingStatus" type="checkbox"
               value="true" <%=checkedWebdavEditing_activate%>/>
      </td>
    </tr>
  </table>
</form>
<fmt:message key="GML.validate" var="validate"/>
<fmt:message key="GML.cancel" var="cancel"/>
<view:buttonPane>
  <view:button action="javascript:onClick=submitForm();" label="${validate}" disabled="false"/>
  <view:button action="javascript:onClick=history.back();" label="${cancel}" disabled="false"/>
</view:buttonPane>

<script type="text/javascript">
  function submitForm() {
    var currentLook = '<caption:out balue="${preferences.look}"/>';
    if (document.UserForm.SelectedLook.value != currentLook) {
      alert("<fmt:message key="myProfile.preferences.ChangeLookAlert"/>");
    }

    document.UserForm.submit();
  }
</script>