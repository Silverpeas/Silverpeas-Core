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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="browseContext" value="${requestScope.browseContext}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<html>
  <head>
    <view:looknfeel />



    <style type="text/css">
      * {
        margin: 0;
        padding: 0;
      }



    </style>
  </head>




  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
    <c:url value="/Rdirectory/Directory" var="Directory" />
    <view:window>

      <view:frame >

        <view:board>
          <form Name="photoForm" action="validation" Method="POST" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
            <table cellpadding="5" width="100%">
              <tr>
                <td class="txtlibform"><fmt:message key="directory.photo" />:</td>
                <td><input type="file" name="WAIMGVAR0" size="60"></td>
              </tr>
              <tr>
                <td class="txtlibform" colspan="2" align="center">
                  <br>
                  <br>
                  <fmt:message key="directory.buttonValid" var="valid"/>
                  <view:button label="${valid}" action="javascript:document.photoForm.submit();" disabled="false" />
                </td>
              </tr>
            </table>
          </form>
        </view:board>
      </view:frame>

    </view:window>

  </body>
</html>