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

<%@page import="org.silverpeas.core.contribution.content.form.displayers.VideoFieldDisplayer"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp" %>
<script type="text/javascript">
  function isCorrectForm() {
    checkFieldName();
    return checkErrors();
  }
</script>
  <%
      String width = VideoFieldDisplayer.DEFAULT_WIDTH;
      String height = VideoFieldDisplayer.DEFAULT_HEIGHT;
      boolean autoplay = VideoFieldDisplayer.DEFAULT_AUTOPLAY;
      if (field != null) {
        if (parameters.containsKey(VideoFieldDisplayer.PARAMETER_WIDTH)) {
          width = parameters.get(VideoFieldDisplayer.PARAMETER_WIDTH);
        }

        if (parameters.containsKey(VideoFieldDisplayer.PARAMETER_HEIGHT)) {
          height = parameters.get(VideoFieldDisplayer.PARAMETER_HEIGHT);
        }

        if (parameters.containsKey(VideoFieldDisplayer.PARAMETER_AUTOPLAY)) {
          autoplay = (Boolean.valueOf(parameters.get(VideoFieldDisplayer.PARAMETER_AUTOPLAY))).booleanValue();
        } else {
          autoplay = false;
        }
      }
  %>
  <%@ include file="includeTopField.jsp" %>
<tr>
  <td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.video.width")%> :</td><td><input type="text" name="Param_<%=VideoFieldDisplayer.PARAMETER_WIDTH%>" value="<%=width%>" size="5" maxLength="4"/></td>
</tr>
<tr>
  <td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.video.height")%> :</td><td><input type="text" name="Param_<%=VideoFieldDisplayer.PARAMETER_HEIGHT%>" value="<%=height%>" size="5" maxLength="4"/></td>
</tr>
<tr>
  <td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.video.autoplay")%> :</td><td><input type="checkbox" name="Param_<%=VideoFieldDisplayer.PARAMETER_AUTOPLAY%>" value="true" <%=(autoplay? "checked=\"checked\"": "")%> /></td>
</tr>
<%@ include file="includeBottomField.jsp" %>