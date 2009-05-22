<%@ page import="com.stratelia.webactiv.calendar.model.Schedulable"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

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
      	taskname 	= Encode.convertHTMLEntities(task.getName());
      	
      	// convertir la date de l'évènement
    	Calendar taskDate = Calendar.getInstance();
    	taskDate.setTime(task.getStartDate());
    	taskDate.set(Calendar.HOUR_OF_DAY, 0);
    	taskDate.set(Calendar.MINUTE, 0);
    	// formatage de la date sous forme jj/mm/aaaa pour paramètre de agenda.jsp
    	String date = DateUtil.getInputDate(task.getStartDate(), language);
    	
    	taskURL	= m_sContext+URLManager.getURL(URLManager.CMP_AGENDA)+"agenda.jsp?Action=SelectDay&Day="+date;
    	
    	if (today.equals(taskDate))
    	{
    		// évènement du jour
    		if ( task.getStartHour() != null)
    		  	out.println("&#149; " + message.getString("today") + ", "+task.getStartHour() + " - " + task.getEndHour() + " : <a href=\""+taskURL+"\">" + taskname + "</a>");
    		else
    			out.println("&#149; " + message.getString("today") + " : <a href=\""+taskURL+"\">" + task.getName() + "</a>");
    	}
    	else if (tomorrow.equals(taskDate))
    	{
    		// évènement du lendemain
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