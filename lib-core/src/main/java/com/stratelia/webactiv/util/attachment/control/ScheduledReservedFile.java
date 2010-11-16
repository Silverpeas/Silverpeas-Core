/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.attachment.control;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.scheduler.Scheduler;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventListener;
import com.stratelia.silverpeas.scheduler.SchedulerFactory;
import com.stratelia.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledReservedFile
    implements SchedulerEventListener {

  public static final String ATTACHMENT_JOB_NAME_PROCESS = "A_ProcessReservedFileAttachment";
  private ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");
  private ResourceLocator generalMessage = new ResourceLocator(
      "com.stratelia.webactiv.multilang.generalMultilang", "");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledReservedFile");
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
      scheduler.unscheduleJob(ATTACHMENT_JOB_NAME_PROCESS);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(ATTACHMENT_JOB_NAME_PROCESS, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("attachment", "ScheduledReservedFile.initialize()",
          "attachment.EX_CANT_INIT_SCHEDULED_RESERVED_FILE", e);
    }
  }

  public void doScheduledReservedFile() throws AttachmentException {
    SilverTrace.info("attachment",
        "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.util.attachment.multilang.attachment", "fr");
      ResourceLocator message_en = new ResourceLocator(
          "com.stratelia.webactiv.util.attachment.multilang.attachment", "en");

      StringBuffer messageBody = new StringBuffer();
      StringBuffer messageBody_en = new StringBuffer();

      // 1. rechercher la liste des fichiers arrivant a echeance
      Date expiryDate;
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      calendar.add(Calendar.DATE, 1);
      expiryDate = calendar.getTime();

      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "expiryDate = " + expiryDate.toString());

      Collection<AttachmentDetail> attachments = getAttachmentBm().getAllAttachmentByDate(
          expiryDate, false);
      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + attachments.size());

      Iterator<AttachmentDetail> it = attachments.iterator();
      while (it.hasNext()) {
        AttachmentDetail att = it.next();
        messageBody.append(message.getString("attachment.notifName")).append(
            " '").append(att.getLogicalName()).append("'");
        messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").append(att.
            getLogicalName()).append("'");
        SilverTrace.info("attachment",
            "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        createMessage(message, messageBody, message_en, messageBody_en, att,
            false, false);
        messageBody = new StringBuffer();
        messageBody_en = new StringBuffer();
      }

      // 2. rechercher la liste des fichiers arrivant a la date intermediaire
      Date alertDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      alertDate = calendar.getTime();

      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "alertDate = " + alertDate.toString());

      attachments = getAttachmentBm().getAllAttachmentByDate(alertDate, true);
      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + attachments.size());

      messageBody = new StringBuffer();
      messageBody_en = new StringBuffer();

      Iterator<AttachmentDetail> itA = attachments.iterator();
      while (itA.hasNext()) {
        AttachmentDetail att = itA.next();
        messageBody.append(message.getString("attachment.notifName")).append(
            " '").append(att.getLogicalName()).append("' ");
        messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").append(att.
            getLogicalName()).append("' ");
        SilverTrace.info("attachment",
            "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        createMessage(message, messageBody, message_en, messageBody_en, att,
            true, false);
        messageBody = new StringBuffer();
        messageBody_en = new StringBuffer();
      }

      // 3. rechercher la liste des fichiers ayant depasse la date d'expiration
      Date libDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      libDate = calendar.getTime();

      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "libDate = " + libDate.toString());

      attachments = getAttachmentBm().getAllAttachmentToLib(libDate);
      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + attachments.size());

      messageBody = new StringBuffer();
      messageBody_en = new StringBuffer();

      Iterator<AttachmentDetail> itL = attachments.iterator();
      while (itL.hasNext()) {
        AttachmentDetail att = itL.next();

        // envoyer une notif
        messageBody.append(message.getString("attachment.notifName")).append(
            " '").append(att.getLogicalName()).append("'");
        messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").append(att.
            getLogicalName()).append("'");
        SilverTrace.info("attachment",
            "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        createMessage(message, messageBody, message_en, messageBody_en, att,
            false, true);
        messageBody = new StringBuffer();
        messageBody_en = new StringBuffer();

        // et pour chaque message, le liberer
        att.setWorkerId(null);
        att.setAlertDate(null);
        att.setExpiryDate(null);
        AttachmentController.updateAttachment(att, false, false);
      }
    } catch (Exception e) {
      throw new AttachmentException(
          "ScheduledReservedFile.doScheduledReservedFile()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("attachment",
        "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void createMessage(ResourceLocator message,
      StringBuffer messageBody,
      ResourceLocator message_en,
      StringBuffer messageBody_en,
      AttachmentDetail att,
      boolean alert,
      boolean lib)
      throws AttachmentException {
    Calendar atDate = Calendar.getInstance();
    atDate.setTime(att.getExpiryDate());
    int day = atDate.get(Calendar.DAY_OF_WEEK);
    String jour = "GML.jour" + day;
    int month = atDate.get(Calendar.MONTH);
    String mois = "GML.mois" + month;
    String date = generalMessage.getString(jour) + " "
        + atDate.get(Calendar.DATE) + " " + generalMessage.getString(mois)
        + " " + atDate.get(Calendar.YEAR);

    SilverTrace.info("attachment", "ScheduledReservedFile.createMessage()",
        "root.MSG_GEN_EXIT_METHOD");
    // french notifications
    String subject = "";
    String body = "";
    if (lib) {
      subject = message.getString("attachment.notifSubjectLib");
      body = messageBody.append(" ").append(
          message.getString("attachment.notifUserLib")).append("\n\n").toString();
    } else {
      if (alert) {
        subject = message.getString("attachment.notifSubjectAlert");
        body = messageBody.append(" ").append(
            message.getString("attachment.notifUserAlert")).append(" (").append(date).append(") ").
            append("\n\n").toString();
      } else {
        subject = message.getString("attachment.notifSubjectExpiry");
        body = messageBody.append(" ").append(
            message.getString("attachment.notifUserExpiry")).append("\n\n").toString();
      }
    }

    // english notifications
    String subject_en = "";
    String body_en = "";
    if (lib) {
      subject_en = message_en.getString("attachment.notifSubjectLib");
      body_en = messageBody.append(" ").append(
          message_en.getString("attachment.notifUserLib")).append("\n\n").toString();
    } else {
      if (alert) {
        subject_en = message_en.getString("attachment.notifSubjectAlert");
        body_en = messageBody_en.append(" ").append(
            message_en.getString("attachment.notifUserAlert")).append(" (").append(date).append(") ").
            append("\n\n").toString();
      } else {
        subject_en = message_en.getString("attachment.notifSubjectExpiry");
        body_en = messageBody_en.append(" ").append(
            message_en.getString("attachment.notifUserExpiry")).append("\n\n").toString();
      }
    }

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);

    notifMetaData.addUserRecipient(att.getWorkerId());

    String url = URLManager.getURL(null, null, att.getPK().getInstanceId())
        + "GoToFilesTab?Id=" + att.getForeignKey().getId();
    notifMetaData.setLink(url);

    notifMetaData.setComponentId(att.getInstanceId());

    // 2. envoie de la notification
    try {
      getAttachmentBm().notifyUser(notifMetaData, att.getWorkerId(),
          att.getInstanceId());
    } catch (Exception e) {
      throw new AttachmentException("ScheduledReservedfile.createMessage()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private static AttachmentBm getAttachmentBm() {
    AttachmentBm attachmentBm = null;
    if (attachmentBm == null) {
      attachmentBm = new AttachmentBmImpl();
    }
    return attachmentBm;
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("Attachment",
        "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' is executed");
    doScheduledReservedFile();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("Attachment",
        "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("Attachment",
        "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}