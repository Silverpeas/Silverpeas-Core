/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;

public class SynchroGroupScheduler implements SchedulerEventListener {

  public static final String ADMINSYNCHROGROUP_JOB_NAME = "AdminSynchroGroupJob";
  private List<String> synchronizedGroupIds = null;

  public void initialize(String cron, List<String> synchronizedGroupIds) {
    try {
      this.synchronizedGroupIds = synchronizedGroupIds;

      Scheduler scheduler = SchedulerProvider.getScheduler();
      scheduler.unscheduleJob(ADMINSYNCHROGROUP_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(ADMINSYNCHROGROUP_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  public void doSynchroGroup() {
    SynchroGroupReport.startSynchro();
    for (int i = 0; synchronizedGroupIds != null && i < synchronizedGroupIds.size(); i++) {
      String groupId = synchronizedGroupIds.get(i);
      try {
        AdministrationServiceProvider.getAdminService().synchronizeGroupByRule(groupId, true);
      } catch (AdminException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
    SynchroGroupReport.stopSynchro();

  }

  public void addGroup(String groupId) {
    if (synchronizedGroupIds == null) {
      synchronizedGroupIds = new ArrayList<String>();
    }
    synchronizedGroupIds.add(groupId);
  }

  public void removeGroup(String groupId) {
    if (synchronizedGroupIds != null) {
      synchronizedGroupIds.remove(groupId);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doSynchroGroup();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    String jobName = anEvent.getJobExecutionContext().getJobName();
    SilverLogger.getLogger(this).error("The domain synchronization job {0} failed!", jobName);
  }
}
