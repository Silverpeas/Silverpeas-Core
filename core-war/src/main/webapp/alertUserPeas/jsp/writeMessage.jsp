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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
      response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
      response.setHeader("Pragma", "no-cache"); //HTTP 1.0
      response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="org.silverpeas.core.admin.user.model.Group"%>
<%@ page import="org.silverpeas.core.util.Pair" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%@ include file="check.jsp" %>


<html>
  <head>
    <title><%=resource.getString("GML.popupTitle")%></title>
    <view:looknfeel/>
    <script language="JavaScript">
      function validateUsers() {
        document.EDform.submit();
      }
    </script>
  </head>
  <%
        String componentURL = (String) request.getAttribute("myComponentURL");
        UserDetail[] userDetails = (UserDetail[]) request.getAttribute("UserR");
        Group[] groups = (Group[]) request.getAttribute("GroupR");
        Pair<String, String> hostComponentNameObject = (Pair<String, String>) request.getAttribute("HostComponentName");
        String hostSpaceName = (String) request.getAttribute("HostSpaceName");
        String hostComponentName = hostComponentNameObject.getFirst();
  %>

  <body>

    <%
          browseBar.setDomainName(hostSpaceName);
          browseBar.setComponentName(hostComponentName);

          out.println(window.printBefore());
          out.println(frame.printBefore());

          //button
          Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false);
          Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=validateUsers();", false);


          //Icons
          String noColorPix = resource.getIcon("alertUserPeas.px");
    %>


    <form name="EDform" Action="<%=componentURL%>ToAlert" method="POST" accept-charset="UTF-8">
      <center>
        <table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
          <tr>
            <td nowrap>
              <table border="0" cellspacing="0" cellpadding="0" class="contourintfdcolor" width="100%"><!--tabl1-->
                <%
                   if (userDetails.length > 0) {
                %>
                <tr>
                  <td align="center" class="txttitrecol" colspan="2">
                    <%=resource.getString("GML.users")%>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" align="center" class="intfdcolor" height="1" width="70%"><img src="<%=noColorPix%>"></td>
                </tr>

                <%
                      for (int i = 0; i < userDetails.length; i++) {
                        UserDetail userDetail = userDetails[i];
                        String actorName = userDetail.getFirstName() + " " + userDetail.getLastName();
                %>
                <tr>
                  <td align="center" colspan="2">
                    <%=actorName%>
                  </td>
                </tr>
                <%
                       }
                %>
                <tr width="70%">
                  <td colspan="2" align="center" class="intfdcolor"  height="1" width="70%"><img src="<%=noColorPix%>"></td>
                </tr>
                <%
                     }
                     if (groups.length > 0) {
                %>
                <tr>
                  <td align="center" class="txttitrecol" colspan="2">
                    <%=resource.getString("GML.groupes")%>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" align="center" class="intfdcolor" height="1" width="70%"><img src="<%=noColorPix%>"></td>
                </tr>

                <%
                                        for (int i = 0; i < groups.length; i++) {
                                          Group group = groups[i];
                                          String groupName = group.getName();
                %>
                <tr>
                  <td align="center" colspan="2">
                    <%=groupName%>
                  </td>
                </tr>
                <%
                                        }
                %>
                <tr width="70%">
                  <td colspan="2" align="center" class="intfdcolor"  height="1" width="70%"><img src="<%=noColorPix%>"></td>
                </tr>
                <%
                      }
                %>
                <tr>
                  <td colspan="2" align="center" class="txtlibform">
                    <b><%=resource.getString("AuthorMessage")%></b> : <br/><textarea cols="80" rows="8" name="messageAux"></textarea>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </center>
    </form>
    <%
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(validateButton);
          buttonPane.addButton(cancelButton);
          buttonPane.setHorizontalPosition();
          out.println("<br/><center>" + buttonPane.print() + "<br></center>");
          out.println(frame.printAfter());
          out.println(window.printAfter());
    %>
  </body>
</html>