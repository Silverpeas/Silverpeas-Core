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
package com.silverpeas.importExport.control;

import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ImportReport;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

import java.io.File;

public class ScheduledImport implements SchedulerEventListener {

  public static final String IMPORTENGINE_JOB_NAME = "ImportEngineJob";
  private final ResourceLocator resources = new ResourceLocator(
      "com.silverpeas.importExport.settings.importSettings", "");
  private File dir = null; // Where the import XML descriptors are stored
  private String postPolicy = null;

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledImport");
      postPolicy = resources.getString("postPolicy", "remove");

      String sDir = resources.getString("importRepository");
      dir = new File(sDir);
      if (!dir.exists() && !dir.isDirectory()) {
        SilverTrace.error("importExport", "ScheduledImport.initialize()",
            "importExport.EX_CANT_INIT_SCHEDULED_IMPORT", "Repository '" + sDir
            + "' does not exists !");
      } else {
        SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.unscheduleJob(IMPORTENGINE_JOB_NAME);
        JobTrigger trigger = JobTrigger.triggerAt(cron);
        scheduler.scheduleJob(IMPORTENGINE_JOB_NAME, trigger, this);
      }
    } catch (Exception e) {
      SilverTrace.error("importExport", "ScheduledImport.initialize()",
          "importExport.EX_CANT_INIT_SCHEDULED_IMPORT", e);
    }
  }

  public void doScheduledImport() {
    SilverTrace.info("importExport", "ScheduledImport.doScheduledImport()",
        "root.MSG_GEN_ENTER_METHOD");

    String userId = resources.getString("userIdAsCreatorId");
    ImportExport importExport = new ImportExport();
    OrganizationController orga = new OrganizationController();
    UserDetail user = orga.getUserDetail(userId);
    ResourceLocator multilang = new ResourceLocator(
        "com.silverpeas.importExportPeas.multilang.importExportPeasBundle", "fr");
    ResourcesWrapper resource = new ResourcesWrapper(multilang, "fr");

    File[] files = dir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        String extension = FileRepositoryManager.getFileExtension(file.getName());
        if ("xml".equalsIgnoreCase(extension)) {
          SilverTrace.info("importExport",
              "ScheduledImport.doScheduledImport()",
              "root.MSG_GEN_PARAM_VALUE", "file = " + file.getAbsolutePath());
          try {
            ImportReport importReport = importExport.processImport(user, file.getAbsolutePath());
            importExport.writeImportToLog(importReport, resource);
            
            // Successfully import. Remove or rename import descriptor. 
            if ("remove".equalsIgnoreCase(postPolicy)) {
              file.delete();
            } else if ("rename".equalsIgnoreCase(postPolicy)) {
              file.renameTo(new File(file.getAbsolutePath() + ".old"));
            }
          } catch (ImportExportException e) {
            SilverTrace.error("importExport", "ScheduledImport.doScheduledImport()",
                "importExport.EX_CANT_PROCESS_IMPORT", "file = " + file.getAbsolutePath(), e);
          }
        }
      }
    }
    SilverTrace.info("importExport", "ScheduledImport.doScheduledImport()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    SilverTrace.debug("importExport", "ScheduledImport.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' is executing");
    doScheduledImport();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("importExport", "ScheduledImport.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("importExport", "ScheduledImport.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}