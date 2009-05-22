/**
 * 
 */
package com.silverpeas.ical;

import java.io.File;
import java.net.URL;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.agenda.control.AgendaRuntimeException;
import com.stratelia.webactiv.agenda.control.AgendaSessionController;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author dle
 *
 */
public class SynchroIcalManager {

	public final static String SYNCHRO_SUCCEEDED = "0";
    private AgendaSessionController                 agendaSessionController;
    private CalendarBm calendarBm;
    
	public SynchroIcalManager(AgendaSessionController agendaSessionController)
    {
		this.agendaSessionController = agendaSessionController;
		setCalendarBm();
    }

	public static void main(String[] args)
	{
	    try {
	
	        // Path to local iCalendar file
	        File localCalendar = new File("C:\\Silverpeas\\KMEdition\\temp\\icsCalendar\\agenda1.ics");
	    		
	        URL remoteCalendar = new URL(args[0]);
	        String username = args[1];
	        String password = args[2];
	
	        // Creates a synchronizer engine
	        SyncEngine engine = new SyncEngine();
	        
	        // Do the synchronization remoteCalendar -> create localCalendar
	        engine.synchronize(localCalendar, remoteCalendar, username, password);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Import remote calendar into Silverpeas calendar (update event if necessary)
	 * @param urlCalendar
	 * @param localCalendar
	 * @param loginICalendar
	 * @param pwdIcalendar
	 * @return ReturnCode
	 */
	public String synchroIcalAgenda(URL urlCalendar, File localCalendar, String loginICalendar, String pwdIcalendar)
	{
		String returnCodeSynchro = AgendaSessionController.SYNCHRO_FAILED;		
	    try {
	
	        // Private iCal URL 
	        // Use the SyncEngine.listCalendars() method to get URLs
	        URL remoteCalendar = urlCalendar;
	
	        // Creates a synchronizer engine
	        SyncEngine engine = new SyncEngine();
	
	        // Do the synchronization :
	        // Remote Calendar -> localfile Calendar
	        String remoteConnect = engine.synchronize(localCalendar, remoteCalendar, loginICalendar, pwdIcalendar);
	        if (remoteConnect.equals(SyncEngine.REMOTE_CONNECT_SUCCEEDED))
	        {
		        // localfile -> Silverpeas Agenda
		        ImportIcalManager impIcalManager = new ImportIcalManager(agendaSessionController);
		        String returnImport = impIcalManager.importIcalAgenda(localCalendar);
		        if (returnImport.equals(AgendaSessionController.IMPORT_FAILED))
			        returnCodeSynchro = AgendaSessionController.SYNCHRO_FAILED;
		        else
		        	returnCodeSynchro = AgendaSessionController.SYNCHRO_SUCCEEDED;
	        }
	        else
	        	returnCodeSynchro = remoteConnect;
	    } catch (Exception e) {
			SilverTrace.error("agenda","SynchroIcalManager.synchroIcalAgenda()","",e.fillInStackTrace());
	    }
    	return returnCodeSynchro; 
	}
	
	/**
	 * Method declaration
	 * @see
	 */
	private void setCalendarBm()
	{
		if (calendarBm == null)
		{
			try
			{
				calendarBm = ((CalendarBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class)).create();
			}
			catch (Exception e)
			{
				throw new AgendaRuntimeException(
					"ImportIcalManager.setCalendarBm()",
					SilverpeasException.ERROR,
					"root.EX_CANT_GET_REMOTE_OBJECT",
					e);
			}
		}
	}
}