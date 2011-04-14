/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class AttachmentSchedulerImpl
    implements SchedulerEventListener {

  public static final String ATTACHMENT_JOB_NAME_PROCESS_ACTIFY = "A_ProcessActify";
  public static final String ATTACHMENT_JOB_NAME_PURGE_ACTIFY = "A_PurgeActify";
  private ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");

  public AttachmentSchedulerImpl() {
  }

  public void initialize() {
    if (resources.getBoolean("ActifyPublisherEnable", false)) {
      try {
        String cronScheduleProcess = resources.getString("ScheduledProcessActify");
        String cronSchedulePurge = resources.getString("ScheduledPurgeActify");

        SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();

        scheduler.unscheduleJob(ATTACHMENT_JOB_NAME_PROCESS_ACTIFY);
        scheduler.unscheduleJob(ATTACHMENT_JOB_NAME_PURGE_ACTIFY);

        JobTrigger processTrigger = JobTrigger.triggerAt(cronScheduleProcess);
        scheduler.scheduleJob(processActify(), processTrigger, this);

        JobTrigger purgeTrigger = JobTrigger.triggerAt(cronSchedulePurge);
        scheduler.scheduleJob(purgeActify(), purgeTrigger, this);
      } catch (Exception e) {
        SilverTrace.error("Attachment", "Attachment.initialize()", "", e);
      }
    }
  }

  /**
   * Publish in Silverpeas 3d files converted by Actify
   * @throws IOException
   * @throws Exception
   */
  public synchronized void doProcessActify() throws IOException,
      Exception {
    String attachmentId;
    String componentId;
    long now = new Date().getTime();

    String resultActifyPath = resources.getString("ActifyPathResult");
    int delayBeforeProcess = Integer.parseInt(resources.getString("DelayBeforeProcess"));

    File folderToAnalyse = new File(FileRepositoryManager.getTemporaryPath() + resultActifyPath);
    File[] elementsList = folderToAnalyse.listFiles();

    // List all folders in Actify
    for (int i = 0; i < elementsList.length; i++) {
      File element = elementsList[i];

      long lastModified = element.lastModified();
      String dirName = element.getName();
      String resultActifyFullPath = FileRepositoryManager.getTemporaryPath()
          + resultActifyPath + File.separator + dirName;

      // Directory to process ?
      if (element.isDirectory()
          && (lastModified + delayBeforeProcess * 1000 * 60 < now)
          && dirName.substring(0, 2).equals("a_")) {
        componentId = dirName.substring(dirName.indexOf('_') + 1, dirName.lastIndexOf('_'));
        attachmentId = dirName.substring(dirName.lastIndexOf('_') + 1);

        String detailPathToAnalyse = element.getAbsolutePath();
        SilverTrace.info("Attachment",
            "AttachmentSchedulerImpl.doProcessActify()",
            "root.MSG_GEN_PARAM_VALUE", "PathToAnalyze=" + detailPathToAnalyse);
        folderToAnalyse = new File(detailPathToAnalyse);
        File[] filesList = folderToAnalyse.listFiles();
        AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);
        AttachmentPK foreignPK = new AttachmentPK(attachmentId, "useless",
            componentId);
        for (int j = 0; j < filesList.length; j++) {
          File file = filesList[j];
          String fileName = file.getName();
          String physicalName = new Long(new Date().getTime()).toString()
              + ".3d";
          String logicalName = fileName.substring(0, fileName.lastIndexOf("."))
              + ".3d";
          String mimeType = AttachmentController.getMimeType(physicalName);

          AttachmentDetail attachmentDetail = new AttachmentDetail(atPK,
              physicalName, logicalName, null, mimeType, file.length(),
              "Images", new Date(), foreignPK);
          AttachmentController.createAttachment(attachmentDetail, false);

          String physicalPath = AttachmentController.createPath(componentId,
              "Images");

          String srcFile = resultActifyFullPath + File.separator + logicalName;
          String destFile = physicalPath + File.separator + physicalName;
          FileRepositoryManager.copyFile(srcFile, destFile);
        }
        FileFolderManager.deleteFolder(resultActifyFullPath);
      }
    }
    SilverTrace.info("Attachment", "AttachmentSchedulerImpl.doProcessActify()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Purge native 3D files alreday converted by Actify
   * @throws Exception
   */
  public synchronized void doPurgeActify() throws Exception {
    int delayBeforePurge = Integer.parseInt(resources.getString("DelayBeforePurge"));
    long now = new Date().getTime();

    File folderToAnalyse = new File(FileRepositoryManager.getTemporaryPath()
        + resources.getString("ActifyPathSource"));
    File[] elementsList = folderToAnalyse.listFiles();

    // List all folders in Actify
    for (int i = 0; i < elementsList.length; i++) {
      File element = elementsList[i];
      long lastModified = element.lastModified();
      if (element.isDirectory()
          && lastModified + delayBeforePurge * 1000 * 60 < now) {
        SilverTrace.info("Attachment",
            "AttachmentSchedulerImpl.doPurgeActify()",
            "root.MSG_GEN_PARAM_VALUE", "PathToPurge=" + element.getName());
        FileFolderManager.deleteFolder(element.getAbsolutePath());
      }
    }
  }

  /**
   * Gets the job relative to the actify processing.
   * @return the job for processing actify.
   */
  private Job processActify() {
    return new Job(ATTACHMENT_JOB_NAME_PROCESS_ACTIFY)    {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        doProcessActify();
      }
    };
  }

  /**
   * Gets the job relative to the actify purging.
   * @return the job for purging actify.
   */
  private Job purgeActify() {
    return new Job(ATTACHMENT_JOB_NAME_PURGE_ACTIFY)    {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        doPurgeActify();
      }
    };
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    SilverTrace.debug("Attachment",
        "Attachment_TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' is starting");
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
