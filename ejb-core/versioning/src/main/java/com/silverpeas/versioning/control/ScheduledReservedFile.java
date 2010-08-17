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
package com.silverpeas.versioning.control;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledReservedFile implements SchedulerEventHandler {

  public static final String VERSIONING_JOB_NAME_PROCESS = "A_ProcessReservedFileVersioning";
  private ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.versioning.Versioning", "");
  private ResourceLocator generalMessage = new ResourceLocator(
      "com.stratelia.webactiv.multilang.generalMultilang", "");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledReservedFile");
      SimpleScheduler.removeJob(this, VERSIONING_JOB_NAME_PROCESS);
      SimpleScheduler.getJob(this, VERSIONING_JOB_NAME_PROCESS, cron, this,
          "doScheduledReservedFile");
    } catch (Exception e) {
      SilverTrace.error("versioning", "ScheduledReservedFile.initialize()",
          "versioning.EX_CANT_INIT_SCHEDULED_RESERVED_FILE", e);
    }
  }

  @Override
  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("versioning",
            "Versioning_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was not successfull");
        break;

      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("versioning",
            "Versioning_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was successfull");
        break;

      default:
        SilverTrace.error("versioning",
            "Versioning_TimeoutManagerImpl.handleSchedulerEvent",
            "Illegal event type");
        break;
    }
  }

  public void doScheduledReservedFile(Date date) {
    SilverTrace.info("versioning",
        "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.util.versioning.multilang.versioning", "fr");
      ResourceLocator message_en = new ResourceLocator(
          "com.stratelia.webactiv.util.versioning.multilang.versioning", "en");

      StringBuffer messageBody = new StringBuffer();
      StringBuffer messageBody_en = new StringBuffer();

      // 1. rechercher la liste des fichiers arrivant à échéance
      Date expiryDate;
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      calendar.add(Calendar.DATE, 1);
      expiryDate = calendar.getTime();

      SilverTrace.info("versioning",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "expiryDate = " + expiryDate.toString());

      Collection documents = getVersioningBm().getAllFilesReservedByDate(
          expiryDate, false);
      SilverTrace.info("versioning",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Documents = " + documents.size());

      Iterator it = documents.iterator();
      while (it.hasNext()) {
        Document doc = (Document) it.next();
        messageBody.append(message.getString("versioning.notifName")).append(
            " '").append(doc.getName()).append("'");
        messageBody_en.append(message_en.getString("versioning.notifName")).append(" '").append(doc.getName()).append("'");
        SilverTrace.info("versioning",
            "ScheduledReservedFile.doScheduledReservedFile()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        // Création du message à envoyer
        createMessage(message, messageBody, message_en, messageBody_en, doc,
            false, false);
        messageBody = new StringBuffer();
        messageBody_en = new StringBuffer();
      }

      // 2. rechercher la liste des fichiers arrivant à la date intermédiaire
      Date alertDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      alertDate = calendar.getTime();

      SilverTrace.info("versioning",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "alertDate = " + alertDate.toString());

      documents = getVersioningBm().getAllFilesReservedByDate(alertDate, true);
      SilverTrace.info("versioning",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Documents = " + documents.size());

      messageBody = new StringBuffer();
      messageBody_en = new StringBuffer();

      Iterator itA = documents.iterator();
      while (itA.hasNext()) {
        Document doc = (Document) itA.next();
        messageBody.append(message.getString("versioning.notifName")).append(
            " '").append(doc.getName()).append("'");
        messageBody_en.append(message_en.getString("versioning.notifName")).append(" '").append(doc.getName()).append("'");
        SilverTrace.info("versioning",
            "ScheduledReservedFile.doScheduledReservedFile()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        // Création du message à envoyer
        createMessage(message, messageBody, message_en, messageBody_en, doc,
            true, false);
        messageBody = new StringBuffer();
        messageBody_en = new StringBuffer();
      }

      // 2. rechercher la liste des fichiers ayant dépassé la date
      // d'expiration
      Date libDate;
      calendar = Calendar.getInstance(Locale.FRENCH);
      libDate = calendar.getTime();

      SilverTrace.info("versioning",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "libDate = " + libDate.toString());

      documents = getVersioningBm().getAllDocumentsToLib(libDate);
      SilverTrace.info("versioning",
          "ScheduledReservedFile.doScheduledReservedFile()",
          "root.MSG_GEN_PARAM_VALUE", "Documents = " + documents.size());

      messageBody = new StringBuffer();
      messageBody_en = new StringBuffer();

      Iterator itL = documents.iterator();
      while (itL.hasNext()) {
        Document doc = (Document) itL.next();

        // envoyer une notif
        messageBody.append(message.getString("versioning.notifName")).append(
            " '").append(doc.getName()).append("'");
        messageBody_en.append(message_en.getString("versioning.notifName")).append(" '").append(doc.getName()).append("'");
        SilverTrace.info("versioning",
            "ScheduledReservedFile.doScheduledReservedFile()",
            "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        // Création du message à envoyer
        createMessage(message, messageBody, message_en, messageBody_en, doc,
            false, true);
        messageBody = new StringBuffer();
        messageBody_en = new StringBuffer();

        // et pour chaque message, le libérer
        getVersioningBm().checkDocumentIn(doc.getPk(), doc.getOwnerId());
      }
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "ScheduledReservedFile.doScheduledReservedFile()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("versionning",
        "ScheduledReservedFile.doScheduledReservedFile()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void createMessage(ResourceLocator message, StringBuffer messageBody,
      ResourceLocator message_en, StringBuffer messageBody_en, Document doc,
      boolean alert, boolean lib) throws VersioningRuntimeException {
    Calendar atDate = Calendar.getInstance();
    atDate.setTime(doc.getExpiryDate());
    int day = atDate.get(Calendar.DAY_OF_WEEK);
    String jour = "GML.jour" + day;
    int month = atDate.get(Calendar.MONTH);
    String mois = "GML.mois" + month;
    String date = generalMessage.getString(jour) + " "
        + atDate.get(Calendar.DATE) + " " + generalMessage.getString(mois)
        + " " + atDate.get(Calendar.YEAR);

    SilverTrace.info("versioning", "ScheduledReservedFile.createMessage()",
        "root.MSG_GEN_ENTER_METHOD");
    // 1. création du message

    // french notifications
    String subject = "";
    String body = "";
    if (lib) {
      subject = message.getString("versioning.notifSubjectLib");
      body = messageBody.append(" ").append(
          message.getString("versioning.notifUserLib")).append("\n\n").toString();
    } else {
      if (alert) {
        subject = message.getString("versioning.notifSubjectAlert");
        body = messageBody.append(" ").append(
            message.getString("versioning.notifUserAlert")).append(" (").append(date).append(") ").append("\n\n").toString();
      } else {
        subject = message.getString("versioning.notifSubjectExpiry");
        body = messageBody.append(" ").append(
            message.getString("versioning.notifUserExpiry")).append("\n\n").toString();
      }
    }

    // english notifications
    String subject_en = "";
    String body_en = "";
    if (lib) {
      subject_en = message_en.getString("versioning.notifSubjectLib");
      body_en = messageBody.append(" ").append(
          message_en.getString("versioning.notifUserLib")).append("\n\n").toString();
    } else {
      if (alert) {
        subject_en = message_en.getString("versioning.notifSubjectAlert");
        body_en = messageBody_en.append(" ").append(
            message_en.getString("versioning.notifUserAlert")).append(" (").append(date).append(") ").append("\n\n").toString();
      } else {
        subject_en = message_en.getString("versioning.notifSubjectExpiry");
        body_en = messageBody_en.append(" ").append(
            message_en.getString("versioning.notifUserExpiry")).append("\n\n").toString();
      }
    }

    SilverTrace.info("versioning", "ScheduledReservedfile.createMessage()",
        "root.MSG_GEN_PARAM_VALUE", " subject = " + subject + "body = " + body);

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);

    SilverTrace.info("versioning", "ScheduledReservedfile.createMessage()",
        "root.MSG_GEN_PARAM_VALUE", " notifMetaData.getLanguages() = "
        + notifMetaData.getLanguages());

    notifMetaData.addUserRecipient(Integer.toString(doc.getOwnerId()));

    String url = URLManager.getURL(null, null, doc.getInstanceId())
        + "GoToFilesTab?Id=" + doc.getForeignKey().getId();
    notifMetaData.setLink(url);

    SilverTrace.info("versioning", "ScheduledReservedfile.createMessage()",
        "root.MSG_GEN_PARAM_VALUE", " url = " + url);

    notifMetaData.setComponentId(doc.getInstanceId());

    SilverTrace.info("versioning", "ScheduledReservedfile.createMessage()",
        "root.MSG_GEN_PARAM_VALUE", " notifMetaData = "
        + notifMetaData.toString());

    // 2. envoie de la notification
    try {
      getVersioningBm().notifyUser(notifMetaData,
          Integer.toString(doc.getOwnerId()), doc.getInstanceId());
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "ScheduledReservedfile.createMessage()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public VersioningBm getVersioningBm() {
    VersioningBm versioning_bm = null;
    try {
      VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      versioning_bm = vscEjbHome.create();
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "ScheduledReservedFile.getVersioningBm",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return versioning_bm;
  }
}
