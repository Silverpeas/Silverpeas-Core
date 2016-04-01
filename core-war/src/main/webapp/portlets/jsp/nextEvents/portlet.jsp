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

<%@page import="org.silverpeas.core.calendar.model.Schedulable"%>
<%@page import="org.owasp.encoder.Encode"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);

RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator events = (Iterator) pReq.getAttribute("Events");

if (!events.hasNext())
{
	out.println(message.getString("NoEvents")+"<br/>");
}
else
{
	// convertir la date du jour
	Calendar today = Calendar.getInstance();
	today.setTime(new Date());
	today.set(Calendar.HOUR_OF_DAY, 0);
	today.set(Calendar.MINUTE, 0);
	today.set(Calendar.SECOND, 0);
	today.set(Calendar.MILLISECOND, 0);

	// convertir la date de demain
	Calendar tomorrow = Calendar.getInstance();
	tomorrow.add(Calendar.DATE,1);
	tomorrow.set(Calendar.HOUR_OF_DAY, 0);
	tomorrow.set(Calendar.MINUTE, 0);
	tomorrow.set(Calendar.SECOND, 0);
	tomorrow.set(Calendar.MILLISECOND, 0);

	Schedulable task 		= null;
	String 		taskname 	= null;
	String		taskDateURL 	= null;
    String    taskEventURL   = null;
	while (events.hasNext())
	{
	task 		= (Schedulable) events.next();
	taskname 	= task.getName();

	// convertir la date de l'évènement
	Calendar taskDate = Calendar.getInstance();
	taskDate.setTime(task.getStartDate());
	taskDate.set(Calendar.HOUR_OF_DAY, 0);
	taskDate.set(Calendar.MINUTE, 0);
	// formatage de la date sous forme jj/mm/aaaa pour param�tre de agenda.jsp
	String date = DateUtil.getInputDate(task.getStartDate(), language);

	taskDateURL = m_sContext + URLUtil.getURL(URLUtil.CMP_AGENDA, null, null)  + "SelectDay?Day=" + date;
      taskEventURL = m_sContext + URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "journal.jsp?JournalId=" + task.getId() + "&Action=Update";

      final StringBuilder sb = new StringBuilder("&#149; <a href=\"");
      sb.append(taskDateURL);
      sb.append("\" class=\"color-inherited\">");

      // évènement du jour
	if (today.equals(taskDate)) {
	  sb.append(message.getString("today"));
	}
      // évènement du lendemain
	else if (tomorrow.equals(taskDate)) {
        sb.append(message.getString("tomorrow"));
	}
      // autres
	else
	{
		String jour = "GML.jour" + taskDate.get(Calendar.DAY_OF_WEEK);
		String mois = "GML.mois" + taskDate.get(Calendar.MONTH);
        sb.append(generalMessage.getString(jour));
        sb.append(" ");
        sb.append(taskDate.get(Calendar.DATE));
        sb.append(" ");
        sb.append(generalMessage.getString(mois));
        sb.append(" ");
        sb.append(taskDate.get(Calendar.YEAR));
      }

      // Heure définie
      if (StringUtil.isDefined(task.getStartHour())) {
        sb.append(", ");
        sb.append(task.getStartHour());
        sb.append(" - ");
        sb.append(task.getEndHour());
      }

      // Accès
      sb.append("</a> : <a href=\"");
      sb.append(taskEventURL);
      sb.append("\">");
      sb.append(Encode.forHtml(taskname));
      sb.append("</a>");
      out.println(sb.toString());

	out.println("<br/>");
	}
}
out.flush();
%>