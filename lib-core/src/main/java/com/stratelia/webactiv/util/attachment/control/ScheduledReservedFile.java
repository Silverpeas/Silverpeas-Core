package com.stratelia.webactiv.util.attachment.control;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledReservedFile implements SchedulerEventHandler {

    public static final String ATTACHMENT_JOB_NAME_PROCESS = "A_ProcessReservedFileAttachment";

	private ResourceLocator resources = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
	private ResourceLocator generalMessage = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", "");


	public void initialize()
	{
		try
		{
			String cron = resources.getString("cronScheduledReservedFile");
			Vector jobList = SimpleScheduler.getJobList(this);
			if (jobList != null && jobList.size() > 0)
				SimpleScheduler.removeJob(this, ATTACHMENT_JOB_NAME_PROCESS);
			SimpleScheduler.getJob(this, ATTACHMENT_JOB_NAME_PROCESS, cron, this, "doScheduledReservedFile");
		}
		catch (Exception e)
		{
			SilverTrace.error("attachment", "ScheduledReservedFile.initialize()", "attachment.EX_CANT_INIT_SCHEDULED_RESERVED_FILE", e);
		}
	}

	public void handleSchedulerEvent(SchedulerEvent aEvent)
    {
        switch (aEvent.getType())
        {
			case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
				SilverTrace.error("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was not successfull");
	            break;

			case SchedulerEvent.EXECUTION_SUCCESSFULL:
	            SilverTrace.debug("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was successfull");
	            break;

			default:
	            SilverTrace.error("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "Illegal event type");
	            break;
        }
    }

	public void doScheduledReservedFile(Date date) throws AttachmentException
	{
		SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_ENTER_METHOD");

		try
		{
	        ResourceLocator	message		= new ResourceLocator("com.stratelia.webactiv.util.attachment.multilang.attachment", "fr");
   			ResourceLocator	message_en	= new ResourceLocator("com.stratelia.webactiv.util.attachment.multilang.attachment", "en");

	        StringBuffer messageBody = new StringBuffer();
        	StringBuffer messageBody_en = new StringBuffer();

			// 1. rechercher la liste des fichiers arrivant à échéance
			Date expiryDate;
			Calendar calendar = Calendar.getInstance(Locale.FRENCH);
			calendar.add(Calendar.DATE, 1);
			expiryDate = calendar.getTime();

			SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_PARAM_VALUE", "expiryDate = " + expiryDate.toString());

			Collection attachments = getAttachmentBm().getAllAttachmentByDate(expiryDate, false);
			SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + attachments.size());

        	Iterator it = attachments.iterator();
	        while (it.hasNext())
	        {
	        	AttachmentDetail att = (AttachmentDetail) it.next();
	        	messageBody.append(message.getString("attachment.notifName")).append(" '").append(att.getLogicalName()).append("'");
 	        	messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").append(att.getLogicalName()).append("'");
	        	SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()", "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
	        	//Création du message à envoyer
	        	createMessage(message, messageBody, message_en, messageBody_en, att, false, false);
	        	messageBody = new StringBuffer();
	        	messageBody_en = new StringBuffer();
	        }

	        // 2. rechercher la liste des fichiers arrivant à la date intermédiaire
			Date alertDate;
			calendar = Calendar.getInstance(Locale.FRENCH);
			alertDate = calendar.getTime();

			SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_PARAM_VALUE", "alertDate = " + alertDate.toString());

			attachments = getAttachmentBm().getAllAttachmentByDate(alertDate, true);
			SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + attachments.size());

	        messageBody = new StringBuffer();
        	messageBody_en = new StringBuffer();

        	Iterator itA = attachments.iterator();
	        while (itA.hasNext())
	        {
	        	AttachmentDetail att = (AttachmentDetail) itA.next();
	        	messageBody.append(message.getString("attachment.notifName")).append(" '").append(att.getLogicalName()).append("' ");
 	        	messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").append(att.getLogicalName()).append("' ");
	        	SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()", "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
	        	//Création du message à envoyer
	        	createMessage(message, messageBody, message_en, messageBody_en, att, true, false);
	        	messageBody = new StringBuffer();
	        	messageBody_en = new StringBuffer();
	        }

	        // 3. rechercher la liste des fichiers ayant dépassé la date d'expiration
			Date libDate;
			calendar = Calendar.getInstance(Locale.FRENCH);
			libDate = calendar.getTime();

			SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_PARAM_VALUE", "libDate = " + libDate.toString());

			attachments = getAttachmentBm().getAllAttachmentToLib(libDate);
			SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + attachments.size());

	        messageBody = new StringBuffer();
        	messageBody_en = new StringBuffer();

        	Iterator itL = attachments.iterator();
	        while (itL.hasNext())
	        {
	        	AttachmentDetail att = (AttachmentDetail) itL.next();

				// envoyer une notif
	        	messageBody.append(message.getString("attachment.notifName")).append(" '").append(att.getLogicalName()).append("'");
 	        	messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").append(att.getLogicalName()).append("'");
	        	SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()", "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
	        	//Création du message à envoyer
	        	createMessage(message, messageBody, message_en, messageBody_en, att, false, true);
	        	messageBody = new StringBuffer();
	        	messageBody_en = new StringBuffer();

	        	// et pour chaque message, le libérer
	        	att.setWorkerId(null);
	        	att.setAlertDate(null);
	        	att.setExpiryDate(null);
				AttachmentController.updateAttachment(att, false, false);
	        }
 		}
		catch (Exception e)
		{
			throw new AttachmentException("ScheduledReservedFile.doScheduledReservedFile()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
		}

		SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()", "root.MSG_GEN_EXIT_METHOD");
	}

	private void createMessage(ResourceLocator	message, StringBuffer messageBody, ResourceLocator	message_en, StringBuffer messageBody_en, AttachmentDetail att, boolean alert, boolean lib) throws AttachmentException
	{
		Calendar atDate = Calendar.getInstance();
        atDate.setTime(att.getExpiryDate());
		int day = atDate.get(Calendar.DAY_OF_WEEK);
     	String jour = "GML.jour" + day;
     	int month = atDate.get(Calendar.MONTH);
     	String mois = "GML.mois" + month;
		String date = generalMessage.getString(jour)+ " " + atDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + atDate.get(Calendar.YEAR);

		SilverTrace.info("attachment", "ScheduledReservedFile.createMessage()", "root.MSG_GEN_EXIT_METHOD");
		// 1. création du message

		//french notifications
		String subject	= "";
		String body 	= "";
		if (lib)
		{
			subject	= message.getString("attachment.notifSubjectLib");
			body 	= messageBody.append(" ").append(message.getString("attachment.notifUserLib")).append("\n\n").toString();
		}
		else
		{
			if (alert)
			{
				subject	= message.getString("attachment.notifSubjectAlert");
				body 	= messageBody.append(" ").append(message.getString("attachment.notifUserAlert")).append(" (").append(date).append(") ").append("\n\n").toString();
			}
			else
			{
				subject	= message.getString("attachment.notifSubjectExpiry");
				body 	= messageBody.append(" ").append(message.getString("attachment.notifUserExpiry")).append("\n\n").toString();
			}
		}

		//english notifications
		String subject_en	= "";
		String body_en 		= "";
		if (lib)
		{
			subject_en	= message_en.getString("attachment.notifSubjectLib");
			body_en	= messageBody.append(" ").append(message_en.getString("attachment.notifUserLib")).append("\n\n").toString();
		}
		else
		{
			if (alert)
			{
				subject_en	= message_en.getString("attachment.notifSubjectAlert");
				body_en  = messageBody_en.append(" ").append(message_en.getString("attachment.notifUserAlert")).append(" (").append(date).append(") ").append("\n\n").toString();
			}
			else
			{
				subject_en	= message_en.getString("attachment.notifSubjectExpiry");
				body_en  = messageBody_en.append(" ").append(message_en.getString("attachment.notifUserExpiry")).append("\n\n").toString();
			}
		}

		NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, subject, body);
		notifMetaData.addLanguage("en", subject_en, body_en);

		notifMetaData.addUserRecipient(att.getWorkerId());

		String url 	= URLManager.getURL(null,null,att.getPK().getInstanceId())+"GoToFilesTab?Id="+att.getForeignKey().getId();
		notifMetaData.setLink(url);

		notifMetaData.setComponentId(att.getInstanceId());

		// 2. envoie de la notification
		try
		{
			getAttachmentBm().notifyUser(notifMetaData, att.getWorkerId(), att.getInstanceId());
		}
		catch (Exception e)
		{
			throw new AttachmentException("ScheduledReservedfile.createMessage()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}

	private static AttachmentBm getAttachmentBm()
	{
		AttachmentBm attachmentBm = null;
		if (attachmentBm == null)
		{
			attachmentBm = new AttachmentBmImpl();
		}
		return attachmentBm;
	}
}