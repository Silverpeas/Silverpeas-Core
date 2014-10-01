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

<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@ page import="com.stratelia.webactiv.beans.admin.AdminController"%>
<%@ page import="com.silverpeas.jobOrganizationPeas.control.JobOrganizationPeasSessionController"%>

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="context" value="${pageContext.request.contextPath}"/>

<%
  Board board = gef.getBoard();
  String userId = (String) request.getAttribute("userid"); //peut être null
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><%=resource.getString("GML.popupTitle")%></title>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
	<script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
	<view:includePlugin name="jquery"/>
	
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
        chemin = '<%=(m_context + URLManager.getURL(URLManager.CMP_PDCSUBSCRIPTION))%>showUserSubscriptions.jsp?userId=<%=userId%>';
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
        	errorMsg+=" - '<fmt:message key="JOP.rightsFrom"/>' <fmt:message key="GML.MustBeFilled"/>\n";
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
      
      $(function() {
          $("#assignRightsDialog").dialog({
          autoOpen: false,
          resizable: false,
          modal: true,
          height: "auto",
          width: 500,
          buttons: {
            "<fmt:message key="GML.ok"/>": function() {
            	if (isCorrectForm()) {
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
      
    //-->
    </script>
    <%
      out.println(gef.getLookStyleSheet());
    %>
    <view:includePlugin name="qtip"/>
  </head>
  <BODY>
    <div id="content">
      <%
        operationPane.addOperation(resource.getIcon("JOP.userPanelAccess"), resource.getString(
            "JOP.select"), "Main");
      	if (StringUtil.isDefined(userId)) {
          operationPane.addOperation(resource.getIcon("PDCSubscription.subscriptions"), resource.
              getString("PDCSubscription.show"), "javascript:showPDCSubscription()");
        }
        operationPane.addOperation(resource.getIcon("JOP.userPanelAccess"), resource.getString(
            "JOP.assignRights"), "javascript:assignSameRights()");
        out.println(window.printBefore());
        out.println(frame.printBefore());
      %>
      <center>
        <%
          out.println(board.printBefore());
        %>
        <table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
          <%
            UserFull userInfos = (UserFull) request.getAttribute("user");
            Group groupInfos = (Group) request.getAttribute("group");

            if (userInfos != null) {//User
          %>
          <!--Nom-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.lastName"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(userInfos.getLastName())%>
            </td>
          </tr>

          <!--Prénom-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.surname"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(userInfos.getFirstName())%>
            </td>
          </tr>

          <!---mail-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.eMail"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <a href="mailto:<%=EncodeHelper.javaStringToHtmlString(userInfos.geteMail())%>"><%=Encode.javaStringToHtmlString(userInfos.geteMail())%></a>
            </td>
          </tr>

          <!--Login-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.login"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(userInfos.getLogin())%>
            </td>
          </tr>

          <!--mot de passe-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("JOP.silverPassword"))%>
              :
            </td>
            <td align='left' valign='baseline'>
              <%
                if (userInfos.isPasswordAvailable() && userInfos.isPasswordValid()) {
                  out.print(EncodeHelper.javaStringToHtmlString(resource.getString("GML.yes")));
                } else {
                  out.print(EncodeHelper.javaStringToHtmlString(resource.getString("GML.no")));
                }
              %>
            </td>
          </tr>

          <!--Login-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("JOP.domain"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(userInfos.getDomain().getName())%>
            </td>
          </tr>

          <%
            String[] specificKeys = userInfos.getPropertiesNames();
            int nbStdInfos = 4;
            int nbInfos = nbStdInfos + specificKeys.length;
            String currentKey = null;
            for (int iSL = nbStdInfos; iSL < nbInfos; iSL++) {
              currentKey = specificKeys[iSL - nbStdInfos];
              // On n'affiche pas le mot de passe !
              if (!currentKey.startsWith("password")) {
          %>
          <!--Specific Info-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(userInfos.getSpecificLabel(resource.
            getLanguage(),
            currentKey))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%
                out.print(EncodeHelper.javaStringToHtmlString(userInfos.getValue(currentKey)));

              %>
            </td>
          </tr>
          <%
              }
            }
          } else if (groupInfos != null) {//Group
				String superGroupName = (String) request.getAttribute("superGroupName");
          %>
          <!--Nom-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.name"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(groupInfos.getName())%>
            </td>
          </tr>

          <!--Nbre d'utilisateurs-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.users"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(String.valueOf(
        groupInfos.getUserIds().length))%>
            </td>
          </tr>

          <!--Description-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("GML.description"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(groupInfos.getDescription())%>
            </td>
          </tr>

          <!--Groupe parent-->
          <tr>
            <td class='textePetitBold'>
              <%=EncodeHelper.javaStringToHtmlString(resource.getString("JOP.parentGroup"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=EncodeHelper.javaStringToHtmlString(superGroupName)%>
            </td>
          </tr>
          <%
          } else {
          %>
          <tr><td class='textePetitBold'>
              <%=resource.getString("JOP.noSelection")%>
            </td></tr>
            <%
              }
            %>
        </table>
        <%
          out.println(board.printAfter());

          // Groups (only for user and not for group view)
          String[][] groups = (String[][]) request.getAttribute("groups");
          if (groups != null && groups.length > 0) {
            out.println("<br>");
            ArrayPane arrayPane = gef.getArrayPane("groups", "ViewUserOrGroup", request, session);

            arrayPane.setVisibleLineNumber(-1);
            arrayPane.setTitle(resource.getString("JOP.groups"));

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
          }

          // Manageable Spaces
          String[] spaces = (String[]) request.getAttribute("spaces");
          if (spaces != null && spaces.length > 0) {
            out.println("<br>");
            ArrayPane arrayPane = gef.getArrayPane("spaces", "ViewUserOrGroup", request, session);

            arrayPane.setVisibleLineNumber(-1);
            arrayPane.setTitle(resource.getString("JOP.spaces"));

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
          }

          // Instances and roles sorted by spaces
          List profiles = (List) request.getAttribute("profiles");
          if (profiles != null && profiles.size() > 0) {
            out.println("<br/>");
            ArrayPane arrayPane = gef.getArrayPane("profiles", "ViewUserOrGroup", request, session);

            arrayPane.setVisibleLineNumber(-1);
            arrayPane.setTitle(resource.getString("JOP.profiles"));

            arrayPane.addArrayColumn(resource.getString("GML.domains"));
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
          }
        %>

      </center>
      <%
        out.println(frame.printAfter());
        out.println(window.printAfter());
      %>
    </div>
    <script type="text/javascript">
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
    </script>
    
    <!-- Dialog choice rights -->
    <fmt:message key="JOP.sourceRightsUserPanel" var="sourceRightsUserPanelIcon" bundle="${icons}" />
    <fmt:message key="JOP.mandatory" var="mandatoryIcon" bundle="${icons}" />
	<div id="assignRightsDialog" title="<fmt:message key="JOP.assignRights"/>">
	  <form name="rightsForm" action="#" method="post">
	    <table>
	    	<tr>
	    		<td></td>
	          	<td>
	          		<input type="radio" name="choiceAssignRights" id="choiceAssignRights" value="<%=JobOrganizationPeasSessionController.REPLACE_RIGHTS%>" checked="checked"/><fmt:message key="JOP.replaceRights"/>
	          		<% 
	          		if (userInfos != null) {//User
	          		  out.println(" "+EncodeHelper.javaStringToHtmlString(userInfos.getDisplayedName())+" ");
	          		} else if (groupInfos != null) {//Group
	          		  out.println(" "+EncodeHelper.javaStringToHtmlString(groupInfos.getName())+" ");
	          		}
          			%>
          			<fmt:message key="GML.by"/>
	          	</td>
	        </tr>
	        <tr>
	        	<td></td>
	          	<td>
	          		<input type="radio" name="choiceAssignRights" id="choiceAssignRights" value="<%=JobOrganizationPeasSessionController.ADD_RIGHTS%>"/><fmt:message key="JOP.addRights"/>
	          		<% 
	          		if (userInfos != null) {//User
	          		  out.println(" "+EncodeHelper.javaStringToHtmlString(userInfos.getDisplayedName())+" ");
	          		} else if (groupInfos != null) {//Group
	          		  out.println(" "+EncodeHelper.javaStringToHtmlString(groupInfos.getName())+" ");
	          		}
          			%>
	          	</td>
	        </tr>
	        <tr>
	        	<td><fmt:message key="JOP.rightsFrom"/> : </td>
	          	<td>
			        <input type="text" name="sourceRightsName" id="sourceRightsName" value="" size="60" readonly="readonly"/>
			        <a href="#" onclick="javascript:SP_openWindow('SelectRightsUserOrGroup','SelectUserGroupWindow',800,600,'');">
						<img src="${context}${sourceRightsUserPanelIcon}" width="15" height="15" border="0" 
							alt="<fmt:message key="JOP.sourceRightsUserPanel"/>"
							title="<fmt:message key="JOP.sourceRightsUserPanel"/>"
							align="absmiddle"/>
					</a>
			        &nbsp;<img src="${context}${mandatoryIcon}" width="5" height="5" border="0"/>
			        <input type="hidden" name="sourceRightsId" id="sourceRightsId" value=""/>
			        <input type="hidden" name="sourceRightsType" id="sourceRightsType" value=""/>   
	          	</td>
	        </tr>
		</table>
		<div class="legend">
			<img src="${context}${mandatoryIcon}" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
		</div>
	  </form>
	</div>
  </body>
</html>
