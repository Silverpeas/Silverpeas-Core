<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<!DOCTYPE html>
<html>
<head>
  <title>Demo VueJS</title>
  <view:looknfeel/>
</head>
<body>
<a href="../index.jsp">Go back</a>
<h1>VueJS - Silverpeas's Link Components</h1>
<p><strong>Fell free to add more!!!</strong></p>
<div id="root">
  <div style="display: inline-block">
    <span>Fill help about following links </span>
    <input type="text" v-model="demo1.help">
    <silverpeas-link v-bind:title="demo1.help"> THE LINK</silverpeas-link>
  </div>
</div>
<script type="text/javascript">
  window.vm = new Vue({
    el : '#root',
    data : function() {
      return {
        demo1 : {
          help : ''
        }
      }
    }
  });
</script>
</body>
</html>