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

package com.stratelia.silverpeas.versioningPeas.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Vector;

import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class VersioningSchedulerImpl implements SchedulerEventHandler {
  public static final String VERSIONING_JOB_NAME_PROCESS_ACTIFY = "V_ProcessActify";
  public static final String VERSIONING_JOB_NAME_PURGE_ACTIFY = "V_PurgeActify";

  private ResourceLocator resourcesAttachment = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");

  public VersioningSchedulerImpl() {
  }

  public void initialize() {
    if (resourcesAttachment.getBoolean("ActifyPublisherEnable", false)) {
      try {
        String cronScheduleProcess = resourcesAttachment.getString("ScheduledProcessActify");
        String cronSchedulePurge = resourcesAttachment.getString("ScheduledPurgeActify");
          SimpleScheduler.unscheduleJob(this, VERSIONING_JOB_NAME_PROCESS_ACTIFY);
          SimpleScheduler.unscheduleJob(this, VERSIONING_JOB_NAME_PURGE_ACTIFY);
        SimpleScheduler.scheduleJob(this, VERSIONING_JOB_NAME_PROCESS_ACTIFY, cronScheduleProcess,
            this, "doProcessActify");
        SimpleScheduler.scheduleJob(this, VERSIONING_JOB_NAME_PURGE_ACTIFY, cronSchedulePurge, this,
            "doPurgeActify");
      } catch (Exception e) {
        SilverTrace.error("versioningPeas", "VersioningScheduleImpl.initialize()", "", e);
      }
    }
  }

  @Override
  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("versioningPeas",
            "VersioningScheduleImpl.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was not successfull");
        break;

      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("versioningPeas",
            "VersioningScheduleImpl.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was successfull");
        break;

      default:
        SilverTrace
            .error("versioningPeas",
            "VersioningScheduleImpl.handleSchedulerEvent",
            "Illegal event type");
        break;
    }
  }

  /**
   * Publish in Silverpeas 3d files converted by Actify
   * @throws IOException
   * @throws Exception
   */
  public synchronized void doProcessActify(java.util.Date date)
      throws VersioningRuntimeException, RemoteException,
      FileNotFoundException, IOException, Exception {
    VersioningUtil versioningUtil = new VersioningUtil();
    String componentId;
    String documentId;
    long now = new Date().getTime();

    String resultActifyPath = resourcesAttachment.getString("ActifyPathResult");
    int delayBeforeProcess = new Integer(resourcesAttachment
        .getString("DelayBeforeProcess")).intValue();

    File folderToAnalyse = new File(FileRepositoryManager.getTemporaryPath()
        + resultActifyPath);
    File[] elementsList = folderToAnalyse.listFiles();

    // List all folders in Actify
    for (int i = 0; i < elementsList.length; i++) {
      File element = elementsList[i];

      long lastModified = element.lastModified();
      String dirName = element.getName();
      String resultActifyFullPath = FileRepositoryManager.getTemporaryPath()
          + resultActifyPath + File.separator + dirName;

      // Directory to process ?
      // Ex of idir: v_kmelia116_docId
      if (element.isDirectory()
          && (lastModified + delayBeforeProcess * 1000 * 60 < now)
          && dirName.substring(0, 2).equals("v_")) {
        componentId = dirName.substring(dirName.indexOf("_") + 1, dirName
            .lastIndexOf("_"));
        documentId = dirName.substring(dirName.lastIndexOf("_") + 1);

        String detailPathToAnalyse = element.getAbsolutePath();
        SilverTrace.info("versioningPeas",
            "VersioningSchedulerImpl.doProcessActify()",
            "root.MSG_GEN_PARAM_VALUE", "detailPathToAnalyse="
            + detailPathToAnalyse);
        folderToAnalyse = new File(detailPathToAnalyse);
        File[] filesList = folderToAnalyse.listFiles();

        DocumentPK docPK = new DocumentPK(Integer.parseInt(documentId),
            componentId);
        DocumentVersion doc = versioningUtil.getLastVersion(docPK);

        for (int j = 0; j < filesList.length; j++) {
          File file = filesList[j];
          String fileName = file.getName();
          String physicalName = new Long(new Date().getTime()).toString()
              + ".3d";
          String logicalName = fileName.substring(0, fileName.lastIndexOf("."))
              + ".3d";
          String mimeType = FileUtil.getMimeType(physicalName);
          DocumentVersion newVersion = new DocumentVersion(null, docPK, doc
              .getMajorNumber(), doc.getMinorNumber(), doc.getAuthorId(),
              new Date(), null, doc.getType(), doc.getStatus(), physicalName,
              logicalName, mimeType, new Long(file.length()).intValue(),
              componentId);
          versioningUtil.addNewDocumentVersion(newVersion, doc.getType());
          String physicalPath = versioningUtil.createPath(null, componentId,
              null);

          String srcFile = resultActifyFullPath + File.separator + logicalName;
          String destFile = physicalPath + File.separator + physicalName;
          FileRepositoryManager.copyFile(srcFile, destFile);
        }
        FileFolderManager.deleteFolder(resultActifyFullPath);
      }
    }
    SilverTrace
        .info("versioningPeas", "VersioningSchedulerImpl.doProcessActify()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Purge native 3D files alreday converted by Actify
   * @throws Exception
   */
  public synchronized void doPurgeActify(Date date) throws Exception {
    int delayBeforePurge = new Integer(resourcesAttachment
        .getString("DelayBeforePurge")).intValue();
    long now = new Date().getTime();

    File folderToAnalyse = new File(FileRepositoryManager.getTemporaryPath()
        + resourcesAttachment.getString("ActifyPathSource"));
    File[] elementsList = folderToAnalyse.listFiles();

    // List all folders in Actify
    for (int i = 0; i < elementsList.length; i++) {
      File element = elementsList[i];
      long lastModified = element.lastModified();
      if (element.isDirectory()
          && lastModified + delayBeforePurge * 1000 * 60 < now) {
        SilverTrace.info("versioningPeas",
            "VersioningSchedulerImpl.doPurgeActify()",
            "root.MSG_GEN_PARAM_VALUE", "pathToPurge=" + element.getName());
        FileFolderManager.deleteFolder(element.getAbsolutePath());
      }
    }
  }

}