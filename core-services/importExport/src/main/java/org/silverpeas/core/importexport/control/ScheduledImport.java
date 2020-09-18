/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ScheduledImport implements SchedulerEventListener, Initialization {

  public static final String IMPORTENGINE_JOB_NAME = "ImportEngineJob";
  private final SettingBundle resources =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.importSettings");
  private File dir = null; // Where the import XML descriptors are stored
  private String postPolicy = null;

  @Inject
  private ImportExport importExport;

  @Override
  public void init() {
    try {
      String cron = resources.getString("cronScheduledImport");
      postPolicy = resources.getString("postPolicy", "remove");

      String sDir = resources.getString("importRepository");
      dir = new File(sDir);
      if (!dir.exists() && !dir.isDirectory()) {
        SilverLogger.getLogger(this).warn("Repository {0} doesn't exist!", sDir);
      } else {
        Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
        scheduler.unscheduleJob(IMPORTENGINE_JOB_NAME);
        JobTrigger trigger = JobTrigger.triggerAt(cron);
        scheduler.scheduleJob(IMPORTENGINE_JOB_NAME, trigger, this);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public void doScheduledImport() {


    String userId = resources.getString("userIdAsCreatorId");
    UserDetail user = OrganizationControllerProvider
        .getOrganisationController().getUserDetail(userId);
    LocalizationBundle multilang = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.importExportPeas.multilang.importExportPeasBundle");
    MultiSilverpeasBundle resource = new MultiSilverpeasBundle(multilang, "fr");

    File[] files = dir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        String extension = FileRepositoryManager.getFileExtension(file.getName());
        if ("xml".equalsIgnoreCase(extension)) {
          try {
            ImportReport importReport = importExport.processImport(user, file.getAbsolutePath());
            importExport.writeImportToLog(importReport, resource);

            // Successfully import. Remove or rename import descriptor.
            if ("remove".equalsIgnoreCase(postPolicy)) {
              Files.delete(file.toPath());
            } else if ("rename".equalsIgnoreCase(postPolicy)) {
              Files.move(file.toPath(), new File(file.getAbsolutePath() + ".old").toPath());
            }
          } catch (IOException | ImportExportException e) {
            SilverLogger.getLogger(this).error(e);
          }
        }
      }
    }

  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doScheduledImport();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this).error("The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}