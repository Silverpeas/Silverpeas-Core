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

package com.silverpeas.importExport.control;

import java.io.File;

import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ImportReport;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class ScheduledImport implements SchedulerEventHandler {

  public static final String IMPORTENGINE_JOB_NAME = "ImportEngineJob";

  private ResourceLocator resources = new ResourceLocator(
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
        SimpleScheduler.unscheduleJob(IMPORTENGINE_JOB_NAME);
        JobTrigger trigger = JobTrigger.triggerAt(cron);
        SimpleScheduler.scheduleJob(IMPORTENGINE_JOB_NAME, trigger, this);
      }
    } catch (Exception e) {
      SilverTrace.error("importExport", "ScheduledImport.initialize()",
          "importExport.EX_CANT_INIT_SCHEDULED_IMPORT", e);
    }
  }

  @Override
  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("importExport",
            "ScheduledImport.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was not successfull");
        break;

      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("importExport",
            "ScheduledImport.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was successfull");
        break;
        
      case SchedulerEvent.EXECUTION:
        SilverTrace.debug("importExport",
            "ScheduledImport.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' is executing");
        doScheduledImport();
        break;

      default:
        SilverTrace.error("importExport",
            "ScheduledImport.handleSchedulerEvent", "Illegal event type");
        break;
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
        "com.silverpeas.importExportPeas.multilang.importExportPeasBundle",
        "fr");
    ResourcesWrapper resource = new ResourcesWrapper(multilang, "fr");

    File[] files = dir.listFiles();
    for (int f = 0; f < files.length; f++) {
      File file = files[f];
      if (file.isFile()) {
        String extension = FileRepositoryManager.getFileExtension(file
            .getName());
        if ("xml".equalsIgnoreCase(extension)) {
          SilverTrace.info("importExport",
              "ScheduledImport.doScheduledImport()",
              "root.MSG_GEN_PARAM_VALUE", "file = " + file.getAbsolutePath());
          try {
            ImportReport importReport = importExport.processImport(user, file
                .getAbsolutePath());
            importExport.writeImportToLog(importReport, resource);
          } catch (ImportExportException e) {
            SilverTrace.error("importExport",
                "ScheduledImport.doScheduledImport()",
                "importExport.EX_CANT_PROCESS_IMPORT", "file = "
                + file.getAbsolutePath(), e);
          } finally {
            if (postPolicy.equalsIgnoreCase("remove")) {
              file.delete();
            } else if (postPolicy.equalsIgnoreCase("rename")) {
              file.renameTo(new File(file.getAbsolutePath() + ".old"));
            } else {
              // We let it as it is !
            }
          }
        }
      }
    }
    SilverTrace.info("importExport", "ScheduledImport.doScheduledImport()",
        "root.MSG_GEN_EXIT_METHOD");
  }
}