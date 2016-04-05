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

<%@ page import="org.silverpeas.core.util.DateUtil,org.silverpeas.core.util.MultiSilverpeasBundle,java.util.Collection,java.util.Iterator"%>
<% MultiSilverpeasBundle resources = (MultiSilverpeasBundle) request.getAttribute("resources");
Collection cResultData = (Collection)request.getAttribute("ResultData");
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
response.setContentType("text/csv");
response.addHeader("Content-Disposition","attachment;filename=\"export_data_" + System.currentTimeMillis() + ".csv");
out.println(resources.getString("GML.name") + "," + resources.getString("silverStatisticsPeas.connectionNumber") + "," + resources.getString("silverStatisticsPeas.durationAvg"));
Iterator   iter = null;
String[] item = null;
if (cResultData != null) {
	iter = cResultData.iterator();
	if(iter.hasNext()) {
		String title;
		while (iter.hasNext()) {
		item = (String[]) iter.next();
		long duration	= Long.valueOf(item[2]).longValue();
		String formattedDuration = DateUtil.formatDuration(duration);
		out.println(item[0] + "," + item[1] + "," + formattedDuration);
		}
	}
}
%>
