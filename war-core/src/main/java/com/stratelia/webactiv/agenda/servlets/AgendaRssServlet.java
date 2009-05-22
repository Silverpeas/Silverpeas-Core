package com.stratelia.webactiv.agenda.servlets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.agenda.control.AgendaAccess;
import com.stratelia.webactiv.agenda.control.AgendaException;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.util.ResourceLocator;

public class AgendaRssServlet extends RssServlet
{
    
	/* (non-Javadoc)
	 * @see com.silverpeas.peasUtil.RssServlet#isComponentRss(java.lang.String)
	 */
	public boolean isComponentRss(String userIdAgenda) {
    	return true;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.peasUtil.RssServlet#isComponentAvailable(java.lang.String, java.lang.String)
	 */
	public boolean isComponentAvailable(AdminController admin, String userIdAgenda, String currentUserId) {
    	return true;
	}
	
	public String getChannelTitle(String userId)
	{
		OrganizationController orga = new OrganizationController();
		UserDetail user = orga.getUserDetail(userId);
		List userIds = new ArrayList();
		userIds.add(userId);
		String lang = (String) orga.getUsersLanguage(userIds).get(userId);
		ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.agenda.multilang.agenda", lang);
		return message.getStringWithParam("agenda.userAgenda", user.getLastName()); 
	}
		
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String, int)
     */
    public Collection getListElements(String userIdAgenda, int nbReturned) throws RemoteException {
    	//récupération de la liste des 10 prochains événements de l'Agenda du user passé en paramètre
    	Collection events;
		try {
			events = AgendaAccess.getJournalHeadersForUserAfterDate(userIdAgenda, new Date(), nbReturned);
		} catch (AgendaException e) {
			throw new RemoteException("AgendaRssServlet.getListElements()", e);
		}
    	return events;
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
     */
    public String getElementTitle(Object element, String currentUserId) {
    	JournalHeader event = (JournalHeader) element;
    	String name = event.getName();
    	if(event.getClassification().isPrivate() && ! event.getDelegatorId().equals(currentUserId)) {
			ResourceLocator messageFrench = new ResourceLocator("com.stratelia.webactiv.agenda.multilang.agenda", "fr");
			ResourceLocator messageEnglish = new ResourceLocator("com.stratelia.webactiv.agenda.multilang.agenda", "en");
			name = messageFrench.getString("agenda.rssPrivateEvent") + " ("+messageEnglish.getString("agenda.rssPrivateEvent")+")";
    	}
    	return name;
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object, java.lang.String)
     */
    public String getElementLink(Object element, String currentUserId) {
    	JournalHeader event = (JournalHeader) element;
    	String eventUrl = URLManager.getApplicationURL()+"/Ragenda/jsp/journal.jsp?Action=Update&JournalId="+event.getId(); //par défaut, lien sur l'événement en lui-meme
    	//URL eventUrl = new URL(getServerURL()+URLManager.getApplicationURL()+"/Journal/"+event.getId());    	
    	if(event.getClassification().isPrivate() && ! event.getDelegatorId().equals(currentUserId)) {
    		//lien sur le calendrier à la date de l'événement
    		eventUrl = URLManager.getApplicationURL()+"/Agenda/"+ event.getDelegatorId();
    	}
    	 
    	return eventUrl;
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object, java.lang.String)
     */
    public String getElementDescription(Object element, String currentUserId) {
    	JournalHeader event = (JournalHeader) element;
    	String description = event.getDescription();
    	if(event.getClassification().isPrivate() && ! event.getDelegatorId().equals(currentUserId)) {
    		description = "";
    	}
    	return description;
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
     */
    public Date getElementDate(Object element) {
    	JournalHeader event = (JournalHeader) element;
    	Calendar calElement = GregorianCalendar.getInstance();
    	calElement.setTime(event.getStartDate());
    	String hourMinute = event.getStartHour(); //hh:mm
    	if(hourMinute != null && hourMinute.trim().length()>0) {
    		/*int hour = new Integer(hourMinute.substring(0, 2)).intValue() - 1; //-1 car bug d'affichage du fil RSS qui affiche toujours 1h en trop*/
    		int hour = new Integer(hourMinute.substring(0, 2)).intValue();
    		int minute = new Integer(hourMinute.substring(3)).intValue();
    		calElement.set(Calendar.HOUR_OF_DAY, hour);
    		calElement.set(Calendar.MINUTE, minute);
    	} else {
    		/*calElement.set(Calendar.HOUR_OF_DAY, -1);//-1 car bug d'affichage du fil RSS qui affiche toujours 1h en trop*/
    		calElement.set(Calendar.HOUR_OF_DAY, 0);
    		calElement.set(Calendar.MINUTE, 0);
    	}
    	return calElement.getTime();
    }
    
    public String getElementCreatorId(Object element) {
    	JournalHeader event = (JournalHeader) element;
 	   	return event.getDelegatorId();
 	}
}