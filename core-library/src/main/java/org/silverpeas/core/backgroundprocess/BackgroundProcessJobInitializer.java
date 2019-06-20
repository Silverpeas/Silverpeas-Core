/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.backgroundprocess;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;

import javax.inject.Inject;

import static java.text.MessageFormat.format;

/**
 * This JOB is in charge of cleanup the background process contexts if necessary.
 */
class BackgroundProcessJobInitializer implements Initialization {

  private static final String JOB_NAME = "BackgroundProcessJob";

  @Inject
  private Scheduler scheduler;

  @Override
  public void init() throws Exception {
    scheduler.unscheduleJob(JOB_NAME);
    final BackgroundProcessCleanerJob job = new BackgroundProcessCleanerJob();
    scheduler.scheduleJob(job, JobTrigger.triggerEvery(5, TimeUnit.MINUTE));
  }

  private static class BackgroundProcessCleanerJob extends Job {

    /**
     * Creates a new job.
     */
    private BackgroundProcessCleanerJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      BackgroundProcessLogger.get().debug(() -> format(
          "cleaning quietly background process task with {0} {0,choice, 1#context| 1<contexts} " +
              "referenced", BackgroundProcessTask.synchronizedContexts.size()));
      BackgroundProcessTask.purgeContexts();
    }
  }
}
