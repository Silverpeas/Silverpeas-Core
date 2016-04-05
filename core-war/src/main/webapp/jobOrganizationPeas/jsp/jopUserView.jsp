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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>

<%@page import="org.silverpeas.web.joborganization.control.JobOrganizationPeasSessionController"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserFull"%>
<%@ page import="org.silverpeas.core.util.EncodeHelper"%>

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%
	response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="context" value="${pageContext.request.contextPath}"/>

<c:set var="userId" value="${requestScope.userid}" />
<c:set var="userInfos" value="${requestScope.user}" />
<c:set var="groupInfos" value="${requestScope.group}" />
<c:set var="superGroupName" value="${requestScope.superGroupName}" />
<c:set var="message" value="${requestScope.message}" />
<c:set var="isRightCopyReplaceActivated" value="${requestScope.isRightCopyReplaceActivated}" />
<c:set var="isAdmin" value="${requestScope.isAdmin}" />
<c:if test="${not empty userInfos}">
	<c:set var="lastName" value="${userInfos.lastName}" />
	<c:set var="displayedLastName"><view:encodeHtml string="${lastName}" /></c:set>
	<c:set var="firstName" value="${userInfos.firstName}" />
	<c:set var="displayedFirstName"><view:encodeHtml string="${firstName}" /></c:set>
	<c:set var="firstName" value="${userInfos.firstName}" />
	<c:set var="displayedFirstName"><view:encodeHtml string="${firstName}" /></c:set>
	<c:set var="email" value="${userInfos.eMail}" />
	<c:set var="displayedEmail"><view:encodeHtml string="${email}" /></c:set>
	<c:set var="login" value="${userInfos.login}" />
	<c:set var="displayedLogin"><view:encodeHtml string="${login}" /></c:set>
	<c:set var="domain" value="${userInfos.domain.name}" />
	<c:set var="displayedDomain"><view:encodeHtml string="${domain}" /></c:set>
</c:if>
<c:if test="${not empty groupInfos}">
	<c:set var="groupName" value="${groupInfos.name}" />
	<c:set var="displayedGroupName"><view:encodeHtml string="${groupName}" /></c:set>
	<c:set var="groupNbUser" value="${fn:length(groupInfos.userIds)}" />
	<c:set var="groupDesc" value="${groupInfos.description}" />
	<c:set var="displayedGroupDesc"><view:encodeHtml string="${groupDesc}" /></c:set>
	<c:set var="displayedSuperGroupName"><view:encodeHtml string="${superGroupName}" /></c:set>
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
    <title><fmt:message key="GML.popupTitle"/></title>
	<view:includePlugin name="jquery"/>
	<view:includePlugin name="qtip"/>

    <script type="text/javascript">
    <!--
    function MM_reloadPage(init) {  //reloads the window if Nav4 resized
        if (init == true)
          with (navigator) {
            if ((appName == "Netscape") && (parseInt(appVersion) == 4)) {
              document.MM_pgW = innerWidth;
              document.MM_pgH = innerHeight;
              onresize = MM_reloadPage;
            }
          }
        else if (innerWidth != document.MM_pgW || innerHeight != document.MM_pgH)
          location.reload();
      }
      function showPDCSubscription() {
        chemin = '<%=(m_context + URLUtil.getURL(URLUtil.CMP_PDCSUBSCRIPTION))%>showUserSubscriptions.jsp?userId=${userId}';
        largeur = "600";
        hauteur = "440";
        SP_openWindow(chemin, "pdcWindow", largeur, hauteur, "resizable=yes,scrollbars=yes");
      }

      MM_reloadPage(true);

      var groupWindow = window;
      function openGroup(groupId) {
        url = '<%=m_context%>/RjobDomainPeas/jsp/groupOpen?groupId=' + groupId;
        windowName = "groupWindow";
        larg = "800";
        haut = "800";
        windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
        if (!groupWindow.closed && groupWindow.name == "groupWindow") {
          groupWindow.close();
        }
        groupWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
      }

      var componentWindow = window;
      function openComponent(componentId) {
        url = '<%=m_context%>/RjobStartPagePeas/jsp/OpenComponent?ComponentId=' + componentId;
        windowName = "componentWindow";
        larg = "800";
        haut = "800";
        windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
        if (!componentWindow.closed && componentWindow.name == "componentWindow") {
          componentWindow.close();
        }
        componentWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
      }

	function isCorrectForm() {
		var errorMsg = "";
		var errorNb = 0;
		var sourceRightsId = stripInitialWhitespace(document.rightsForm.sourceRightsId.value);

		if (isWhitespace(sourceRightsId)) {
		errorMsg+=" - '<fmt:message key="JOP.as"/>' <fmt:message key="GML.MustBeFilled"/>\n";
		errorNb++;
		}

		var result = false;
		switch (errorNb) {
		case 0 :
				result = true;
				break;
			case 1 :
				errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
				window.alert(errorMsg);
				break;
			default :
				errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
				window.alert(errorMsg);
				break;
		}
		return result;
	}

    <c:if test="${isRightCopyReplaceActivated && isAdmin}">
      $(function() {
          $("#assignRightsDialog").dialog({
          autoOpen: false,
          resizable: false,
          modal: true,
          height: "auto",
          width: 550,
          buttons: {
            "<fmt:message key="GML.ok"/>": function() {
		if (isCorrectForm()) {
                $.progressMessage();
			if(!document.rightsForm.checkNodeAssignRights.checked) {
				document.rightsForm.nodeAssignRights.value = "false";
			}
			document.rightsForm.action = "AssignRights";
			document.rightsForm.submit();
		}
            },
            "<fmt:message key="GML.cancel" />": function() {
              $(this).dialog("close");
            }
          }
        });
      });
      function assignSameRights() {
	  $("#assignRightsDialog").dialog("open");
      }
    </c:if>

	// Create the tooltips only on document load
      $(document).ready(function() {
        // Use the each() method to gain access to each elements attributes
        $('a[rel]', $('#content')).each(function() {
          $(this).qtip({
            content : {
              // Set the text to an image HTML string with the correct src URL
              ajax : {
                url : $(this).attr('rel') // Use the rel attribute of each element for the url to load
              },
              text : "Loading..."
            },
            position : {
              adjust : {
                method : "flip flip"
              },
              at : "bottom center", // Position the tooltip above the link
              my : "top center",
              viewport : $(window) // Keep the tooltip on-screen at all times
            },
            show : {
              solo : true, // Only show one tooltip at a time
              event : "mouseover"
            },
            hide : {
              event : "mouseout"
            },
            style : {
              tip : true,
              classes : "qtip-shadow qtip-green"
            }
          });
        });
      });
    //-->
    </script>
</head>
<body id="profil">

	<fmt:message key="JOP.userPanelAccess" var="selectIcon" bundle="${icons}" />
	<fmt:message key="JOP.select" var="selectAction" />
	<fmt:message key="PDCSubscription.subscriptions" var="subscriptionIcon" bundle="${icons}" />
	<fmt:message key="PDCSubscription.show" var="subscriptionAction" />
	<fmt:message key="JOP.assignRights" var="assignRightsAction" />
	<view:operationPane>
		<view:operation altText="${selectAction}" icon="${deleteIcon}" action="Main" />
		<c:if test="${silfn:isDefined(userId)}">
			<view:operation altText="${subscriptionAction}" icon="${subscriptionIcon}" action="javascript:showPDCSubscription()" />
		</c:if>
    <c:if test="${isRightCopyReplaceActivated && isAdmin}">
	    <view:operation altText="${assignRightsAction}" icon="${selectIcon}" action="javascript:assignSameRights()" />
    </c:if>
	</view:operationPane>

	<div id="content">
	<view:window>
	<view:frame>
	<c:if test="${not empty message}">
		<div class="inlineMessage">${message}</div>
	</c:if>
	<%
	UserFull userInfos = (UserFull) request.getAttribute("user");
	%>

		<c:if test="${not empty userInfos}">
		<div class="table">
				<div class="cell">
				<fieldset class="skinFieldset" id="identity-main">
				<legend><fmt:message key="JOP.profile.fieldset.main"/></legend>
				<ul class="fields">
					<!--Last name-->
					<li id="form-row-lastname" class="field">
					<label class="txtlibform"><fmt:message key="GML.lastName"/></label>
					<div class="champs">${displayedLastName}</div>
				</li>
					<!--Surname-->
					<li id="form-row-surname" class="field">
					<label class="txtlibform"><fmt:message key="GML.surname"/></label>
					<div class="champs">${displayedFirstName}</div>
				</li>
				<!---Email-->
					<li id="form-row-email" class="field">
					<label class="txtlibform"><fmt:message key="GML.eMail"/></label>
					<div class="champs"><a href="mailto:${displayedEmail}">${displayedEmail}</a></div>
				</li>
				<!---Rights-->
					<li id="form-row-rights" class="field">
					<label class="txtlibform"><fmt:message key="JOP.userRights"/></label>
					<div class="champs">
						<c:choose>
							<c:when test="${userInfos.accessLevel.code == 'A'}">
								<fmt:message key="GML.administrateur"/>
							</c:when>
							<c:when test="${userInfos.accessLevel.code == 'G'}">
								<fmt:message key="GML.guest"/>
							</c:when>
							<c:when test="${userInfos.accessLevel.code == 'K'}">
								<fmt:message key="GML.kmmanager"/>
							</c:when>
							<c:when test="${userInfos.accessLevel.code == 'D'}">
								<fmt:message key="GML.domainManager"/>
							</c:when>
							<c:when test="${userInfos.accessLevel.code == 'U'}">
								<fmt:message key="GML.user"/>
							</c:when>
							<c:otherwise>
								<fmt:message key="GML.no"/>
							</c:otherwise>
						</c:choose>
					</div>
				</li>
				<!---State-->
					<li id="form-row-rights" class="field">
					<label class="txtlibform"><fmt:message key="JOP.userState"/></label>
					<div class="champs"><fmt:message key="GML.user.account.state.${userInfos.state.name}"/></div>
				</li>
						<!--Login-->
					<li id="form-row-login" class="field">
					<label class="txtlibform"><fmt:message key="GML.login"/></label>
					<div class="champs">${displayedLogin}</div>
				</li>
				<!--Password Silverpeas ? -->
						<li id="form-row-passwordsp" class="field">
					<label class="txtlibform"><fmt:message key="JOP.silverPassword"/></label>
					<div class="champs">
						<c:choose>
							<c:when test="${userInfos.passwordAvailable && userInfos.passwordValid}">
								<fmt:message key="GML.yes"/>
							</c:when>
							<c:otherwise>
								<fmt:message key="GML.no"/>
							</c:otherwise>
						</c:choose>
					</div>
				</li>
				<!--Domain-->
						<li id="form-row-domain" class="field">
					<label class="txtlibform"><fmt:message key="JOP.domain"/></label>
					<div class="champs">${displayedDomain}</div>
				</li>
					</ul>
			</fieldset>
		</div>

				<div class="cell">
			<fieldset class="skinFieldset" id="identity-extra">
			<legend class="without-img"><fmt:message key="JOP.profile.fieldset.extra"/></legend>
					<ul class="fields">
					<%
		            String[] specificKeys = userInfos.getPropertiesNames();
		            int nbStdInfos = 4;
		            int nbInfos = nbStdInfos + specificKeys.length;
		            String currentKey = null;
		            for (int iSL = nbStdInfos; iSL < nbInfos; iSL++) {
						currentKey = specificKeys[iSL - nbStdInfos];
						// Not display the password !
						if (!currentKey.startsWith("password")) {
					%>
					<!--Specific Info-->
					<li id="form-row-<%=currentKey%>" class="field">
							<label class="txtlibform">
								<%=EncodeHelper.javaStringToHtmlString(userInfos.
						    getSpecificLabel(resource.getLanguage(),
							currentKey))%>
					</label>
							<div class="champs">
								<%
					            if ("STRING".equals(userInfos.getPropertyType(currentKey)) ||
					                "USERID".equals(userInfos.getPropertyType(currentKey))) {
								%>
								<%=EncodeHelper.javaStringToHtmlString(userInfos.getValue(currentKey))%>
								<%
					            } else if ("BOOLEAN".equals(userInfos.getPropertyType(currentKey))) {

					              if (userInfos.getValue(currentKey) != null &&
					                  "1".equals(userInfos.getValue(currentKey))) {
					            %>
							<fmt:message key="GML.yes"/>
					            <%
					              } else if (userInfos.getValue(currentKey) == null ||
					                  "".equals(userInfos.getValue(currentKey)) ||
					                  "0".equals(userInfos.getValue(currentKey))) {
					            %>
							<fmt:message key="GML.no"/>
					           <%
					              }
					            }
					           %>
							</div>
		                </li>
				<%
				}
			}
				%>
				</ul>
			</fieldset>
			</div>
			</div>
		</c:if>

		<c:if test="${not empty groupInfos}">
		<div class="table">
				<div class="cell">
				<fieldset class="skinFieldset" id="identity-main">
				<legend><fmt:message key="JOP.profile.fieldset.main"/></legend>
				<ul class="fields">
					<!--Name-->
					<li id="form-row-groupName" class="field">
					<label class="txtlibform"><fmt:message key="GML.name"/></label>
					<div class="champs">${displayedGroupName}</div>
				</li>
				<!--Nb user-->
					<li id="form-row-groupNbUser" class="field">
					<label class="txtlibform"><fmt:message key="GML.users"/></label>
					<div class="champs">${groupNbUser}</div>
				</li>
				<!--Description-->
				<li id="form-row-groupDesc" class="field">
					<label class="txtlibform"><fmt:message key="GML.description"/></label>
					<div class="champs">${displayedGroupDesc}</div>
				</li>
				<!--Parent group name-->
				<li id="form-row-superGroupName" class="field">
					<label class="txtlibform"><fmt:message key="JOP.parentGroup"/></label>
					<div class="champs">${displayedSuperGroupName}</div>
				</li>
					</ul>
			</fieldset>
			</div>
			</div>
		</c:if>

		<c:if test="${(empty userInfos) && (empty groupInfos)}">
			<div class="table">
				<div class="cell">
				<fieldset class="skinFieldset" id="identity-main">
				<legend><fmt:message key="JOP.profile.fieldset.main"/></legend>
				<ul class="fields">
					<!--Name-->
					<li id="form-row-groupName" class="field">
					<label class="txtlibform"><fmt:message key="JOP.noSelection"/></label>
					<div class="champs"></div>
				</li>
				</ul>
			</fieldset>
			</div>
			</div>
		</c:if>

        <%
          // Groups (only for user and not for group view)
          String[][] groups = (String[][]) request.getAttribute("groups");
          if (groups != null && groups.length > 0) {
        %>
		<fieldset class="skinFieldset" id="profil-groups-belong">
        <legend><fmt:message key="JOP.groups"/></legend>
        <%
            ArrayPane arrayPane = gef.getArrayPane("profil-groups", "ViewUserOrGroup", request, session);

            arrayPane.setVisibleLineNumber(-1);

            arrayPane.addArrayColumn(resource.getString("GML.name"));
            arrayPane.addArrayColumn(resource.getString("GML.users"));
            arrayPane.addArrayColumn(resource.getString("GML.description"));

            for (final String[] group : groups) {
              //création des ligne de l'arrayPane
              ArrayLine arrayLine = arrayPane.addArrayLine();
              arrayLine.addArrayCellText("<a href=\"#\" onclick=\"openGroup('" + group[0] +
                  "')\" rel=\"/silverpeas/JobDomainPeasGroupPathServlet?GroupId=" + group[0] +
                  "\">" + group[1] + "</a>");
              arrayLine.addArrayCellText(group[2]);
              arrayLine.addArrayCellText(group[3]);
            }
            if (arrayPane.getColumnToSort() == 0) {
              arrayPane.setColumnToSort(1);
            }
            out.println(arrayPane.print());
        %>
        </fieldset>
        <%
          }

          // Manageable Spaces
          String[] spaces = (String[]) request.getAttribute("spaces");
          if (spaces != null && spaces.length > 0) {
        %>
		<fieldset class="skinFieldset" id="profil-spaces-management">
        <legend><fmt:message key="JOP.spaces"/></legend>
        <%
            ArrayPane arrayPane = gef.getArrayPane("profil-spaces", "ViewUserOrGroup", request, session);

            arrayPane.setVisibleLineNumber(-1);

            arrayPane.addArrayColumn(resource.getString("GML.name"));

            for (int i = 0; i < spaces.length; i++) {
              //création des ligne de l'arrayPane
              ArrayLine arrayLine = arrayPane.addArrayLine();
              arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(spaces[i]));
            }
            if (arrayPane.getColumnToSort() == 0) {
              arrayPane.setColumnToSort(1);
            }
            out.println(arrayPane.print());
        %>
        </fieldset>
        <%
          }

          // Instances and roles sorted by spaces
          List profiles = (List) request.getAttribute("profiles");
          if (profiles != null && profiles.size() > 0) {
        %>
        <fieldset class="skinFieldset" id="profiles">
        <legend><fmt:message key="JOP.profiles"/></legend>
        <%
            ArrayPane arrayPane = gef.getArrayPane("profiles", "ViewUserOrGroup", request, session);

            arrayPane.setVisibleLineNumber(-1);

            arrayPane.addArrayColumn(resource.getString("GML.space"));
            arrayPane.addArrayColumn(resource.getString("JOP.instance"));
            arrayPane.addArrayColumn(resource.getString("GML.jobPeas"));
            arrayPane.addArrayColumn(resource.getString("JOP.profile"));

            String[] profile = null;
            for (int i = 0; i < profiles.size(); i++) {
              profile = (String[]) profiles.get(i);

              //création des ligne de l'arrayPane
              ArrayLine arrayLine = arrayPane.addArrayLine();
              arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(profile[0]));
              arrayLine.addArrayCellText("<a href=\"#\" onclick=\"openComponent('" + profile[2] + profile[1]
                  + "')\" rel=\"/silverpeas/JobDomainPeasComponentPathServlet?ComponentId=" + profile[1]
                  + "\">" + EncodeHelper.javaStringToHtmlString(profile[3]) + "</a>");
              arrayLine.addArrayCellText(profile[4]);
              arrayLine.addArrayCellText(profile[5]);
            }
            if (arrayPane.getColumnToSort() == 0) {
              arrayPane.setColumnToSort(1);
            }
            out.println(arrayPane.print());
		%>
        </fieldset>
        <%
          }
        %>
    </view:frame>
	</view:window>
	</div>

    <!-- Dialog choice rights -->
    <fmt:message key="JOP.sourceRightsUserPanel" var="sourceRightsUserPanelIcon" bundle="${icons}" />
    <fmt:message key="JOP.mandatory" var="mandatoryIcon" bundle="${icons}" />

<c:if test="${isRightCopyReplaceActivated && isAdmin}">
	<div id="assignRightsDialog" title="<fmt:message key="JOP.assignRights"/>">
	  <form accept-charset="UTF-8" enctype="multipart/form-data;charset=utf-8" id="affected-profil"
		name="rightsForm" action="#" method="post">
		<label class="label-ui-dialog" for="profil-from"><fmt:message key="JOP.as"/></label>
		<span class="champ-ui-dialog">
		<input type="text" id="sourceRightsName" name="sourceRightsName" value="" size="50" readonly="readonly"/>
		<a title="<fmt:message key="JOP.sourceRightsUserPanel"/>" href="#" onclick="javascript:SP_openWindow('SelectRightsUserOrGroup','SelectUserGroupWindow',800,600,'');">
				<img src="${context}${sourceRightsUserPanelIcon}"
					alt="<fmt:message key="JOP.sourceRightsUserPanel"/>"
					title="<fmt:message key="JOP.sourceRightsUserPanel"/>"/>
			</a>
			&nbsp;<img src="${context}${mandatoryIcon}" width="5" height="5" border="0"/>
	        <input type="hidden" name="sourceRightsId" id="sourceRightsId" value=""/>
	        <input type="hidden" name="sourceRightsType" id="sourceRightsType" value=""/>
		</span>
		<label class="label-ui-dialog"><fmt:message key="JOP.assignMode"/></label>
	    <span class="champ-ui-dialog">
		<input type="radio" name="choiceAssignRights" id="choiceAssignRights" value="<%=JobOrganizationPeasSessionController.ADD_RIGHTS%>" checked="checked"/>
		<strong><fmt:message key="JOP.addRights"/></strong> <fmt:message key="JOP.actualRights"/>
		<input type="radio" name="choiceAssignRights" id="choiceAssignRights" value="<%=JobOrganizationPeasSessionController.REPLACE_RIGHTS%>"/>
		<strong><fmt:message key="JOP.replaceRights"/></strong> <fmt:message key="JOP.theActualRights"/>
	    </span>
	    <label class="label-ui-dialog"></label>
	    <span class="champ-ui-dialog">
		<input type="checkbox" name="checkNodeAssignRights" id="checkNodeAssignRights" checked="checked"/>
		<fmt:message key="JOP.nodeAssignRights"/>
			<input type="hidden" name="nodeAssignRights" id="nodeAssignRights" value="true"/>
	    </span>
		<label class="label-ui-dialog">
			<img src="${context}${mandatoryIcon}" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
		</label>
	  </form>
	</div>
</c:if>

  <view:progressMessage/>
  </body>
</html>
