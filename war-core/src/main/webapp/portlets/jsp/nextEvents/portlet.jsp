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
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.stratelia.webactiv.calendar.model.Schedulable"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);

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
  	String		taskURL 	= null;
  	while (events.hasNext())
  	{
      	task 		= (Schedulable) events.next();
      	taskname 	= EncodeHelper.convertHTMLEntities(task.getName());

      	// convertir la date de l'�v�nement
    	Calendar taskDate = Calendar.getInstance();
    	taskDate.setTime(task.getStartDate());
    	taskDate.set(Calendar.HOUR_OF_DAY, 0);
    	taskDate.set(Calendar.MINUTE, 0);
    	// formatage de la date sous forme jj/mm/aaaa pour param�tre de agenda.jsp
    	String date = DateUtil.getInputDate(task.getStartDate(), language);

    	taskURL	= m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date;

    	if (today.equals(taskDate))
    	{
    		// �v�nement du jour
    		if ( task.getStartHour() != null)
    		  	out.println("&#149; " + message.getString("today") + ", "+task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+taskURL+"\">" + taskname + "</a>");
    		else
    			out.println("&#149; " + message.getString("today") + " : <a href=\""+taskURL+"\">" + task.getName() + "</a>");
    	}
    	else if (tomorrow.equals(taskDate))
    	{
    		// �v�nement du lendemain
    		if ( task.getStartHour() != null)
    		  	out.println("&#149; " + message.getString("tomorrow") + ", "+task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+taskURL+"\">" + taskname + "</a>");
    		else
       		  	out.println("&#149; " + message.getString("tomorrow") + " : <a href=\""+taskURL+"\">" + taskname + "</a>");
    	}
    	else
    	{
    	  	int day = taskDate.get(Calendar.DAY_OF_WEEK);
    	 	String jour = "GML.jour" + day;
			int month = taskDate.get(Calendar.MONTH);
    	  	String mois = "GML.mois" + month;

    	  	if (task.getStartHour() != null)
    	   		out.println("&#149; "+ generalMessage.getString(jour)+ " " + taskDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + taskDate.get(Calendar.YEAR) + ", " + task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+taskURL+"\">" + taskname + "</a>");
    	  	else
 		  		out.println("&#149; "+ generalMessage.getString(jour)+ " " + taskDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + taskDate.get(Calendar.YEAR) + " : " + "<a href=\""+taskURL+"\">" + taskname + "</a>");
      	}
    	out.println("<br/>");
  	}
}
out.flush();
%>