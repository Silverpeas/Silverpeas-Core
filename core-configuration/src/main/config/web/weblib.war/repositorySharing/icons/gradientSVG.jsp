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

<jsp:directive.page contentType="image/svg+xml;charset=UTF-8" pageEncoding="UTF-8"/>
<svg xmlns:svg="http://www.w3.org/2000/svg" xmlns="http://www.w3.org/2000/svg" version="1.1" width="100%" height="100%">
<defs>
		<linearGradient id="linear-gradient" x1="0%" y1="0%" x2="<%=request.getParameter("vertical")%>%" y2="<%=request.getParameter("horizontal")%>%">
				<stop offset="0%" stop-color="#<%=request.getParameter("from")%>" stop-opacity="1"/>
				<stop offset="100%" stop-color="#<%=request.getParameter("to")%>" stop-opacity="1"/>
		</linearGradient>
</defs>
<rect width="100%" height="100%" fill="url(#linear-gradient)"/>
</svg>