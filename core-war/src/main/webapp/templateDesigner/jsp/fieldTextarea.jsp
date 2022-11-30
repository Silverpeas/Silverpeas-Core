<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@page import="org.silverpeas.core.contribution.content.form.displayers.TextAreaFieldDisplayer"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp" %>
<script type="application/javascript">
  function isCorrectForm()
  {
    checkFieldName();
    return checkErrors();
  }
</script>
<%
  String rows = "";
  String cols = "";
  String defaultValue = "";

  if (field != null) {
    if (parameters.containsKey(TextAreaFieldDisplayer.PARAM_ROWS)) {
      rows = parameters.get(TextAreaFieldDisplayer.PARAM_ROWS);
    }

    if (parameters.containsKey(TextAreaFieldDisplayer.PARAM_COLS)) {
      cols = parameters.get(TextAreaFieldDisplayer.PARAM_COLS);
    }

    if (parameters.containsKey("default")) {
      defaultValue = parameters.get("default");
    }
  }
%>
<%@ include file="includeTopField.jsp" %>
<tr>
  <td class="txtlibform"><%=resource.getString("templateDesigner.rows")%> :</td><td><input type="text" name="Param_<%= TextAreaFieldDisplayer.PARAM_ROWS%>" value="<%=rows%>" size="5" maxLength="3"/></td>
</tr>
<tr>
  <td class="txtlibform"><%=resource.getString("templateDesigner.cols")%> :</td><td><input type="text" name="Param_<%= TextAreaFieldDisplayer.PARAM_COLS%>" value="<%=cols%>" size="5" maxLength="3"/></td>
</tr>
<tr>
  <td class="txtlibform"><%=resource.getString("templateDesigner.displayer.default")%> :</td><td><textarea name="Param_default"><%=defaultValue%></textarea></td>
</tr>
<%@ include file="includeBottomField.jsp" %>