/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.attachment.control;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledReservedFile implements SchedulerEventListener {

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

      StringBuilder messageBody = new StringBuilder();
      StringBuilder messageBody_en = new StringBuilder();

      // 1. rechercher la liste des fichiers arrivant a echeance
      Date expiryDate;
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      calendar.add(Calendar.DATE, 1);
      expiryDate = calendar.getTime();

      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "expiryDate = " + expiryDate.toString());

      Collection<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
          listExpiringDocuments(expiryDate, null);
      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + documents.size());

      for (SimpleDocument document : documents) {
        messageBody.append(message.getString("attachment.notifName")).append(" '").append(document.
            getFilename()).append("'");
        messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").
            append(document.getFilename()).append("'");
        SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        createMessage(message, messageBody, message_en, messageBody_en, document, false, false);
        messageBody = new StringBuilder();
        messageBody_en = new StringBuilder();
      }

      // 2. rechercher la liste des fichiers arrivant a la date intermediaire
      Date alertDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      alertDate = calendar.getTime();

      SilverTrace.info("attachment",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "alertDate = " + alertDate.toString());

      documents = AttachmentServiceFactory.getAttachmentService().listDocumentsRequiringWarning(
          alertDate, null);
      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + documents.size());

      messageBody = new StringBuilder();
      messageBody_en = new StringBuilder();

      for (SimpleDocument document : documents) {
        messageBody.append(message.getString("attachment.notifName")).append(" '").append(document.
            getFilename()).append("' ");
        messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").
            append(document.getFilename()).append("' ");
        SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()",

            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        createMessage(message, messageBody, message_en, messageBody_en, document, true, false);
        messageBody = new StringBuilder();
        messageBody_en = new StringBuilder();
      }

      // 3. rechercher la liste des fichiers ayant depasse la date d'expiration
      Date libDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      libDate = calendar.getTime();

      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "libDate = " + libDate.toString());

      documents = AttachmentServiceFactory.getAttachmentService().listDocumentsToUnlock(libDate,
          null);
      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + documents.size());
     

      messageBody = new StringBuilder();
      messageBody_en = new StringBuilder();

      for (SimpleDocument document : documents) {
        // envoyer une notif
        messageBody.append(message.getString("attachment.notifName")).append(" '").append(document.
            getFilename()).append("'");
        messageBody_en.append(message_en.getString("attachment.notifName")).append(" '").
            append(document.getFilename()).append("'");
        SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        createMessage(message, messageBody, message_en, messageBody_en, document, false, true);
        messageBody = new StringBuilder();
        messageBody_en = new StringBuilder();

        // et pour chaque message, le liberer
        document.unlock();
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, false, false);
      }
    } catch (Exception e) {
      throw new AttachmentException("ScheduledReservedFile.doScheduledReservedFile()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void createMessage(ResourceLocator message, StringBuilder messageBody,
      ResourceLocator message_en, StringBuilder messageBody_en, SimpleDocument document,
      boolean alert, boolean lib) throws AttachmentException {
    Calendar atDate = Calendar.getInstance();
    atDate.setTime(document.getExpiry());
    int day = atDate.get(Calendar.DAY_OF_WEEK);
    String jour = "GML.jour" + day;
    int month = atDate.get(Calendar.MONTH);
    String mois = "GML.mois" + month;
    String date = generalMessage.getString(jour) + " " + atDate.get(Calendar.DATE) + " "
        + generalMessage.getString(mois) + " " + atDate.get(Calendar.YEAR);

    SilverTrace.info("attachment", "ScheduledReservedFile.createMessage()",
        "root.MSG_GEN_EXIT_METHOD");
    // french notifications
    String subject;
    String body;
    if (lib) {
      subject = message.getString("attachment.notifSubjectLib");
      body = messageBody.append(" ").append(message.getString("attachment.notifUserLib")).append(
          "\n\n").toString();
    } else {
      if (alert) {
        subject = message.getString("attachment.notifSubjectAlert");
        body = messageBody.append(" ").append(message.getString("attachment.notifUserAlert")).
            append(" (").append(date).append(") ").append("\n\n").toString();
      } else {
        subject = message.getString("attachment.notifSubjectExpiry");
        body = messageBody.append(" ").append(
            message.getString("attachment.notifUserExpiry")).append("\n\n").toString();
      }
    }

    // english notifications
    String subject_en;
    String body_en;
    if (lib) {
      subject_en = message_en.getString("attachment.notifSubjectLib");
      body_en = messageBody.append(" ").append(message_en.getString("attachment.notifUserLib")).
          append("\n\n").toString();
    } else {
      if (alert) {
        subject_en = message_en.getString("attachment.notifSubjectAlert");
        body_en = messageBody_en.append(" ").append(
            message_en.getString("attachment.notifUserAlert")).append(" (").append(date).append(
            ") ").append("\n\n").toString();
      } else {
        subject_en = message_en.getString("attachment.notifSubjectExpiry");
        body_en = messageBody_en.append(" ").append(
            message_en.getString("attachment.notifUserExpiry")).append("\n\n").toString();
      }
    }

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);
    notifMetaData.addUserRecipient(new UserRecipient(document.getEditedBy()));
    String url = URLManager.getURL(null, null, document.getInstanceId()) + "GoToFilesTab?Id="
        + document.getForeignId();
    notifMetaData.setLink(url);
    notifMetaData.setComponentId(document.getInstanceId());
    // 2. envoie de la notification
    try {
      getAttachmentBm().notifyUser(notifMetaData, document.getEditedBy(), document.getInstanceId());
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
    SilverTrace.debug("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' is executed");
    doScheduledReservedFile();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}