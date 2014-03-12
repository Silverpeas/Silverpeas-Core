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

<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@ page import="com.stratelia.webactiv.beans.admin.AdminController"%>

<%@ include file="check.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  Board board = gef.getBoard();
  String userId = (String) request.getAttribute("userid"); //peut être null
%>
<html>
  <head>
    <title><%=resource.getString("GML.popupTitle")%></title>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

    <script language="javascript">
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
        SP_openWindow(chemin, "", largeur, hauteur, "resizable=yes,scrollbars=yes");
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
        if (userId != null && userId != "") {
          operationPane.addOperation(resource.getIcon("PDCSubscription.subscriptions"), resource.
              getString("PDCSubscription.show"), "javascript:showPDCSubscription()");
        }
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
              <%=Encode.javaStringToHtmlString(resource.getString("GML.lastName"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(userInfos.getLastName())%>
            </td>
          </tr>

          <!--Prénom-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("GML.surname"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(userInfos.getFirstName())%>
            </td>
          </tr>

          <!---mail-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("GML.eMail"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <a href="mailto:<%=Encode.javaStringToHtmlString(userInfos.geteMail())%>"><%=Encode.javaStringToHtmlString(userInfos.geteMail())%></a>
            </td>
          </tr>

          <!--Login-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("GML.login"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(userInfos.getLogin())%>
            </td>
          </tr>

          <!--mot de passe-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("JOP.silverPassword"))%>
              :
            </td>
            <td align='left' valign='baseline'>
              <%
                if (userInfos.isPasswordAvailable() && userInfos.isPasswordValid()) {
                  out.print(Encode.javaStringToHtmlString(resource.getString("GML.yes")));
                } else {
                  out.print(Encode.javaStringToHtmlString(resource.getString("GML.no")));
                }
              %>
            </td>
          </tr>

          <!--Login-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("JOP.domain"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(userInfos.getDomain().getName())%>
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
              <%=Encode.javaStringToHtmlString(userInfos.getSpecificLabel(resource.
            getLanguage(),
            currentKey))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%
                out.print(Encode.javaStringToHtmlString(userInfos.getValue(currentKey)));

              %>
            </td>
          </tr>
          <%
              }
            }
          } else if (groupInfos != null) {//Group
          %>
          <!--Nom-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("GML.name"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(groupInfos.getName())%>
            </td>
          </tr>

          <!--Nbre d'utilisateurs-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("GML.users"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(String.valueOf(
        groupInfos.getUserIds().length))%>
            </td>
          </tr>

          <!--Description-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("GML.description"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%=Encode.javaStringToHtmlString(groupInfos.getDescription())%>
            </td>
          </tr>

          <!--Groupe parent-->
          <tr>
            <td class='textePetitBold'>
              <%=Encode.javaStringToHtmlString(resource.getString("JOP.parentGroup"))%>
              :
            </td>
            <td align=left valign='baseline'>
              <%
                String parentId = groupInfos.getSuperGroupId();
                if (parentId == null || parentId.equals("")) {
              %>
              -
              <%} else {
                AdminController adminController = (AdminController) request.getAttribute("adminController");
              %>
              <%=Encode.javaStringToHtmlString(adminController.getGroupName(
          groupInfos.getSuperGroupId()))%>
              <%
                }
              %>
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
  </body>
</html>
