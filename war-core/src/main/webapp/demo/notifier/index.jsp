<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Demo notifier</title>
<view:looknfeel/>
<view:includePlugin name="notifier"/>
</head>
<body>
<h1>Usage examples of Silverpeas plugin 'notifier'</h1>
<ul>
<li><a href="javascript:onclick=notyInfo('This is an information')">Display 'info' noty</a></li>
<li><a href="javascript:onclick=notySuccess('Great! It works!')">Display 'success' noty</a></li>
<li><a href="javascript:onclick=notyWarning('Warning! Close it manually!')">Display 'warning' noty</a></li>
<li><a href="javascript:onclick=notyError('Error! Error! Error!')">Display 'error' noty</a></li>
</ul>
</body>
</html>