/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerProvider;
import com.silverpeas.scheduler.trigger.JobTrigger;
import org.silverpeas.initialization.Initialization;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.util.Link;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduledReservedFile implements SchedulerEventListener, Initialization {

  public static final String ATTACHMENT_JOB_NAME_PROCESS = "A_ProcessReservedFileAttachment";
  private ResourceLocator resources =
      new ResourceLocator("org.silverpeas.util.attachment.Attachment", "");
  private ResourceLocator generalMessage =
      new ResourceLocator("org.silverpeas.multilang.generalMultilang", "");

  @Override
  public void init() {
    try {
      String cron = resources.getString("cronScheduledReservedFile");
      Logger.getLogger(getClass().getSimpleName())
          .log(Level.INFO, "Reserved File Processor scheduled with cron ''{0}''", cron);
      Scheduler scheduler = SchedulerProvider.getScheduler();
      scheduler.unscheduleJob(ATTACHMENT_JOB_NAME_PROCESS);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(ATTACHMENT_JOB_NAME_PROCESS, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("attachment", "ScheduledReservedFile.initialize()",
          "attachment.EX_CANT_INIT_SCHEDULED_RESERVED_FILE", e);
    }
  }

  public void doScheduledReservedFile() throws AttachmentException {
    SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      ResourceLocator message =
          new ResourceLocator("org.silverpeas.util.attachment.multilang.attachment",
              DisplayI18NHelper.getDefaultLanguage());

      StringBuilder messageBody = new StringBuilder();

      // 1. Looking for expired documents
      Date expiryDate;
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      calendar.add(Calendar.DATE, 1);
      expiryDate = calendar.getTime();

      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "expiryDate = " + expiryDate.toString());

      Collection<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
          listExpiringDocuments(expiryDate, null);
      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachments = " + documents.size());

      for (SimpleDocument document : documents) {
        String date = getExpiryDate(document);
        String subject = createMessageSubject(message, false, false);
        messageBody.append(message.getString("attachment.notifName")).append(" '")
            .append(document.
                getFilename()).append("'");
        SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        String body = createMessageBody(message, messageBody, date, false, false);
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, body);

        createMessage(date, notifMetaData, document, false, false);
        messageBody = new StringBuilder();
      }

      // 2. Looking for the documents that will be soon expired
      Date alertDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      alertDate = calendar.getTime();

      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "alertDate = " + alertDate.toString());

      documents = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsRequiringWarning(alertDate, null);
      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + documents.size());

      messageBody = new StringBuilder();

      for (SimpleDocument document : documents) {
        String date = getExpiryDate(document);
        String subject = createMessageSubject(message, true, false);
        messageBody.append(message.getString("attachment.notifName")).append(" '")
            .append(document.
                getFilename()).append("'");
        SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        String body = createMessageBody(message, messageBody, date, true, false);
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, body);

        createMessage(date, notifMetaData, document, true, false);
        messageBody = new StringBuilder();
      }

      // 3. Looking for the documents to unlock
      Date libDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      libDate = calendar.getTime();

      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "libDate = " + libDate.toString());

      documents =
          AttachmentServiceProvider.getAttachmentService().listDocumentsToUnlock(libDate, null);
      SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Attachemnts = " + documents.size());


      messageBody = new StringBuilder();

      for (SimpleDocument document : documents) {
        String date = getExpiryDate(document);
        String subject = createMessageSubject(message, false, true);
        messageBody.append(message.getString("attachment.notifName")).append(" '")
            .append(document.
                getFilename()).append("'");
        SilverTrace.info("attachment", "ScheduledAlertUser.doScheduledAlertUser()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        String body = createMessageBody(message, messageBody, date, false, true);
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, body);

        createMessage(date, notifMetaData, document, false, true);
        messageBody = new StringBuilder();

        document.unlock();
        AttachmentServiceProvider.getAttachmentService().updateAttachment(document, false, false);
      }
    } catch (Exception e) {
      throw new AttachmentException("ScheduledReservedFile.doScheduledReservedFile()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("attachment", "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private String getExpiryDate(SimpleDocument document) {
    Calendar atDate = Calendar.getInstance();
    atDate.setTime(document.getExpiry());
    String day = "GML.jour" + atDate.get(Calendar.DAY_OF_WEEK);
    String month = "GML.mois" + atDate.get(Calendar.MONTH);
    String date = generalMessage.getString(day) + " " + atDate.get(Calendar.DATE) + " " +
        generalMessage.getString(month) + " " + atDate.get(Calendar.YEAR);
    return date;
  }

  private void createMessageByLanguage(String date, String url, SimpleDocument document,
      NotificationMetaData notifMetaData, boolean alert, boolean lib) throws AttachmentException {
    for (String language : DisplayI18NHelper.getLanguages()) {
      ResourceLocator message =
          new ResourceLocator("org.silverpeas.util.attachment.multilang.attachment", language);
      String subject = createMessageSubject(message, alert, lib);
      StringBuilder messageBody = new StringBuilder();
      messageBody.append(message.getString("attachment.notifName")).append(" '")
          .append(document.
              getFilename()).append("'");
      String body = createMessageBody(message, messageBody, date, alert, lib);
      notifMetaData.addLanguage(language, subject, body);
      Link link = new Link(url, message.getString("attachment.notifLinkLabel"));
      notifMetaData.setLink(link, language);
    }
  }

  private void createMessage(String date,
      NotificationMetaData notifMetaData, SimpleDocument document,
      boolean alert, boolean lib) throws AttachmentException {
    SilverTrace
        .info("attachment", "ScheduledReservedFile.createMessage()", "root.MSG_GEN_EXIT_METHOD");

    String url = URLManager.getURL(null, null, document.getInstanceId()) + "GoToFilesTab?Id=" +
        document.getForeignId();

    createMessageByLanguage(date, url, document, notifMetaData, alert, lib);

    notifMetaData.addUserRecipient(new UserRecipient(document.getEditedBy()));
    notifMetaData.setComponentId(document.getInstanceId());
    notifyUser(notifMetaData, "-1", document.getInstanceId());
  }

  private String createMessageBody(ResourceLocator message, StringBuilder body, String date,
      boolean alert, boolean lib) {
    if (lib) {
      return body.append(" ").append(message.getString("attachment.notifUserLib")).append("\n\n")
          .toString();
    } else if (alert) {
      return body.append(" ").append(message.getString("attachment.notifUserAlert")).
          append(" (").append(date).append(") ").append("\n\n").toString();
    }
    return body.append(" ").append(message.getString("attachment.notifUserExpiry")).append("\n\n")
        .toString();

  }

  private String createMessageSubject(ResourceLocator message, boolean alert, boolean lib) {
    if (lib) {
      return message.getString("attachment.notifSubjectLib");
    } else if (alert) {
      return message.getString("attachment.notifSubjectAlert");
    }
    return message.getString("attachment.notifSubjectExpiry");
  }

  public void notifyUser(NotificationMetaData notifMetaData, String senderId, String componentId)
      throws AttachmentException {
    SilverTrace.info("attachment", "AttachmentBmImpl.notifyUser()", "root.MSG_GEN_EXIT_METHOD");
    try {
      SilverTrace.info("attachment", "AttachmentBmImpl.notifyUser()", "root.MSG_GEN_EXIT_METHOD",
          " senderId = " + senderId + " componentId = " + componentId);
      if (!StringUtil.isDefined(notifMetaData.getSender())) {
        notifMetaData.setSender(senderId);
      }
      NotificationSender notifSender = new NotificationSender(componentId);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new AttachmentException("AttachmentBmImpl.notifyUser()",
          SilverpeasRuntimeException.ERROR, "attachment.MSG_ATTACHMENT_NOT_EXIST", e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is executed");
    doScheduledReservedFile();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successful");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("Attachment", "Attachment_TimeoutManagerImpl.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successful");
  }
}